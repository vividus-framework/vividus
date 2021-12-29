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

package org.vividus.json.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.jayway.jsonpath.PathNotFoundException;

import org.hamcrest.Matcher;
import org.jbehave.core.steps.ParameterConverters.FluentEnumConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.json.JsonContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.json.JsonPathUtils;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class JsonStepsTests
{
    private static final String OBJECT_PATH = "$.test";
    private static final String OBJECT_PATH_RESULT = "{\"name\":\"value\","
            + "\"number\":42,"
            + "\"number-float\":42.1,"
            + "\"number-float-e\":4.22E+1,"
            + "\"number-float-e2\":4E+2,"
            + "\"bool\":true,"
            + "\"array\":[1,2],"
            + "\"nested-json\":\"{\\\"nested-name\\\":\\\"nested-value\\\"}\","
            + "\"null\":null}";

    private static final String STRING_PATH = "$.test.name";
    private static final String STRING_PATH_VALUE_RESULT = "value";

    private static final String NULL_PATH = "$.test.null";
    private static final String NULL_PATH_RESULT = "null";

    private static final String NUMBER_PATH = "$.test.number";
    private static final String NUMBER_PATH_RESULT = "42";

    private static final String BOOLEAN_PATH = "$.test.bool";
    private static final String BOOLEAN_PATH_RESULT = "true";

    private static final String NESTED_JSON_PATH = "$.test.nested-json";
    private static final String NESTED_JSON_PATH_RESULT = "{\"nested-name\":\"nested-value\"}";

    private static final String ARRAY_PATH = "$.test.array";
    private static final String NUMBER_INDEFINITE_PATH = "$..number";
    private static final String BOOLEAN_INDEFINITE_PATH = "$..bool";
    private static final String NON_EXISTING_PATH = "$['non-existing-element']";

    private static final String JSON = "{\"test\":" + OBJECT_PATH_RESULT + "}";

    private static final String VARIABLE_NAME = "name";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.SCENARIO);

    private static final String ARRAY = "array";
    private static final String OBJECT = "object";

    @Mock private VariableContext variableContext;
    @Mock private ISoftAssert softAssert;
    @Mock private JsonContext jsonContext;

    private JsonSteps jsonSteps;

    @BeforeEach
    void beforeEach()
    {
        JsonPathUtils.setJacksonConfiguration();
        jsonSteps = new JsonSteps(new FluentEnumConverter(), jsonContext, variableContext);
        jsonSteps.setSoftAssert(softAssert);
    }

    static Stream<Arguments> jsonValues()
    {
        return Stream.of(
                arguments(STRING_PATH,      STRING_PATH_VALUE_RESULT),
                arguments(NULL_PATH,        NULL_PATH_RESULT),
                arguments(NUMBER_PATH,      NUMBER_PATH_RESULT),
                arguments(BOOLEAN_PATH,     BOOLEAN_PATH_RESULT),
                arguments(NESTED_JSON_PATH, NESTED_JSON_PATH_RESULT)
        );
    }

    @ParameterizedTest
    @MethodSource("jsonValues")
    void shouldSaveJsonValueFromContext(String jsonPath, String expectedData)
    {
        shouldSaveJsonValue(() -> {
            when(jsonContext.getJsonContext()).thenReturn(JSON);
            jsonSteps.saveJsonValueFromContextToVariable(jsonPath, SCOPES, VARIABLE_NAME);
        }, expectedData);
    }

    @ParameterizedTest
    @MethodSource("jsonValues")
    void shouldSaveJsonValueFromInput(String jsonPath, String expectedData)
    {
        shouldSaveJsonValue(() -> jsonSteps.saveJsonValueToVariable(JSON, jsonPath, SCOPES, VARIABLE_NAME),
                expectedData);
    }

    private void shouldSaveJsonValue(Runnable test, String expectedData)
    {
        test.run();
        verify(variableContext).putVariable(SCOPES, VARIABLE_NAME, expectedData);
        verifyNoInteractions(softAssert);
    }

    static Stream<Arguments> jsonElementsErrors()
    {
        return Stream.of(
                arguments(OBJECT_PATH,             OBJECT),
                arguments(ARRAY_PATH,              ARRAY),
                arguments(NUMBER_INDEFINITE_PATH,  ARRAY),
                arguments(BOOLEAN_INDEFINITE_PATH, ARRAY)
        );
    }

    @ParameterizedTest
    @MethodSource("jsonElementsErrors")
    void shouldFailToSaveNonPrimitiveJsonElementValueFromContext(String jsonPath, String errorType)
    {
        shouldFailToSaveNonPrimitiveJsonElementValue(() -> {
            when(jsonContext.getJsonContext()).thenReturn(JSON);
            jsonSteps.saveJsonValueFromContextToVariable(jsonPath, SCOPES, VARIABLE_NAME);
        }, jsonPath, errorType);
    }

    @ParameterizedTest
    @MethodSource("jsonElementsErrors")
    void shouldFailToSaveNonPrimitiveJsonElementValueFromInput(String jsonPath, String errorType)
    {
        shouldFailToSaveNonPrimitiveJsonElementValue(
                () -> jsonSteps.saveJsonValueToVariable(JSON, jsonPath, SCOPES, VARIABLE_NAME), jsonPath, errorType);
    }

    private void shouldFailToSaveNonPrimitiveJsonElementValue(Runnable test, String jsonPath, String errorType)
    {
        test.run();
        verifyUnexpectedType(jsonPath, errorType);
        verifyNoInteractions(variableContext);
    }

    @Test
    void shouldFailToSaveNonExistingJsonValueFromContext()
    {
        shouldFailToSaveNonExistingJsonValue(jsonPath -> {
            when(jsonContext.getJsonContext()).thenReturn(JSON);
            jsonSteps.saveJsonValueFromContextToVariable(jsonPath, SCOPES, VARIABLE_NAME);
        });
    }

    @Test
    void shouldFailToSaveNonExistingJsonValueFromInput()
    {
        shouldFailToSaveNonExistingJsonValue(
                jsonPath -> jsonSteps.saveJsonValueToVariable(JSON, jsonPath, SCOPES, VARIABLE_NAME));
    }

    private void shouldFailToSaveNonExistingJsonValue(Consumer<String> test)
    {
        test.accept(NON_EXISTING_PATH);
        verifyPathNotFoundExceptionRecording(NON_EXISTING_PATH);
        verifyNoInteractions(variableContext);
    }

    static Stream<Arguments> jsonValueValidations()
    {
        return Stream.of(
                arguments(STRING_PATH,              "is equal to",                 STRING_PATH_VALUE_RESULT),
                arguments(STRING_PATH,              "contains",                    "val"),
                arguments(STRING_PATH,              "does not contain ",           "var"),
                arguments(STRING_PATH,              " matches ",                   "va\\w{3}"),
                arguments(NULL_PATH,                " is equal to ",               null),
                arguments(NUMBER_PATH,              "IS_EQUAL_TO",                 42),
                arguments(NUMBER_PATH,              "is less than",                42.1),
                arguments(NUMBER_PATH,              "IS_LESS_THAN_OR_EQUAL_TO",    42.1),
                arguments(NUMBER_PATH,              " is greater than",            41.9),
                arguments(NUMBER_PATH,              "IS_GREATER_THAN_OR_EQUAL_TO", 41.9),
                arguments(NUMBER_PATH,              "IS NOT_EQUAL_TO",             41),
                arguments("$.test.number-float",    " IS_EQUAL_TO",                42.1),
                arguments("$.test.number-float-e",  "IS_EQUAL_TO ",                42.2),
                arguments("$.test.number-float-e2", "  IS_EQUAL_TO  ",             400),
                arguments(BOOLEAN_PATH,             " IS_EQUAL_TO ",               true),
                arguments(NESTED_JSON_PATH,         "is EQUAL_tO",                 NESTED_JSON_PATH_RESULT)
        );
    }

    @ParameterizedTest
    @MethodSource("jsonValueValidations")
    void shouldCheckIfJsonValueFromContextIsEqualToExpected(String jsonPath, String comparisonRule, Object expectedData)
    {
        shouldCheckIfJsonValueIsEqualToExpected(jsonPath, () -> {
            when(jsonContext.getJsonContext()).thenReturn(JSON);
            return jsonSteps.isValueByJsonPathFromContextEqual(jsonPath, comparisonRule, expectedData);
        });
    }

    @ParameterizedTest
    @MethodSource("jsonValueValidations")
    void shouldCheckIfJsonValueFromInputIsEqualToExpected(String jsonPath, String comparisonRule, Object expectedData)
    {
        shouldCheckIfJsonValueIsEqualToExpected(jsonPath,
                () -> jsonSteps.isValueByJsonPathEqual(JSON, jsonPath, comparisonRule, expectedData));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private void shouldCheckIfJsonValueIsEqualToExpected(String jsonPath, BooleanSupplier test)
    {
        var expectedCaptor = ArgumentCaptor.forClass(Object.class);
        var matchedCaptor = ArgumentCaptor.forClass(Matcher.class);
        when(softAssert.assertThat(eq("Value of JSON element found by JSON path '" + jsonPath + "'"),
                expectedCaptor.capture(), matchedCaptor.capture())).thenReturn(true);
        boolean result = test.getAsBoolean();
        assertTrue(result);
        assertThat(expectedCaptor.getValue(), matchedCaptor.getValue());
    }

    @Test
    void shouldFailToCheckIfMissingJsonValueFromContextIsEqualToExpected()
    {
        shouldFailToCheckIfMissingJsonValueIsEqualToExpected((jsonPath, expectedData) -> {
            when(jsonContext.getJsonContext()).thenReturn(JSON);
            jsonSteps.isValueByJsonPathFromContextEqual(jsonPath, null, expectedData);
        });
    }

    @Test
    void shouldFailToCheckIfMissingJsonValueFromInputIsEqualToExpected()
    {
        shouldFailToCheckIfMissingJsonValueIsEqualToExpected(
                (jsonPath, expectedData) -> jsonSteps.isValueByJsonPathEqual(JSON, jsonPath, null, expectedData));
    }

    private void shouldFailToCheckIfMissingJsonValueIsEqualToExpected(BiConsumer<String, Object> test)
    {
        var expectedData = mock(Object.class);
        test.accept(NON_EXISTING_PATH, expectedData);
        verifyPathNotFoundExceptionRecording(NON_EXISTING_PATH);
        verifyNoInteractions(expectedData);
    }

    @ParameterizedTest
    @MethodSource("jsonElementsErrors")
    void shouldFailToCheckIfNonPrimitiveJsonValueFromContextIsEqualToExpected(String jsonPath, String errorType)
    {
        shouldFailToCheckIfNonPrimitiveJsonValueIsEqualToExpected(jsonPath, errorType, expectedData -> {
            when(jsonContext.getJsonContext()).thenReturn(JSON);
            jsonSteps.isValueByJsonPathFromContextEqual(jsonPath, null, expectedData);
        });
    }

    @ParameterizedTest
    @MethodSource("jsonElementsErrors")
    void shouldFailToCheckIfNonPrimitiveJsonValueFromInputIsEqualToExpected(String jsonPath, String errorType)
    {
        shouldFailToCheckIfNonPrimitiveJsonValueIsEqualToExpected(jsonPath, errorType,
                expectedData -> jsonSteps.isValueByJsonPathEqual(JSON, jsonPath, null, expectedData));
    }

    private void shouldFailToCheckIfNonPrimitiveJsonValueIsEqualToExpected(String jsonPath, String errorType,
            Consumer<Object> test)
    {
        var expectedData = mock(Object.class);
        test.accept(expectedData);
        verifyUnexpectedType(jsonPath, errorType);
        verifyNoInteractions(expectedData);
    }

    @Test
    void shouldGetDataByJsonPathSafelyWithoutFailureRecording()
    {
        var result = jsonSteps.getDataByJsonPathSafely(JSON, NON_EXISTING_PATH, false);
        assertEquals(Optional.empty(), result);
        verifyNoInteractions(softAssert);
    }

    private void verifyUnexpectedType(String jsonPath, String errorType)
    {
        verify(softAssert).recordFailedAssertion(String.format(
                "Value of JSON element found by JSON path '%s' must be either null, or boolean, or string, or number,"
                        + " but found %s", jsonPath, errorType));
        verifyNoMoreInteractions(softAssert);
    }

    private void verifyPathNotFoundExceptionRecording(String nonExistingJsonPath)
    {
        var exceptionCaptor = ArgumentCaptor.forClass(Exception.class);
        verify(softAssert).recordFailedAssertion(exceptionCaptor.capture());
        var failure = exceptionCaptor.getValue();
        assertInstanceOf(PathNotFoundException.class, failure);
        assertEquals("No results for path: " + nonExistingJsonPath, failure.getMessage());
        verifyNoMoreInteractions(softAssert);
    }
}
