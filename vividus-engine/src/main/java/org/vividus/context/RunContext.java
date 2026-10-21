/*
 * Copyright 2019-2026 the original author or authors.
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

package org.vividus.context;

import java.util.Deque;

import org.vividus.batch.BatchConfiguration;
import org.vividus.model.RunningStory;

public interface RunContext
{
    RunningStory getRunningStory();

    RunningStory getRootRunningStory();

    Deque<RunningStory> getStoriesChain();

    String getRunningBatchKey();

    /**
     * Gets the index of the currently running batch.
     *
     * @return current batch index
     * @throws IllegalStateException if no batch is running
     */
    int getRunningBatchIndex();

    /**
     * Returns the configuration of the currently running batch.
     *
     * @return the configuration of the currently running batch
     * @throws IllegalStateException if no batch is running
     */
    BatchConfiguration getRunningBatch();

    void setDryRun(boolean dryRun);

    boolean isDryRun();

    void startRun();

    boolean isRunInProgress();

    void completeRun();
}
