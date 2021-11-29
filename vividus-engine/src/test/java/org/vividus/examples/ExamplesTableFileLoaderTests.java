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

package org.vividus.examples;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassRelativeResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.vividus.resource.ResourceLoadException;
import org.vividus.resource.TestResourceLoader;

@ExtendWith(MockitoExtension.class)
class ExamplesTableFileLoaderTests
{
    private static final String TABLE_FILENAME = "unittest.table";
    private static final String TABLE_CONTENT = "|header1|";

    @Mock private TestResourceLoader testResourceLoader;
    @InjectMocks private ExamplesTableFileLoader examplesTableFileLoader;

    private final ResourceLoader resourceLoader = new ClassRelativeResourceLoader(getClass());

    @Test
    void testLoadExamplesTable()
    {
        Resource resource = resourceLoader.getResource(TABLE_FILENAME);
        when(testResourceLoader.getResource(TABLE_FILENAME)).thenReturn(resource);
        String actual = examplesTableFileLoader.loadExamplesTable(TABLE_FILENAME);
        assertEquals(TABLE_CONTENT, actual.trim());
    }

    @Test
    void shouldCacheExamplesTableByPath()
    {
        Resource resource = resourceLoader.getResource(TABLE_FILENAME);
        when(testResourceLoader.getResource(TABLE_FILENAME)).thenReturn(resource);
        examplesTableFileLoader.setCacheTables(true);
        assertEquals(TABLE_CONTENT, examplesTableFileLoader.loadExamplesTable(TABLE_FILENAME).trim());
        assertEquals(TABLE_CONTENT, examplesTableFileLoader.loadExamplesTable(TABLE_FILENAME).trim());
        verify(testResourceLoader).getResource(TABLE_FILENAME);
    }

    @Test
    void testLoadExamplesTableWithIOException() throws IOException
    {
        var resourcePath = "resourcePath";
        Resource resource = mock(Resource.class);
        when(testResourceLoader.getResource(resourcePath)).thenReturn(resource);
        String exceptionMessage = "Resource IOException";
        IOException ioException = new IOException(exceptionMessage);
        when(resource.getInputStream()).thenThrow(ioException);
        ResourceLoadException exception = assertThrows(ResourceLoadException.class,
            () -> examplesTableFileLoader.loadExamplesTable(resourcePath));
        assertEquals(exceptionMessage, exception.getMessage());
        assertEquals(ioException, exception.getCause());
    }
}
