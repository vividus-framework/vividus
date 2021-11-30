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

package org.vividus;

import static org.mockito.Mockito.inOrder;

import org.jbehave.core.model.Lifecycle.ExecutionType;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.StepCollector.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.ReportControlContext;

@ExtendWith(MockitoExtension.class)
class ReportControlStoryReporterTests
{
    @Mock private StoryReporter next;
    @Mock private ReportControlContext context;
    @InjectMocks private ReportControlStoryReporter reporter;

    @BeforeEach
    void beforeEach()
    {
        reporter.setNext(next);
    }

    @Test
    void shouldDisableReportingOnBeforeStorySystemStepsAndEnableOnAfterStorySteps()
    {
        InOrder order = inOrder(context, next);

        reporter.beforeStorySteps(Stage.BEFORE, ExecutionType.SYSTEM);
        reporter.afterStorySteps(Stage.BEFORE, ExecutionType.SYSTEM);

        order.verify(context).disableReporting();
        order.verify(next).beforeStorySteps(Stage.BEFORE, ExecutionType.SYSTEM);
        order.verify(context).enableReporting();
        order.verify(next).afterStorySteps(Stage.BEFORE, ExecutionType.SYSTEM);
        order.verifyNoMoreInteractions();
    }

    @Test
    void shouldDisableReportingOnBeforeScenarioSystemStepsAndEnableOnAfterScenarioSteps()
    {
        InOrder order = inOrder(context, next);

        reporter.beforeScenarioSteps(Stage.BEFORE, ExecutionType.SYSTEM);
        reporter.afterScenarioSteps(Stage.BEFORE, ExecutionType.SYSTEM);

        order.verify(context).disableReporting();
        order.verify(next).beforeScenarioSteps(Stage.BEFORE, ExecutionType.SYSTEM);
        order.verify(context).enableReporting();
        order.verify(next).afterScenarioSteps(Stage.BEFORE, ExecutionType.SYSTEM);
        order.verifyNoMoreInteractions();
    }

    @Test
    void shouldNotInteractWithReporterContextOnNonSystemSteps()
    {
        InOrder order = inOrder(context, next);

        reporter.beforeStorySteps(Stage.BEFORE, ExecutionType.USER);
        reporter.afterStorySteps(Stage.BEFORE, ExecutionType.USER);
        reporter.beforeScenarioSteps(Stage.BEFORE, ExecutionType.USER);
        reporter.afterScenarioSteps(Stage.BEFORE, ExecutionType.USER);

        order.verify(next).beforeStorySteps(Stage.BEFORE, ExecutionType.USER);
        order.verify(next).afterStorySteps(Stage.BEFORE, ExecutionType.USER);
        order.verify(next).beforeScenarioSteps(Stage.BEFORE, ExecutionType.USER);
        order.verify(next).afterScenarioSteps(Stage.BEFORE, ExecutionType.USER);
        order.verifyNoMoreInteractions();
    }
}
