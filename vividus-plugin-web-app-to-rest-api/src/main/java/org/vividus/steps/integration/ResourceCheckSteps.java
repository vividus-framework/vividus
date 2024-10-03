/*
 * Copyright 2019-2024 the original author or authors.
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

import static org.vividus.util.UriUtils.buildNewUrl;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.net.URIBuilder;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.model.ExamplesTable;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.vividus.html.HtmlLocatorType;
import org.vividus.html.JsoupUtils;
import org.vividus.http.HttpMethod;
import org.vividus.http.HttpRequestExecutor;
import org.vividus.http.HttpTestContext;
import org.vividus.http.validation.CheckStatus;
import org.vividus.http.validation.ResourceValidator;
import org.vividus.reporter.event.AttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.testcontext.ContextCopyingExecutor;
import org.vividus.ui.web.configuration.WebApplicationConfiguration;
import org.vividus.validator.model.ResourceValidationError;
import org.vividus.validator.model.WebPageResourceValidation;

public class ResourceCheckSteps
{
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    private static final String URL_FRAGMENT = "#";
    private static final String HREF_ATTR = "href";
    private static final String HTML_TITLE_TAG = "title";

    private final ResourceValidator<WebPageResourceValidation> resourceValidator;
    private final AttachmentPublisher attachmentPublisher;
    private final HttpRequestExecutor httpRequestExecutor;
    private final ISoftAssert softAssert;
    private final WebApplicationConfiguration webApplicationConfiguration;
    private final ContextCopyingExecutor executor;
    private final HttpTestContext httpTestContext;

    private List<String> attributesToCheck;
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
     * @param htmlLocatorType The <b>CSS selector</b> or <b>XPath</b>
     * @param htmlLocator     The locator to locate elements in HTML document
     * @param html            The HTML containing elements to validate
     */
    @Then("all resources found by $htmlLocatorType `$htmlLocator` in $html are valid")
    public void checkResources(HtmlLocatorType htmlLocatorType, String htmlLocator, String html)
    {
        softAssert.runIgnoringTestFailFast(() -> execute(() ->
        {
            Document document = JsoupUtils.getDocument(html);
            Collection<Element> resourcesToValidate = htmlLocatorType.findElements(document, htmlLocator);
            boolean contextCheck = document.head().getElementsByTag(HTML_TITLE_TAG).isEmpty();
            Stream<WebPageResourceValidation> validations = createResourceValidations(resourcesToValidate,
                    resourceValidation -> {
                        URI uriToCheck = resourceValidation.getUriOrError().getLeft();
                        if (uriToCheck != null && isNotAbsolute(uriToCheck))
                        {
                            String message = String.format(
                                    "Unable to resolve %s resource since the main application page URL is not set",
                                    uriToCheck);
                            softAssert.recordFailedAssertion(message);

                            resourceValidation.setError(message);
                            resourceValidation.setCheckStatus(CheckStatus.BROKEN);
                        }
                    }, contextCheck);
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
        return r.getCheckStatus() != null ? r : resourceValidator.perform(r);
    }

    private Stream<WebPageResourceValidation> createResourceValidations(Collection<Element> elements,
            Consumer<WebPageResourceValidation> resourceValidator, boolean contextCheck)
    {
        return elements.stream()
                .map(e -> parseElement(e, contextCheck))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .peek(resourceValidator)
                .parallel();
    }

    private Optional<WebPageResourceValidation> parseElement(Element element, boolean contextCheck)
    {
        String attributesToCheckAsString = String.join("/", attributesToCheck);
        String elementUriAsString = getElementUri(element).trim();
        if (elementUriAsString.startsWith("data:"))
        {
            return Optional.empty();
        }

        String elementCssSelector = element.cssSelector();
        if (elementUriAsString.isEmpty())
        {
            return Optional.of(ResourceValidationError.EMPTY_HREF_SRC
                    .onAssertion(softAssert::recordFailedAssertion, elementCssSelector, attributesToCheckAsString)
                    .createValidation(null, elementCssSelector, attributesToCheckAsString));
        }
        try
        {
            URI uriToValidate = resolveUri(elementUriAsString);
            Pair<URI, String> elementUri = Pair.of(uriToValidate, null);
            WebPageResourceValidation validation = new WebPageResourceValidation(elementUri, elementCssSelector);
            boolean jumpLink = isJumpLink(elementUriAsString);
            if (!jumpLink && !isSchemaAllowed(uriToValidate)
                          || excludeHrefsPattern.matcher(uriToValidate.toString()).matches())
            {
                validation.setCheckStatus(CheckStatus.FILTERED);
                return Optional.of(validation);
            }

            if (jumpLink)
            {
                String fragment = elementUri.getLeft().getFragment();
                Element root = element.root();
                boolean targetNotPresent = root.getElementById(fragment) == null
                                        && root.getElementsByAttributeValue("name", fragment).isEmpty();
                if (targetNotPresent)
                {
                    if (contextCheck)
                    {
                        WebPageResourceValidation contextJumpLinkValidation = new WebPageResourceValidation(
                                Pair.of(null, String.format(
                                        "Validation of jump link (the target is \"%s\") is skipped as the current "
                                                + "context is restricted to a portion of the document.",
                                        elementUriAsString)), elementCssSelector);
                        contextJumpLinkValidation.setCheckStatus(CheckStatus.FILTERED);
                        return Optional.of(contextJumpLinkValidation);
                    }
                    return Optional.of(ResourceValidationError.MISSING_JUMPLINK_TARGET
                            .onAssertion(softAssert::recordFailedAssertion, elementCssSelector, fragment)
                            .createValidation(null, elementCssSelector, fragment));
                }
                validation.setCheckStatus(CheckStatus.PASSED);
            }

            return Optional.of(validation);
        }
        catch (URISyntaxException e)
        {
            return Optional.of(ResourceValidationError.INVALID_HREF_SRC
                    .onAssertion(msg -> softAssert.recordFailedAssertion(msg, e), elementCssSelector,
                            attributesToCheckAsString, elementUriAsString)
                    .createValidation(null, elementCssSelector, attributesToCheckAsString, elementUriAsString));
        }
    }

    private static boolean isJumpLink(String url)
    {
        return url.startsWith(URL_FRAGMENT) && url.length() > 1;
    }

    private static boolean isSchemaAllowed(URI uri)
    {
        return Optional.ofNullable(uri.getScheme()).map(ALLOWED_SCHEMES::contains).orElse(false);
    }

    private String getElementUri(Element element)
    {
        Optional<String> attributeToCheck = attributesToCheck.stream().filter(a -> !element.attr(a).isEmpty())
                .findFirst();
        String attributeWithValue = attributeToCheck.orElse(StringUtils.EMPTY);

        if (attributeWithValue.equals(HREF_ATTR))
        {
            String hrefToCheck = element.attr(attributeWithValue);
            if (URL_FRAGMENT.equals(hrefToCheck) || isJumpLink(hrefToCheck))
            {
                return hrefToCheck;
            }

            String absoluteUrl = element.absUrl(attributeWithValue);
            // For scripts e.g. href="javascript:alert('...');" the abs url will be empty
            return absoluteUrl.isEmpty() ? hrefToCheck : absoluteUrl;
        }
        return attributeToCheck.map(element::attr).orElse(StringUtils.EMPTY);
    }

    private URI resolveUri(String uri) throws URISyntaxException
    {
        URI uriToCheck = new URI(uri);
        if (isNotAbsolute(uriToCheck))
        {
            URI mainApplicationPageUrl = webApplicationConfiguration.getMainApplicationPageUrlUnsafely();
            if (mainApplicationPageUrl != null)
            {
                if (uri.length() <= 1 || uri.charAt(1) != '/')
                {
                    return buildNewUrl(mainApplicationPageUrl, uri);
                }
                String scheme = mainApplicationPageUrl.getScheme();
                if (scheme != null)
                {
                    return new URIBuilder(uri).setScheme(scheme).build();
                }
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
     * Then all resources found by CSS selector `a` are valid on:
     * |pages|
     * |https://vividus.org|
     * |/test-automation-made-awesome|
     * </pre>
     * @param htmlLocatorType The <b>CSS selector</b> or <b>XPath</b>
     * @param htmlLocator     The locator to locate elements in HTML document
     * @param pages           The URLs of pages containing resources to validate
     */
    @Then("all resources found by $htmlLocatorType `$htmlLocator` are valid on:$pages")
    public void checkResources(HtmlLocatorType htmlLocatorType, String htmlLocator, ExamplesTable pages)
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
                            WebPageResourceValidation valdation = ResourceValidationError.INVALID_PAGE_URL
                                    .onAssertion(msg -> softAssert.recordFailedAssertion(msg, e), rawPageUrl)
                                    .createValidation(rawPageUrl, null, rawPageUrl);
                            return Stream.of(valdation);
                        }

                        try
                        {
                            httpRequestExecutor.executeHttpRequest(HttpMethod.GET, pageUrl, Optional.empty());
                            return Optional.ofNullable(httpTestContext.getResponse().getResponseBodyAsString())
                                    .map(response -> htmlLocatorType
                                            .findElements(JsoupUtils.getDocument(response, pageUrl), htmlLocator))
                                    .map(elements -> createResourceValidations(elements,
                                            rV -> rV.setPageURL(pageUrl), false
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
        return ResourceValidationError.UNREACHABLE_PAGE
                .onAssertion(msg -> softAssert.recordFailedAssertion(msg, exception), pageURL)
                .createValidation(pageURL, null, pageURL);
    }

    private WebPageResourceValidation createMissingPageBodyValidation(String pageURL)
    {
        return ResourceValidationError.RESPONSE_IS_EMPTY.onAssertion(softAssert::recordFailedAssertion, pageURL)
                .createValidation(pageURL, null, pageURL);
    }

    private WebPageResourceValidation createUnresolvablePageValidation(String pageUrl)
    {
        return ResourceValidationError.MAIN_PAGE_IS_NOT_SET.onAssertion(softAssert::recordFailedAssertion, pageUrl)
                .createValidation(pageUrl, null, pageUrl);
    }

    public void setAttributesToCheck(List<String> attributesToCheck)
    {
        this.attributesToCheck = attributesToCheck;
    }

    public void setUriToIgnoreRegex(Optional<String> uriToIgnoreRegex)
    {
        this.uriToIgnoreRegex = uriToIgnoreRegex;
    }
}
