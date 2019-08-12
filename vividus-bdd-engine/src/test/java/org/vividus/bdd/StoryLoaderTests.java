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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.vividus.bdd.examples.IExamplesTableLoader;
import org.vividus.bdd.resource.ResourceLoadException;

@ExtendWith(MockitoExtension.class)
class StoryLoaderTests
{
    private static final String STORY_FILENAME = "unittest.story";
    private static final String STORY_CONTENT = "unittest";

    @Mock
    private ResourcePatternResolver resourcePatternResolver;

    @Mock
    private IExamplesTableLoader examplesTableLoader;

    @InjectMocks
    private StoryLoader storyLoader;

    private final ResourceLoader resourceLoader = new ClassRelativeResourceLoader(getClass());

    @Test
    void testLoadResourceAsText()
    {
        Resource resource = resourceLoader.getResource(STORY_FILENAME);
        when(resourcePatternResolver.getResource(STORY_FILENAME)).thenReturn(resource);
        String actual = storyLoader.loadResourceAsText(STORY_FILENAME);
        assertEquals(STORY_CONTENT, actual.trim());
    }

    @Test
    void shouldThrowResourceLoadException() throws IOException
    {
        String resourcePath = "resourcePath";
        Resource resource = mock(Resource.class);
        when(resourcePatternResolver.getResource(resourcePath)).thenReturn(resource);
        ResourceLoadException ioException = new ResourceLoadException("Resource IOException");
        when(resource.getInputStream()).thenThrow(ioException);
        assertThrows(ResourceLoadException.class, () -> storyLoader.loadResourceAsText(resourcePath));
    }

    @Test
    void testLoadStoryAsText()
    {
        StoryLoader spy = Mockito.spy(storyLoader);
        String storyPath = "storyPath";
        String expected = "resource";
        doReturn(expected).when(spy).loadResourceAsText(storyPath);
        String actual = spy.loadStoryAsText(storyPath);
        assertEquals(expected, actual);
    }

    @Test
    void testLoadExamplesTableAsText()
    {
        String tablePath = "unittest.table";
        String tableContent = "tableContent";
        when(examplesTableLoader.loadExamplesTable(tablePath)).thenReturn(tableContent);
        String actual = storyLoader.loadResourceAsText(tablePath);
        assertEquals(tableContent, actual);
    }
}
