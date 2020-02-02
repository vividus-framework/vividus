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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.batch.BatchResourceConfiguration;
import org.vividus.bdd.batch.BatchStorage;
import org.vividus.bdd.model.RunningStory;
import org.vividus.testcontext.SimpleTestContext;

@ExtendWith(MockitoExtension.class)
class BddRunContextTests
{
    private static final String BATCH_KEY = "batchKey";

    @Mock
    private BatchStorage batchStorage;

    @InjectMocks
    private BddRunContext bddRunContext;

    @Test
    void testGetStoriesChain()
    {
        bddRunContext.setTestContext(new SimpleTestContext());
        RunningStory rootStory = new RunningStory();
        bddRunContext.putRunningStory(rootStory, false);
        RunningStory givenStory = new RunningStory();
        bddRunContext.putRunningStory(givenStory, true);
        assertEquals(new LinkedList<>(List.of(givenStory, rootStory)), bddRunContext.getStoriesChain());
    }

    @Test
    void testPutRunningStory()
    {
        bddRunContext.setTestContext(new SimpleTestContext());
        RunningStory runningStory = new RunningStory();
        bddRunContext.putRunningStory(runningStory, false);
        assertEquals(runningStory, bddRunContext.getRunningStory());
    }

    @Test
    void testRemoveRunningStory()
    {
        bddRunContext.setTestContext(new SimpleTestContext());
        RunningStory runningStory = new RunningStory();
        bddRunContext.putRunningStory(runningStory, false);
        bddRunContext.removeRunningStory(false);
        assertNull(bddRunContext.getRunningStory());
    }

    @Test
    void testPutRunningGivenStory()
    {
        bddRunContext.setTestContext(new SimpleTestContext());
        RunningStory runningStory = new RunningStory();
        bddRunContext.putRunningStory(runningStory, true);
        assertEquals(runningStory, bddRunContext.getRunningStory());
    }

    @Test
    void testRemoveRunningGivenStory()
    {
        bddRunContext.setTestContext(new SimpleTestContext());
        RunningStory runningStory = new RunningStory();
        bddRunContext.putRunningStory(runningStory, true);
        bddRunContext.removeRunningStory(true);
        assertNull(bddRunContext.getRunningStory());
    }

    @Test
    void testPutRunningBatch()
    {
        String batchKey = BATCH_KEY;
        bddRunContext.putRunningBatch(batchKey);
        BatchResourceConfiguration batch = new BatchResourceConfiguration();
        when(batchStorage.getBatchResourceConfiguration(batchKey)).thenReturn(batch);
        assertEquals(batch, bddRunContext.getRunningBatch());
    }

    @Test
    void testGetRunningBatchKey()
    {
        bddRunContext.putRunningBatch(BATCH_KEY);
        assertEquals(BATCH_KEY, bddRunContext.getRunningBatchKey());
    }

    @Test
    void testRemoveRunningBatch()
    {
        bddRunContext.putRunningBatch(BATCH_KEY);
        bddRunContext.removeRunningBatch();
        IllegalStateException exception = assertThrows(IllegalStateException.class, bddRunContext :: getRunningBatch);
        assertEquals("No running batch is found", exception.getMessage());
    }

    @Test
    void testSetDryRun()
    {
        bddRunContext.setDryRun(true);
        assertTrue(bddRunContext.isDryRun());
    }
}
