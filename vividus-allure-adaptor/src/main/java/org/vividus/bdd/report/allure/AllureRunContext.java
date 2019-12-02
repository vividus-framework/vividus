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

package org.vividus.bdd.report.allure;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import org.vividus.bdd.report.allure.model.ScenarioExecutionStage;
import org.vividus.bdd.report.allure.model.StoryExecutionStage;
import org.vividus.testcontext.TestContext;

import io.qameta.allure.model.Label;

public class AllureRunContext implements IAllureRunContext
{
    private static final String STORY_LABELS = "allureStoryLabels";
    private static final String GIVEN_STORY_LABELS = "allureGivenStoryLabels";

    private final TestContext testContext;

    public AllureRunContext(TestContext testContext)
    {
        this.testContext = testContext;
    }

    @Override
    public List<Label> createNewStoryLabels(boolean givenStory)
    {
        if (givenStory)
        {
            List<Label> storyLabels = new ArrayList<>();
            getGivenStoriesLabels().push(storyLabels);
            return storyLabels;
        }
        return getRootStoryLabels();
    }

    @Override
    public List<Label> getCurrentStoryLabels()
    {
        List<Label> givenStoryLabels = getGivenStoriesLabels().peek();
        return givenStoryLabels != null ? givenStoryLabels : getRootStoryLabels();
    }

    @Override
    public List<Label> getRootStoryLabels()
    {
        return testContext.get(STORY_LABELS, ArrayList::new);
    }

    @Override
    public void resetCurrentStoryLabels(boolean givenStory)
    {
        if (givenStory)
        {
            getGivenStoriesLabels().pop();
        }
        else
        {
            testContext.remove(STORY_LABELS);
        }
    }

    @Override
    public void initExecutionStages()
    {
        getExecutionStages().push(new ExecutionStages());
    }

    @Override
    public StoryExecutionStage getStoryExecutionStage()
    {
        return getExecutionStages().peek().storyStage;
    }

    @Override
    public void setStoryExecutionStage(StoryExecutionStage stage)
    {
        getExecutionStages().peek().storyStage = stage;
    }

    @Override
    public void resetExecutionStages()
    {
        getExecutionStages().pop();
    }

    @Override
    public ScenarioExecutionStage getScenarioExecutionStage()
    {
        ExecutionStages executionStages = getExecutionStages().peek();
        return executionStages != null ? executionStages.scenarioStage : null;
    }

    @Override
    public void setScenarioExecutionStage(ScenarioExecutionStage stage)
    {
        getExecutionStages().peek().scenarioStage = stage;
    }

    @Override
    public void resetScenarioExecutionStage()
    {
        setScenarioExecutionStage(null);
    }

    private Deque<List<Label>> getGivenStoriesLabels()
    {
        return testContext.get(GIVEN_STORY_LABELS, LinkedList::new);
    }

    private Deque<ExecutionStages> getExecutionStages()
    {
        return testContext.get(ExecutionStages.class, LinkedList::new);
    }

    private static class ExecutionStages
    {
        private StoryExecutionStage storyStage;
        private ScenarioExecutionStage scenarioStage;
    }
}
