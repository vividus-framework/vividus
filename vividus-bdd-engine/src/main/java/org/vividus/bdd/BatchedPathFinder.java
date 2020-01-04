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

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.vividus.bdd.resource.ResourceBatch;

public class BatchedPathFinder implements IBatchedPathFinder
{
    private IPathFinder pathFinder;
    private IBatchStorage batchStorage;

    @Override
    public Map<String, List<String>> findPaths() throws IOException
    {
        Map<String, List<String>> batchedPaths = new LinkedHashMap<>();
        for (Entry<String, ResourceBatch> batch : batchStorage.getBatches().entrySet())
        {
            batchedPaths.put(batch.getKey(), pathFinder.findPaths(batch.getValue()));
        }
        return batchedPaths;
    }

    public void setPathFinder(IPathFinder pathFinder)
    {
        this.pathFinder = pathFinder;
    }

    public void setBatchStorage(IBatchStorage batchStorage)
    {
        this.batchStorage = batchStorage;
    }
}
