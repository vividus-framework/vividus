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
import java.lang.reflect.Field;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.jayway.jsonpath.PathNotFoundException;

import org.apache.http.ConnectionClosedException;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.hamcrest.BaseMatcher;
import org.hamcrest.TypeSafeMatcher;
import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.ISubStepExecutor;
import org.vividus.bdd.steps.ISubStepExecutorFactory;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.http.HttpRequestExecutor;
import org.vividus.http.HttpTestContext;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.json.IJsonUtils;
import org.vividus.util.json.JsonPathUtils;
import org.vividus.util.json.JsonUtils;

import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Options;

@ExtendWith(MockitoExtension.class)
class JsonResponseValidationStepsTests
{
    private static final String HTTP_REQUEST_EXECUTOR_FIELD = "httpRequestExecutor";
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

    private final IJsonUtils jsonUtils = new JsonUtils(PropertyNamingStrategy.LOWER_CAMEL_CASE);

    @Mock
    private IBddVariableContext bddVariableContext;

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private ISubStepExecutorFactory subStepExecutorFactory;

    @Mock
    private HttpTestContext httpTestContext;

    @Mock
    private IHttpClient httpClient;

    @InjectMocks
    private JsonResponseValidationSteps jsonResponseValidationSteps;

    @BeforeEach
    void beforeEach()
    {
        jsonResponseValidationSteps.setJsonUtils(jsonUtils);
        JsonPathUtils.setJacksonConfiguration();
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
                verifyMatcher(TypeSafeMatcher.class, elementsNumber));
    }

    @ParameterizedTest
    @MethodSource("checkJsonElementsNumberDataProvider")
    void testDoesJsonPathElementsFromJsonMatchRule(String jsonPath, int elementsNumber)
    {
        jsonResponseValidationSteps.doesJsonPathElementsFromJsonMatchRule(JSON, jsonPath, ComparisonRule.EQUAL_TO,
                elementsNumber);
        verify(softAssert).assertThat(eq(THE_NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + jsonPath), eq(elementsNumber),
                verifyMatcher(TypeSafeMatcher.class, elementsNumber));
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
        spy.saveJsonElementFromContexToVariable(STRING_PATH, scopes, variableName);
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
        ExamplesTable stepsAsTable = mock(ExamplesTable.class);
        ISubStepExecutor subStepExecutor = mock(ISubStepExecutor.class);
        when(subStepExecutorFactory.createSubStepExecutor(stepsAsTable)).thenReturn(subStepExecutor);
        when(softAssert.assertThat(eq(THE_NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + jsonPath), eq(number),
                verifyMatcher(TypeSafeMatcher.class, number))).thenReturn(true);
        jsonResponseValidationSteps.performAllStepsForProvidedJsonIfFound(ComparisonRule.GREATER_THAN_OR_EQUAL_TO,
                number, json, jsonPath, stepsAsTable);
        verify(subStepExecutor, times(number)).execute(Optional.empty());
        verify(httpTestContext).getJsonContext();
        verify(httpTestContext).putJsonContext(null);
    }

    @Test
    void testPerformAllStepsForJsonIfFound()
    {
        when(httpTestContext.getJsonContext()).thenReturn(JSON);
        ExamplesTable stepsAsTable = mock(ExamplesTable.class);
        when(softAssert.assertThat(eq(THE_NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + JSON_PATH), eq(0),
                verifyMatcher(TypeSafeMatcher.class, 3))).thenReturn(false);
        jsonResponseValidationSteps.performAllStepsForJsonIfFound(ComparisonRule.GREATER_THAN_OR_EQUAL_TO, 0,
                JSON_PATH, stepsAsTable);
        verifyNoInteractions(stepsAsTable, subStepExecutorFactory);
        verify(httpTestContext, times(0)).putJsonContext(any());
    }

    @Test
    void testWaitForJsonFieldAppearsWithoutPolling() throws IOException, IllegalArgumentException,
            IllegalAccessException, NoSuchFieldException, SecurityException
    {
        mockResponse(JSON);
        testWaitForJsonFieldAppears(1);
        verify(httpClient).execute(argThat(base -> base instanceof HttpRequestBase
                && ((HttpRequestBase) base).getMethod().equals(GET)
                && ((HttpRequestBase) base).getURI().equals(URI.create(URL))),
                argThat(context -> context instanceof HttpClientContext));
    }

    @Test
    void testWaitForJsonFieldAppearsWithPolling() throws IOException, IllegalArgumentException,
            IllegalAccessException, NoSuchFieldException, SecurityException
    {
        String body = "{\"key\":\"value\"}";
        mockResponse(body);
        testWaitForJsonFieldAppears(0);
        verify(httpClient, atLeast(6)).execute(argThat(base -> base instanceof HttpRequestBase
                && ((HttpRequestBase) base).getMethod().equals(GET)
                && ((HttpRequestBase) base).getURI().equals(URI.create(URL))),
                argThat(context -> context instanceof HttpClientContext));
        verify(httpClient, atMost(10)).execute(argThat(base -> base instanceof HttpRequestBase),
                argThat(context -> context instanceof HttpClientContext));
    }

    @Test
    void testWaitForJsonFieldAppearsHandledException() throws IOException, IllegalArgumentException,
            IllegalAccessException, NoSuchFieldException, SecurityException
    {
        when(httpClient.execute(argThat(base -> base instanceof HttpRequestBase
                && ((HttpRequestBase) base).getMethod().equals(GET)
                && ((HttpRequestBase) base).getURI().equals(URI.create(URL))),
                argThat(context -> context instanceof HttpClientContext)))
            .thenThrow(new ConnectionClosedException());
        HttpRequestExecutor httpRequestExecutor = new HttpRequestExecutor(httpClient, httpTestContext, softAssert);
        Field executorField = jsonResponseValidationSteps.getClass().getDeclaredField(HTTP_REQUEST_EXECUTOR_FIELD);
        executorField.setAccessible(true);
        executorField.set(jsonResponseValidationSteps, httpRequestExecutor);
        jsonResponseValidationSteps.waitForJsonFieldAppearance(STRING_PATH, URL, Duration.parse("PT1S"));
        verify(softAssert).recordFailedAssertion(
                (Exception) argThat(arg -> ConnectionClosedException.class.isInstance(arg)
                        && "Connection is closed".equals(((Exception) arg).getMessage())));
    }

    private void testWaitForJsonFieldAppears(int elementsFound) throws IllegalArgumentException,
            IllegalAccessException, NoSuchFieldException, SecurityException, IOException
    {
        HttpRequestExecutor httpRequestExecutor = new HttpRequestExecutor(httpClient, httpTestContext, softAssert);
        Field executorField = jsonResponseValidationSteps.getClass().getDeclaredField(HTTP_REQUEST_EXECUTOR_FIELD);
        executorField.setAccessible(true);
        executorField.set(jsonResponseValidationSteps, httpRequestExecutor);
        jsonResponseValidationSteps.waitForJsonFieldAppearance(STRING_PATH, URL, Duration.parse("PT2S"));
        verify(softAssert).assertThat(eq(THE_NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + STRING_PATH),
                eq(elementsFound), verifyMatcher(TypeSafeMatcher.class, 1));
    }

    private void mockResponse(String body) throws IOException
    {
        HttpResponse response = new HttpResponse();
        response.setResponseTimeInMs(0);
        response.setResponseBody(body != null ? body.getBytes(StandardCharsets.UTF_8) : null);
        when(httpTestContext.getResponse()).thenReturn(response);
        when(httpTestContext.getJsonContext()).thenReturn(body);
        when(httpClient.execute(argThat(base -> base instanceof HttpRequestBase),
                argThat(context -> context instanceof HttpClientContext))).thenReturn(response);
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
                verifyMatcher(TypeSafeMatcher.class, number));
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

    private void verifyPathNotFoundExceptionRecording(String nonExistingJsonPath)
    {
        verify(softAssert).recordFailedAssertion((Exception) argThat(
            arg -> arg instanceof PathNotFoundException && ("No results for path: " + nonExistingJsonPath)
                    .equals(((PathNotFoundException) arg).getMessage())));
        verifyNoMoreInteractions(softAssert);
    }

    private <T, K> T verifyMatcher(Class<? extends BaseMatcher> clazz, K matching)
    {
        return argThat(arg -> clazz.isInstance(arg) && clazz.cast(arg).matches(matching));
    }
}
