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

package org.vividus.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.vividus.model.RunningStory;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.testcontext.TestContext;

class RunTestContextTests
{
    private static final String BATCH_KEY = "batchKey";

    private final RunTestContext runTestContext = new RunTestContext();

    @Test
    void testGetStoriesChain()
    {
        runTestContext.setTestContext(new SimpleTestContext());
        RunningStory rootStory = new RunningStory();
        runTestContext.putRunningStory(rootStory, false);
        RunningStory givenStory = new RunningStory();
        runTestContext.putRunningStory(givenStory, true);
        assertEquals(new LinkedList<>(List.of(givenStory, rootStory)), runTestContext.getStoriesChain());
    }

    @Test
    void testPutRunningStory()
    {
        runTestContext.setTestContext(new SimpleTestContext());
        RunningStory runningStory = new RunningStory();
        runTestContext.putRunningStory(runningStory, false);
        Assertions.assertEquals(runningStory, runTestContext.getRunningStory());
    }

    @Test
    void testRemoveRunningStory()
    {
        TestContext testContext = mock(TestContext.class);
        runTestContext.setTestContext(testContext);
        runTestContext.removeRunningStory(false);
        verifyNoInteractions(testContext);
    }

    @Test
    void testPutRunningGivenStory()
    {
        runTestContext.setTestContext(new SimpleTestContext());
        RunningStory runningStory = new RunningStory();
        runTestContext.putRunningStory(runningStory, true);
        Assertions.assertEquals(runningStory, runTestContext.getRunningStory());
    }

    @Test
    void testRemoveRunningGivenStory()
    {
        runTestContext.setTestContext(new SimpleTestContext());
        RunningStory runningStory = new RunningStory();
        runTestContext.putRunningStory(runningStory, true);
        runTestContext.removeRunningStory(true);
        assertNull(runTestContext.getRunningStory());
    }

    @Test
    void testGetDefaultRunningBatchKey()
    {
        assertEquals("batch-1", runTestContext.getRunningBatchKey());
    }

    @Test
    void testGetRunningBatchKey()
    {
        runTestContext.putRunningBatch(BATCH_KEY);
        assertEquals(BATCH_KEY, runTestContext.getRunningBatchKey());
    }

    @Test
    void testRemoveRunningBatchKey()
    {
        runTestContext.putRunningBatch(BATCH_KEY);
        runTestContext.removeRunningBatch();
        IllegalStateException exception = assertThrows(IllegalStateException.class, runTestContext::getRunningBatchKey);
        assertEquals("No running batch is found", exception.getMessage());
    }

    @Test
    void testSetDryRun()
    {
        runTestContext.setDryRun(true);
        assertTrue(runTestContext.isDryRun());
    }

    @Test
    void shouldCompleteRun()
    {
        assertFalse(runTestContext.isRunCompleted());
        runTestContext.completeRun();
        assertTrue(runTestContext.isRunCompleted());
    }
}
