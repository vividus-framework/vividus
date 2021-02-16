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

package org.vividus.bdd.model;

import java.util.Deque;
import java.util.LinkedList;

import org.jbehave.core.model.Story;

public class RunningStory
{
    private Story story;
    private boolean failed;
    private boolean notExcluded;
    private RunningScenario runningScenario;
    private Deque<String> runningSteps = new LinkedList<>();

    public Story getStory()
    {
        return story;
    }

    public void setStory(Story story)
    {
        this.story = story;
    }

    public boolean isFailed()
    {
        return failed;
    }

    public boolean isNotExcluded()
    {
        return notExcluded;
    }

    public void setNotExcluded(boolean notExcluded)
    {
        this.notExcluded = notExcluded;
    }

    public void fail()
    {
        failed = true;
        if (runningScenario != null)
        {
            runningScenario.setFailed(true);
        }
    }

    public String getName()
    {
        return getStory().getName().replace(".story", "");
    }

    public RunningScenario getRunningScenario()
    {
        return runningScenario;
    }

    public void setRunningScenario(RunningScenario runningScenario)
    {
        this.runningScenario = runningScenario;
    }

    public String removeRunningStep()
    {
        return runningSteps.pollFirst();
    }

    public void putRunningStep(String runningStep)
    {
        runningSteps.push(runningStep);
    }

    public Deque<String> getRunningSteps()
    {
        return new LinkedList<>(runningSteps);
    }

    public void setRunningSteps(Deque<String> runningSteps)
    {
        this.runningSteps = runningSteps;
    }
}
