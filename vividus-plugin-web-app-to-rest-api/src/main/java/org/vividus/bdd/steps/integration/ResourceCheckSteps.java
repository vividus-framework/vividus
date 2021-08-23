/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.bdd.steps.integration;

import static org.vividus.util.HtmlUtils.getElements;

import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.model.ExamplesTable;
import org.jsoup.nodes.Element;
import org.jsoup.select.Selector.SelectorParseException;
import org.vividus.http.HttpMethod;
import org.vividus.http.HttpRequestExecutor;
import org.vividus.http.HttpTestContext;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.validation.ResourceValidator;
import org.vividus.http.validation.model.CheckStatus;
import org.vividus.reporter.event.AttachmentPublisher;
import org.vividus.softassert.SoftAssert;
import org.vividus.testcontext.ContextCopyingExecutor;
import org.vividus.ui.web.configuration.WebApplicationConfiguration;
import org.vividus.util.UriUtils;
import org.vividus.validator.model.WebPageResourceValidation;

public class ResourceCheckSteps
{
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");
    private static final String URL_FRAGMENT = "#";
    private static final String HREF_ATTR = "href";

    private final ResourceValidator<WebPageResourceValidation> resourceValidator;
    private final AttachmentPublisher attachmentPublisher;
    private final HttpRequestExecutor httpRequestExecutor;
    private final SoftAssert softAssert;
    private final WebApplicationConfiguration webApplicationConfiguration;
    private final ContextCopyingExecutor executor;
    private final HttpTestContext httpTestContext;
    private Pattern excludeHrefsPattern;

    private Optional<String> uriToIgnoreRegex;

    public ResourceCheckSteps(ResourceValidator<WebPageResourceValidation> resourceValidator,
            AttachmentPublisher attachmentPublisher, HttpRequestExecutor httpRequestExecutor, SoftAssert softAssert,
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
     * @throws InterruptedException when a thread is interrupted
     * @throws ExecutionException  when exception thrown before result get
     */
    @Then("all resources by selector `$cssSelector` from $html are valid")
    public void checkResources(String cssSelector, String html) throws InterruptedException, ExecutionException
    {
        execute(() -> {
            Collection<Element> resourcesToValidate = getElements(html, cssSelector);
            Stream<WebPageResourceValidation> validations = createResourceValidations(resourcesToValidate,
                p -> new WebPageResourceValidation(p.getLeft(), p.getRight()));
            validateResources(validations);
        });
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
        return r.getUri() == null || CheckStatus.FILTERED == r.getCheckStatus()
                ? r
                : resourceValidator.perform(r);
    }

    private Stream<WebPageResourceValidation> createResourceValidations(Collection<Element> elements,
            Function<Pair<URI, String>, WebPageResourceValidation> resourceValidationFactory)
    {
        return elements.parallelStream().map(e ->
            Pair.of(getHrefAttribute(e).orElseGet(() -> e.attr("src")).trim(), getSelector(e)))
                       .filter(p -> !p.getKey().isEmpty() || softAssert.recordFailedAssertion(
                        "Element by selector " + p.getValue() + " doesn't contain href/src attributes"))
                       .map(p -> Pair.of(createUri(p.getKey()), p.getValue()))
                       .map(resourceValidationFactory)
                       .peek(rv ->
                       {
                           if (!Optional.ofNullable(rv.getUri().getScheme()).map(ALLOWED_SCHEMES::contains)
                                   .orElse(false) || excludeHrefsPattern.matcher(rv.toString()).matches())
                           {
                               rv.setCheckStatus(CheckStatus.FILTERED);
                           }
                       });
    }

    private String getSelector(Element element)
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

    private static Optional<String> getHrefAttribute(Element element)
    {
        String href = element.attr(HREF_ATTR);
        if (!href.isEmpty())
        {
            if (URL_FRAGMENT.equals(href))
            {
                return Optional.of(href);
            }

            String absUrl = element.absUrl(HREF_ATTR);
            // For scripts e.g. href="javascript:alert('...');" the abs url will be empty
            return Optional.of(absUrl.isEmpty() ? href : absUrl);
        }

        return Optional.empty();
    }

    private URI createUri(String uri)
    {
        URI uriToCheck = URI.create(uri);
        if (uri.charAt(0) != '/' || uriToCheck.isAbsolute())
        {
            return uriToCheck;
        }
        return UriUtils.buildNewUrl(webApplicationConfiguration.getMainApplicationPageUrl(), uriToCheck.getPath());
    }

    /**
     * Step purpose is checking of resources availability;
     * Steps follows next logic for each page URL:
     *     1. Gathers all the elements by CSS selector
     *     2. For each element executes HEAD request;
     *         a. If status code acceptable than check considered as passed;
     *         b. If status code not acceptable but one of (404, 405, 501, 503) then GET request will be sendt;
     *         c. If GET status code acceptable than check considered as passed otherwise failed;
     * <b>Example</b>
     * Then all resources by selector a are valid on:
     * |pages|
     * |https://vividus.org|
     * |/test-automation-made-awesome|
     * @param cssSelector to locate resources
     * @param pages where resources will be validated
     * @throws InterruptedException when a thread is interrupted
     * @throws ExecutionException  when exception thrown before result get
     */
    @Then("all resources by selector `$cssSelector` are valid on:$pages")
    public void checkResources(String cssSelector, ExamplesTable pages) throws InterruptedException, ExecutionException
    {
        execute(() -> {
            Stream<WebPageResourceValidation> resourcesToValidate =
                pages.getRows()
                     .parallelStream()
                     .map(m -> m.get("pages"))
                     .map(this::createUri)
                     .map(URI::toString)
                     .flatMap(pageURL ->
                     {
                         try
                         {
                             httpRequestExecutor.executeHttpRequest(HttpMethod.GET, pageURL, Optional.empty());
                             return Optional.ofNullable(httpTestContext.getResponse())
                                            .map(HttpResponse::getResponseBodyAsString)
                                            .map(b -> createResourceValidations(getElements(pageURL, b, cssSelector),
                                                p -> new WebPageResourceValidation(p.getLeft(), p.getRight(), pageURL)))
                                            .orElseGet(() ->
                                                        Stream.of(brokenResourceValidation(pageURL, Optional.empty())));
                         }
                         catch (IOException toReport)
                         {
                             return Stream.of(brokenResourceValidation(pageURL, Optional.of(toReport)));
                         }
                     });
            validateResources(resourcesToValidate);
        });
    }

    private void execute(Runnable executable) throws InterruptedException, ExecutionException
    {
        executor.execute(executable,
            (t, e) -> softAssert.recordFailedAssertion("Exception occured in thread with name: " + t.getName(), e));
    }

    private WebPageResourceValidation brokenResourceValidation(String pageURL, Optional<Exception> exception)
    {
        WebPageResourceValidation resourceValidation = new WebPageResourceValidation();
        resourceValidation.setCheckStatus(CheckStatus.BROKEN);
        resourceValidation.setPageURL(pageURL);
        String message = "Unable to get page with URL: " + pageURL;
        exception.ifPresentOrElse(e -> softAssert.recordFailedAssertion(message, e),
            () -> softAssert.recordFailedAssertion(message + "; Response is received without body;"));
        return resourceValidation;
    }

    public void setUriToIgnoreRegex(Optional<String> uriToIgnoreRegex)
    {
        this.uriToIgnoreRegex = uriToIgnoreRegex;
    }
}
