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

package org.vividus.xray.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.vividus.bdd.model.jbehave.Scenario;
import org.vividus.bdd.model.jbehave.Step;

class TestCaseTypeTests
{
    private static final String VALUE = "value";
    private static final String COMMENT = "comment";
    private static final String SUCCESS = "success";

    static Stream<Arguments> steps()
    {
        return Stream.of(
            arguments(TestCaseType.MANUAL, List.of(createStep(COMMENT), createStep(COMMENT), createStep(COMMENT))),
            arguments(TestCaseType.CUCUMBER, List.of(createStep(SUCCESS), createStep(COMMENT), createStep(COMMENT))),
            arguments(TestCaseType.CUCUMBER, List.of(createStep(SUCCESS), createStep(SUCCESS), createStep(SUCCESS)))
        );
    }

    @MethodSource("steps")
    @ParameterizedTest
    void shouldDetermineTestCaseType(TestCaseType type, List<Step> steps)
    {
        Scenario scenario = new Scenario();
        scenario.setSteps(steps);
        assertEquals(type, TestCaseType.getTestCaseType(scenario));
    }

    private static Step createStep(String outcome)
    {
        Step step = new Step();
        step.setOutcome(outcome);
        step.setValue(VALUE);
        return step;
    }
}
