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

package org.vividus.batch;

/**
 * Represents the batch currently being executed.
 */
public final class RunningBatch
{
    private final BatchConfiguration batchConfiguration;
    private final String batchKey;
    private final int batchIndex;

    /**
     * Creates a running batch and derives its index from the key.
     *
     * @param batchKey batch key
     * @param batchConfiguration batch configuration
     */
    public RunningBatch(String batchKey, BatchConfiguration batchConfiguration)
    {
        this.batchConfiguration = batchConfiguration;
        this.batchKey = batchKey;
        this.batchIndex = BatchStorage.getBatchIndex(batchKey);
    }

    /**
     * @return batch configuration
     */
    public BatchConfiguration getBatchConfiguration()
    {
        return batchConfiguration;
    }

    /**
     * @return batch key
     */
    public String getBatchKey()
    {
        return batchKey;
    }

    /**
     * @return batch index
     */
    public int getBatchIndex()
    {
        return batchIndex;
    }
}
