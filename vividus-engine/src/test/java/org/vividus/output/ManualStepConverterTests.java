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

package org.vividus.output;

import static java.lang.System.lineSeparator;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.vividus.model.jbehave.Step;

class ManualStepConverterTests
{
    private static final String STORY_TITLE = "story title";
    private static final String SCENARIO_TITLE = "scenario title";
    private static final String ERROR_MESSAGE = "Error:" + lineSeparator() + "Story: " + STORY_TITLE + lineSeparator()
        + "Scenario: " + SCENARIO_TITLE + lineSeparator()
        + "Manual scenario rules:" + lineSeparator()
        + "1. Manual scenario entries must be prepended with '!-- ' sequence" + lineSeparator()
        + "2. 'Step:' designator is required to be the first one in the manual step block" + lineSeparator()
        + "3. 'Data:' and 'Result:' designators are optional"  + lineSeparator()
        + "4. 'Step:' designator is mandatory" + lineSeparator();
    private static final String DUPLICATE_ENTRY_MESSAGE = "Only one %s is expected to be present in the data";

    private static final String ESCAPE = "- ";
    private static final String PREFIX = "!-- ";
    private static final String STEP = "step";
    private static final String DATA = "data";
    private static final String RESULT = "result";
    private static final String STEP_ENTRY = PREFIX + "Step: " + STEP;
    private static final String DATA_ENTRY = PREFIX + "Data: " + DATA;
    private static final String RESULT_ENTRY = PREFIX + "Result: " + RESULT;
    private static final String ADDITIONAL_DATA = "additional data";

    static Stream<Arguments> invalidFormatStepsProvider()
    {
        return Stream.of(
                arguments(List.of(createStep(DATA_ENTRY))),
                arguments(List.of(createStep(null, "When I open main application page")))
                );
    }

    @MethodSource("invalidFormatStepsProvider")
    @ParameterizedTest
    void shouldFailIfStepsDoNotCorrespondRules(List<Step> steps)
    {
        SyntaxException exception = assertThrows(SyntaxException.class,
            () -> ManualStepConverter.convert(STORY_TITLE, SCENARIO_TITLE, steps));
        assertEquals(ERROR_MESSAGE, exception.getMessage());
    }

    static Stream<Arguments> invalidStepsSequenceProvider()
    {
        return Stream.of(
            arguments("Data:", List.of(createStep(STEP_ENTRY), createStep(DATA_ENTRY), createStep(DATA_ENTRY))),
            arguments("Result:", List.of(createStep(STEP_ENTRY), createStep(RESULT_ENTRY), createStep(RESULT_ENTRY)))
        );
    }

    @MethodSource("invalidStepsSequenceProvider")
    @ParameterizedTest
    void shouldFailIfDuplicateEntries(String sign, List<Step> steps)
    {
        SyntaxException exception = assertThrows(SyntaxException.class,
            () -> ManualStepConverter.convert(STORY_TITLE, SCENARIO_TITLE, steps));
        assertEquals(String.format(DUPLICATE_ENTRY_MESSAGE, sign), exception.getMessage());
    }

    @Test
    void shouldConvertStepsIntoManualSteps() throws SyntaxException
    {
        List<Step> steps = List.of(
                createStep(STEP_ENTRY),
                createStep(DATA_ENTRY),
                createStep(RESULT_ENTRY),
                createStep(STEP_ENTRY),
                createStep(PREFIX + ADDITIONAL_DATA),
                createStep(RESULT_ENTRY),
                createStep(PREFIX + ESCAPE + ADDITIONAL_DATA),
                createStep(DATA_ENTRY),
                createStep(PREFIX + ADDITIONAL_DATA),
                createStep(STEP_ENTRY),
                createStep(STEP_ENTRY)
                );

        List<ManualTestStep> manualSteps = ManualStepConverter.convert(STORY_TITLE, SCENARIO_TITLE, steps);
        assertThat(manualSteps, hasSize(4));
        assertManualStep(manualSteps.get(0), STEP, DATA, RESULT);
        assertManualStep(manualSteps.get(1), STEP + lineSeparator() + ADDITIONAL_DATA,
                DATA + lineSeparator() + ADDITIONAL_DATA, RESULT + lineSeparator() + ADDITIONAL_DATA);
        assertManualStep(manualSteps.get(2), STEP, null, null);
        assertManualStep(manualSteps.get(3), STEP, null, null);
    }

    private static void assertManualStep(ManualTestStep step, String action, String data, String expectedResult)
    {
        assertEquals(action, step.getAction());
        assertEquals(data, step.getData());
        assertEquals(expectedResult, step.getExpectedResult());
    }

    private static Step createStep(String value)
    {
        return createStep("comment", value);
    }

    private static Step createStep(String comment, String value)
    {
        Step step = new Step();
        step.setOutcome(comment);
        step.setValue(value);
        return step;
    }
}
