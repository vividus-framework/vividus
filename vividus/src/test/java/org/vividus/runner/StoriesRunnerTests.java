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

package org.vividus.runner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.function.FailableConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.BatchedEmbedder;
import org.vividus.IBatchedPathFinder;
import org.vividus.configuration.BeanFactory;
import org.vividus.configuration.Vividus;
import org.vividus.log.TestInfoLogger;

@ExtendWith(MockitoExtension.class)
class StoriesRunnerTests
{
    private static final Map<String, List<String>> BATCH_TO_PATHS = Map.of("batch-1",
            List.of("path-1", "path-2", "path-3"));

    @Mock private BatchedEmbedder batchedEmbedder;
    @Mock private IBatchedPathFinder batchedPathFinder;

    @Test
    void shouldRun() throws IOException
    {
        performTest(testInfoLogger ->
        {
            StoriesRunner runner = new StoriesRunner();

            runner.run();

            verify(batchedEmbedder).runStoriesAsPaths(BATCH_TO_PATHS);
            testInfoLogger.verify(() -> TestInfoLogger.logExecutionPlan(BATCH_TO_PATHS));
        });
    }

    @Test
    void shouldGetStoryPaths() throws IOException
    {
        performTest(testInfoLogger ->
        {
            StoriesRunner runner = new StoriesRunner();

            assertEquals(BATCH_TO_PATHS.values().iterator().next(), runner.storyPaths());
        });
    }

    private void performTest(FailableConsumer<MockedStatic<TestInfoLogger>, IOException> test) throws IOException
    {
        try (MockedStatic<Vividus> vividus = mockStatic(Vividus.class);
                MockedStatic<BeanFactory> beanFactory = mockStatic(BeanFactory.class);
                MockedStatic<TestInfoLogger> testInfoLogger = mockStatic(TestInfoLogger.class))
        {
            beanFactory.when(() -> BeanFactory.getBean(IBatchedPathFinder.class)).thenReturn(batchedPathFinder);
            when(batchedPathFinder.getPaths()).thenReturn(BATCH_TO_PATHS);
            beanFactory.when(() -> BeanFactory.getBean(BatchedEmbedder.class)).thenReturn(batchedEmbedder);
            Properties springProperties = mock(Properties.class);
            beanFactory.when(() -> BeanFactory.getBean("properties", Properties.class)).thenReturn(springProperties);

            test.accept(testInfoLogger);
        }
    }
}
