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

package org.vividus.json.steps;

import static org.junit.jupiter.api.Assertions.assertThrows;
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

import java.util.Optional;

import org.hamcrest.TypeSafeMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.converter.FluentTrimmedEnumConverter;
import org.vividus.json.JsonContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.SubSteps;
import org.vividus.util.json.JsonUtils;

@ExtendWith(MockitoExtension.class)
class JsonNestedStepsTests
{
    private static final String JSON_PATH = "$..value";
    private static final String THE_NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE =
            "The number of JSON elements by JSON path: ";
    private static final String JSON =
        "{\"test\": {\"name\":\"value\",\"number\":42,\"bool\":true,\"array\":[1,2] ,\"null\":null}}";
    private static final String VARIABLE = "variable";
    private static final String JSON_PATH_ANY_ELEMENT = "$..*";
    private static final String EXPECTED_VALUE = "1";

    @Mock private VariableContext variableContext;
    @Mock private ISoftAssert softAssert;
    @Mock private JsonContext jsonContext;

    private JsonNestedSteps jsonNestedSteps;

    @BeforeEach
    void beforeEach()
    {
        JsonUtils jsonUtils = new JsonUtils();
        this.jsonNestedSteps = new JsonNestedSteps(variableContext, jsonContext,
                new JsonSteps(new FluentTrimmedEnumConverter(), jsonContext, variableContext, jsonUtils),
                softAssert, jsonUtils);
    }

    @ParameterizedTest
    @CsvSource({
            "'[{\"value\":\"b\"},{\"value\":\"c\"},{\"value\":\"a\"}]',   $..value,   3",
            "'[{\"value\":\"b\"},{\"value1\":\"c\"},{\"value1\":\"a\"}]', $[0].value, 1"
    })
    void testPerformAllStepsForProvidedJsonIfFound(String json, String jsonPath, int number)
    {
        var subSteps = mock(SubSteps.class);
        when(softAssert.assertThat(eq(THE_NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + jsonPath), eq(number),
                verifyMatcher(number))).thenReturn(true);
        jsonNestedSteps.performAllStepsForProvidedJsonIfFound(ComparisonRule.GREATER_THAN_OR_EQUAL_TO,
                number, json, jsonPath, subSteps);
        verify(subSteps, times(number)).execute(Optional.empty());
        verify(jsonContext).getJsonContext();
        verify(jsonContext).putJsonContext(null);
    }

    @Test
    void shouldNotPerformStepsWhenNumberOfElementsInJsonIsZeroAndComparisonRuleIsPassed()
    {
        var subSteps = mock(SubSteps.class);
        when(softAssert.assertThat(eq(THE_NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + JSON_PATH), eq(0),
                verifyMatcher(0))).thenReturn(true);
        jsonNestedSteps.performAllStepsForProvidedJsonIfFound(ComparisonRule.LESS_THAN_OR_EQUAL_TO,
                0, "{}", JSON_PATH, subSteps);
        verifyNoInteractions(subSteps, jsonContext);
    }

    @Test
    void testPerformAllStepsForJsonIfFound()
    {
        when(jsonContext.getJsonContext()).thenReturn(JSON);
        var subSteps = mock(SubSteps.class);
        when(softAssert.assertThat(eq(THE_NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + JSON_PATH), eq(0),
                verifyMatcher(3))).thenReturn(false);
        jsonNestedSteps.performAllStepsForJsonIfFound(ComparisonRule.GREATER_THAN_OR_EQUAL_TO, 0,
                JSON_PATH, subSteps);
        verifyNoInteractions(subSteps);
        verify(jsonContext, times(0)).putJsonContext(any());
    }

    @Test
    void shouldPerformIterationUntilVariableIsNotCorrespondsToTheCondition()
    {
        var subSteps = mock(SubSteps.class);
        when(softAssert.assertThat(eq(THE_NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + JSON_PATH_ANY_ELEMENT), eq(8),
                verifyMatcher(1))).thenReturn(true);
        when(variableContext.getVariable(VARIABLE)).thenReturn(null).thenReturn(null).thenReturn(2).thenReturn(1);
        jsonNestedSteps.performAllStepsForJsonEntriesExpectingVariable(ComparisonRule.GREATER_THAN_OR_EQUAL_TO, 1, JSON,
                JSON_PATH_ANY_ELEMENT, VARIABLE, StringComparisonRule.IS_EQUAL_TO, EXPECTED_VALUE, subSteps);
        verify(subSteps, times(3)).execute(Optional.empty());
        verify(jsonContext, times(3)).putJsonContext(any(String.class));
        verify(jsonContext).getJsonContext();
        verify(softAssert, never()).recordFailedAssertion(any(String.class));
        verify(jsonContext).putJsonContext(null);
    }

    @Test
    void shouldPerformIterationUntilVariableIsNotCorrespondsToTheConditionUsingJsonContext()
    {
        var subSteps = mock(SubSteps.class);
        when(softAssert.assertThat(eq(THE_NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + JSON_PATH_ANY_ELEMENT), eq(8),
                verifyMatcher(1))).thenReturn(true);
        when(variableContext.getVariable(VARIABLE)).thenReturn(null).thenReturn(null).thenReturn(1);
        when(jsonContext.getJsonContext()).thenReturn(JSON);
        jsonNestedSteps.performAllStepsForJsonEntriesExpectingVariable(ComparisonRule.GREATER_THAN_OR_EQUAL_TO, 1,
                JSON_PATH_ANY_ELEMENT, VARIABLE, StringComparisonRule.IS_EQUAL_TO, EXPECTED_VALUE, subSteps);
        verify(subSteps, times(2)).execute(Optional.empty());
        verify(jsonContext, times(3)).putJsonContext(any(String.class));
        verify(softAssert, never()).recordFailedAssertion(any(String.class));
        verify(jsonContext, times(2)).getJsonContext();
        verify(jsonContext).putJsonContext(JSON);
    }

    @Test
    void shouldRecordFailedAssertionIfVariableWasNotInitialized()
    {
        var subSteps = mock(SubSteps.class);
        when(softAssert.assertThat(eq(THE_NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + JSON_PATH_ANY_ELEMENT), eq(8),
                verifyMatcher(1))).thenReturn(true);
        jsonNestedSteps.performAllStepsForJsonEntriesExpectingVariable(ComparisonRule.GREATER_THAN_OR_EQUAL_TO, 1, JSON,
                JSON_PATH_ANY_ELEMENT, VARIABLE, StringComparisonRule.IS_EQUAL_TO, EXPECTED_VALUE, subSteps);
        verify(subSteps, times(8)).execute(Optional.empty());
        verify(jsonContext, times(8)).putJsonContext(any(String.class));
        verify(jsonContext).getJsonContext();
        verify(softAssert).recordFailedAssertion("Variable `variable` was not initialized");
        verify(jsonContext).putJsonContext(null);
    }

    @Test
    void shouldResetContextInCaseOfException()
    {
        var subSteps = mock(SubSteps.class);
        when(softAssert.assertThat(eq(THE_NUMBER_OF_JSON_ELEMENTS_ASSERTION_MESSAGE + JSON_PATH_ANY_ELEMENT), eq(8),
                verifyMatcher(1))).thenReturn(true);
        doThrow(new IllegalArgumentException()).when(subSteps).execute(Optional.empty());
        assertThrows(IllegalArgumentException.class,
                () -> jsonNestedSteps.performAllStepsForJsonEntriesExpectingVariable(
                        ComparisonRule.GREATER_THAN_OR_EQUAL_TO, 1, JSON, JSON_PATH_ANY_ELEMENT, VARIABLE,
                        StringComparisonRule.IS_EQUAL_TO, EXPECTED_VALUE, subSteps));
        verify(subSteps).execute(Optional.empty());
        verify(jsonContext).getJsonContext();
        verify(jsonContext).putJsonContext(any(String.class));
        verify(jsonContext).putJsonContext(null);
        verifyNoMoreInteractions(jsonContext);
    }

    private <T, K> T verifyMatcher(K matching)
    {
        var clazz = TypeSafeMatcher.class;
        return argThat(arg -> clazz.isInstance(arg) && clazz.cast(arg).matches(matching));
    }
}
