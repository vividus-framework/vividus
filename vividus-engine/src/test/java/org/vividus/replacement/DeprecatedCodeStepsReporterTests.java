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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.jbehave.core.annotations.When;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.annotation.Replacement;

@ExtendWith({ TestLoggerFactoryExtension.class, MockitoExtension.class })
class DeprecatedCodeStepsReporterTests
{
    private static final String REMOVE_VERSION = "0.7.0";
    private static final String DEPRECATED_STEP_VALUE = "deprecated step";
    private static final String ACTUAL_STEP_PATTERN = "Then actual step";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(DeprecatedCodeStepsReporter.class);

    @Mock private DeprecatedStepNotificationFactory deprecatedStepNotificationFactory;
    @InjectMocks private DeprecatedCodeStepsReporter loggingMonitor;

    @Test
    void shouldLogDeprecatedStepIfAnnotationPresent() throws NoSuchMethodException
    {
        String deprecatedStepString = "When " + DEPRECATED_STEP_VALUE;
        String expectedLogMessage = String.format(
                "The step: \"%s\" is deprecated and will be removed in VIVIDUS %s. Use step: \"%s\"",
                deprecatedStepString, REMOVE_VERSION, ACTUAL_STEP_PATTERN);
        var method = DeprecatedCodeStepsReporterTests.class.getDeclaredMethod("stepMethod");
        when(deprecatedStepNotificationFactory.createDeprecatedStepNotification(REMOVE_VERSION,
                ACTUAL_STEP_PATTERN)).thenReturn(expectedLogMessage);
        loggingMonitor.beforePerforming(null, false, method);
        assertThat(logger.getLoggingEvents(), is(List.of(info(expectedLogMessage))));
    }

    @Test
    void shouldNotLogIfAnnotationOrMethodNotPresent() throws NoSuchMethodException
    {
        var method = DeprecatedCodeStepsReporterTests.class
                .getDeclaredMethod("stepMethodWithoutReplacementAnnotation");
        loggingMonitor.beforePerforming(null, false, method);
        loggingMonitor.beforePerforming(null, false, null);
        verifyNoInteractions(deprecatedStepNotificationFactory);
    }

    @Replacement(versionToRemoveStep = REMOVE_VERSION, replacementFormatPattern = ACTUAL_STEP_PATTERN)
    @When(DEPRECATED_STEP_VALUE)
    void stepMethod()
    {
        // nothing to do
    }

    @When(DEPRECATED_STEP_VALUE)
    void stepMethodWithoutReplacementAnnotation()
    {
        // nothing to do
    }
}
