/*
 * Copyright 2019-2020 the original author or authors.
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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.browserup.harreader.model.HarEntry;
import com.browserup.harreader.model.HarPostData;
import com.browserup.harreader.model.HarPostDataParam;
import com.browserup.harreader.model.HarQueryParam;
import com.browserup.harreader.model.HarRequest;
import com.browserup.harreader.model.HttpMethod;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.http.HttpStatus;
import org.hamcrest.Matcher;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.proxy.IProxy;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.action.IWebWaitActions;

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
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Inject private IProxy proxy;
    @Inject private ISoftAssert softAssert;
    @Inject private IAttachmentPublisher attachmentPublisher;
    @Inject private IBddVariableContext bddVariableContext;
    @Inject private IWebWaitActions waitActions;

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
     * @param httpMethods the comma-separated HTTP methods to filter by
     * @param urlPattern the regular expression to match HTTP request URL
     * @param comparisonRule The rule to compare values
     * (<i>Possible values:<b> LESS_THAN, LESS_THAN_OR_EQUAL_TO, GREATER_THAN, GREATER_THAN_OR_EQUAL_TO,
     * EQUAL_TO</b></i>)
     * @param number The number to compare with
     * @throws IOException If any error happens during operation
     * @return Filtered HAR entries
     */
    @Then("number of HTTP $httpMethods requests with URL pattern `$urlPattern` is $comparisonRule `$number`")
    public List<HarEntry> checkNumberOfRequests(Set<HttpMethod> httpMethods, Pattern urlPattern,
            ComparisonRule comparisonRule, int number) throws IOException
    {
        List<HarEntry> harEntries = getLogEntries(httpMethods, urlPattern);
        String description = String.format("Number of HTTP %s requests matching URL pattern '%s'",
                methodsToString(httpMethods, ", "), urlPattern);
        if (!softAssert.assertThat(description, harEntries.size(), comparisonRule.getComparisonRule(number)))
        {
            byte[] harBytes = OBJECT_MAPPER.writeValueAsBytes(proxy.getRecordedData());
            attachmentPublisher.publishAttachment(harBytes, "har.har");
        }
        return harEntries;
    }

    /**
     * Saves the query string from request with given URL-pattern into the variable
     * with specified name and scopes.
     * <p>
     * This step requires proxy to be turned on.
     * It can be done via setting properties or switching on <b>@proxy</b> metatag inside the story file.
     * Step gets proxies log, extract from contained requests URLs and match them with URL-pattern
     * If there is one entry, it saves the query string from request as Map of keys and values
     * into the variable with specified name and scopes.
     * If there weren't any calls or more than one matching requirements, HAR file with all
     * calls will be attached to report.
     * </p>
     * @param httpMethods the "or"-separated HTTP methods to filter by, e.g. 'GET or POST or PUT'
     * @param urlPattern the regular expression to match HTTP request URL
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A variable name
     * @throws IOException If any error happens during operation
     */
    @When("I capture HTTP $httpMethods request with URL pattern `$urlPattern` and save URL query to $scopes "
            + "variable `$variableName`")
    public void captureRequestAndSaveURLQuery(Set<HttpMethod> httpMethods, Pattern urlPattern,
            Set<VariableScope> scopes, String variableName) throws IOException
    {
        saveHarEntryFieldInTheScope(httpMethods, urlPattern, scopes, variableName,
                harEntry -> getQueryParameters(harEntry.getRequest()));
    }

    /**
     * Saves the URL from request with given URL-pattern into the variable
     * with specified name and scopes.
     * <p>
     * This step requires proxy to be turned on.
     * It can be done via setting properties or switching on <b>@proxy</b> metatag inside the story file.
     * Step gets proxy's log, extract from contained requests URLs and match them with URL-pattern
     * If there is one entry, it saves the query string from request as Map of keys and values
     * into the variable with specified name and scopes.
     * If there weren't any calls or more than one matching requirements, HAR file with all
     * calls will be attached to report.
     * </p>
     * @param httpMethods the "or"-separated HTTP methods to filter by, e.g. 'GET or POST or PUT'
     * @param urlPattern the regular expression to match HTTP request URL
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A variable name
     * @throws IOException If any error happens during operation
     */
    @When("I capture HTTP $httpMethods request with URL pattern `$urlPattern` and save URL to $scopes "
            + "variable `$variableName`")
    public void captureRequestAndSaveURL(Set<HttpMethod> httpMethods, Pattern urlPattern, Set<VariableScope> scopes,
            String variableName) throws IOException
    {
        saveHarEntryFieldInTheScope(httpMethods, urlPattern, scopes, variableName,
                harEntry -> harEntry.getRequest().getUrl());
    }

    /**
     * Saves the query string, body from request with given URL-pattern and response status from response
     * into the variable with specified name and scopes.
     * <p>
     * This step requires proxy to be turned on.
     * It can be done via setting properties or switching on <b>@proxy</b> metatag inside the story file.
     * Step gets proxies log, extract from contained requests URLs and match them with URL-pattern
     * If there is one entry, it saves the query string from request as Map of keys and values
     * into the variable with specified name and scopes.
     * If there weren't any calls or more than one matching requirements, HAR file with all
     * calls will be attached to report.
     * </p>
     * @param httpMethods the "or"-separated HTTP methods to filter by, e.g. 'GET or POST or PUT'
     * @param urlPattern the regular expression to match HTTP request URL
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A variable name
     * @throws IOException If any error happens during operation
     */
    @When("I capture HTTP $httpMethods request with URL pattern `$urlPattern` and save request data to $scopes "
            + "variable `$variableName`")
    public void captureRequestAndSaveRequestData(Set<HttpMethod> httpMethods, Pattern urlPattern,
            Set<VariableScope> scopes, String variableName) throws IOException
    {
        saveHarEntryFieldInTheScope(httpMethods, urlPattern, scopes, variableName, this::extractFullRequestData);
    }

    private void saveHarEntryFieldInTheScope(Set<HttpMethod> httpMethods, Pattern urlPattern,
            Set<VariableScope> scopes, String variableName, Function<HarEntry, Object> valueExtractor)
            throws IOException
    {
        List<HarEntry> harEntries = checkNumberOfRequests(httpMethods, urlPattern, ComparisonRule.EQUAL_TO, 1);
        if (harEntries.size() == 1)
        {
            HarEntry harEntry = harEntries.get(0);
            bddVariableContext.putVariable(scopes, variableName, valueExtractor.apply(harEntry));
        }
    }

    private Map<String, Object> extractFullRequestData(HarEntry harEntry)
    {
        HarRequest request = harEntry.getRequest();
        HarPostData postData = request.getPostData();
        return Map.of(
                "query", getQueryParameters(request),
                "requestBody", getRequestBody(postData),
                "requestBodyParameters", getRequestBodyParameters(postData),
                "responseStatus", harEntry.getResponse().getStatus()
        );
    }

    private Map<String, String> getRequestBody(HarPostData postData)
    {
        return Map.of(
                "mimeType", postData.getMimeType(),
                "text", postData.getText(),
                "comment", String.valueOf(postData.getComment())
        );
    }

    private Map<String, String> getRequestBodyParameters(HarPostData postData)
    {
        return postData.getParams().stream()
                .collect(Collectors.toMap(HarPostDataParam::getName, HarPostDataParam::getValue));
    }

    private Map<String, String> getQueryParameters(HarRequest request)
    {
        return request.getQueryString().stream()
                .collect(Collectors.toMap(HarQueryParam::getName, HarQueryParam::getValue));
    }

    /**
     * Waits for appearance of HTTP request matched <b>httpMethod</b> and <b>urlPattern</b> in proxy log
     * @param httpMethods the "or"-separated HTTP methods to filter by, e.g. 'GET or POST or PUT'
     * @param urlPattern the regular expression to match HTTP request URL
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
     * @param url The string value of URL to match comparison rule
     * @param headers ExamplesTable representing the list of the headers with columns "name" and "value" specifying
     * HTTP header names and values respectively
     */
    @When("I add headers to proxied requests with URL pattern which $comparisonRule `$url`:$headers")
    public void addHeadersToProxyRequest(StringComparisonRule comparisonRule, String url, DefaultHttpHeaders headers)
    {
        applyUrlFilter(comparisonRule, url, request -> {
            request.headers().add(headers);
            return null;
        });
    }

    /**
     * The step allows to mock HTTP request, and provide within response status code, headers and content.
     * Once the request is matched by URL comparison rule response will be returned. In this case no actual request will
     * be executed.
     * @param comparisonRule String comparison rule: "is equal to", "contains", "does not contain", "matches"
     * @param url The string value of URL to match comparison rule
     * @param responseCode The code will be used in response
     * @param content The content will be used in the response
     * @param headers ExamplesTable representing the list of the headers with columns "name" and "value" specifying
     * HTTP header names and values respectively
     */
    @When(value = "I mock HTTP responses with request URL which $comparisonRule `$url` using"
            + " response code `$responseCode`, content `$payload` and headers:$headers", priority = 1)
    public void mockHttpRequests(StringComparisonRule comparisonRule, String url, int responseCode, Object content,
            DefaultHttpHeaders headers)
    {
        byte[] contentBytes = getBytes(content);
        applyUrlFilter(comparisonRule, url, request -> {
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
     * The step allows to mock HTTP request, and provide within response status code and headers.
     * Once the request is matched by URL comparison rule response will be returned. In this case no actual request will
     * be executed.
     * @param comparisonRule String comparison rule: "is equal to", "contains", "does not contain", "matches"
     * @param url The string value of URL to match comparison rule
     * @param responseCode The code will be used in response
     * @param headers ExamplesTable representing the list of the headers with columns "name" and "value" specifying
     * HTTP header names and values respectively
     */
    @When("I mock HTTP responses with request URL which $comparisonRule `$url` using response code `$responseCode`"
            + " and headers:$headers")
    public void mockHttpRequests(StringComparisonRule comparisonRule, String url, int responseCode,
            DefaultHttpHeaders headers)
    {
        applyUrlFilter(comparisonRule, url, request -> {
            DefaultHttpResponse mockedRequest =
                new DefaultHttpResponse(request.protocolVersion(), HttpResponseStatus.valueOf(responseCode));
            mockedRequest.headers().add(headers);
            return mockedRequest;
        });
    }

    private byte[] getBytes(Object content)
    {
        if (content instanceof String)
        {
            return ((String) content).getBytes(StandardCharsets.UTF_8);
        }
        else if (content instanceof byte[])
        {
            return (byte[]) content;
        }
        throw new IllegalArgumentException("Unsupported content type: " + content.getClass());
    }

    private void applyUrlFilter(StringComparisonRule comparisonRule, String url,
            Function<HttpRequest, HttpResponse> requestProcessor)
    {
        Matcher<String> expected = comparisonRule.createMatcher(url);
        proxy.addRequestFilter((request, contents, messageInfo) -> {
            if (expected.matches(messageInfo.getUrl()))
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
