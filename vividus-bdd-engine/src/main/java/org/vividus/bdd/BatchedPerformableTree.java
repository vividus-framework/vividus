/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd;

import org.jbehave.core.embedder.PerformableTree;
import org.jbehave.core.steps.StepCollector.Stage;

public class BatchedPerformableTree extends PerformableTree
{
    private boolean ignoreFailureInBatches;
    private boolean reportBeforeStories;
    private boolean reportAfterStories;

    @Override
    public void performBeforeOrAfterStories(RunContext context, Stage stage)
    {
        if (reportBeforeStories && Stage.BEFORE.equals(stage) || (Stage.AFTER.equals(stage)
                && (reportAfterStories || !ignoreFailureInBatches && !context.getFailures().isEmpty())))
        {
            super.performBeforeOrAfterStories(context, stage);
        }
    }

    public void setReportBeforeStories(boolean reportBeforeStories)
    {
        this.reportBeforeStories = reportBeforeStories;
    }

    public void setReportAfterStories(boolean reportAfterStories)
    {
        this.reportAfterStories = reportAfterStories;
    }

    public void setIgnoreFailureInBatches(boolean ignoreFailureInBatches)
    {
        this.ignoreFailureInBatches = ignoreFailureInBatches;
    }
}
