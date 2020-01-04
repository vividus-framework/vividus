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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;

import org.jbehave.core.embedder.EmbedderControls;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.util.property.IPropertyMapper;

@ExtendWith(MockitoExtension.class)
class EmbedderControlsProviderTests
{
    private static final String STORY_EXECUTION_TIMEOUT = "testStoryTimeouts";
    private static final String BATCH = "batch-1";

    @Mock
    private IPropertyMapper propertyMapper;

    @InjectMocks
    private EmbedderControlsProvider embedderControlsProvider;

    @Test
    void testGet() throws IOException
    {
        when(propertyMapper.readValues("bdd.batch-", Map.class))
                .thenReturn(Map.of("1", Map.of("threads", "2")));
        embedderControlsProvider.setStoryExecutionTimeout(STORY_EXECUTION_TIMEOUT);
        embedderControlsProvider.setGenerateViewAfterBatches(true);
        embedderControlsProvider.setIgnoreFailureInStories(true);
        embedderControlsProvider.init();
        assertEmbedderControls(2, true, true, embedderControlsProvider.get(BATCH));
    }

    @Test
    void testGetDefault()
    {
        embedderControlsProvider.setStoryExecutionTimeout(STORY_EXECUTION_TIMEOUT);
        assertEmbedderControls(1, false, false, embedderControlsProvider.getDefault());
    }

    @Test
    void testGetAgain()
    {
        assertEquals(embedderControlsProvider.getDefault(), embedderControlsProvider.get(BATCH));
    }

    @Test
    void testGenerateViewAfterBatches()
    {
        assertFalse(embedderControlsProvider.isGenerateViewAfterBatches());
        embedderControlsProvider.setGenerateViewAfterBatches(true);
        assertTrue(embedderControlsProvider.isGenerateViewAfterBatches());
    }

    private void assertEmbedderControls(int expectedThreads, boolean expectedGenerateViewAfterStories,
            boolean expectedIgnoreFailureInStories, EmbedderControls actual)
    {
        assertEquals(expectedThreads, actual.threads());
        assertEquals(STORY_EXECUTION_TIMEOUT, actual.storyTimeouts());
        assertEquals(expectedGenerateViewAfterStories, actual.generateViewAfterStories());
        assertEquals(expectedIgnoreFailureInStories, actual.ignoreFailureInStories());
    }
}
