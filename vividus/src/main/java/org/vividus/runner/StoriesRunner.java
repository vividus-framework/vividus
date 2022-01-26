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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jbehave.core.embedder.Embedder;
import org.jbehave.core.junit.JUnit4StoryRunner;
import org.junit.runner.RunWith;
import org.vividus.BatchedEmbedder;
import org.vividus.IBatchedPathFinder;
import org.vividus.configuration.BeanFactory;

@RunWith(JUnit4StoryRunner.class)
public class StoriesRunner extends AbstractTestRunner
{
    static
    {
        setRunnerClass(StoriesRunner.class);
    }

    private final BatchedEmbedder batchedEmbedder;
    private final IBatchedPathFinder batchedPathFinder;

    public StoriesRunner()
    {
        batchedPathFinder = BeanFactory.getBean(IBatchedPathFinder.class);
        batchedEmbedder = BeanFactory.getBean(BatchedEmbedder.class);
    }

    @Override
    public void run()
    {
        batchedEmbedder.runStoriesAsPaths(getPaths());
    }

    @Override
    public List<String> storyPaths()
    {
        return getPaths().values().stream().flatMap(List::stream).collect(Collectors.toList());
    }

    private Map<String, List<String>> getPaths()
    {
        try
        {
            return batchedPathFinder.getPaths();
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public Embedder configuredEmbedder()
    {
        return batchedEmbedder;
    }
}
