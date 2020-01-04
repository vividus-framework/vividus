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

import java.util.Map;

import javax.inject.Inject;

import org.jbehave.core.embedder.PerformableTree.Status;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.vividus.bdd.context.BddRunContext;
import org.vividus.bdd.model.RunningScenario;
import org.vividus.bdd.model.RunningStory;

public class RunContextStoryReporter extends ChainedStoryReporter
{
    @Inject private BddRunContext bddRunContext;

    @Override
    public void beforeStory(Story story, boolean givenStory)
    {
        RunningStory runningStory = new RunningStory();
        runningStory.setStory(story);
        runningStory.setAllowed(bddRunContext.getStoryStatus(story) != Status.NOT_ALLOWED);
        bddRunContext.putRunningStory(runningStory, givenStory);
        super.beforeStory(story, givenStory);
    }

    @Override
    public void afterStory(boolean givenStory)
    {
        super.afterStory(givenStory);
        bddRunContext.removeRunningStory(givenStory);
    }

    @Override
    public void beforeScenario(Scenario scenario)
    {
        RunningScenario runningScenario = new RunningScenario();
        runningScenario.setScenario(scenario);
        bddRunContext.getRunningStory().setRunningScenario(runningScenario);
        super.beforeScenario(scenario);
    }

    @Override
    public void afterScenario()
    {
        super.afterScenario();
        bddRunContext.getRunningStory().setRunningScenario(null);
    }

    @Override
    public void beforeStep(String step)
    {
        bddRunContext.getRunningStory().setRunningStep(step);
        super.beforeStep(step);
    }

    @Override
    public void successful(String step)
    {
        super.successful(step);
        finishStep();
    }

    @Override
    public void failed(String step, Throwable cause)
    {
        super.failed(step, cause);
        bddRunContext.getRunningStory().fail();
        finishStep();
    }

    @Override
    public void ignorable(String step)
    {
        super.ignorable(step);
        finishStep();
    }

    @Override
    public void comment(String step)
    {
        super.comment(step);
        finishStep();
    }

    @Override
    public void pending(String step)
    {
        super.pending(step);
        finishStep();
    }

    @Override
    public void notPerformed(String step)
    {
        super.notPerformed(step);
        finishStep();
    }

    @Override
    public void example(Map<String, String> tableRow, int exampleIndex)
    {
        RunningScenario runningScenario = bddRunContext.getRunningStory().getRunningScenario();
        runningScenario.setIndex(exampleIndex);
        runningScenario.setTitle(runningScenario.getScenario().getTitle()
                + (exampleIndex == -1 ? "" : " [" + (exampleIndex + 1) + ']'));
        super.example(tableRow, exampleIndex);
    }

    @Override
    public void storyNotAllowed(Story story, String filter)
    {
        bddRunContext.setStoryStatus(story, Status.NOT_ALLOWED);
        super.storyNotAllowed(story, filter);
    }

    @Override
    public void dryRun()
    {
        bddRunContext.setDryRun(true);
    }

    private void finishStep()
    {
        bddRunContext.getRunningStory().setRunningStep(null);
    }
}
