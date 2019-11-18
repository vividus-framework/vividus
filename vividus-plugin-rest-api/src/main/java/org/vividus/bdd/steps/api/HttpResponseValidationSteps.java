/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd.steps.api;

import static org.hamcrest.Matchers.contains;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.tika.Tika;
import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.steps.ByteArrayValidationRule;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.http.HttpTestContext;
import org.vividus.http.client.HttpResponse;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.ResourceUtils;
import org.vividus.util.json.JsonProcessingException;
import org.vividus.util.json.JsonUtils;

public class HttpResponseValidationSteps
{
    private static final Tika TIKA = new Tika();

    @Inject private HttpTestContext httpTestContext;
    @Inject private ISoftAssert softAssert;
    @Inject private IBddVariableContext bddVariableContext;

    /**
     * Checks HTTP response does not contain body
     * <p>
     * <b>Actions performed at this step:</b>
     * </p>
     * <ul>
     * <li>Checks that there is no body in response</li>
     * </ul>
     * <p><i>Step can't be used without previous step with HTTP-method</i></p>
     */
    @Then("the response does not contain body")
    public void doesResponseNotContainBody()
    {
        performIfHttpResponseIsPresent(
            response -> softAssert.assertNull("The response does not contain body", response.getResponseBody()));
    }

    /**
     * Checks HTTP response body matches to the specified content according to the provided string validation rule
     * <p>
     * <b>Actions performed at this step:</b>
     * </p>
     * <ul>
     * <li>Retrieves response body from HTTP response</li>
     * <li>Checks that response matches to the specified content according to the provided string validation rule</li>
     * </ul>
     * <p><i>Step can't be used without previous step with HTTP-method</i></p>
     * @param comparisonRule String validation rule: "is equal to", "contains", "does not contain"
     * @param content body content part
     */
    @Then("the response body $comparisonRule '$content'")
    public void doesResponseBodyMatch(StringComparisonRule comparisonRule, String content)
    {
        performIfHttpResponseIsPresent(response -> softAssert.assertThat("HTTP response body",
                response.getResponseBodyAsString(), comparisonRule.createMatcher(content)));
    }

    /**
     * Checks content type of HTTP response body matches to the specified expected content type according to
     * the provided string validation rule
     * @param comparisonRule String validation rule: "is equal to", "contains", "does not contain"
     * @param contentType Expected content type, e.g. text/html, application/xml and etc.
     */
    @Then("content type of response body $comparisonRule `$contentType`")
    public void assertContentTypeOfResponseBody(StringComparisonRule comparisonRule, String contentType)
    {
        performIfHttpResponseIsPresent(response ->
        {
            byte[] responseBody = response.getResponseBody();
            String actualContentType = TIKA.detect(responseBody);
            if ("text/plain".equals(actualContentType) && isJson(responseBody))
            {
                actualContentType = "application/json";
            }
            softAssert.assertThat("Content type of response body", actualContentType,
                    comparisonRule.createMatcher(contentType));
        });
    }

    /**
     * Checks HTTP response body as bytes matches to the specified content as bytes according to the provided
     * validation rule
     * <p>
     * <b>Actions performed at this step:</b>
     * </p>
     * <ul>
     * <li>Retrieves response body from HTTP response</li>
     * <li>Checks that response as bytes matches to the specified content as bytes according to the provided validation
     * rule</li>
     * </ul>
     * <p><i>Step can't be used without previous step with HTTP-method</i></p>
     * @param validationRule validation rule: "IS_EQUAL_TO", "IS_NOT_EQUAL_TO"
     * @param resourcePath path to the resource
     */
    @Then(value = "the response body $validationRule resource at '$resourcePath'", priority = 1)
    public void doesResponseBodyMatchResource(ByteArrayValidationRule validationRule, String resourcePath)
    {
        performIfHttpResponseIsPresent(response -> validationRule.assertMatchesRule(softAssert,
                ResourceUtils.loadResourceAsByteArray(getClass(), resourcePath), response.getResponseBody()));
    }

    /**
     * Compare size of decompressed HTTP response body with value <b>sizeInBytes</b>
     * @param comparisonRule The rule to compare values<br>
     * (<i>Possible values:<b> less than, less than or equal to, greater than, greater than or equal to,
     * equal to</b></i>)
     * @param sizeInBytes expected size of the response body in bytes
     */
    @Then("size of decompressed response body is $comparisonRule `$sizeInBytes`")
    public void doesDecomressedResponseBodySizeConfirmRule(ComparisonRule comparisonRule, int sizeInBytes)
    {
        performIfHttpResponseIsPresent(response ->
            softAssert.assertThat("Size of decompressed HTTP response body", response.getResponseBody().length,
                    comparisonRule.getComparisonRule(sizeInBytes)));
    }

    /**
     * This step should be preceded with the step executing HTTP request
     * Step asserts that the response has the expected HTTP <b>responseCode</b>
     * @param comparisonRule The rule to compare values (Available values: less than, less than or equal to,
     * greater than, greater than or equal to, equal to)
     * @param responseCode for example 200, 404
     */
    @Then("the response code is $comparisonRule '$responseCode'")
    public void assertResponseCode(ComparisonRule comparisonRule, int responseCode)
    {
        performIfHttpResponseIsPresent(response -> softAssert.assertThat("HTTP response status code",
                response.getStatusCode(), comparisonRule.getComparisonRule(responseCode)));
    }

    /**
     * This step should be preceded with the step
     * 'When I issue a HTTP GET request for a resource with the URL '$url''
     * Step is validating the response time in milliseconds
     * @param responseTimeThresholdMs maximum response time in milliseconds
     */
    @Then("the response time should be less than '$responseTimeThresholdMs' milliseconds")
    public void thenTheResponseTimeShouldBeLessThan(long responseTimeThresholdMs)
    {
        performIfHttpResponseIsPresent(
            response -> softAssert.assertThat("The response time is less than response time threshold.",
                    response.getResponseTimeInMs(), Matchers.lessThan(responseTimeThresholdMs)));
    }

    /**
     * Validates that response <b>header</b> contains expected <b>attributes</b>.
     * <p>
     * <b>Actions performed at this step:</b>
     * </p>
     * <ul>
     * <li>Checks that HTTP response header contains expected attributes.</li>
     * </ul>
     * @param httpHeaderName HTTP header name. For example, <b>Content-Type</b>
     * @param attributes expected HTTP header attributes. For example, <b>charset=utf-8</b>
     */
    @Then("response header '$httpHeaderName' contains attribute: $attributes")
    public void assertHeaderContainsAttributes(String httpHeaderName, ExamplesTable attributes)
    {
        performIfHttpResponseIsPresent(response ->
        {
            getHeaderByName(response, httpHeaderName).ifPresent(header ->
            {
                List<String> actualAttributes = Arrays.stream(header.getElements()).map(HeaderElement::getName)
                        .collect(Collectors.toList());
                for (Parameters row : attributes.getRowsAsParameters(true))
                {
                    String expectedAttribute = row.valueAs("attribute", String.class);
                    softAssert.assertThat(httpHeaderName + " header contains " + expectedAttribute + " attribute",
                            actualAttributes, contains(expectedAttribute));
                }
            });
        });
    }

    /**
     * Validates that the value of specified response header matches to the expected value according to the
     * provided string validation rule
     * <p><b>Actions performed by this step:</b></p>
     * <ul><li>Checks that HTTP response header value matches to the expected one.</li></ul>
     * @param httpHeaderName HTTP header name. For example, <b>Content-Type</b>
     * @param comparisonRule String validation rule: "is equal to", "contains", "does not contain"
     * @param value expected HTTP header value. For example, <b>text/html</b>
     */
    @Then("the value of the response header '$httpHeaderName' $comparisonRule '$value'")
    @Alias("the value of the response header \"$httpHeaderName\" $comparisonRule \"$value\"")
    public void doesHeaderMatch(String httpHeaderName, StringComparisonRule comparisonRule, String value)
    {
        performIfHttpResponseIsPresent(response -> getHeaderValueByName(response, httpHeaderName)
                .ifPresent(actualValue -> softAssert.assertThat("'" + httpHeaderName + "' header value", actualValue,
                        comparisonRule.createMatcher(value))));
    }

    /**
     * Checks whether the response contains specified number of headers with the name <b>headerName</b>
     * @param headerName HTTP header name. For example, <b>Content-Type</b>
     * @param comparisonRule The rule to compare values
     * (<i>Possible values:<b> LESS_THAN, LESS_THAN_OR_EQUAL_TO, GREATER_THAN, GREATER_THAN_OR_EQUAL_TO,
     * EQUAL_TO</b></i>)
     * @param value The number to compare with
     */
    @Then("the number of the response headers with the name '$headerName' is $comparisonRule $value")
    public void isHeaderWithNameFound(String headerName, ComparisonRule comparisonRule, int value)
    {
        performIfHttpResponseIsPresent(response -> softAssert.assertThat(
                String.format("The number of the response headers with the name '%s'", headerName),
                (int) response.getHeadersByName(headerName).count(), comparisonRule.getComparisonRule(value)));
    }

    /**
     * Saves the value of specified response header into the variable with given scope and name.
     * @param httpHeaderName HTTP header name. For example, <b>Content-Type</b>.
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A variable name
     */
    @When("I save response header '$httpHeaderName' value to $scopes variable '$variableName'")
    public void saveHeaderValue(String httpHeaderName, Set<VariableScope> scopes, String variableName)
    {
        performIfHttpResponseIsPresent(response -> getHeaderValueByName(response, httpHeaderName)
                .ifPresent(value -> bddVariableContext.putVariable(scopes, variableName, value)));
    }

    /**
     * Saves the value of response body into the variable with given scope and name.
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A variable name
     */
    @When("I save response body to the $scopes variable '$variableName'")
    public void saveResponseBody(Set<VariableScope> scopes, String variableName)
    {
        performIfHttpResponseIsPresent(
            response -> bddVariableContext.putVariable(scopes, variableName, response.getResponseBodyAsString()));
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

    private HttpResponse getResponse()
    {
        return httpTestContext.getResponse();
    }

    private boolean isJson(byte[] responseBody)
    {
        try
        {
            new JsonUtils().toJson(responseBody);

            //Single number ("1") is valid JSON as well, but we can't guarantee that it's really JSON
            return StringUtils.startsWithAny(new String(responseBody, StandardCharsets.UTF_8).trim(), "[", "{");
        }
        catch (JsonProcessingException e)
        {
            return false;
        }
    }

    private void performIfHttpResponseIsPresent(Consumer<HttpResponse> responseConsumer)
    {
        HttpResponse response = getResponse();
        if (softAssert.assertNotNull("HTTP response is not null", response))
        {
            responseConsumer.accept(response);
        }
    }
}
