/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.steps.CandidateSteps;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.jbehave.core.steps.StepCandidate;
import org.junit.jupiter.api.Test;
import org.vividus.configuration.BeanFactory;
import org.vividus.runner.StepsCollector.Step;

class StepsCollectorTests
{
    private static final String GIVEN = "Given";
    private static final String WHEN = "When";
    private static final String THEN = "Then";
    private static final String GIVEN_PATTERN = "initial state is '$status'";
    private static final String WHEN_PATTERN = "I do '$action'";
    private static final String THEN_PATTERN = "I run composite step with comment";

    @Test
    void shouldReturnSteps() throws ReflectiveOperationException
    {
        try (var beanFactory = mockStatic(BeanFactory.class))
        {
            var stepsFactory = mock(InjectableStepsFactory.class);
            beanFactory.when(() -> BeanFactory.getBean(InjectableStepsFactory.class)).thenReturn(stepsFactory);
            var candidateSteps = mock(CandidateSteps.class);
            when(stepsFactory.createCandidateSteps()).thenReturn(List.of(candidateSteps));
            var plainStep = mockStepCandidate(GIVEN, GIVEN_PATTERN, "simpleMethod");
            var deprecatedSteps = mockStepCandidate(WHEN, WHEN_PATTERN, "deprecatedMethod");
            var deprecatedCompositeStep = mockStepCandidate(THEN, THEN_PATTERN, (Method) null,
                    "!-- DEPRECATED: The step is deprecated and will be removed in VIVIDUS 0.6.0");
            when(candidateSteps.listCandidates()).thenReturn(
                    List.of(plainStep, deprecatedSteps, deprecatedCompositeStep));
            List<Step> steps = StepsCollector.getSteps().stream().sorted().toList();
            assertEquals(3, steps.size());
            assertStep(steps.get(0), GIVEN, GIVEN_PATTERN, false, false, StringUtils.EMPTY);
            assertStep(steps.get(1), WHEN, WHEN_PATTERN, true, false, StringUtils.EMPTY);
            assertStep(steps.get(2), THEN, THEN_PATTERN, true, true, null);
        }
    }

    private void assertStep(Step step, String word, String pattern, boolean deprecated, boolean composite,
            String location)
    {
        Step actualStep = new Step(word, pattern);
        actualStep.setDeprecated(deprecated);
        actualStep.setCompositeInStepsFile(composite);
        actualStep.setLocation(location);
        assertEquals(step, actualStep);
    }

    private StepCandidate mockStepCandidate(String startingWord, String patternAsString, String methodName,
            String... composedSteps) throws ReflectiveOperationException
    {
        return mockStepCandidate(startingWord, patternAsString, getClass().getDeclaredMethod(methodName),
                composedSteps);
    }

    private static StepCandidate mockStepCandidate(String startingWord, String patternAsString, Method method,
            String... composedSteps)
    {
        var stepCandidate = mock(StepCandidate.class);
        when(stepCandidate.getStartingWord()).thenReturn(startingWord);
        when(stepCandidate.getPatternAsString()).thenReturn(patternAsString);
        when(stepCandidate.getMethod()).thenReturn(method);
        when(stepCandidate.composedSteps()).thenReturn(composedSteps);
        return stepCandidate;
    }

    @SuppressWarnings("unused")
    private void simpleMethod()
    {
        // used for testing purposes
    }

    @SuppressWarnings({"unused", "checkstyle:MissingDeprecated", "checkstyle:RequiredParameterForAnnotation"})
    @Deprecated
    private void deprecatedMethod()
    {
        // used for testing purposes
    }
}
