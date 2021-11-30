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

package org.vividus.variable;

import org.jbehave.core.model.Step;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.Timing;
import org.vividus.ChainedStoryReporter;
import org.vividus.context.VariableContext;

public class VariableStoryReporter extends ChainedStoryReporter
{
    private final VariableContext variableContext;

    public VariableStoryReporter(VariableContext variableContext)
    {
        this.variableContext = variableContext;
    }

    @Override
    public void beforeStory(Story story, boolean givenStory)
    {
        if (!givenStory)
        {
            variableContext.initVariables();
        }
        super.beforeStory(story, givenStory);
    }

    @Override
    public void beforeStep(Step step)
    {
        variableContext.initStepVariables();
        super.beforeStep(step);
    }

    @Override
    public void successful(String step)
    {
        super.successful(step);
        variableContext.clearStepVariables();
    }

    @Override
    public void failed(String step, Throwable cause)
    {
        super.failed(step, cause);
        variableContext.clearStepVariables();
    }

    @Override
    public void afterScenario(Timing timing)
    {
        super.afterScenario(timing);
        variableContext.clearScenarioVariables();
    }
}
