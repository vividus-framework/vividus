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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.vividus.examples.IExamplesTableLoader;
import org.vividus.resource.ResourceLoadException;
import org.vividus.steps.VariableResolver;

@ExtendWith(MockitoExtension.class)
class StoryLoaderTests
{
    @Mock private VariableResolver variableResolver;
    @Mock private ResourcePatternResolver resourcePatternResolver;
    @Mock private IExamplesTableLoader examplesTableLoader;
    @InjectMocks private StoryLoader storyLoader;

    private final ResourceLoader resourceLoader = new ClassRelativeResourceLoader(getClass());

    @BeforeEach
    void beforeEach()
    {
        storyLoader.setResourcePatternResolver(resourcePatternResolver);
        storyLoader.setExamplesTableLoader(examplesTableLoader);
    }

    @Test
    void testLoadResourceAsText()
    {
        var preProcessedStoryPath = "${unit}test.story";
        var storyPath = "unittest.story";
        when(variableResolver.resolve(preProcessedStoryPath)).thenReturn(storyPath);
        Resource resource = resourceLoader.getResource(storyPath);
        when(resourcePatternResolver.getResource(storyPath)).thenReturn(resource);
        String actual = storyLoader.loadResourceAsText(preProcessedStoryPath);
        assertEquals("unittest", actual.trim());
    }

    @Test
    void shouldThrowResourceLoadException() throws IOException
    {
        var resourcePath = "resourcePath";
        when(variableResolver.resolve(resourcePath)).thenReturn(resourcePath);
        Resource resource = mock(Resource.class);
        when(resourcePatternResolver.getResource(resourcePath)).thenReturn(resource);
        ResourceLoadException ioException = new ResourceLoadException("Resource IOException");
        when(resource.getInputStream()).thenThrow(ioException);
        assertThrows(ResourceLoadException.class, () -> storyLoader.loadResourceAsText(resourcePath));
    }

    @Test
    void testLoadStoryAsText()
    {
        var storyPath = "storyPath";
        var expected = "resource";
        StoryLoader spy = spy(storyLoader);
        doReturn(expected).when(spy).loadResourceAsText(storyPath);
        String actual = spy.loadStoryAsText(storyPath);
        assertEquals(expected, actual);
    }

    @Test
    void testLoadExamplesTableAsText()
    {
        var tablePath = "unittest.table";
        var tableContent = "tableContent";
        when(variableResolver.resolve(tablePath)).thenReturn(tablePath);
        when(examplesTableLoader.loadExamplesTable(tablePath)).thenReturn(tableContent);
        String actual = storyLoader.loadResourceAsText(tablePath);
        assertEquals(tableContent, actual);
    }
}
