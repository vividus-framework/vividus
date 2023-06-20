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

package org.vividus.replacement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jbehave.core.model.Step;
import org.jbehave.core.steps.StepCreator.StepExecutionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.log.LoggingStoryReporter;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.testcontext.TestContext;

@ExtendWith(MockitoExtension.class)
class DeprecatedCompositeStepsReporterTests
{
    private static final Object DEPRECATED_STEP_NOTIFICATION_KEY = DeprecatedCompositeStepsReporter.class;
    private static final String REMOVE_VERSION = "0.6.0";
    private static final String DEPRECATED_STEP_STRING = "When I deprecated new step with parameter `value`";
    private static final String ACTUAL_STEP_PATTERN = "When I use actual step with parameter `%1$s`";
    private static final String ACTUAL_STEP_STRING = "When I use actual step with parameter `value`";
    private static final String DEPRECATION_INFO = String.format("!-- DEPRECATED: %s, %s",
            REMOVE_VERSION, ACTUAL_STEP_PATTERN);
    private static final String DEPRECATION_INFO_RESOLVED = String.format(
            "The step: \"%s\" is deprecated and will be removed in VIVIDUS %s. Use step: \"%s\"",
            DEPRECATED_STEP_STRING, REMOVE_VERSION, ACTUAL_STEP_STRING);

    @Spy private TestContext testContext = new SimpleTestContext();
    @Mock private LoggingStoryReporter nextReporter;
    @Mock private DeprecatedStepNotificationFactory deprecatedStepNotificationFactory;
    @InjectMocks private DeprecatedCompositeStepsReporter deprecatedCompositeStepsReporter;

    @BeforeEach
    void beforeEach()
    {
        deprecatedCompositeStepsReporter.setNext(nextReporter);
    }

    @Test
    void shouldResolveCommentInDeprecatedCompositeStep()
    {
        runBeforeStep();
        assertEquals(DEPRECATION_INFO_RESOLVED, testContext.get(DEPRECATED_STEP_NOTIFICATION_KEY));
        verify(nextReporter).beforeStep(argThat(s -> s.getExecutionType() == StepExecutionType.COMMENT
                && s.getStepAsString().equals(DEPRECATION_INFO_RESOLVED)));
    }

    @ParameterizedTest
    @CsvSource({"EXECUTABLE, Step for web",
                "COMMENT,    !-- Comment for content"})
    void shouldIgnoreNotCommentTypeOrUnsuitableComment(StepExecutionType executionType, String stepStringValue)
    {
        Step unsuitableStep = new Step(executionType, stepStringValue);
        deprecatedCompositeStepsReporter.beforeStep(unsuitableStep);
        assertNull(testContext.get(DEPRECATED_STEP_NOTIFICATION_KEY));
        verify(nextReporter).beforeStep(unsuitableStep);
    }

    @Test
    void shouldGetResolvedCommentFromContextAndPutItNext()
    {
        runBeforeStep();
        deprecatedCompositeStepsReporter.comment(DEPRECATION_INFO);
        verify(nextReporter).comment(DEPRECATION_INFO_RESOLVED);
        assertNull(testContext.get(DEPRECATED_STEP_NOTIFICATION_KEY));
    }

    @Test
    void shouldIgnoreUnsuitableComment()
    {
        Step unsuitableStep = new Step(StepExecutionType.COMMENT, "!-- Comment for repent");
        deprecatedCompositeStepsReporter.beforeStep(unsuitableStep);
        deprecatedCompositeStepsReporter.comment(unsuitableStep.getStepAsString());
        verify(nextReporter).comment(unsuitableStep.getStepAsString());
    }

    private void runBeforeStep()
    {
        Step commentWithDeprecationInfo = new Step(StepExecutionType.COMMENT, DEPRECATION_INFO);
        when(deprecatedStepNotificationFactory.createDeprecatedStepNotification(REMOVE_VERSION,
                ACTUAL_STEP_PATTERN)).thenReturn(DEPRECATION_INFO_RESOLVED);
        deprecatedCompositeStepsReporter.beforeStep(commentWithDeprecationInfo);
    }
}
