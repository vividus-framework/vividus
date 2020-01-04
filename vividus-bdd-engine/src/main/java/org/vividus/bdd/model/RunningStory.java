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

package org.vividus.bdd.model;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.jbehave.core.model.Story;

public class RunningStory
{
    private final String uuid = UUID.randomUUID().toString();
    private Story story;
    private boolean failed;
    private boolean allowed;
    private RunningScenario runningScenario;
    private String runningStep;
    private final AtomicInteger nextVariableIndex = new AtomicInteger();

    public Story getStory()
    {
        return story;
    }

    public void setStory(Story story)
    {
        this.story = story;
    }

    public String getUuid()
    {
        return uuid;
    }

    public boolean isFailed()
    {
        return failed;
    }

    public boolean isAllowed()
    {
        return allowed;
    }

    public void setAllowed(boolean allowed)
    {
        this.allowed = allowed;
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

    public String getRunningStep()
    {
        return runningStep;
    }

    public void setRunningStep(String runningStep)
    {
        this.runningStep = runningStep;
    }

    public int calculateNextVariableIndex()
    {
        return nextVariableIndex.incrementAndGet();
    }
}
