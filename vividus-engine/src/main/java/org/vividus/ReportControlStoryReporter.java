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

import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Lifecycle.ExecutionType;
import org.jbehave.core.steps.StepCollector.Stage;
import org.vividus.context.ReportControlContext;

public class ReportControlStoryReporter extends ChainedStoryReporter
{
    private final ReportControlContext context;

    public ReportControlStoryReporter(ReportControlContext context)
    {
        this.context = context;
    }

    @Override
    public void beforeStorySteps(Stage stage, Lifecycle.ExecutionType type)
    {
        disableReporting(type);
        super.beforeStorySteps(stage, type);
    }

    @Override
    public void afterStorySteps(Stage stage, Lifecycle.ExecutionType type)
    {
        context.enableReporting();
        super.afterStorySteps(stage, type);
    }

    @Override
    public void beforeScenarioSteps(Stage stage, Lifecycle.ExecutionType type)
    {
        disableReporting(type);
        super.beforeScenarioSteps(stage, type);
    }

    @Override
    public void afterScenarioSteps(Stage stage, Lifecycle.ExecutionType type)
    {
        context.enableReporting();
        super.afterScenarioSteps(stage, type);
    }

    private void disableReporting(Lifecycle.ExecutionType type)
    {
        if (type == ExecutionType.SYSTEM)
        {
            context.disableReporting();
        }
    }
}
