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

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.jbehave.core.io.CodeLocations;
import org.jbehave.core.reporters.DelegatingStoryReporter;
import org.jbehave.core.reporters.FilePrintStreamFactory.FilePathResolver;
import org.jbehave.core.reporters.Format;
import org.jbehave.core.reporters.StoryReporter;
import org.jbehave.core.reporters.StoryReporterBuilder;

public class ExtendedStoryReporterBuilder extends StoryReporterBuilder
{
    private static final ThreadLocal<Map<String, StoryReporter>> STORY_REPORTERS = new ThreadLocal<>();
    private StoryReporter storyReporter;

    @Override
    public StoryReporter build(String storyPath)
    {
        if (STORY_REPORTERS.get() == null || !STORY_REPORTERS.get().containsKey(storyPath))
        {
            StoryReporter reporter = new DelegatingStoryReporter(super.build(storyPath), storyReporter);
            STORY_REPORTERS.set(Map.of(storyPath, reporter));
        }
        return STORY_REPORTERS.get().get(storyPath);
    }

    public void setStoryReporter(StoryReporter storyReporter)
    {
        this.storyReporter = storyReporter;
    }

    public void setFormats(List<Format> formats)
    {
        if (formats != null)
        {
            withFormats(formats.toArray(new Format[0]));
        }
    }

    public void setCodeLocation(String codeLocation) throws IOException
    {
        FileUtils.forceMkdir(new File(codeLocation));
        withCodeLocation(CodeLocations.codeLocationFromPath(codeLocation));
    }

    public void setReportFailureTrace(boolean reportFailureTrace)
    {
        withFailureTrace(reportFailureTrace);
    }

    public void setPathResolver(FilePathResolver pathResolver)
    {
        withPathResolver(pathResolver);
    }
}
