/*
 * Copyright 2019-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.vividus.steps.integration;

import static org.vividus.util.HtmlUtils.getElementsByCssSelector;
import static org.vividus.util.UriUtils.buildNewUrl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.model.ExamplesTable;
import org.jsoup.nodes.Element;
import org.jsoup.select.Selector.SelectorParseException;
import org.vividus.http.HttpMethod;
import org.vividus.http.HttpRequestExecutor;
import org.vividus.http.HttpTestContext;
import org.vividus.http.validation.ResourceValidator;
import org.vividus.http.validation.model.CheckStatus;
import org.vividus.reporter.event.AttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.testcontext.ContextCopyingExecutor;
import org.vividus.ui.web.configuration.WebApplicationConfiguration;
import org.vividus.validator.model.WebPageResourceValidation;

public class ResourceCheckSteps
{
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    private static final String URL_FRAGMENT = "#";
    private static final String HREF_ATTR = "href";

    private final ResourceValidator<WebPageResourceValidation> resourceValidator;
    private final AttachmentPublisher attachmentPublisher;
    private final HttpRequestExecutor httpRequestExecutor;
    private final ISoftAssert softAssert;
    private final WebApplicationConfiguration webApplicationConfiguration;
    private final ContextCopyingExecutor executor;
    private final HttpTestContext httpTestContext;
    private Pattern excludeHrefsPattern;

    private Optional<String> uriToIgnoreRegex;

    public ResourceCheckSteps(ResourceValidator<WebPageResourceValidation> resourceValidator,
            AttachmentPublisher attachmentPublisher, HttpRequestExecutor httpRequestExecutor, ISoftAssert softAssert,
            WebApplicationConfiguration webApplicationConfiguration, ContextCopyingExecutor executor,
            HttpTestContext httpTestContext)
    {
        this.resourceValidator = resourceValidator;
        this.attachmentPublisher = attachmentPublisher;
        this.httpRequestExecutor = httpRequestExecutor;
        this.softAssert = softAssert;
        this.webApplicationConfiguration = webApplicationConfiguration;
        this.executor = executor;
        this.httpTestContext = httpTestContext;
    }

    public void init()
    {
        excludeHrefsPattern = Pattern.compile(uriToIgnoreRegex.map(p -> p + "|" + URL_FRAGMENT)
                                                           .orElse(URL_FRAGMENT));
    }

    /**
     * Step purpose is checking of resources availability;
     * Steps follows next logic:
     *     1. Gathers all the elements by CSS selector
     *     2. For each element executes HEAD request;
     *         a. If status code acceptable than check considered as passed;
     *         b. If status code not acceptable but one of (404, 405, 501, 503) then GET request will be sent;
     *         c. If GET status code acceptable than check considered as passed otherwise failed;
     *     3. If element doesn't contain href or src attribute fail assertion will be recorded
     * @param cssSelector to locate resources
     * @param html to validate
     */
    @Then("all resources by selector `$cssSelector` from $html are valid")
    public void checkResources(String cssSelector, String html)
    {
        softAssert.runIgnoringTestFailFast(() -> execute(() ->
        {
            Collection<Element> resourcesToValidate = getElementsByCssSelector(html, cssSelector);
            Stream<WebPageResourceValidation> validations = createResourceValidations(resourcesToValidate,
                    resourceValidation -> {
                        URI uriToCheck = resourceValidation.getUri();
                        if (isNotAbsolute(uriToCheck))
                        {
                            softAssert.recordFailedAssertion(String.format(
                                    "Unable to resolve %s resource since the main application page URL is not set",
                                    uriToCheck));

                            resourceValidation.setCheckStatus(CheckStatus.BROKEN);
                        }
                    });
            validateResources(validations);
        }));
    }

    private void validateResources(Stream<WebPageResourceValidation> resourceValidation)
    {
        Set<WebPageResourceValidation> results = resourceValidation
               .map(this::validate)
               .collect(Collectors.toCollection(TreeSet::new));
        attachmentPublisher.publishAttachment("resources-validation-result.ftl", Map.of("results", results),
                "Resource validation results");
    }

    private WebPageResourceValidation validate(WebPageResourceValidation r)
    {
        return CheckStatus.BROKEN == r.getCheckStatus() || CheckStatus.FILTERED == r.getCheckStatus()
                ? r
                : resourceValidator.perform(r);
    }

    private Stream<WebPageResourceValidation> createResourceValidations(Collection<Element> elements,
            Consumer<WebPageResourceValidation> resourceValidator)
    {
        return elements.stream()
                .map(this::parseElement)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(rv ->
                {
                    resourceValidator.accept(rv);
                    if ((!isSchemaAllowed(rv.getUri()) || excludeHrefsPattern.matcher(rv.toString()).matches())
                            && rv.getCheckStatus() != CheckStatus.BROKEN)
                    {
                        rv.setCheckStatus(CheckStatus.FILTERED);
                    }
                })
                .parallel();
    }

    private Optional<WebPageResourceValidation> parseElement(Element element)
    {
        String elementUriAsString = getElementUri(element).trim();
        if (elementUriAsString.startsWith("data:"))
        {
            return Optional.empty();
        }

        String elementCssSelector = getCssSelector(element);
        if (elementUriAsString.isEmpty())
        {
            softAssert.recordFailedAssertion(
                    String.format("Element by selector %s doesn't contain href/src attributes", elementCssSelector));
            return Optional.empty();
        }
        try
        {
            URI elementUri = resolveUri(elementUriAsString);
            return Optional.of(new WebPageResourceValidation(elementUri, elementCssSelector));
        }
        catch (URISyntaxException e)
        {
            softAssert.recordFailedAssertion(
                    String.format("Element by selector %s has href/src attribute with invalid URL", elementCssSelector),
                    e);
            return Optional.empty();
        }
    }

    private static boolean isSchemaAllowed(URI uri)
    {
        return Optional.ofNullable(uri.getScheme()).map(ALLOWED_SCHEMES::contains).orElse(false);
    }

    private static String getElementUri(Element element)
    {
        String href = element.attr(HREF_ATTR);
        if (!href.isEmpty())
        {
            if (URL_FRAGMENT.equals(href))
            {
                return href;
            }

            String absUrl = element.absUrl(HREF_ATTR);
            // For scripts e.g. href="javascript:alert('...');" the abs url will be empty
            return absUrl.isEmpty() ? href : absUrl;
        }

        return element.attr("src");
    }

    private String getCssSelector(Element element)
    {
        try
        {
            return element.cssSelector();
        }
        catch (SelectorParseException exception)
        {
            return "N/A";
        }
    }

    private URI resolveUri(String uri) throws URISyntaxException
    {
        URI uriToCheck = new URI(uri);
        if (isNotAbsolute(uriToCheck))
        {
            URI mainApplicationPageUrl = webApplicationConfiguration.getMainApplicationPageUrlUnsafely();
            if (mainApplicationPageUrl != null)
            {
                return buildNewUrl(mainApplicationPageUrl, uri);
            }
        }
        return uriToCheck;
    }

    private static boolean isNotAbsolute(URI uri)
    {
        return !uri.isAbsolute() && uri.toString().charAt(0) == '/';
    }

    /**
     * Step purpose is checking of resources availability;
     * Steps follows next logic for each page URL:
     *     1. Gathers all the elements by CSS selector
     *     2. For each element executes HEAD request;
     *         a. If status code acceptable than check considered as passed;
     *         b. If status code not acceptable but one of (404, 405, 501, 503) then GET request will be sendt;
     *         c. If GET status code acceptable than check considered as passed otherwise failed;
     * <b>Example:</b>
     * <pre>
     * Then all resources by selector a are valid on:
     * |pages|
     * |https://vividus.org|
     * |/test-automation-made-awesome|
     * </pre>
     * @param cssSelector to locate resources
     * @param pages where resources will be validated
     */
    @Then("all resources by selector `$cssSelector` are valid on:$pages")
    public void checkResources(String cssSelector, ExamplesTable pages)
    {
        softAssert.runIgnoringTestFailFast(() -> execute(() -> {
            Stream<WebPageResourceValidation> resourcesToValidate = pages.getRows().stream().map(m -> m.get("pages"))
                    .parallel().flatMap(rawPageUrl ->
                    {
                        String pageUrl;
                        try
                        {
                            URI uri = resolveUri(rawPageUrl);
                            pageUrl = uri.toString();
                            if (isNotAbsolute(uri))
                            {
                                return Stream.of(createUnresolvablePageValidation(pageUrl));
                            }
                        }
                        catch (URISyntaxException e)
                        {
                            softAssert.recordFailedAssertion("Invalid page URL", e);
                            return Stream.of(createBrokenPageValidation(rawPageUrl));
                        }

                        try
                        {
                            httpRequestExecutor.executeHttpRequest(HttpMethod.GET, pageUrl, Optional.empty());
                            return Optional.ofNullable(httpTestContext.getResponse().getResponseBodyAsString())
                                    .map(response -> getElementsByCssSelector(pageUrl, response, cssSelector))
                                    .map(elements -> createResourceValidations(elements,
                                            rV -> rV.setPageURL(pageUrl)
                                    ))
                                    .orElseGet(() -> Stream.of(createMissingPageBodyValidation(pageUrl)));
                        }
                        catch (IOException toReport)
                        {
                            return Stream.of(createUnreachablePageValidation(pageUrl, toReport));
                        }
                    });
            validateResources(resourcesToValidate);
        }));
    }

    private void execute(Runnable executable)
    {
        try
        {
            executor.execute(executable, (t, e) -> softAssert
                    .recordFailedAssertion("Exception occured in thread with name: " + t.getName(), e));
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
        catch (ExecutionException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private WebPageResourceValidation createUnreachablePageValidation(String pageURL, Exception exception)
    {
        softAssert.recordFailedAssertion("Unable to get page with URL: " + pageURL, exception);
        return createBrokenPageValidation(pageURL);
    }

    private WebPageResourceValidation createMissingPageBodyValidation(String pageURL)
    {
        softAssert.recordFailedAssertion(
                String.format("Unable to get page with URL: %s; Response is received without body;", pageURL));
        return createBrokenPageValidation(pageURL);
    }

    private WebPageResourceValidation createUnresolvablePageValidation(String pageUrl)
    {
        softAssert.recordFailedAssertion(
                String.format("Unable to resolve %s page since the main application page URL is not set", pageUrl));
        return createBrokenPageValidation(pageUrl);
    }

    private WebPageResourceValidation createBrokenPageValidation(String pageUrl)
    {
        WebPageResourceValidation resourceValidation = new WebPageResourceValidation();
        resourceValidation.setCheckStatus(CheckStatus.BROKEN);
        resourceValidation.setPageURL(pageUrl);
        return resourceValidation;
    }

    public void setUriToIgnoreRegex(Optional<String> uriToIgnoreRegex)
    {
        this.uriToIgnoreRegex = uriToIgnoreRegex;
    }
}
