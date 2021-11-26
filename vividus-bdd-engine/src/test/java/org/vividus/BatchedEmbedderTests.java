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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.EmbedderMonitor;
import org.jbehave.core.embedder.MetaFilter;
import org.jbehave.core.embedder.PerformableTree;
import org.jbehave.core.embedder.StoryManager;
import org.jbehave.core.embedder.StoryTimeouts.TimeoutParser;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.batch.BatchExecutionConfiguration;
import org.vividus.batch.BatchStorage;
import org.vividus.context.RunTestContext;
import org.vividus.context.VariableContext;
import org.vividus.spring.ExtendedConfiguration;

@ExtendWith(MockitoExtension.class)
class BatchedEmbedderTests
{
    private static final int THREADS = 2;
    private static final String PATH = "path1";
    private static final String BATCH = "batch-1";
    private static final String META_FILTERS = "groovy: !skip";

    @Mock
    private EmbedderMonitor embedderMonitor;

    @Mock
    private BatchStorage batchStorage;

    @Mock
    private RunTestContext runTestContext;

    @Mock
    private VariableContext variableContext;

    @Mock
    private StoryManager storyManager;

    @Test
    void testRunStoriesAsPathsIgnoreFailureInBatchesTrue()
    {
        BatchedEmbedder spy = createBatchedEmbedderSpy(false);
        MetaFilter mockedFilter = mock(MetaFilter.class);
        doReturn(mockedFilter).when(spy).metaFilter();
        List<String> testStoryPaths = List.of(PATH);
        String key = "key";
        Throwable throwable = mock(Throwable.class);
        doNothing().when(storyManager).runStoriesAsPaths(eq(testStoryPaths), eq(mockedFilter), argThat(failures -> {
            failures.put(key, throwable);
            return true;
        }));
        mockBatchExecutionConfiguration(true);
        Map<String, List<String>> batches = new LinkedHashMap<>();
        batches.put(BATCH, testStoryPaths);
        batches.put("batch-2", List.of("path2"));
        spy.runStoriesAsPaths(batches);
        InOrder ordered = inOrder(spy, embedderMonitor, storyManager, runTestContext, variableContext);
        ordered.verify(spy).processSystemProperties();
        ordered.verify(spy).useEmbedderControls(argThat(this::assertEmbedderControls));
        ordered.verify(spy).useMetaFilters(List.of(META_FILTERS));
        ordered.verify(embedderMonitor).usingControls(argThat(this::assertEmbedderControls));
        List<ExecutorService> service = new ArrayList<>(1);
        ordered.verify(spy).useExecutorService(argThat(service::add));
        ordered.verify(runTestContext).putRunningBatch(BATCH);
        ordered.verify(storyManager).runStoriesAsPaths(eq(testStoryPaths), eq(mockedFilter),
                argThat(failures -> failures.size() == 1 && failures.containsKey(key) && failures
                        .containsValue(throwable)));
        ordered.verify(variableContext).clearBatchVariables();
        ordered.verify(runTestContext).removeRunningBatch();
        ordered.verifyNoMoreInteractions();
        verifyExecutorService(service.get(0));
    }

    @Test
    void testRunStoriesAsPaths()
    {
        BatchedEmbedder spy = createBatchedEmbedderSpy(true);
        doNothing().when(spy).generateReportsView();
        MetaFilter mockedFilter = mock(MetaFilter.class);
        doReturn(mockedFilter).when(spy).metaFilter();
        List<String> testStoryPaths = List.of(PATH);
        EmbedderControls mockedEmbedderControls = mockEmbedderControls(spy);
        when(mockedEmbedderControls.threads()).thenReturn(THREADS);
        mockBatchExecutionConfiguration(false);
        spy.runStoriesAsPaths(Map.of(BATCH, testStoryPaths));
        InOrder ordered = inOrder(spy, embedderMonitor, storyManager, runTestContext, variableContext);
        ordered.verify(spy).processSystemProperties();
        ordered.verify(embedderMonitor).usingControls(mockedEmbedderControls);
        List<ExecutorService> service = new ArrayList<>(1);
        ordered.verify(spy).useExecutorService(argThat(service::add));
        ordered.verify(runTestContext).putRunningBatch(BATCH);
        ordered.verify(storyManager).runStoriesAsPaths(eq(testStoryPaths), eq(mockedFilter), any(BatchFailures.class));
        ordered.verify(variableContext).clearBatchVariables();
        ordered.verify(runTestContext).removeRunningBatch();
        ordered.verify(spy).generateReportsView();
        ordered.verifyNoMoreInteractions();
        verifyExecutorService(service.get(0));
    }

    private void verifyExecutorService(ExecutorService service)
    {
        ThreadPoolExecutor executorService = (ThreadPoolExecutor) service;
        assertEquals(THREADS, executorService.getCorePoolSize());
        assertTrue(executorService.isTerminated());
        assertTrue(executorService.isShutdown());
        assertThat(executorService.getThreadFactory(), instanceOf(BasicThreadFactory.class));
    }

    @Test
    void testRunStoriesAsPathsSkip()
    {
        BatchedEmbedder spy = createBatchedEmbedderSpy(false);
        EmbedderControls mockedEmbedderControls = mockEmbedderControls(spy);
        when(mockedEmbedderControls.threads()).thenReturn(THREADS);
        when(mockedEmbedderControls.skip()).thenReturn(true);
        List<String> testStoryPaths = List.of(PATH);
        mockBatchExecutionConfiguration(false);
        spy.runStoriesAsPaths(Map.of(BATCH, testStoryPaths));
        verify(spy).processSystemProperties();
        verify(embedderMonitor).usingControls(mockedEmbedderControls);
        verify(embedderMonitor).storiesSkipped(testStoryPaths);
    }

    @Test
    void testStoryManager()
    {
        ExtendedConfiguration mockedConfiguration = mock(ExtendedConfiguration.class);
        ExtendedStoryReporterBuilder mockedExtendedStoryReporterBuilder = mock(ExtendedStoryReporterBuilder.class);
        when(mockedConfiguration.storyReporterBuilder()).thenReturn(mockedExtendedStoryReporterBuilder);
        BatchedEmbedder embedder = new BatchedEmbedder(runTestContext, variableContext, batchStorage);
        embedder.setEmbedderMonitor(embedderMonitor);
        BatchedEmbedder spy = spy(embedder);
        spy.useConfiguration(mockedConfiguration);
        InjectableStepsFactory mockedInjectableStepsFactory = mock(InjectableStepsFactory.class);
        spy.useStepsFactory(mockedInjectableStepsFactory);
        EmbedderControls mockedEmbedderControls = mockEmbedderControls(spy);
        when(mockedEmbedderControls.threads()).thenReturn(THREADS);
        ExecutorService mockedExecutorService = mock(ExecutorService.class);
        spy.useExecutorService(mockedExecutorService);
        PerformableTree mockedPerformableTree = mock(BatchedPerformableTree.class);
        PerformableTree.PerformableRoot mockedPerformableRoot = mock(PerformableTree.PerformableRoot.class);
        when(mockedPerformableTree.getRoot()).thenReturn(mockedPerformableRoot);
        spy.usePerformableTree(mockedPerformableTree);
        TimeoutParser mockedTimeoutParser = mock(TimeoutParser.class);
        TimeoutParser[] timeoutParsers = { mockedTimeoutParser };
        spy.useTimeoutParsers(timeoutParsers);
        StoryManager expected = new StoryManager(mockedConfiguration, mockedInjectableStepsFactory,
                mockedEmbedderControls, embedderMonitor, mockedExecutorService, mockedPerformableTree, timeoutParsers);
        StoryManager actual = spy.storyManager();
        assertEquals(expected.performableRoot(), actual.performableRoot());
    }

    @Test
    void testPerformableTree()
    {
        BatchedEmbedder spy = createBatchedEmbedderSpy(false);
        BatchedPerformableTree mockedPerformableTree = mock(BatchedPerformableTree.class);
        spy.usePerformableTree(mockedPerformableTree);
        spy.performableTree();
        verify(mockedPerformableTree).setReportBeforeStories(true);
        verify(mockedPerformableTree).setReportAfterStories(false);
    }

    @Test
    void testSetPerformableTree()
    {
        BatchedEmbedder spy = createBatchedEmbedderSpy(false);
        PerformableTree mockedPerformableTree = mock(BatchedPerformableTree.class);
        spy.setPerformableTree(mockedPerformableTree);
        verify(spy).usePerformableTree(mockedPerformableTree);
    }

    private BatchedEmbedder createBatchedEmbedderSpy(boolean generateViewAfterBatches)
    {
        TestBatchedEmbedder embedder = new TestBatchedEmbedder(runTestContext, variableContext, batchStorage,
                storyManager);
        embedder.setEmbedderMonitor(embedderMonitor);
        embedder.setGenerateViewAfterBatches(generateViewAfterBatches);
        return spy(embedder);
    }

    private EmbedderControls mockEmbedderControls(BatchedEmbedder spy)
    {
        EmbedderControls mockedEmbedderControls = mock(EmbedderControls.class);
        doReturn(mockedEmbedderControls).when(spy).embedderControls();
        return mockedEmbedderControls;
    }

    private void mockBatchExecutionConfiguration(boolean failFast)
    {
        BatchExecutionConfiguration batchExecutionConfiguration = new BatchExecutionConfiguration();
        batchExecutionConfiguration.setStoryExecutionTimeout(Duration.ofHours(1));
        batchExecutionConfiguration.setMetaFilters(META_FILTERS);
        batchExecutionConfiguration.setThreads(2);
        batchExecutionConfiguration.setFailFast(failFast);
        when(batchStorage.getBatchExecutionConfiguration(BATCH)).thenReturn(batchExecutionConfiguration);
    }

    private boolean assertEmbedderControls(EmbedderControls controls)
    {
        return controls.threads() == THREADS
                && "3600".equals(controls.storyTimeouts())
                && controls.ignoreFailureInStories()
                && !controls.generateViewAfterStories();
    }

    @SuppressWarnings("checkstyle:FinalClass")
    private static class TestBatchedEmbedder extends BatchedEmbedder
    {
        private final StoryManager storyManagerToBeUsedAfterReset;

        private TestBatchedEmbedder(RunTestContext runTestContext, VariableContext variableContext,
                BatchStorage batchStorage, StoryManager storyManagerToBeUsedAfterReset)
        {
            super(runTestContext, variableContext, batchStorage);
            storyManager = mock(StoryManager.class);
            this.storyManagerToBeUsedAfterReset = storyManagerToBeUsedAfterReset;
        }

        @Override
        public StoryManager storyManager()
        {
            if (storyManager == null)
            {
                storyManager = storyManagerToBeUsedAfterReset;
            }
            return storyManager;
        }
    }
}
