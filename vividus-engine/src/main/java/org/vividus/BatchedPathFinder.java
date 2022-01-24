/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.vividus.batch.BatchResourceConfiguration;
import org.vividus.batch.BatchStorage;

/**
 * The API provided by this class is <b><u>not</u></b> thread-safe and expected to be invoked from the main thread
 * by the test runner.
 */
public class BatchedPathFinder implements IBatchedPathFinder
{
    private final IPathFinder pathFinder;
    private final BatchStorage batchStorage;

    private Map<String, List<String>> batchKeyToStoryPaths;

    public BatchedPathFinder(IPathFinder pathFinder, BatchStorage batchStorage)
    {
        this.pathFinder = pathFinder;
        this.batchStorage = batchStorage;
    }

    @Override
    public Map<String, List<String>> getPaths() throws IOException
    {
        if (batchKeyToStoryPaths == null)
        {
            batchKeyToStoryPaths = new LinkedHashMap<>();
            for (Entry<String, BatchResourceConfiguration> batch : batchStorage.getBatchResourceConfigurations()
                    .entrySet())
            {
                batchKeyToStoryPaths.put(batch.getKey(), pathFinder.findPaths(batch.getValue()));
            }
        }
        return batchKeyToStoryPaths;
    }
}
