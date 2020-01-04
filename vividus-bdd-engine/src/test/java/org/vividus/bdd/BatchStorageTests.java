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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.vividus.bdd.resource.ResourceBatch;
import org.vividus.util.property.IPropertyMapper;

class BatchStorageTests
{
    private static final String BATCH1 = "batch-1";
    private static final String BATCH2 = "batch-2";
    private static final String BATCH3 = "batch-3";
    private static final String BATCH4 = "batch-4";
    private static final String BATCH5 = "batch-5";
    private static final String BATCH6 = "batch-6";
    private static final String BATCH7 = "batch-7";
    private static final String BATCH8 = "batch-8";
    private static final String BATCH9 = "batch-9";
    private static final String BATCH10 = "batch-10";
    private static final String BATCH11 = "batch-11";

    private BatchStorage batchStorage;

    private Map<String, ResourceBatch> batches;

    @BeforeEach
    void beforeEach() throws IOException
    {
        batches = new HashMap<>();
        batches.put(BATCH2, new ResourceBatch());
        batches.put("unknown-batch", new ResourceBatch());
        batches.put(BATCH1, new ResourceBatch());
        batches.put(BATCH10, new ResourceBatch());
        batches.put(BATCH3, new ResourceBatch());
        batches.put(BATCH4, new ResourceBatch());
        batches.put(BATCH5, new ResourceBatch());
        batches.put(BATCH6, new ResourceBatch());
        batches.put(BATCH7, new ResourceBatch());
        batches.put(BATCH8, new ResourceBatch());
        batches.put(BATCH9, new ResourceBatch());
        batches.put(BATCH11, new ResourceBatch());
        IPropertyMapper propertyMapper = mock(IPropertyMapper.class);
        String propertyPrefix = "org.vividus.prefix";
        when(propertyMapper.readValues(propertyPrefix, ResourceBatch.class)).thenReturn(batches);
        batchStorage = new BatchStorage(propertyMapper, propertyPrefix);
    }

    @Test
    void testGetBatchByKey()
    {
        ResourceBatch batch = batchStorage.getBatch(BATCH1);
        assertEquals(batches.get(BATCH1), batch);
    }

    @Test
    void testGetAllBatches()
    {
        Map<String, ResourceBatch> allBatches = batchStorage.getBatches();
        assertThat(allBatches.keySet(), Matchers.contains(BATCH1, BATCH2, BATCH3, BATCH4, BATCH5,
                BATCH6, BATCH7, BATCH8, BATCH9, BATCH10, BATCH11));
    }
}
