/*
 * Copyright 2019-2023 the original author or authors.
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

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.embedder.PerformableTree;
import org.jbehave.core.embedder.PerformableTree.PerformableStory;
import org.jbehave.core.io.StoryLocation;
import org.jbehave.core.reporters.FilePrintStreamFactory.ResolveToSimpleName;
import org.vividus.context.RunContext;

public class ResolveToUniqueSimpleName extends ResolveToSimpleName
{
    private static final char UNIX_PATH_SEPARATOR = '/';

    private final RunContext runContext;
    private final PerformableTree performableTree;

    public ResolveToUniqueSimpleName(RunContext runContext, PerformableTree performableTree)
    {
        this.runContext = runContext;
        this.performableTree = performableTree;
    }

    @Override
    public String resolveName(StoryLocation storyLocation, String extension)
    {
        String storyPath = storyLocation.getStoryPath();

        if ("BeforeStories".equals(storyPath) || "AfterStories".equals(storyPath))
        {
            return super.resolveName(storyLocation, extension);
        }

        String storyName = performableTree.getRoot().getStories().stream()
                           .map(PerformableStory::getStory)
                           .filter(s -> s.getPath().equals(storyPath))
                           .findFirst().get().getName();
        String storyNameOutput = StringUtils.removeEnd(storyName, "story")
                                            .replace(UNIX_PATH_SEPARATOR, '.') + extension;

        return runContext.getRunningBatchKey() + File.separator + storyNameOutput;
    }
}
