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

package org.vividus.bdd.steps.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.atMost;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.jayway.jsonpath.PathNotFoundException;

import org.apache.http.ConnectionClosedException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.SubSteps;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.http.HttpRequestExecutor;
import org.vividus.http.HttpTestContext;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.ResourceUtils;
import org.vividus.util.json.JsonPathUtils;
import org.vividus.util.json.JsonUtils;

import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Options;

@SuppressWarnings("MethodCount")
@ExtendWith(MockitoExtension.class)
class JsonResponseValidationStepsTests
{
    private static final int DURATION_DIVIDER = 10;
    private static final String GET = "GET";
    private static final String SOME_PATH = "$.some";
    private static final String RESPONSE_PATH = "$.response";
    private static final String RESPONSE_NULL = "{\"response\": null}";
    private static final String NON_EXISTING_PATH = "$['non-existing-element']";
    private static final String NON_EXISTING_SOME_PATH = "$['some']";
    private static final String BOOL_ELEMENT_JSON_PATH = "$..bool";
    private static final String TRUE = "true";

    private static final String OBJECT_PATH = "$.test";
    private static final String OBJECT_PATH_RESULT = "{\"name\":\"value\",\"number\":42,\"bool\":true,\"array\":[1,2]}";

    private static final String STRING_PATH = "$.test.name";
    private static final String STRING_PATH_RESULT = "\"value\"";

    private static final String ARRAY_PATH = "$.test.array";
    private static final String ARRAY_PATH_RESULT = "[1,2]";

    private static final String VARIABLE_NAME = "name";
    private static final String JSON = "{\"test\":" + OBJECT_PATH_RESULT + "}";
    private static final int ELEMENTS_NUMBER = 1;

    private static final String URL = "http://www.example.com/";

    private static final String JSON_PATH = "$..value";
    private static final String THE_NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE =
            "The number of JSON elements by JSON path: ";
    private static final String HTML =
            "<html><head><meta http-equiv=\"Content-Type\" content=\"text/html;charset=utf-8\"/>";

    @Mock
    private IBddVariableContext bddVariableContext;

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private HttpTestContext httpTestContext;

    @Mock
    private IHttpClient httpClient;

    @Mock
    private IAttachmentPublisher attachmentPublisher;

    private JsonResponseValidationSteps jsonResponseValidationSteps;

    @BeforeEach
    void beforeEach()
    {
        JsonPathUtils.setJacksonConfiguration();
        HttpRequestExecutor httpRequestExecutor = new HttpRequestExecutor(httpClient, httpTestContext, softAssert);
        jsonResponseValidationSteps = new JsonResponseValidationSteps(httpTestContext, bddVariableContext,
                attachmentPublisher, httpRequestExecutor, new JsonUtils());
        jsonResponseValidationSteps.setSoftAssert(softAssert);
    }

    static Stream<Arguments> defaultDataProvider()
    {
        // @formatter:off
        return Stream.of(
                Arguments.of(STRING_PATH,               STRING_PATH_RESULT),
                Arguments.of(OBJECT_PATH,               OBJECT_PATH_RESULT),
                Arguments.of("$.test.number",           "42"),
                Arguments.of("$.test.bool",             TRUE),
                Arguments.of(ARRAY_PATH,                ARRAY_PATH_RESULT),
                Arguments.of("$..number",               "[42]"),
                Arguments.of(BOOL_ELEMENT_JSON_PATH,    "[true]")
        );
        // @formatter:on
    }

    static Stream<Arguments> checkJsonElementsNumberDataProvider()
    {
        // @formatter:off
        return Stream.of(
                Arguments.of(STRING_PATH, ELEMENTS_NUMBER),
                Arguments.of(ARRAY_PATH,          2),
                Arguments.of(NON_EXISTING_PATH,   0)
        );
        // @formatter:on
    }

    @ParameterizedTest
    @MethodSource("defaultDataProvider")
    void testIsDataByJsonPathEqual(String jsonPath, String expectedData)
    {
        when(httpTestContext.getJsonContext()).thenReturn(JSON);
        testIsDataByJsonPathEqual(jsonPath, expectedData, expectedData, Options.empty());
    }

    @ParameterizedTest
    @MethodSource("defaultDataProvider")
    void testIsDataByJsonPathFromJsonEqual(String jsonPath, String expectedData)
    {
        jsonResponseValidationSteps.isDataByJsonPathFromJsonEqual(JSON, jsonPath, expectedData, Options.empty());
        verifyJsonAssertion(jsonPath, expectedData, expectedData);
    }

    @Test
    void testIsDataByJsonPathEqualIgnoringArrayOrder()
    {
        when(httpTestContext.getJsonContext()).thenReturn(JSON);
        testIsDataByJsonPathEqual(ARRAY_PATH, "[2,1]", ARRAY_PATH_RESULT,
                new Options(Option.IGNORING_ARRAY_ORDER));
    }

    @Test
    void testIsDataByJsonPathFromJsonEqualCheckEmptyArrayMissmatch()
    {
        String json = "{ \"arrayKey\": [ { \"idKey\": \"q4jn0f8\", \"randValueKey\": \"i4t8ivC\"} ] }";
        String expected = "[ { \"idKey\": \"b54Y8id\", \"randValueKey\": \"i4t8ivC\"} ]";

        jsonResponseValidationSteps.isDataByJsonPathFromJsonEqual(json, "$.arrayKey.[?(@.idKey==\"b54Y8id\")]",
                expected, Options.empty());

        verify(softAssert).recordFailedAssertion("Array \"\" has different length, expected: <1> but was: <0>.");
        verify(softAssert).recordFailedAssertion("Array \"\" has different content. Missing values: "
                + "[{\"idKey\":\"b54Y8id\",\"randValueKey\":\"i4t8ivC\"}], expected: <[{\"idKey\":\"b54Y8id\","
                + "\"randValueKey\":\"i4t8ivC\"}]> but was: <[]>");
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void testIsDataByJsonPathEqualIgnoringArrayOrderAndExtraArrayItems()
    {
        when(httpTestContext.getJsonContext()).thenReturn(JSON);
        testIsDataByJsonPathEqual(ARRAY_PATH, "[2]", ARRAY_PATH_RESULT,
                new Options(Option.IGNORING_ARRAY_ORDER, Option.IGNORING_EXTRA_ARRAY_ITEMS));
    }

    private void testIsDataByJsonPathEqual(String jsonPath, String expectedData, String actualData, Options options)
    {
        jsonResponseValidationSteps.isDataByJsonPathEqual(jsonPath, expectedData, options);
        verifyJsonAssertion(jsonPath, expectedData, actualData);
    }

    private void verifyJsonAssertion(String jsonPath, String expectedData, String actualData)
    {
        verify(softAssert).assertThat(
                eq("Data by JSON path: " + jsonPath + " is equal to '" + expectedData + "'"), eq(actualData),
                argThat(matcher -> matcher.matches(actualData)));
    }

    @Test
    void testIsDataByJsonPathEqualWithPathNotFoundException()
    {
        when(httpTestContext.getJsonContext()).thenReturn(JSON);
        String nonExistingPath = NON_EXISTING_PATH;
        jsonResponseValidationSteps.isDataByJsonPathEqual(nonExistingPath, STRING_PATH_RESULT,
                Options.empty());
        verifyPathNotFoundExceptionRecording(nonExistingPath);
    }

    @ParameterizedTest
    @MethodSource("checkJsonElementsNumberDataProvider")
    void testDoesJsonPathElementsMatchRule(String jsonPath, int elementsNumber)
    {
        when(httpTestContext.getJsonContext()).thenReturn(JSON);
        jsonResponseValidationSteps.doesJsonPathElementsMatchRule(jsonPath, ComparisonRule.EQUAL_TO, elementsNumber);
        verify(softAssert).assertThat(eq(THE_NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + jsonPath), eq(elementsNumber),
                verifyMatcher(elementsNumber));
    }

    @ParameterizedTest
    @MethodSource("checkJsonElementsNumberDataProvider")
    void testDoesJsonPathElementsFromJsonMatchRule(String jsonPath, int elementsNumber)
    {
        jsonResponseValidationSteps.doesJsonPathElementsFromJsonMatchRule(JSON, jsonPath, ComparisonRule.EQUAL_TO,
                elementsNumber);
        verify(softAssert).assertThat(eq(THE_NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + jsonPath), eq(elementsNumber),
                verifyMatcher(elementsNumber));
    }

    @ParameterizedTest
    @MethodSource("checkJsonElementsNumberDataProvider")
    void testSaveElementsNumberByJsonPath(String jsonPath, int elementsNumber)
    {
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        when(httpTestContext.getJsonContext()).thenReturn(JSON);
        jsonResponseValidationSteps.saveElementsNumberByJsonPath(jsonPath, scopes, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(scopes, VARIABLE_NAME, elementsNumber);
    }

    @Test
    void testSaveJsonFromContextElementToVariable()
    {
        when(httpTestContext.getJsonContext()).thenReturn(JSON);
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        String variableName = VARIABLE_NAME;
        JsonResponseValidationSteps spy = Mockito.spy(jsonResponseValidationSteps);
        spy.saveJsonElementFromContextToVariable(STRING_PATH, scopes, variableName);
        verify(spy).saveJsonElementToVariable(JSON, STRING_PATH, scopes, variableName);
    }

    @ParameterizedTest
    @MethodSource("defaultDataProvider")
    void testSaveJsonElementFromGivenJsonToVariable(String jsonPath, String expectedData)
    {
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        String variableName = VARIABLE_NAME;
        jsonResponseValidationSteps.saveJsonElementToVariable(JSON, jsonPath, scopes, variableName);
        verify(bddVariableContext).putVariable(scopes, variableName, expectedData);
    }

    @Test
    void testSaveJsonElementFromGivenJsonToVariableWithPathNotFoundException()
    {
        String nonExistingPath = NON_EXISTING_PATH;
        jsonResponseValidationSteps
                .saveJsonElementToVariable(JSON, nonExistingPath, Set.of(VariableScope.STORY), VARIABLE_NAME);
        verifyPathNotFoundExceptionRecording(nonExistingPath);
        verifyNoMoreInteractions(bddVariableContext);
    }

    @ParameterizedTest
    @CsvSource({
            "'[{\"value\":\"b\"},{\"value\":\"c\"},{\"value\":\"a\"}]',   $..value,   3",
            "'[{\"value\":\"b\"},{\"value1\":\"c\"},{\"value1\":\"a\"}]', $[0].value, 1"
    })
    void testPerformAllStepsForProvidedJsonIfFound(String json, String jsonPath, int number)
    {
        SubSteps subSteps = mock(SubSteps.class);
        when(softAssert.assertThat(eq(THE_NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + jsonPath), eq(number),
                verifyMatcher(number))).thenReturn(true);
        jsonResponseValidationSteps.performAllStepsForProvidedJsonIfFound(ComparisonRule.GREATER_THAN_OR_EQUAL_TO,
                number, json, jsonPath, subSteps);
        verify(subSteps, times(number)).execute(Optional.empty());
        verify(httpTestContext).getJsonContext();
        verify(httpTestContext).putJsonContext(null);
    }

    @Test
    void testPerformAllStepsForJsonIfFound()
    {
        when(httpTestContext.getJsonContext()).thenReturn(JSON);
        SubSteps subSteps = mock(SubSteps.class);
        when(softAssert.assertThat(eq(THE_NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + JSON_PATH), eq(0),
                verifyMatcher(3))).thenReturn(false);
        jsonResponseValidationSteps.performAllStepsForJsonIfFound(ComparisonRule.GREATER_THAN_OR_EQUAL_TO, 0,
                JSON_PATH, subSteps);
        verifyNoInteractions(subSteps);
        verify(httpTestContext, times(0)).putJsonContext(any());
    }

    @Test
    void testWaitForJsonFieldAppearsWithoutPolling() throws IOException, IllegalAccessException, NoSuchFieldException
    {
        mockResponse(JSON);
        testWaitForJsonFieldAppears(1);
        verify(httpClient).execute(argThat(base -> base instanceof HttpRequestBase
                && base.getMethod().equals(GET)
                && base.getURI().equals(URI.create(URL))),
                argThat(context -> context instanceof HttpClientContext));
    }

    @Test
    void testWaitForJsonFieldAppearsWithPolling() throws IOException, IllegalAccessException, NoSuchFieldException
    {
        String body = "{\"key\":\"value\"}";
        mockResponse(body);
        testWaitForJsonFieldAppears(0);
        verify(httpClient, atLeast(5)).execute(argThat(base -> base instanceof HttpRequestBase
                && base.getMethod().equals(GET)
                && base.getURI().equals(URI.create(URL))),
                argThat(context -> context instanceof HttpClientContext));
        verify(httpClient, atMost(10)).execute(argThat(base -> base instanceof HttpRequestBase),
                argThat(context -> context instanceof HttpClientContext));
    }

    @Test
    void shouldIgnoreInvalidBodyDuringTimeoutTest() throws IOException
    {
        HttpResponse response = mock(HttpResponse.class);
        when(httpClient.execute(argThat(base -> base instanceof HttpRequestBase),
                argThat(context -> context instanceof HttpClientContext))).thenReturn(response);
        when(httpTestContext.getResponse()).thenReturn(response);
        when(httpTestContext.getJsonContext()).thenReturn(JSON);
        when(response.getResponseBody()).thenReturn(JSON.getBytes(StandardCharsets.UTF_8));
        when(response.getResponseBodyAsString()).thenReturn(HTML, JSON);
        jsonResponseValidationSteps.waitForJsonFieldAppearance(STRING_PATH, URL, Duration.ofSeconds(1),
                DURATION_DIVIDER);
        verify(httpClient, times(2)).execute(argThat(base -> base instanceof HttpRequestBase),
                argThat(context -> context instanceof HttpClientContext));
        verify(softAssert).assertThat(eq(THE_NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + STRING_PATH), eq(1),
                argThat(m -> "a value greater than <0>".equals(m.toString())));
    }

    @Test
    void testFailedAssertionRecordingIfResponseDidNtContainJson() throws IOException
    {
        HttpResponse response = mock(HttpResponse.class);
        when(httpClient.execute(argThat(base -> base instanceof HttpRequestBase),
                argThat(context -> context instanceof HttpClientContext))).thenReturn(response);
        when(httpTestContext.getResponse()).thenReturn(response);
        when(response.getResponseBodyAsString()).thenReturn(HTML);
        jsonResponseValidationSteps.waitForJsonFieldAppearance(STRING_PATH, URL, Duration.ofSeconds(1),
                DURATION_DIVIDER);
        verify(softAssert).recordFailedAssertion("HTTP response body is not present");
    }

    @Test
    void testWaitForJsonFieldAppearsHandledException() throws IOException
    {
        when(httpClient.execute(argThat(base -> base instanceof HttpRequestBase
                && base.getMethod().equals(GET)
                && base.getURI().equals(URI.create(URL))),
                argThat(context -> context instanceof HttpClientContext)))
            .thenThrow(new ConnectionClosedException());
        jsonResponseValidationSteps.waitForJsonFieldAppearance(STRING_PATH, URL, Duration.ofSeconds(1),
                DURATION_DIVIDER);
        verify(softAssert).recordFailedAssertion(
                (Exception) argThat(arg -> arg instanceof ConnectionClosedException
                        && "Connection is closed".equals(((Exception) arg).getMessage())));
    }

    @Test
    void shouldWaitForJsonElement()
    {
        SubSteps stepsToExecute = mock(SubSteps.class);
        when(httpTestContext.getResponse())
                .thenReturn(createHttpResponse(HTML))
                .thenReturn(createHttpResponse(OBJECT_PATH_RESULT))
                .thenReturn(new HttpResponse())
                .thenReturn(createHttpResponse(JSON));
        when(httpTestContext.getJsonContext()).thenReturn(JSON);
        int retryTimes = 10;
        jsonResponseValidationSteps.waitForJsonElement(STRING_PATH, Duration.ofSeconds(2), retryTimes, stepsToExecute);
        verify(stepsToExecute, atLeast(4)).execute(Optional.empty());
        verify(stepsToExecute, atMost(retryTimes)).execute(Optional.empty());
        verify(softAssert).assertThat(eq(THE_NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + STRING_PATH), eq(1),
                verifyMatcher(1));
    }

    @Test
    void shouldWaitForJsonElementWithPollingInterval()
    {
        SubSteps stepsToExecute = mock(SubSteps.class);
        when(httpTestContext.getResponse())
                .thenReturn(createHttpResponse(HTML))
                .thenReturn(createHttpResponse(OBJECT_PATH_RESULT))
                .thenReturn(new HttpResponse())
                .thenReturn(createHttpResponse(JSON));
        when(httpTestContext.getJsonContext()).thenReturn(JSON);
        int retryTimes = 4;
        jsonResponseValidationSteps.waitForJsonElementWithPollingInterval(STRING_PATH,
                Duration.ofSeconds(1), retryTimes, stepsToExecute);
        verify(stepsToExecute, times(retryTimes)).execute(Optional.empty());
        verify(softAssert).assertThat(eq(THE_NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + STRING_PATH), eq(1),
                verifyMatcher(1));
    }

    @Test
    void shouldNotWaitForJsonElementWhenHttpResponseIsNull()
    {
        SubSteps stepsToExecute = mock(SubSteps.class);
        when(httpTestContext.getResponse()).thenReturn(null);
        int retryTimes = 3;
        jsonResponseValidationSteps.waitForJsonElement(STRING_PATH, Duration.ofSeconds(1), retryTimes, stepsToExecute);
        verify(stepsToExecute, times(1)).execute(Optional.empty());
        verifyNoInteractions(softAssert);
    }

    private void testWaitForJsonFieldAppears(int elementsFound) throws IOException
    {
        jsonResponseValidationSteps.waitForJsonFieldAppearance(STRING_PATH, URL, Duration.parse("PT2S"),
                DURATION_DIVIDER);
        verify(softAssert).assertThat(eq(THE_NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + STRING_PATH),
                eq(elementsFound), verifyMatcher(1));
    }

    private void mockResponse(String body) throws IOException
    {
        HttpResponse response = createHttpResponse(body);
        when(httpTestContext.getResponse()).thenReturn(response);
        when(httpTestContext.getJsonContext()).thenReturn(body);
        when(httpClient.execute(argThat(base -> base instanceof HttpRequestBase),
                argThat(context -> context instanceof HttpClientContext))).thenReturn(response);
    }

    private HttpResponse createHttpResponse(String body)
    {
        HttpResponse response = new HttpResponse();
        response.setResponseTimeInMs(0);
        response.setResponseBody(body.getBytes(StandardCharsets.UTF_8));
        return response;
    }

    @ParameterizedTest
    @CsvSource({
        "$.response, 1",
        "$.some, 0"
    })
    void testJsonPathElementsMatchRuleEmptyData(String jsonPath, int number)
    {
        when(httpTestContext.getJsonContext()).thenReturn(RESPONSE_NULL);
        jsonResponseValidationSteps.doesJsonPathElementsMatchRule(jsonPath, ComparisonRule.EQUAL_TO, number);
        verify(softAssert).assertThat(eq(THE_NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + jsonPath), eq(number),
                verifyMatcher(number));
    }

    @Test
    void testSaveJsonElementToVariableNullData()
    {
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        jsonResponseValidationSteps.saveJsonElementToVariable(RESPONSE_NULL, RESPONSE_PATH, scopes, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(scopes, VARIABLE_NAME, "null");
    }

    @Test
    void testSaveJsonElementToVariableNoData()
    {
        jsonResponseValidationSteps.saveJsonElementToVariable(RESPONSE_NULL, SOME_PATH, Set.of(VariableScope.STORY),
                VARIABLE_NAME);
        verifyPathNotFoundExceptionRecording(NON_EXISTING_SOME_PATH);
    }

    @Test
    void testIsDataByJsonPathFromJsonEqualCheckMissmatches()
    {
        String json = ResourceUtils.loadResource(getClass(), "missmatches-actual-data.json");
        String expected = ResourceUtils.loadResource(getClass(), "missmatches-expected-data.json");
        jsonResponseValidationSteps.isDataByJsonPathFromJsonEqual(json, "$", expected,
                new Options(Option.IGNORING_ARRAY_ORDER));
        verify(softAssert).recordFailedAssertion("Different value found when comparing expected array element [0] to"
                + " actual element [0].");
        verify(softAssert).recordFailedAssertion("Different keys found in node \"[0]\", missing: \"[0].missing_value\""
                + ", extra: \"[0].extra_value\",\"[0].line_terminator\", expected: <{\"different_value\":\"nf83u\""
                + ",\"missing_value\":\"2k3nf\",\"numbers_array\":[0, 1, 2]}> but was: <{\"different_value\":\"2ince\""
                + ",\"extra_value\":\"4ujeu\",\"line_terminator\":\"jfnse4\n4mn2j\nm38uff\n\",\"numbers_array\":"
                + "[0, -1]}>");
        verify(softAssert).recordFailedAssertion("Different value found in node \"[0].different_value\", expected:"
                + " <\"nf83u\"> but was: <\"2ince\">.");
        verify(softAssert).recordFailedAssertion("Array \"[0].numbers_array\" has different length, expected: <3>"
                + " but was: <2>.");
        verify(softAssert).recordFailedAssertion("Array \"[0].numbers_array\" has different content. Missing values:"
                + " [1, 2], extra values: [-1], expected: <[0,1,2]> but was: <[0,-1]>");
        verifyNoMoreInteractions(softAssert);
    }

    private void verifyPathNotFoundExceptionRecording(String nonExistingJsonPath)
    {
        verify(softAssert).recordFailedAssertion((Exception) argThat(
            arg -> arg instanceof PathNotFoundException && ("No results for path: " + nonExistingJsonPath)
                    .equals(((PathNotFoundException) arg).getMessage())));
        verifyNoMoreInteractions(softAssert);
    }

    private <T, K> T verifyMatcher(K matching)
    {
        Class<TypeSafeMatcher> clazz = TypeSafeMatcher.class;
        return argThat(arg -> clazz.isInstance(arg) && clazz.cast(arg).matches(matching));
    }
}
