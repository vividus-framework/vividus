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

package org.vividus.bdd;

import org.jbehave.core.model.Story;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.steps.ISubStepsListener;
import org.vividus.bdd.variable.VariableScope;

public class BddVariableStoryReporter extends ChainedStoryReporter implements ISubStepsListener
{
    private IBddVariableContext bddVariableContext;

    private final ThreadLocal<Boolean> subStepPublishing = ThreadLocal.withInitial(() -> Boolean.FALSE);

    @Override
    public void beforeStory(Story story, boolean givenStory)
    {
        bddVariableContext.initVariables();
        super.beforeStory(story, givenStory);
    }

    @Override
    public void successful(String step)
    {
        super.successful(step);
        cleanUpStepScopeContext();
    }

    @Override
    public void failed(String step, Throwable cause)
    {
        super.failed(step, cause);
        cleanUpStepScopeContext();
    }

    @Override
    public void ignorable(String step)
    {
        super.ignorable(step);
        cleanUpStepScopeContext();
    }

    @Override
    public void pending(String step)
    {
        super.pending(step);
        cleanUpStepScopeContext();
    }

    @Override
    public void notPerformed(String step)
    {
        super.notPerformed(step);
        cleanUpStepScopeContext();
    }

    @Override
    public void restarted(String step, Throwable cause)
    {
        super.restarted(step, cause);
        cleanUpStepScopeContext();
    }

    @Override
    public void beforeSubSteps()
    {
        setSubStepPublishing(Boolean.TRUE);
    }

    @Override
    public void afterSubSteps()
    {
        setSubStepPublishing(Boolean.FALSE);
    }

    private void setSubStepPublishing(Boolean value)
    {
        subStepPublishing.set(value);
    }

    private void cleanUpStepScopeContext()
    {
        if (!subStepPublishing.get())
        {
            bddVariableContext.clearVariables(VariableScope.STEP);
        }
    }

    public void setBddVariableContext(IBddVariableContext bddVariableContext)
    {
        this.bddVariableContext = bddVariableContext;
    }
}
