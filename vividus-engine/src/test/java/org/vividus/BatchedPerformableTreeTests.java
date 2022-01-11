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

package org.vividus;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.jbehave.core.embedder.PerformableTree.RunContext;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.steps.StepCollector.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BatchedPerformableTreeTests
{
    private static final String AFTER_STORIES = "AfterStories";

    private final BatchedPerformableTree batchedPerformableTree = new BatchedPerformableTree();

    @Mock
    private RunContext runContext;

    @Test
    void testPerformBeforeOrAfterStoriesPerformBefore()
    {
        mockRunContext();
        batchedPerformableTree.setReportBeforeStories(true);
        batchedPerformableTree.performBeforeOrAfterStories(runContext, Stage.BEFORE);
        verify(runContext).currentPath("BeforeStories");
    }

    @Test
    void testPerformBeforeOrAfterStoriesPerformAfter()
    {
        mockRunContext();
        batchedPerformableTree.setReportAfterStories(true);
        batchedPerformableTree.performBeforeOrAfterStories(runContext, Stage.AFTER);
        verify(runContext).currentPath(AFTER_STORIES);
    }

    @Test
    void testPerformBeforeOrAfterStoriesPerformAfterDontIgnoreFailureInBatches()
    {
        mockRunContext();
        when(runContext.getFailures()).thenReturn(getFailures());
        batchedPerformableTree.setFailFast(true);
        batchedPerformableTree.setReportAfterStories(false);
        batchedPerformableTree.performBeforeOrAfterStories(runContext, Stage.AFTER);
        verify(runContext).currentPath(AFTER_STORIES);
    }

    @Test
    void testPerformBeforeOrAfterStoriesPerformAfterFailureIgnoreFailureInBatches()
    {
        batchedPerformableTree.setFailFast(false);
        testPerformBeforeOrAfterStoriesPerformNone(Stage.AFTER);
    }

    @Test
    void testPerformBeforeOrAfterStoriesPerformNoneForBefore()
    {
        testPerformBeforeOrAfterStoriesPerformNone(Stage.BEFORE);
    }

    @Test
    void testPerformBeforeOrAfterStoriesPerformNoneForAfter()
    {
        batchedPerformableTree.setFailFast(false);
        testPerformBeforeOrAfterStoriesPerformNone(Stage.AFTER);
    }

    private BatchFailures getFailures()
    {
        BatchFailures failures = new BatchFailures();
        failures.put("key", mock(Throwable.class));
        return failures;
    }

    private void testPerformBeforeOrAfterStoriesPerformNone(Stage stage)
    {
        batchedPerformableTree.performBeforeOrAfterStories(runContext, stage);
        verifyNoInteractions(runContext);
    }

    private void mockRunContext()
    {
        when(runContext.reporter()).thenReturn(mock(StoryReporter.class));
    }
}
