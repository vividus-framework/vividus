/*
 * Copyright 2019 the original author or authors.
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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.jbehave.core.embedder.EmbedderControls;
import org.vividus.util.property.IPropertyMapper;

public class EmbedderControlsProvider implements IEmbedderControlsProvider
{
    private static final String PROPERTIES_PREFIX = "bdd.batch-";
    private static final String DEFAULT_BATCH = "batch-1";

    private final Map<String, Integer> threads = new HashMap<>();
    private String storyExecutionTimeout;
    private boolean ignoreFailureInStories;
    private boolean generateViewAfterStories;

    private final Map<String, EmbedderControls> batchedControls = new HashMap<>();

    private IPropertyMapper propertyMapper;

    public void init() throws IOException
    {
        propertyMapper.readValues(PROPERTIES_PREFIX, Map.class)
                .forEach((k, v) -> threads.put("batch-" + k, NumberUtils.createInteger((String) v.get("threads"))));
    }

    @Override
    public EmbedderControls get(String batch)
    {
        if (batchedControls.containsKey(batch))
        {
            return batchedControls.get(batch);
        }
        EmbedderControls controls = new EmbedderControls();
        if (threads.containsKey(batch))
        {
            controls.useThreads(threads.get(batch));
        }
        controls.useStoryTimeouts(storyExecutionTimeout);
        controls.doIgnoreFailureInStories(ignoreFailureInStories);
        controls.doGenerateViewAfterStories(generateViewAfterStories);
        batchedControls.put(batch, controls);
        return controls;
    }

    @Override
    public EmbedderControls getDefault()
    {
        return get(DEFAULT_BATCH);
    }

    public void setStoryExecutionTimeout(String storyExecutionTimeout)
    {
        this.storyExecutionTimeout = storyExecutionTimeout;
    }

    public void setIgnoreFailureInStories(boolean ignoreFailureInStories)
    {
        this.ignoreFailureInStories = ignoreFailureInStories;
    }

    public void setGenerateViewAfterStories(boolean generateViewAfterStories)
    {
        this.generateViewAfterStories = generateViewAfterStories;
    }

    public void setPropertyMapper(IPropertyMapper propertyMapper)
    {
        this.propertyMapper = propertyMapper;
    }
}
