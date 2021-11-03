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

package org.vividus.bdd;

import java.util.Map;

import javax.inject.Inject;

import org.jbehave.core.embedder.PerformableTree.Status;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Step;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.StepCollector.Stage;
import org.jbehave.core.steps.Timing;
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
        runningStory.setNotExcluded(bddRunContext.getStoryStatus(story) != Status.EXCLUDED);
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
    public void afterScenario(Timing timing)
    {
        super.afterScenario(timing);
        bddRunContext.getRunningStory().setRunningScenario(null);
    }

    @Override
    public void beforeStep(Step step)
    {
        perform(() -> bddRunContext.getRunningStory().putRunningStep(step.getStepAsString()));
        super.beforeStep(step);
    }

    @Override
    public void successful(String step)
    {
        super.successful(step);
        perform(() -> bddRunContext.getRunningStory().removeRunningStep());
    }

    @Override
    public void failed(String step, Throwable cause)
    {
        super.failed(step, cause);
        perform(() ->
        {
            bddRunContext.getRunningStory().fail();
            bddRunContext.getRunningStory().removeRunningStep();
        });
    }

    @Override
    public void example(Map<String, String> tableRow, int exampleIndex)
    {
        RunningScenario runningScenario = bddRunContext.getRunningStory().getRunningScenario();
        runningScenario.setIndex(exampleIndex);
        runningScenario.setTitle(runningScenario.getScenario().getTitle()
                + (exampleIndex == -1 ? "" : " [" + (exampleIndex + 1) + ']'));
        runningScenario.setExample(tableRow);
        super.example(tableRow, exampleIndex);
    }

    @Override
    public void storyExcluded(Story story, String filter)
    {
        bddRunContext.setStoryStatus(story, Status.EXCLUDED);
        super.storyExcluded(story, filter);
    }

    @Override
    public void dryRun()
    {
        bddRunContext.setDryRun(true);
    }

    @Override
    public void beforeStoriesSteps(Stage stage)
    {
        if (stage == Stage.AFTER)
        {
            bddRunContext.completeRun();
        }
        super.beforeStoriesSteps(stage);
    }

    private void perform(Runnable runnable)
    {
        if (!bddRunContext.isRunCompleted())
        {
            runnable.run();
        }
    }
}
