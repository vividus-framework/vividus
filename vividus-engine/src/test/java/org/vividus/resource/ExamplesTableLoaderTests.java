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

package org.vividus.resource;

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

@ExtendWith(MockitoExtension.class)
class ExamplesTableLoaderTests
{
    private static final String TABLE_CONTENT = "|header1|";
    private static final String TABLE_FILENAME = "unittest.table";

    @Mock private TestResourceLoader testResourceLoader;
    @InjectMocks private ExamplesTableLoader examplesTableLoader;

    private final ResourceLoader resourceLoader = new ClassRelativeResourceLoader(getClass());

    @Test
    void shouldLoadExamplesTable()
    {
        var fullPath = "/org/vividus/resource/" + TABLE_FILENAME;
        var resource = resourceLoader.getResource(fullPath);
        when(testResourceLoader.getResources("/org/vividus/resource", TABLE_FILENAME)).thenReturn(
                new Resource[] { resource });
        var actual = examplesTableLoader.loadExamplesTable(fullPath);
        assertEquals(TABLE_CONTENT, actual.trim());
    }

    @Test
    void shouldCacheExamplesTableByPath()
    {
        var resource = resourceLoader.getResource(TABLE_FILENAME);
        when(testResourceLoader.getResources("", TABLE_FILENAME)).thenReturn(new Resource[] { resource });
        examplesTableLoader.setCacheTables(true);
        assertEquals(TABLE_CONTENT, examplesTableLoader.loadExamplesTable(TABLE_FILENAME).trim());
        assertEquals(TABLE_CONTENT, examplesTableLoader.loadExamplesTable(TABLE_FILENAME).trim());
        verify(testResourceLoader).getResources("", TABLE_FILENAME);
    }

    @Test
    void shouldFailWhenIOExceptionOccurred() throws IOException
    {
        var resourcePath = "/broken/invalid.table";
        var resource = mock(Resource.class);
        when(testResourceLoader.getResources("/broken", "invalid.table")).thenReturn(new Resource[] { resource });
        var exceptionMessage = "Resource IOException";
        var ioException = new IOException(exceptionMessage);
        when(resource.getInputStream()).thenThrow(ioException);
        var exception = assertThrows(ResourceLoadException.class,
                () -> examplesTableLoader.loadExamplesTable(resourcePath));
        assertEquals(exceptionMessage, exception.getMessage());
        assertEquals(ioException, exception.getCause());
    }

    @Test
    void shouldFailWhenMoreThanOneExamplesTableFound()
    {
        var resourcePath = "/tables/*.table";
        var resource = mock(Resource.class);
        when(testResourceLoader.getResources("/tables", "*.table")).thenReturn(
                new Resource[] { resource, resource });
        var exception = assertThrows(ResourceLoadException.class,
                () -> examplesTableLoader.loadExamplesTable(resourcePath));
        assertEquals("More than 1 ExamplesTable resource is found for " + resourcePath, exception.getMessage());
    }

    @Test
    void shouldFailWhenNoExamplesTableFound()
    {
        var resourcePath = "/missing/unknown.table";
        when(testResourceLoader.getResources("/missing", "unknown.table")).thenReturn(new Resource[] {});
        var exception = assertThrows(ResourceLoadException.class,
                () -> examplesTableLoader.loadExamplesTable(resourcePath));
        assertEquals("No ExamplesTable resource is found for " + resourcePath, exception.getMessage());
    }
}
