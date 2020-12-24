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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.vividus.bdd.model.RunningStory;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.testcontext.TestContext;

class BddRunContextTests
{
    private static final String BATCH_KEY = "batchKey";

    private final BddRunContext bddRunContext = new BddRunContext();

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
        TestContext testContext = mock(TestContext.class);
        bddRunContext.setTestContext(testContext);
        bddRunContext.removeRunningStory(false);
        verifyNoInteractions(testContext);
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
    void testGetDefaultRunningBatchKey()
    {
        assertEquals("batch-1", bddRunContext.getRunningBatchKey());
    }

    @Test
    void testGetRunningBatchKey()
    {
        bddRunContext.putRunningBatch(BATCH_KEY);
        assertEquals(BATCH_KEY, bddRunContext.getRunningBatchKey());
    }

    @Test
    void testRemoveRunningBatchKey()
    {
        bddRunContext.putRunningBatch(BATCH_KEY);
        bddRunContext.removeRunningBatch();
        IllegalStateException exception = assertThrows(IllegalStateException.class, bddRunContext::getRunningBatchKey);
        assertEquals("No running batch is found", exception.getMessage());
    }

    @Test
    void testSetDryRun()
    {
        bddRunContext.setDryRun(true);
        assertTrue(bddRunContext.isDryRun());
    }
}
