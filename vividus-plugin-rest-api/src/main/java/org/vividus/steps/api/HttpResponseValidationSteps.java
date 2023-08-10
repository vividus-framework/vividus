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

package org.vividus.steps.api;

import static org.hamcrest.Matchers.contains;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HeaderElement;
import org.apache.hc.core5.http.message.MessageSupport;
import org.apache.tika.Tika;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.vividus.context.VariableContext;
import org.vividus.http.ConnectionDetails;
import org.vividus.http.HttpTestContext;
import org.vividus.http.client.HttpResponse;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ByteArrayValidationRule;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.SubSteps;
import org.vividus.util.ResourceUtils;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.wait.DurationBasedWaiter;
import org.vividus.util.wait.WaitMode;
import org.vividus.variable.VariableScope;

public class HttpResponseValidationSteps
{
    private static final String HTTP_RESPONSE_STATUS_CODE = "HTTP response status code";
    private static final Tika TIKA = new Tika();

    private final HttpTestContext httpTestContext;
    private final VariableContext variableContext;
    private final ISoftAssert softAssert;
    private final JsonUtils jsonUtils;

    public HttpResponseValidationSteps(HttpTestContext httpTestContext, VariableContext variableContext,
            ISoftAssert softAssert, JsonUtils jsonUtils)
    {
        this.httpTestContext = httpTestContext;
        this.variableContext = variableContext;
        this.softAssert = softAssert;
        this.jsonUtils = jsonUtils;
    }

    /**
     * Validates that the HTTP response does not contain a body.
     */
    @Then("response does not contain body")
    public void doesResponseContainNoBody()
    {
        performIfHttpResponseIsPresent(
            response -> softAssert.assertNull("The response does not contain body", response.getResponseBody()));
    }

    /**
     * Checks content type of HTTP response body matches to the specified expected content type according to
     * the provided string validation rule
     *
     * @param comparisonRule The rule to match the variable value. The supported rules:
     *                       <ul>
     *                       <li>is equal to</li>
     *                       <li>contains</li>
     *                       <li>does not contain</li>
     *                       <li>matches</li>
     *                       </ul>
     * @param contentType    The eexpected content type, e.g. text/html, application/xml and etc.
     */
    @Then("content type of response body $comparisonRule `$contentType`")
    public void assertContentTypeOfResponseBody(StringComparisonRule comparisonRule, String contentType)
    {
        performIfHttpResponseIsPresent(response ->
        {
            byte[] responseBody = response.getResponseBody();
            String actualContentType = TIKA.detect(responseBody);
            if (ContentType.TEXT_PLAIN.getMimeType().equals(actualContentType) && jsonUtils.isJson(
                    new String(responseBody, StandardCharsets.UTF_8)))
            {
                actualContentType = "application/json";
            }
            softAssert.assertThat("Content type of response body", actualContentType,
                    comparisonRule.createMatcher(contentType));
        });
    }

    /**
     * Compares the HTTP response body against the resource data according to the provided rule.
     *
     * @param validationRule The validation rule, either <b>is equal to</b> or <b>is not equal to</b>
     * @param resourcePath   The resource path
     */
    @Then("response body $validationRule resource at `$resourcePath`")
    public void compareResponseBodyAgainstResource(ByteArrayValidationRule validationRule, String resourcePath)
    {
        performIfHttpResponseIsPresent(response -> validationRule.assertMatchesRule(softAssert,
                ResourceUtils.loadResourceAsByteArray(getClass(), resourcePath), response.getResponseBody()));
    }

    /**
     * Compare size of decompressed HTTP response body with value <b>sizeInBytes</b>
     *
     * @param comparisonRule The rule to match the variable value. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param sizeInBytes    The expected size of the response body in bytes
     */
    @Then("size of decompressed response body is $comparisonRule `$sizeInBytes`")
    public void doesDecompressedResponseBodySizeConfirmRule(ComparisonRule comparisonRule, int sizeInBytes)
    {
        performIfHttpResponseIsPresent(response ->
            softAssert.assertThat("Size of decompressed HTTP response body", response.getResponseBody().length,
                    comparisonRule.getComparisonRule(sizeInBytes)));
    }

    /**
     * Compares the <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status">HTTP response status code</a>
     * against the expected number.
     *
     * @param comparisonRule The rule to match the variable value. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param responseCode   The expected <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Status">HTTP
     * response status code</a>
     */
    @Then("response code is $comparisonRule `$responseCode`")
    public void validateResponseCode(ComparisonRule comparisonRule, int responseCode)
    {
        performIfHttpResponseIsPresent(response -> softAssert.assertThat(HTTP_RESPONSE_STATUS_CODE,
                response.getStatusCode(), comparisonRule.getComparisonRule(responseCode)));
    }

    /**
     * Compares the HTTP response time against the expected number in milliseconds.
     *
     * @param comparisonRule The rule to match the variable value. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param responseTime   The expected response time in milliseconds
     */
    @Then("response time is $comparisonRule `$responseTime` milliseconds")
    public void validateResponseTime(ComparisonRule comparisonRule, long responseTime)
    {
        performIfHttpResponseIsPresent(response -> softAssert.assertThat("HTTP response time",
                response.getResponseTimeInMs(), comparisonRule.getComparisonRule(responseTime)));
    }

    /**
     * Validates that the response header with the specified name contains elements. Might be useful to verify such
     * HTTP headers as <b>Set-Cookie</b> that have values that can be decomposed into multiple elements.
     * <br>
     * <br>
     * <i>HTTP header with elements format:</i>
     * <br>
     * <code>
     * header = [ element ] *( "," [ element ] )
     * </code>
     * <br>
     * <br>
     * <i>Step example:</i>
     * <br>
     * <code>
     * Then response header `Set-Cookie` contains elements:
     * <br>
     * |element |
     * <br>
     * |JSESSION|
     * <br>
     * |clientId|
     * </code>
     *
     * @param headerName The <a href="https://en.wikipedia.org/wiki/List_of_HTTP_header_fields#Response_fields">
     * HTTP response header</a> name
     * @param elements The ExamplesTable with expected elements
     */
    @Then("response header `$headerName` contains elements:$elements")
    public void checkHeaderContainsElements(String headerName, ExamplesTable elements)
    {
        performIfHttpResponseIsPresent(response -> getHeaderByName(response, headerName).ifPresent(header ->
        {
            List<String> headerElements = extractHeaderElementsNames(header);
            elements.getRowsAsParameters(true).stream()
                    .map(params -> params.valueAs("element", String.class))
                    .forEach(element -> softAssert.assertThat(
                            String.format("%s header contains %s element", headerName, element), headerElements,
                            contains(element)));
        }));
    }

    /**
     * Compares the header value against the expected value according to the comparison rule.
     *
     * @param headerName     The <a href="https://en.wikipedia.org/wiki/List_of_HTTP_header_fields#Response_fields">
     * HTTP response header</a> name
     * @param comparisonRule The rule to match the variable value. The supported rules:
     *                       <ul>
     *                       <li>is equal to</li>
     *                       <li>contains</li>
     *                       <li>does not contain</li>
     *                       <li>matches</li>
     *                       </ul>
     * @param value          The expected HTTP header value
     */
    @Then("value of response header `$headerName` $comparisonRule `$value`")
    public void validateHeaderValue(String headerName, StringComparisonRule comparisonRule, String value)
    {
        performIfHttpResponseIsPresent(response -> getHeaderValueByName(response, headerName)
                .ifPresent(actualValue -> softAssert.assertThat("'" + headerName + "' header value", actualValue,
                        comparisonRule.createMatcher(value))));
    }

    /**
     * Validates the number of HTTP response headers filtered by the specified name.
     *
     * @param headerName     The <a href="https://en.wikipedia.org/wiki/List_of_HTTP_header_fields#Response_fields">
     * HTTP response header</a> name
     * @param comparisonRule The rule to match the quantity of headers. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param number         The expected number of headers
     */
    @Then("number of response headers with name `$headerName` is $comparisonRule $number")
    public void validateNumberOfResponseHeaders(String headerName, ComparisonRule comparisonRule, int number)
    {
        performIfHttpResponseIsPresent(response -> softAssert.assertThat(
                String.format("Number of the response headers with name '%s'", headerName),
                (int) response.getHeadersByName(headerName).count(), comparisonRule.getComparisonRule(number)));
    }

    /**
     * Saves the HTTP response header value into a variable.
     *
     * @param headerName   The <a href="https://en.wikipedia.org/wiki/List_of_HTTP_header_fields#Response_fields">
     * HTTP response header</a> name
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                     scopes.<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName The variable name to store the header value.
     */
    @When("I save response header `$headerName` value to $scopes variable `$variableName`")
    public void saveHeaderValueToVariable(String headerName, Set<VariableScope> scopes, String variableName)
    {
        performIfHttpResponseIsPresent(response -> getHeaderValueByName(response, headerName)
                .ifPresent(value -> variableContext.putVariable(scopes, variableName, value)));
    }

    /**
     * Validates that the HTTP connection is secured with the defined security protocol.
     *
     * @param securityProtocol The expected security protocol, e.g. <b>TLSv1.2</b>, <b>TLSv1.3</b>
     */
    @Then("connection is secured using $securityProtocol protocol")
    public void validateConnectionIsSecured(String securityProtocol)
    {
        ConnectionDetails connectionDetails = httpTestContext.getConnectionDetails();
        if (softAssert.assertTrue("Connection is secure", connectionDetails.isSecure()))
        {
            softAssert.assertEquals("Security protocol", securityProtocol,
                    connectionDetails.getSecurityProtocol());
        }
    }

    /**
     * Waits for the specified number of times until HTTP response code is equal to what is expected.
     * <p>
     * <b>Actions performed:</b>
     * </p>
     * <ul>
     * <li>Execute sub-steps</li>
     * <li>Check if HTTP response code is equal to what is expected</li>
     * </ul>
     * @param responseCode The expected HTTP status code.
     * @param duration The time duration to wait in ISO-8601 format.
     * @param retryTimes The number of times the request will be retried: `duration/retryTimes = timeout` is a polling
     * timeout between requests.
     * @param stepsToExecute The steps to execute at each wait iteration.
     */
    @When("I wait for response code `$responseCode` for `$duration` duration retrying $retryTimes times"
            + "$stepsToExecute")
    public void waitForResponseCode(int responseCode, Duration duration, int retryTimes,
                                    SubSteps stepsToExecute)
    {
        new DurationBasedWaiter(new WaitMode(duration, retryTimes)).wait(
                () -> stepsToExecute.execute(Optional.empty()),
                () -> isResponseCodeIsEqualToExpected(httpTestContext.getResponse(), responseCode)
        );
        performIfHttpResponseIsPresent(
                response -> softAssert.assertEquals(HTTP_RESPONSE_STATUS_CODE, responseCode, response.getStatusCode()));
    }

    private static List<String> extractHeaderElementsNames(Header header)
    {
        return Stream.of(MessageSupport.parse(header)).map(HeaderElement::getName).collect(Collectors.toList());
    }

    private boolean isResponseCodeIsEqualToExpected(HttpResponse response, int expectedResponseCode)
    {
        return response != null && response.getStatusCode() == expectedResponseCode;
    }

    private Optional<String> getHeaderValueByName(HttpResponse response, String httpHeaderName)
    {
        return getHeaderByName(response, httpHeaderName).map(Header::getValue);
    }

    private Optional<Header> getHeaderByName(HttpResponse response, String httpHeaderName)
    {
        Optional<Header> header = response.getHeaderByName(httpHeaderName);
        softAssert.assertTrue(httpHeaderName + " header is present", header.isPresent());
        return header;
    }

    private void performIfHttpResponseIsPresent(Consumer<HttpResponse> responseConsumer)
    {
        HttpResponse response = httpTestContext.getResponse();
        if (softAssert.assertNotNull("HTTP response is not null", response))
        {
            responseConsumer.accept(response);
        }
    }
}
