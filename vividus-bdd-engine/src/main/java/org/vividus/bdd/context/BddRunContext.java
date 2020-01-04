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

package org.vividus.bdd.context;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;

import org.jbehave.core.embedder.PerformableTree.Status;
import org.jbehave.core.model.Story;
import org.vividus.bdd.IBatchStorage;
import org.vividus.bdd.model.RunningStory;
import org.vividus.bdd.resource.ResourceBatch;
import org.vividus.testcontext.TestContext;

public class BddRunContext implements IBddRunContext
{
    private TestContext testContext;
    private IBatchStorage batchStorage;

    private Optional<String> runningBatchKey;
    private boolean dryRun;

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
        else
        {
            testContext.remove(RunningStory.class);
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
    public ResourceBatch getRunningBatch()
    {
        String batchKey = runningBatchKey.orElseThrow(() -> new IllegalStateException("No running batch is found"));
        return batchStorage.getBatch(batchKey);
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

    public void setTestContext(TestContext testContext)
    {
        this.testContext = testContext;
    }

    public void setBatchStorage(IBatchStorage batchStorage)
    {
        this.batchStorage = batchStorage;
    }
}
