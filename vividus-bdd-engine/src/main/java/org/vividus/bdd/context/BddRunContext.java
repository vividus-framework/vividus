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

package org.vividus.bdd.context;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

import org.jbehave.core.embedder.PerformableTree.Status;
import org.jbehave.core.model.Story;
import org.vividus.bdd.model.RunningStory;
import org.vividus.testcontext.TestContext;

public class BddRunContext implements IBddRunContext
{
    private TestContext testContext;

    // must be initialized for jbehave-junit-runner
    private Optional<String> runningBatchKey = Optional.of("batch-1");
    private boolean dryRun;
    private boolean runCompleted;

    public void putRunningStory(RunningStory story, boolean givenStory)
    {
        if (givenStory)
        {
            getGivenStories().push(story);
        }
        else
        {
            testContext.put(RunningStory.class, story);
        }
    }

    public void removeRunningStory(boolean givenStory)
    {
        if (givenStory)
        {
            getGivenStories().pop();
        }
    }

    @Override
    public RunningStory getRunningStory()
    {
        RunningStory givenStory = getGivenStories().peek();
        return givenStory != null ? givenStory : getRootRunningStory();
    }

    @Override
    public RunningStory getRootRunningStory()
    {
        return testContext.get(RunningStory.class, RunningStory.class);
    }

    @Override
    public Deque<RunningStory> getStoriesChain()
    {
        LinkedList<RunningStory> storiesChain = new LinkedList<>(getGivenStories());
        storiesChain.addLast(getRootRunningStory());
        return storiesChain;
    }

    private Deque<RunningStory> getGivenStories()
    {
        return testContext.get("RunningGivenStory", LinkedList::new);
    }

    public void putRunningBatch(String batchKey)
    {
        runningBatchKey = Optional.of(batchKey);
    }

    public void removeRunningBatch()
    {
        runningBatchKey = Optional.empty();
    }

    @Override
    public String getRunningBatchKey()
    {
        return runningBatchKey.orElseThrow(() -> new IllegalStateException("No running batch is found"));
    }

    public Status getStoryStatus(Story story)
    {
        return testContext.get(story);
    }

    public void setStoryStatus(Story story, Status status)
    {
        testContext.put(story, status);
    }

    @Override
    public void setDryRun(boolean dryRun)
    {
        this.dryRun = dryRun;
    }

    @Override
    public boolean isDryRun()
    {
        return dryRun;
    }

    @Override
    public boolean isRunCompleted()
    {
        return runCompleted;
    }

    @Override
    public void completeRun()
    {
        this.runCompleted = true;
    }

    public void setTestContext(TestContext testContext)
    {
        this.testContext = testContext;
    }
}
