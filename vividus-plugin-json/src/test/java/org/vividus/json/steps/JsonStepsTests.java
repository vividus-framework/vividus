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

package org.vividus.json.steps;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import com.jayway.jsonpath.PathNotFoundException;

import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.jbehave.core.steps.ParameterConverters.FluentEnumConverter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.json.JsonContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.SubSteps;
import org.vividus.util.ResourceUtils;
import org.vividus.util.json.JsonPathUtils;
import org.vividus.util.json.JsonUtils;
import org.vividus.variable.VariableScope;

import net.javacrumbs.jsonunit.core.Option;

@SuppressWarnings("MethodCount")
@ExtendWith(MockitoExtension.class)
class JsonStepsTests
{
    private static final String OBJECT_PATH = "$.test";
    private static final String OBJECT_PATH_RESULT = "{\"name\":\"value\","
            + "\"number\":42,"
            + "\"number-float\":42.1,"
            + "\"number-float-e\":42.2,"
            + "\"number-float-e2\":400.0,"
            + "\"number-float-e3\":4E+2,"
            + "\"bool\":true,"
            + "\"array\":[1,2],"
            + "\"nested-json\":\"{\\\"nested-name\\\":\\\"nested-value\\\"}\","
            + "\"null\":null}";

    private static final String STRING_PATH = "$.test.name";
    private static final String STRING_PATH_VALUE_RESULT = "value";
    private static final String STRING_PATH_ELEMENT_RESULT = "\"value\"";

    private static final String NULL_PATH = "$.test.null";
    private static final String NULL_PATH_RESULT = "null";

    private static final String NUMBER_PATH = "$.test.number";
    private static final String NUMBER_PATH_RESULT = "42";

    private static final String BOOLEAN_PATH = "$.test.bool";
    private static final String BOOLEAN_PATH_RESULT = "true";

    private static final String NESTED_JSON_PATH = "$.test.nested-json";
    private static final String NESTED_JSON_PATH_RESULT = "{\"nested-name\":\"nested-value\"}";

    private static final String ARRAY_PATH = "$.test.array";
    private static final String ARRAY_PATH_RESULT = "[1,2]";

    private static final String NUMBER_INDEFINITE_PATH = "$..number";
    private static final String NUMBER_INDEFINITE_PATH_RESULT = "[42]";

    private static final String BOOLEAN_INDEFINITE_PATH = "$..bool";
    private static final String BOOLEAN_INDEFINITE_PATH_RESULT = "[true]";

    private static final String NON_EXISTING_PATH = "$['non-existing-element']";
    private static final String ANY_ELEMENT_PATH = "$.test.*";

    private static final String JSON = "{\"test\":{\"name\":\"value\","
            + "\"number\":42,\"number-float\":42.1,\"number-float-e\":4.22E+1,\"number-float-e2\":400.0,"
            + "\"number-float-e3\":4E+2,\"bool\":true,\"array\":[1,2],"
            + "\"nested-json\":\"{\\\"nested-name\\\":\\\"nested-value\\\"}\",\"null\":null}}";

    private static final String VARIABLE_NAME = "name";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.SCENARIO);

    private static final String ARRAY = "array";
    private static final String OBJECT = "object";

    private static final String NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE = "The number of JSON elements by JSON "
            + "path: ";
    private static final String VARIABLE = "variable";
    private static final String EXPECTED_VALUE = "1";

    private static final String INVALID_COMPARISON_RULE_MESSAGE = "Unable to compare actual JSON element value '%s' "
            + "against expected value '%s' using comparison rule '%s'";
    private static final String ANY_STRING = "any";

    @Mock private VariableContext variableContext;
    @Mock private ISoftAssert softAssert;
    @Mock private JsonContext jsonContext;
    @Mock private IAttachmentPublisher attachmentPublisher;

    private JsonSteps steps;

    @BeforeEach
    void beforeEach()
    {
        JsonPathUtils.setJacksonConfiguration();
        steps = new JsonSteps(new FluentEnumConverter(), jsonContext, variableContext, new JsonUtils(), softAssert,
                attachmentPublisher, Map.of());
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
    void shouldSaveJsonValueToVariable(String jsonPath, String expectedData)
    {
        steps.saveJsonValueToVariable(JSON, jsonPath, SCOPES, VARIABLE_NAME);
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
    void shouldFailToSaveNonPrimitiveJsonElementValue(String jsonPath, String errorType)
    {
        steps.saveJsonValueToVariable(JSON, jsonPath, SCOPES, VARIABLE_NAME);
        verifyUnexpectedType(jsonPath, errorType);
        verifyNoInteractions(variableContext);
    }

    @Test
    void shouldFailToSaveNonExistingJsonValueFromInput()
    {
        steps.saveJsonValueToVariable(JSON, NON_EXISTING_PATH, SCOPES, VARIABLE_NAME);
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
                arguments(NESTED_JSON_PATH,         "is EQUAL_tO",                 NESTED_JSON_PATH_RESULT),
                arguments(NESTED_JSON_PATH,         "is NOT equal to",             null),
                arguments(STRING_PATH,              "IS_NOT_EQUAL_TO  ",           null),
                arguments(NUMBER_PATH,              "is not equal to",             null),
                arguments(BOOLEAN_PATH,             " is not EQUAL_tO",            null),
                arguments(NULL_PATH,                " is not equal to ",           43)
        );
    }

    @SuppressWarnings("unchecked")
    @ParameterizedTest
    @MethodSource("jsonValueValidations")
    void shouldCheckIfJsonValueFromInputIsEqualToExpected(String jsonPath, String comparisonRule, Object expectedData)
    {
        var expectedCaptor = ArgumentCaptor.forClass(Object.class);
        var matchedCaptor = ArgumentCaptor.forClass(Matcher.class);
        when(softAssert.assertThat(eq(String.format("Value of JSON element found by JSON path '%s'", jsonPath)),
                expectedCaptor.capture(), matchedCaptor.capture())).thenReturn(true);
        steps.assertValueByJsonPath(JSON, jsonPath, comparisonRule, expectedData);
        assertThat(expectedCaptor.getValue(), matchedCaptor.getValue());
    }

    static Stream<Arguments> jsonToVariableSource()
    {
        return Stream.of(
            arguments("{\"k\" : \"v\"}", Map.of("k", "v")),
            arguments("[{\"k2\" : \"v2\"}, {\"k1\" : \"v1\"}]", List.of(Map.of("k2", "v2"), Map.of("k1", "v1")))
        );
    }

    @ParameterizedTest
    @MethodSource("jsonToVariableSource")
    void shouldCovertJsonToVariable(String json, Object expectedData)
    {
        steps.convertJsonToVariable(json, SCOPES, VARIABLE_NAME);
        verify(variableContext).putVariable(SCOPES, VARIABLE_NAME, expectedData);
    }

    @Test
    void shouldThrowErrorIfUnexpectedComparisonRule()
    {
        var expectedData = mock(Object.class);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> steps.assertValueByJsonPath(JSON, NULL_PATH, ANY_STRING, expectedData));
        assertEquals(String.format(INVALID_COMPARISON_RULE_MESSAGE, null, expectedData,
                ANY_STRING), exception.getMessage());
        verifyNoInteractions(expectedData);
    }

    @Test
    void shouldFailToCheckIfMissingJsonValueFromInputIsEqualToExpected()
    {
        var expectedData = mock(Object.class);
        steps.assertValueByJsonPath(JSON, NON_EXISTING_PATH, ANY_STRING, expectedData);
        verifyPathNotFoundExceptionRecording(NON_EXISTING_PATH);
        verifyNoInteractions(expectedData);
    }

    @ParameterizedTest
    @MethodSource("jsonElementsErrors")
    void shouldFailToCheckIfNonPrimitiveJsonValueFromInputIsEqualToExpected(String jsonPath, String errorType)
    {
        var expectedData = mock(Object.class);
        steps.assertValueByJsonPath(JSON, jsonPath, ANY_STRING, expectedData);
        verifyUnexpectedType(jsonPath, errorType);
        verifyNoInteractions(expectedData);
    }

    @ParameterizedTest
    @CsvSource({
            "'[{\"value\":\"b\"},{\"value\":\"c\"},{\"value\":\"a\"}]',   $..value,   3",
            "'[{\"value\":\"b\"},{\"value1\":\"c\"},{\"value1\":\"a\"}]', $[0].value, 1",
            "{\"value\":null},                                            $.value,    1"
    })
    void shouldExecuteAllStepsForFoundJsonElements(String json, String jsonPath, int number)
    {
        var subSteps = mock(SubSteps.class);
        when(softAssert.assertThat(eq(NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + jsonPath), eq(number),
                verifyMatcher(number))).thenReturn(true);
        steps.executeStepsForFoundJsonElements(ComparisonRule.GREATER_THAN_OR_EQUAL_TO, number, json, jsonPath,
                subSteps);
        verify(subSteps, times(number)).execute(Optional.empty());
        verify(jsonContext).getJsonContext();
        verify(jsonContext).putJsonContext(null);
    }

    @Test
    void shouldNotExecuteStepsWhenNumberOfElementsInJsonIsZeroAndComparisonRuleIsPassed()
    {
        var subSteps = mock(SubSteps.class);
        String jsonPath = "$..value";
        when(softAssert.assertThat(eq(NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + jsonPath), eq(0),
                verifyMatcher(0))).thenReturn(true);
        steps.executeStepsForFoundJsonElements(ComparisonRule.LESS_THAN_OR_EQUAL_TO, 0, "{}", jsonPath,
                subSteps);
        verifyNoInteractions(subSteps, jsonContext);
    }

    @Test
    void shouldNotExecuteStepsWhenNumberOfElementsInJsonIsNotZeroAndComparisonRuleIsFailed()
    {
        var subSteps = mock(SubSteps.class);
        String jsonPath = "$..any";
        when(softAssert.assertThat(eq(NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + jsonPath), eq(0),
                verifyMatcher(1))).thenReturn(false);
        steps.executeStepsForFoundJsonElements(ComparisonRule.EQUAL_TO, 1, "[]", jsonPath, subSteps);
        verifyNoInteractions(subSteps, jsonContext);
    }

    @Test
    void shouldExecuteIterationsUntilVariableIsNotCorrespondsToTheCondition()
    {
        var subSteps = mock(SubSteps.class);
        when(softAssert.assertThat(eq(NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + ANY_ELEMENT_PATH), eq(10),
                verifyMatcher(1))).thenReturn(true);
        when(variableContext.getVariable(VARIABLE)).thenReturn(null).thenReturn(null).thenReturn(2).thenReturn(1);
        steps.executeStepsForFoundJsonElementsExpectingVariable(ComparisonRule.GREATER_THAN_OR_EQUAL_TO, 1, JSON,
                ANY_ELEMENT_PATH, VARIABLE, StringComparisonRule.IS_EQUAL_TO, EXPECTED_VALUE, subSteps);
        verify(subSteps, times(3)).execute(Optional.empty());
        verify(jsonContext, times(3)).putJsonContext(any(String.class));
        verify(jsonContext).getJsonContext();
        verify(softAssert, never()).recordFailedAssertion(any(String.class));
        verify(jsonContext).putJsonContext(null);
    }

    @Test
    void shouldRecordFailedAssertionIfVariableWasNotInitialized()
    {
        var subSteps = mock(SubSteps.class);
        when(softAssert.assertThat(eq(NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + ANY_ELEMENT_PATH), eq(10),
                verifyMatcher(1))).thenReturn(true);
        steps.executeStepsForFoundJsonElementsExpectingVariable(ComparisonRule.GREATER_THAN_OR_EQUAL_TO, 1, JSON,
                ANY_ELEMENT_PATH, VARIABLE, StringComparisonRule.IS_EQUAL_TO, EXPECTED_VALUE, subSteps);
        verify(subSteps, times(10)).execute(Optional.empty());
        verify(jsonContext, times(10)).putJsonContext(any(String.class));
        verify(jsonContext).getJsonContext();
        verify(softAssert).recordFailedAssertion("Variable `variable` was not initialized");
        verify(jsonContext).putJsonContext(null);
    }

    @Test
    void shouldResetContextInCaseOfException()
    {
        var subSteps = mock(SubSteps.class);
        when(softAssert.assertThat(eq(NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + ANY_ELEMENT_PATH), eq(10),
                verifyMatcher(1))).thenReturn(true);
        doThrow(new IllegalArgumentException()).when(subSteps).execute(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> steps.executeStepsForFoundJsonElementsExpectingVariable(
                        ComparisonRule.GREATER_THAN_OR_EQUAL_TO, 1, JSON, ANY_ELEMENT_PATH, VARIABLE,
                        StringComparisonRule.IS_EQUAL_TO, EXPECTED_VALUE, subSteps));
        verify(subSteps).execute(Optional.empty());
        verify(jsonContext).getJsonContext();
        verify(jsonContext).putJsonContext(any(String.class));
        verify(jsonContext).putJsonContext(null);
        verifyNoMoreInteractions(jsonContext);
    }

    @Test
    void testIsDataByJsonPathFromJsonEqualCheckMismatches()
    {
        var json = ResourceUtils.loadResource(getClass(), "missmatches-actual-data.json");
        var expected = ResourceUtils.loadResource(getClass(), "missmatches-expected-data.json");
        steps.assertElementByJsonPath(json, "$", expected, EnumSet.of(Option.IGNORING_ARRAY_ORDER));
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

    static Stream<Arguments> jsonValuesAndElements()
    {
        return Stream.of(
                arguments(STRING_PATH,             STRING_PATH_ELEMENT_RESULT),
                arguments(NULL_PATH,               NULL_PATH_RESULT),
                arguments(NUMBER_PATH,             NUMBER_PATH_RESULT),
                arguments(BOOLEAN_PATH,            BOOLEAN_PATH_RESULT),
                arguments(OBJECT_PATH,             OBJECT_PATH_RESULT),
                arguments(ARRAY_PATH,              ARRAY_PATH_RESULT),
                arguments(NUMBER_INDEFINITE_PATH,  NUMBER_INDEFINITE_PATH_RESULT),
                arguments(BOOLEAN_INDEFINITE_PATH, BOOLEAN_INDEFINITE_PATH_RESULT)
        );
    }

    @ParameterizedTest
    @MethodSource("jsonValuesAndElements")
    void shouldAssertDataByJsonPathIsEqualToExpectedOne(String jsonPath, String expectedData)
    {
        steps.assertElementByJsonPath(JSON, jsonPath, expectedData, Set.of());
        verifyJsonEqualityAssertion(jsonPath, expectedData, expectedData);
    }

    @Test
    void shouldAssertArrayByJsonPathIsEqualToExpectedOneIgnoringArrayOrder()
    {
        var expectedJson = "[2,1]";
        steps.assertElementByJsonPath(JSON, ARRAY_PATH, expectedJson, EnumSet.of(Option.IGNORING_ARRAY_ORDER));
        verifyJsonEqualityAssertion(ARRAY_PATH, expectedJson, ARRAY_PATH_RESULT);
    }

    @Test
    void shouldAssertArrayByJsonPathIsEqualToExpectedOneIgnoringArrayOrderAndExtraArrayItems()
    {
        var expectedJson = "[2]";
        steps.assertElementByJsonPath(JSON, ARRAY_PATH, expectedJson,
                EnumSet.of(Option.IGNORING_ARRAY_ORDER, Option.IGNORING_EXTRA_ARRAY_ITEMS));
        verifyJsonEqualityAssertion(ARRAY_PATH, expectedJson, ARRAY_PATH_RESULT);
    }

    @Test
    void shouldAssertArrayByJsonPathWithDifferentArrayLengthAndIgnoringExtraArrayItemsOption()
    {
        var expectedJson = "[1,2,3]";

        steps.assertElementByJsonPath(JSON, ARRAY_PATH, expectedJson, EnumSet.of(Option.IGNORING_EXTRA_ARRAY_ITEMS));

        verify(softAssert).recordFailedAssertion("Array \"\" has invalid length, expected: <at least 3> but was: <2>.");
        verify(softAssert).recordFailedAssertion(
                "Array \"\" has different content. Missing values: [3], expected: <[1,2,3]> but was: <[1,2]>");
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void shouldAssertCollectionByJsonPathIsEqualToSingleUnwrappedItem()
    {
        var expectedJson = NUMBER_PATH_RESULT;
        String jsonPath = NUMBER_INDEFINITE_PATH;
        steps.assertElementByJsonPath(JSON, jsonPath, expectedJson, Set.of());
        verifyJsonEqualityAssertion(jsonPath, expectedJson, expectedJson);
    }

    private void verifyJsonEqualityAssertion(String jsonPath, String expectedData, String ectualData)
    {
        verify(softAssert).assertThat(
                eq("Data by JSON path: " + jsonPath + " is equal to '" + expectedData + "'"), eq(ectualData),
                argThat(matcher -> matcher.matches(expectedData)));
    }

    @Test
    void testIsDataByJsonPathFromJsonEqualCheckEmptyArrayMismatch()
    {
        var json = "{ \"arrayKey\": [ { \"idKey\": \"q4jn0f8\", \"randValueKey\": \"i4t8ivC\"} ] }";
        var expected = "[ { \"idKey\": \"b54Y8id\", \"randValueKey\": \"i4t8ivC\"} ]";

        steps.assertElementByJsonPath(json, "$.arrayKey.[?(@.idKey==\"b54Y8id\")]", expected, Set.of());

        verify(softAssert).recordFailedAssertion("Array \"\" has different length, expected: <1> but was: <0>.");
        verify(softAssert).recordFailedAssertion("Array \"\" has different content. Missing values: "
                + "[{\"idKey\":\"b54Y8id\",\"randValueKey\":\"i4t8ivC\"}], expected: <[{\"idKey\":\"b54Y8id\","
                + "\"randValueKey\":\"i4t8ivC\"}]> but was: <[]>");
        verifyNoMoreInteractions(softAssert);
    }

    static Stream<Arguments> checkJsonElementsNumberDataProvider()
    {
        return Stream.of(
                arguments(STRING_PATH,       1),
                arguments(NULL_PATH,         1),
                arguments(ARRAY_PATH,        2),
                arguments(NON_EXISTING_PATH, 0)
        );
    }

    @ParameterizedTest
    @MethodSource("checkJsonElementsNumberDataProvider")
    void testDoesJsonPathElementsFromJsonMatchRule(String jsonPath, int elementsNumber)
    {
        steps.assertNumberOfJsonElements(JSON, jsonPath, ComparisonRule.EQUAL_TO, elementsNumber);
        verify(softAssert).assertThat(eq(NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + jsonPath), eq(elementsNumber),
                verifyMatcher(elementsNumber));
    }

    @ParameterizedTest
    @MethodSource("checkJsonElementsNumberDataProvider")
    void testSaveElementsNumberFromJsonByJsonPath(String jsonPath, int elementsNumber)
    {
        steps.saveElementsNumberByJsonPath(JSON, jsonPath, SCOPES, VARIABLE_NAME);
        verify(variableContext).putVariable(SCOPES, VARIABLE_NAME, elementsNumber);
    }

    @ParameterizedTest
    @MethodSource("jsonValuesAndElements")
    void shouldSaveJsonElementFromGivenJson(String jsonPath, String expectedData)
    {
        steps.saveJsonElementToVariable(JSON, jsonPath, SCOPES, VARIABLE_NAME);
        verify(variableContext).putVariable(SCOPES, VARIABLE_NAME, expectedData);
        verifyNoInteractions(softAssert);
    }

    @Test
    void shouldFailToSaveNonExistingJsonElementFromGivenJson()
    {
        steps.saveJsonElementToVariable(JSON, NON_EXISTING_PATH, Set.of(VariableScope.STORY), VARIABLE_NAME);
        verifyPathNotFoundExceptionRecording(NON_EXISTING_PATH);
        verifyNoInteractions(variableContext);
    }

    private <T, K> T verifyMatcher(K matching)
    {
        var clazz = TypeSafeMatcher.class;
        return argThat(arg -> clazz.isInstance(arg) && clazz.cast(arg).matches(matching));
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
