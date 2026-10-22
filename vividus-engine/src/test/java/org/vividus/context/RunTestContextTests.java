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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.LinkedList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.vividus.batch.BatchConfiguration;
import org.vividus.model.RunningStory;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.testcontext.TestContext;

class RunTestContextTests
{
    private static final String NO_RUNNING_BATCH = "No running batch is found";
    private static final String BATCH_1_KEY = "batch-1";

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
        assertEquals(runningStory, runTestContext.getRunningStory());
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
        assertEquals(runningStory, runTestContext.getRunningStory());
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
    void testGetDefaultRunningBatch()
    {
        assertEquals(BATCH_1_KEY, runTestContext.getRunningBatch().getBatchKey());
        assertEquals(1, runTestContext.getRunningBatch().getBatchIndex());
        assertNull(runTestContext.getRunningBatch().getBatchConfiguration());
    }

    @Test
    void testGetRunningBatch()
    {
        String batchKey = "batch-42";
        BatchConfiguration batchConfiguration = new BatchConfiguration();
        runTestContext.putRunningBatch(batchKey, batchConfiguration);
        var runningBatch = runTestContext.getRunningBatch();
        assertEquals(batchKey, runningBatch.getBatchKey());
        assertEquals(42, runningBatch.getBatchIndex());
        assertEquals(batchConfiguration, runningBatch.getBatchConfiguration());
    }

    @Test
    void testRemoveRunningBatch()
    {
        runTestContext.putRunningBatch(BATCH_1_KEY, new BatchConfiguration());
        runTestContext.removeRunningBatch();
        IllegalStateException exception = assertThrows(IllegalStateException.class, runTestContext::getRunningBatch);
        assertEquals(NO_RUNNING_BATCH, exception.getMessage());
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
        runTestContext.startRun();
        assertTrue(runTestContext.isRunInProgress());
        runTestContext.completeRun();
        assertFalse(runTestContext.isRunInProgress());
    }
}
