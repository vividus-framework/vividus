/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.proxy.steps;

import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.browserup.bup.util.HttpMessageInfo;
import com.browserup.harreader.model.HarEntry;
import com.browserup.harreader.model.HttpMethod;

import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.vividus.context.VariableContext;
import org.vividus.monitor.PublishHarOnFailure;
import org.vividus.monitor.TakeScreenshotOnFailure;
import org.vividus.proxy.IProxy;
import org.vividus.proxy.model.HttpMessagePart;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.DataWrapper;
import org.vividus.steps.StringComparisonRule;
import org.vividus.ui.action.IWaitActions;
import org.vividus.variable.VariableScope;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

@SuppressWarnings("PMD.ExcessiveImports")
@TakeScreenshotOnFailure(onlyInDebugMode = "proxy")
public class ProxySteps
{
    @Inject private IProxy proxy;
    @Inject private ISoftAssert softAssert;
    @Inject private VariableContext variableContext;
    @Inject private IWaitActions waitActions;

    /**
     * Clears the proxy log
     */
    @When("I clear proxy log")
    public void clearProxyLog()
    {
        proxy.clearRecordedData();
    }

    /**
     * Checks if the number of requests with given URL-pattern exist.
     * <p>
     * This step requires proxy to be turned on.
     * It can be done via setting properties or switching on <b>@proxy</b> metatag.
     * Step gets proxies log, extract from contained requests URLs and match them with URL-pattern
     * If URLs are the same, there were calls with given URL-pattern, otherwise - weren't.
     * If there weren't any calls matching requirements, HAR file with all calls will be attached to report.
     * If response contains status code 302 - corresponding request will be filtered out.
     * </p>
     * @param httpMethods    The comma-separated HTTP methods to filter by
     * @param urlPattern     The regular expression to match HTTP request URL
     * @param comparisonRule The rule to match the quantity of requests. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param number         The number to compare with
     * @return Filtered HAR entries
     */
    @PublishHarOnFailure
    @Then("number of HTTP $httpMethods requests with URL pattern `$urlPattern` is $comparisonRule `$number`")
    public List<HarEntry> checkNumberOfRequests(Set<HttpMethod> httpMethods, Pattern urlPattern,
            ComparisonRule comparisonRule, int number)
    {
        List<HarEntry> harEntries = getLogEntries(httpMethods, urlPattern);
        String description = String.format("Number of HTTP %s requests matching URL pattern '%s'",
                methodsToString(httpMethods, ", "), urlPattern);
        softAssert.assertThat(description, harEntries.size(), comparisonRule.getComparisonRule(number));
        return harEntries;
    }

    /**
     * Saves the HTTP message part from the request with given URL-pattern into the variable with specified name and
     * the scopes.
     * <p>
     * This step requires proxy to be turned on. It can be done in properties or by switching on <code>@proxy</code>
     * meta tag at the story level.
     * </p>
     * The actions performed by the step:
     * <ul>
     * <li>extract HTTP messages from the recorded proxy archive</li>
     * <li>filter out the HTTP messages with the response status code `302 Moved Temporarily`</li>
     * <li>find HTTP requests matching the provided HTTP methods and the URL regular expression</li>
     * <li>check that total number of the found HTTP messages is equal to 1</li>
     * <li>save the HTTP message part to the variable</li>
     * </ul>
     * In case of failure the full HTTP archive (HAR) is attached to the report.
     *
     * @param httpMethods     The "or"-separated set of HTTP methods to filter by, e.g. 'GET or POST or PUT'
     * @param urlPattern      The regular expression to match HTTP request URL
     * @param httpMessagePart The data to get from HAR entry<br>
     *                        <i>Available data:</i>
     *                        <ul>
     *                       <li><b>URL</b> - the request URL
     *                       <li><b>URL_QUERY</b> - the request query parameters
     *                       <li><b>REQUEST_DATA</b> - the request body and the response status code
     *                       <li><b>RESPONSE_DATA</b> - the response body
     *                       </ul>
     * @param scopes         The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     *                       <i>Available scopes:</i>
     *                       <ul>
     *                       <li><b>STEP</b> - the variable will be available only within the step,
     *                       <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                       <li><b>STORY</b> - the variable will be available within the whole story,
     *                       <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                       </ul>
     * @param variableName The variable name to save the URL.
     */
    @When("I capture HTTP $httpMethods request with URL pattern `$urlPattern` and save $httpMessagePart to $scopes "
            + "variable `$variableName`")
    public void captureRequestAndSaveURL(Set<HttpMethod> httpMethods, Pattern urlPattern,
            HttpMessagePart httpMessagePart, Set<VariableScope> scopes, String variableName)
    {
        List<HarEntry> harEntries = checkNumberOfRequests(httpMethods, urlPattern, ComparisonRule.EQUAL_TO, 1);
        if (harEntries.size() == 1)
        {
            HarEntry harEntry = harEntries.get(0);
            variableContext.putVariable(scopes, variableName, httpMessagePart.get(harEntry));
        }
    }

    /**
     * Saves the URL query parameters, the request body and the response status code from the message with given
     * request  URL-pattern into the variable with specified name and the scopes.
     * <p>
     * This step requires proxy to be turned on. It can be done in properties or by switching on <code>@proxy</code>
     * meta tag at the story level.
     * </p>
     * The actions performed by the step:
     * <ul>
     * <li>extract HTTP messages from the recorded proxy archive</li>
     * <li>filter out the HTTP messages with the response status code `302 Moved Temporarily`</li>
     * <li>find HTTP requests matching the provided HTTP methods and the URL regular expression</li>
     * <li>check that total number of the found HTTP messages is equal to 1</li>
     * <li>save the HTTP message data to the specified variable</li>
     * </ul>
     * In case of failure the full HTTP archive (HAR) is attached to the report.
     *
     * @param httpMethods  The "or"-separated set of HTTP methods to filter by, e.g. 'GET or POST or PUT'
     * @param urlPattern   The regular expression to match HTTP request URL
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName The variable name to store results. If the variable name is my-var, the following
     *                     variables will be created:
     *                     <ul>
     *                     <li>${my-var.query} - The URL query is stored as a collection of key and value pairs,
     *                     where key is the name of the query parameter and value is the list of query parameter
     *                     values. The query parameter values are accessible via zero-based index.</li>
     *                     <li>${my-var.requestBody.mimeType} - The MIME type of posted data, the variable will not
     *                     be created if MIME type is not present.</li>
     *                     <li>${my-var.requestBody.text} - The posted data as plain text, the variable will not be
     *                     created if the request body is not present.</li>
     *                     <li>${my-var.requestBodyParameters} - The form data parameters are stored as a collection
     *                     of key and value pairs, where key is the name of the form parameter and value is the list
     *                     of form parameter values. The form parameter values are accessible via zero-based index.</li>
     *                     <li>${my-var.responseStatus} - The response status, the variable will not be created if
     *                     the response is not present.</li>
     *                     </ul>
     * @throws IOException If any error happens during operation
     */

    /**
     * Waits for appearance of HTTP request matched <b>httpMethod</b> and <b>urlPattern</b> in proxy log
     * @param httpMethods The "or"-separated HTTP methods to filter by, e.g. 'GET or POST or PUT'
     * @param urlPattern  The regular expression to match HTTP request URL
     */
    @When("I wait until HTTP $httpMethods request with URL pattern `$urlPattern` exists in proxy log")
    public void waitRequestInProxyLog(Set<HttpMethod> httpMethods, Pattern urlPattern)
    {
        waitActions.wait(urlPattern, new Function<>()
        {
            @Override
            public Boolean apply(Pattern urlPattern)
            {
                return !getLogEntries(httpMethods, urlPattern).isEmpty();
            }

            @Override
            public String toString()
            {
                return String.format("waiting for HTTP %s request with URL pattern %s",
                        methodsToString(httpMethods, " or "), urlPattern);
            }
        });
    }

    /**
     * Add headers to the proxied HTTP request satisfying the desired condition
     * @param comparisonRule String comparison rule: "is equal to", "contains", "does not contain"
     * @param url            The string value of URL to match comparison rule
     * @param headers        ExamplesTable representing the list of the headers with columns "name" and
     *                       "value" specifying HTTP header names and values respectively
     */
    @When("I add headers to proxied requests with URL pattern which $comparisonRule `$url`:$headers")
    public void addHeadersToProxyRequest(StringComparisonRule comparisonRule, String url, DefaultHttpHeaders headers)
    {
        Matcher<String> expected = comparisonRule.createMatcher(url);
        applyUrlFilter(List.of(m -> expected.matches(m.getUrl())), request -> {
            request.headers().add(headers);
            return null;
        });
    }

    /**
     * The step allows to mock HTTP request, and provide within response status code, headers and content.
     * Once the request is matched by URL comparison rule response will be returned. In this case no actual request will
     * be executed.
     * @param comparisonRule String comparison rule: "is equal to", "contains", "does not contain", "matches"
     * @param url            The string value of URL to match comparison rule
     * @param responseCode   The code will be used in response
     * @param content        The content will be used in the response
     * @param headers        ExamplesTable representing the list of the headers with columns "name" and
     *                       "value" specifying HTTP header names and values respectively
     */
    @When(value = "I mock HTTP responses with request URL which $comparisonRule `$url` using"
            + " response code `$responseCode`, content `$payload` and headers:$headers", priority = 1)
    public void mockHttpRequests(StringComparisonRule comparisonRule, String url, int responseCode, DataWrapper content,
            DefaultHttpHeaders headers)
    {
        mockHttpRequests(Optional.empty(), comparisonRule, url, responseCode, content, headers);
    }

    /**
     * The step allows to mock HTTP request, and provide within response status code and headers.
     * Once the request is matched by URL comparison rule response will be returned. In this case no actual request will
     * be executed.
     * @param comparisonRule String comparison rule: "is equal to", "contains", "does not contain", "matches"
     * @param url            The string value of URL to match comparison rule
     * @param responseCode   The code will be used in response
     * @param headers        ExamplesTable representing the list of the headers with columns "name" and
     *                       "value" specifying HTTP header names and values respectively
     */
    @When("I mock HTTP responses with request URL which $comparisonRule `$url` using response code `$responseCode`"
            + " and headers:$headers")
    public void mockHttpRequests(StringComparisonRule comparisonRule, String url, int responseCode,
            DefaultHttpHeaders headers)
    {
        Matcher<String> expected = comparisonRule.createMatcher(url);
        applyUrlFilter(List.of(m -> expected.matches(m.getUrl())), request -> {
            DefaultHttpResponse mockedRequest =
                new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.valueOf(responseCode));
            mockedRequest.headers().add(headers);
            return mockedRequest;
        });
    }

    /**
     * The step allows to mock HTTP request of specified method, and provide within response status code,
     * headers and content. Once the request is matched by method and URL comparison rule response will be returned.
     * In this case no actual request will be executed.
     * @param httpMethods    The "or"-separated set of HTTP methods to filter by, e.g. 'GET or POST or PUT'
     * @param comparisonRule String comparison rule: "is equal to", "contains", "does not contain", "matches"
     * @param url            The string value of URL to match comparison rule
     * @param responseCode   The code will be used in response
     * @param content        The content will be used in the response
     * @param headers        ExamplesTable representing the list of the headers with columns "name" and
     *                       "value" specifying HTTP header names and values respectively
     */
    @When(value = "I mock HTTP $httpMethods responses with request URL which $comparisonRule `$url` using"
            + " response code `$responseCode`, content `$payload` and headers:$headers", priority = 1)
    public void mockHttpRequests(Set<HttpMethod> httpMethods, StringComparisonRule comparisonRule, String url,
            int responseCode, DataWrapper content, DefaultHttpHeaders headers)
    {
        mockHttpRequests(Optional.of(httpMethods), comparisonRule, url, responseCode, content, headers);
    }

    private void mockHttpRequests(Optional<Set<HttpMethod>> httpMethods, StringComparisonRule comparisonRule,
            String url, int responseCode, DataWrapper content, DefaultHttpHeaders headers)
    {
        List<Predicate<HttpMessageInfo>> filters = new ArrayList<>();
        Matcher<String> expected = comparisonRule.createMatcher(url);
        filters.add(m -> expected.matches(m.getUrl()));
        if (!httpMethods.isEmpty())
        {
            filters.add(m -> httpMethods.get().stream()
                    .anyMatch(e -> m.getOriginalRequest().method().toString().equals(e.toString())));
        }
        byte[] contentBytes = content.getBytes();
        applyUrlFilter(filters, request -> {
            HttpResponseStatus responseStatus = HttpResponseStatus.valueOf(responseCode);
            HttpVersion protocolVersion = request.protocolVersion();

            DefaultFullHttpResponse mockedRequest =
                new DefaultFullHttpResponse(protocolVersion, responseStatus, Unpooled.wrappedBuffer(contentBytes));

            HttpHeaders httpHeaders = mockedRequest.headers();
            httpHeaders.add("Content-Length", contentBytes.length);
            httpHeaders.add(headers);
            return mockedRequest;
        });
    }

    /**
     * Resets previously created proxy mocks
     */
    @When("I clear proxy mocks")
    public void resetMocks()
    {
        proxy.clearRequestFilters();
    }

    private void applyUrlFilter(List<Predicate<HttpMessageInfo>> messageFilters,
            Function<HttpRequest, HttpResponse> requestProcessor)
    {
        proxy.addRequestFilter((request, contents, messageInfo) ->
        {
            if (messageFilters.stream().allMatch(p -> p.test(messageInfo)))
            {
                return requestProcessor.apply(request);
            }
            return null;
        });
    }

    private List<HarEntry> getLogEntries(Set<HttpMethod> httpMethods, Pattern urlPattern)
    {
        return proxy.getRecordedData().getLog()
                .findEntries(urlPattern)
                .stream()
                .filter(entry -> entry.getResponse().getStatus() != HttpStatus.SC_MOVED_TEMPORARILY
                        && httpMethods.contains(entry.getRequest().getMethod()))
                .collect(toList());
    }

    private String methodsToString(Set<HttpMethod> httpMethods, String delimiter)
    {
        return httpMethods.stream().map(HttpMethod::toString).collect(Collectors.joining(delimiter));
    }
}
