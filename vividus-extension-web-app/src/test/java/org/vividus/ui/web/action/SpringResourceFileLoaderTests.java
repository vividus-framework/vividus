/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.action;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class SpringResourceFileLoaderTests
{
    private static final String FILE_PATH = "filePath";
    private static final String ABSOLUTE_PATH = "file:/D:/file.txt";
    private static final String JAR_ARCHIVE_FILE_TXT = "jar:file:/D:/archive.jar!/file.txt";

    @Mock private ResourceLoader resourceLoader;
    @Mock private Resource resource;
    @Mock private File file;

    @InjectMocks private SpringResourceFileLoader resourceFileLoader;

    @BeforeEach
    void setUp() throws IOException
    {
        Mockito.when(resource.exists()).thenReturn(true);
        Mockito.when(resource.getURL()).thenReturn(URI.create(ABSOLUTE_PATH).toURL());
        Mockito.when(resourceLoader.getResource(ResourceLoader.CLASSPATH_URL_PREFIX + FILE_PATH)).thenReturn(resource);
    }

    @Test
    void shouldLoadFileFromClasspath() throws IOException
    {
        Mockito.when(file.exists()).thenReturn(true);
        Mockito.when(resource.exists()).thenReturn(true);
        Mockito.when(resource.getFile()).thenReturn(file);
        File actualFile = resourceFileLoader.loadFile(FILE_PATH);
        Assertions.assertEquals(file, actualFile);
    }

    @Test
    void shouldLoadFileFromFileSystem() throws IOException
    {
        Mockito.when(resource.exists()).thenReturn(false);
        Mockito.when(resource.getFile()).thenReturn(file);
        Mockito.when(resourceLoader.getResource(ResourceUtils.FILE_URL_PREFIX + FILE_PATH)).thenReturn(resource);
        Mockito.when(file.exists()).thenReturn(true);
        File actualFile = resourceFileLoader.loadFile(FILE_PATH);
        Assertions.assertEquals(file, actualFile);
    }

    @Test
    void shouldThrowExceptionWhenFileDoesNotExist() throws IOException
    {
        Mockito.when(resource.getFile()).thenReturn(file);
        Mockito.when(file.exists()).thenReturn(false);
        var exception = Assertions.assertThrows(IllegalArgumentException.class,
                () -> resourceFileLoader.loadFile(FILE_PATH));
        Assertions.assertEquals("File filePath exists", exception.getMessage());
    }

    @Test
    void shouldLoadFileFromJar() throws IOException
    {
        try (var fileUtils = Mockito.mockStatic(FileUtils.class);
                var fileMock = Mockito.mockConstruction(File.class, (mock, context) -> {
                    Assertions.assertEquals(List.of(FILE_PATH), context.arguments());
                    Mockito.when(mock.exists()).thenReturn(true);
                    Mockito.when(mock.getAbsolutePath()).thenReturn(ABSOLUTE_PATH);
                }))
        {
            Mockito.when(resource.getURL()).thenReturn(URI.create(JAR_ARCHIVE_FILE_TXT).toURL());
            InputStream inputStream = new ByteArrayInputStream("text".getBytes(StandardCharsets.UTF_8));
            Mockito.when(resource.getInputStream()).thenReturn(inputStream);
            resourceFileLoader.loadFile(FILE_PATH);
            var unpackedFile = fileMock.constructed().get(0);
            fileUtils.verify(() -> FileUtils.copyInputStreamToFile(resource.getInputStream(), unpackedFile));
        }
    }
}
