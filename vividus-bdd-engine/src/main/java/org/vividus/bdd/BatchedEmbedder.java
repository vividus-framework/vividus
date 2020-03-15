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

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.embedder.EmbedderControls;
import org.jbehave.core.embedder.EmbedderMonitor;
import org.jbehave.core.embedder.MetaFilter;
import org.jbehave.core.embedder.PerformableTree;
import org.jbehave.core.failures.BatchFailures;
import org.jbehave.core.steps.InjectableStepsFactory;
import org.vividus.bdd.batch.BatchExecutionConfiguration;
import org.vividus.bdd.batch.BatchStorage;
import org.vividus.bdd.context.BddRunContext;
import org.vividus.bdd.context.IBddVariableContext;

public class BatchedEmbedder extends Embedder
{
    private final BddRunContext bddRunContext;
    private final IBddVariableContext bddVariableContext;
    private final BatchStorage batchStorage;

    private boolean ignoreFailureInBatches;
    private boolean reportBeforeStories = true;
    private boolean reportAfterStories;
    private boolean generateViewAfterBatches;

    private String batch;

    public BatchedEmbedder(BddRunContext bddRunContext, IBddVariableContext bddVariableContext,
            BatchStorage batchStorage)
    {
        this.bddRunContext = bddRunContext;
        this.bddVariableContext = bddVariableContext;
        this.batchStorage = batchStorage;
    }

    public void runStoriesAsPaths(Map<String, List<String>> storyPathsBatches)
    {
        processSystemProperties();
        int batchesSize = storyPathsBatches.size();
        Iterator<Entry<String, List<String>>> iterator = storyPathsBatches.entrySet().iterator();
        generateViewAfterExecution(() ->
        {
            for (int i = 1; iterator.hasNext(); i++)
            {
                reportBeforeStories = i == 1;
                reportAfterStories = i == batchesSize;

                Entry<String, List<String>> storyPathsBatch = iterator.next();
                batch = storyPathsBatch.getKey();

                BatchExecutionConfiguration batchExecutionConfiguration = batchStorage.getBatchExecutionConfiguration(
                        batch);
                useEmbedderControls(createEmbedderControls(batchExecutionConfiguration));
                useMetaFilters(batchExecutionConfiguration.getMetaFilters());

                EmbedderControls embedderControls = embedderControls();
                embedderMonitor.usingControls(embedderControls);
                ExecutorService executorService = createExecutorService(embedderControls.threads());
                useExecutorService(executorService);

                List<String> storyPaths = storyPathsBatch.getValue();
                if (embedderControls.skip())
                {
                    embedderMonitor.storiesSkipped(storyPaths);
                    continue;
                }

                try
                {
                    bddRunContext.putRunningBatch(batch);

                    // JBehaveJUnitRunner may have already initialized StoryManager with default PerformableTree and
                    // EmebedderControls, so we need to reset it and new one will be created in storyManager()
                    storyManager = null;

                    MetaFilter filter = metaFilter();
                    BatchFailures failures = new BatchFailures(embedderControls.verboseFailures());

                    storyManager().runStoriesAsPaths(storyPaths, filter, failures);

                    handleFailures(failures);
                    if (!ignoreFailureInBatches && !failures.isEmpty())
                    {
                        break;
                    }
                }
                finally
                {
                    bddVariableContext.clearVariables();
                    bddRunContext.removeRunningBatch();
                    executorService.shutdownNow();
                    storyManager = null;
                }
            }
        });
    }

    private void generateViewAfterExecution(Runnable runnable)
    {
        try
        {
            runnable.run();
        }
        finally
        {
            if (generateViewAfterBatches)
            {
                generateReportsView();
            }
        }
    }

    @Override
    public PerformableTree performableTree()
    {
        BatchedPerformableTree performableTree = (BatchedPerformableTree) super.performableTree();
        performableTree.setReportBeforeStories(reportBeforeStories);
        performableTree.setReportAfterStories(reportAfterStories);
        performableTree.setIgnoreFailureInBatches(ignoreFailureInBatches);
        return performableTree;
    }

    private ExecutorService createExecutorService(int threads)
    {
        ThreadFactory threadFactory = new BasicThreadFactory.Builder()
                .namingPattern(batch + "-thread-%d")
                .build();
        return Executors.newFixedThreadPool(threads, threadFactory);
    }

    private EmbedderControls createEmbedderControls(BatchExecutionConfiguration batchExecutionConfiguration)
    {
        EmbedderControls embedderControls = new EmbedderControls();
        embedderControls.useStoryTimeouts(
                Long.toString(batchExecutionConfiguration.getStoryExecutionTimeout().toSeconds()));
        Optional.ofNullable(batchExecutionConfiguration.getThreads())
                .ifPresent(embedderControls::useThreads);
        embedderControls.doIgnoreFailureInStories(true);
        embedderControls.doGenerateViewAfterStories(false);
        return embedderControls;
    }

    public void setPerformableTree(PerformableTree performableTree)
    {
        usePerformableTree(performableTree);
    }

    public void setIgnoreFailureInBatches(boolean ignoreFailureInBatches)
    {
        this.ignoreFailureInBatches = ignoreFailureInBatches;
    }

    public void setConfiguration(Configuration configuration)
    {
        useConfiguration(configuration);
    }

    public void setStepFactory(InjectableStepsFactory stepsFactory)
    {
        useStepsFactory(stepsFactory);
    }

    public void setEmbedderMonitor(EmbedderMonitor embedderMonitor)
    {
        useEmbedderMonitor(embedderMonitor);
    }

    public void setGenerateViewAfterBatches(boolean generateViewAfterBatches)
    {
        this.generateViewAfterBatches = generateViewAfterBatches;
    }
}
