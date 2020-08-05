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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.inject.Inject;

import com.browserup.harreader.model.Har;
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
import org.jbehave.core.model.ExamplesTable;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.proxy.IProxy;
import org.vividus.proxy.ProxyLog;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.action.IWaitActions;

import io.netty.handler.codec.http.DefaultHttpHeaders;

@SuppressWarnings("PMD.ExcessiveImports")
public class ProxySteps
{
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Inject private IProxy proxy;
    @Inject private ISoftAssert softAssert;
    @Inject private IAttachmentPublisher attachmentPublisher;
    @Inject private IBddVariableContext bddVariableContext;
    @Inject private IWaitActions waitActions;

    private final ThreadLocal<Optional<ProxyLog>> externalProxyLog = ThreadLocal.withInitial(Optional::empty);

    /**
     * Clears the proxy log
     */
    @When("I clear proxy log")
    public void clearProxyLog()
    {
        proxy.getLog().clear();
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
     * @param httpMethod HTTP method to filter by
     * @param urlPattern the string value of URL-pattern to filter by
     * @param comparisonRule The rule to compare values
     * (<i>Possible values:<b> LESS_THAN, LESS_THAN_OR_EQUAL_TO, GREATER_THAN, GREATER_THAN_OR_EQUAL_TO,
     * EQUAL_TO</b></i>)
     * @param number The number to compare with
     * @throws IOException If any error happens during operation
     * @return Filtered HAR entries
     */
    @Then("number of HTTP $httpMethod requests with URL pattern `$urlPattern` is $comparisonRule `$number`")
    public List<HarEntry> checkNumberOfRequests(HttpMethod httpMethod, String urlPattern,
            ComparisonRule comparisonRule, long number) throws IOException
    {
        List<HarEntry> harEntries = getHarEntries(httpMethod, urlPattern);
        assertSize(String.format("Number of HTTP %s requests matching URL pattern '%s'", httpMethod, urlPattern),
                harEntries, comparisonRule, number);
        return harEntries;
    }

    private List<HarEntry> getHarEntries(HttpMethod httpMethod, String urlPattern)
    {
        return getProxyLog().getLogEntries(httpMethod, urlPattern)
                .stream()
                .filter(h -> h.getResponse().getStatus() != HttpStatus.SC_MOVED_TEMPORARILY)
                .collect(Collectors.toList());
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
     * @param httpMethod HTTP method to filter by
     * @param urlPattern The string value of URL-pattern to filter by
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
    @When("I capture HTTP $httpMethod request with URL pattern `$urlPattern` and save URL query to $scopes "
            + "variable `$variableName`")
    public void captureRequestAndSaveURLQuery(HttpMethod httpMethod, String urlPattern, Set<VariableScope> scopes,
            String variableName) throws IOException
    {
        List<HarEntry> harEntries = checkNumberOfRequests(httpMethod, urlPattern, ComparisonRule.EQUAL_TO, 1);
        if (harEntries.size() == 1)
        {
            HarEntry harEntry = harEntries.get(0);
            bddVariableContext.putVariable(scopes, variableName, getQueryParameters(harEntry.getRequest()));
        }
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
     * @param httpMethod HTTP method to filter by
     * @param urlPattern The string value of URL-pattern to filter by
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
    @When("I capture HTTP $httpMethod request with URL pattern `$urlPattern` and save request data to $scopes "
            + "variable `$variableName`")
    public void captureRequestAndSaveRequestData(HttpMethod httpMethod, String urlPattern, Set<VariableScope> scopes,
            String variableName) throws IOException
    {
        List<HarEntry> harEntries = checkNumberOfRequests(httpMethod, urlPattern, ComparisonRule.EQUAL_TO, 1);
        if (harEntries.size() == 1)
        {
            HarEntry harEntry = harEntries.get(0);
            HarRequest request = harEntry.getRequest();
            HarPostData postData = request.getPostData();
            Map<String, Object> requestData = Map.of(
                    "query", getQueryParameters(request),
                    "requestBody", getRequestBody(postData),
                    "requestBodyParameters", getRequestBodyParameters(postData),
                    "responseStatus", harEntry.getResponse().getStatus()
            );
            bddVariableContext.putVariable(scopes, variableName, requestData);
        }
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
     * @param httpMethod HTTP method to filter by
     * @param urlPattern The string value of URL-pattern to filter by
     */
    @When("I wait until HTTP $httpMethod request with URL pattern `$urlPattern` exists in proxy log")
    public void waitRequestInProxyLog(HttpMethod httpMethod, String urlPattern)
    {
        waitActions.wait(urlPattern, new Function<>()
        {
            @Override
            public Boolean apply(String urlPattern)
            {
                List<HarEntry> entries = getHarEntries(httpMethod, urlPattern);
                return !entries.isEmpty();
            }

            @Override
            public String toString()
            {
                return String.format("waiting for HTTP %s request with URL pattern %s", httpMethod, urlPattern);
            }
        });
    }

    /**
     * Add headers to proxy request that will be used while sending request with given urlPattern
     * @param headers ExamplesTable representing list of headers with columns "name" and "value" specifying HTTP header
     * names and values respectively
     * @param comparisonRule String comparison rule: "is equal to", "contains", "does not contain"
     * @param urlPattern The string value of URL-pattern to filter by
     */
    @When("I add headers to proxied requests with URL pattern which $comparisonRule `$urlPattern`:$headers")
    public void addHeadersToProxyRequest(StringComparisonRule comparisonRule, String urlPattern, ExamplesTable headers)
    {
        DefaultHttpHeaders httpHeaders = new DefaultHttpHeaders();
        headers.getRowsAsParameters(true)
                .forEach(row -> httpHeaders.add(
                        row.valueAs("name", String.class),
                        (Object) row.valueAs("value", String.class)
                ));
        Matcher<String> expected = comparisonRule.createMatcher(urlPattern);
        proxy.addRequestFilter((request, contents, messageInfo) -> {
            if (expected.matches(messageInfo.getUrl()))
            {
                request.headers().add(httpHeaders);
            }
            return null;
        });
    }

    private boolean assertSize(String assertionDescription, Collection<?> collection, ComparisonRule comparisonRule,
            long expectedSize) throws IOException
    {
        boolean assertionPassed = softAssert.assertThat(assertionDescription, (long) collection.size(),
                comparisonRule.getComparisonRule(expectedSize));
        if (!assertionPassed)
        {
            publishHar();
        }
        return assertionPassed;
    }

    private void publishHar() throws IOException
    {
        Har har = proxy.getProxyServer().getHar();
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream())
        {
            OBJECT_MAPPER.writeValue(byteArrayOutputStream, har);
            attachmentPublisher.publishAttachment(byteArrayOutputStream.toByteArray(), "har.har");
        }
    }

    private ProxyLog getProxyLog()
    {
        return externalProxyLog.get().orElse(proxy.getLog());
    }
}
