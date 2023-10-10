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

package org.vividus.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;
import org.springframework.context.event.ContextStartedEvent;

class StartContextListenerTests
{
    private final StartContextListener startContextListener = new StartContextListener();

    @Test
    void shouldCleanCleanableDirectories(@TempDir Path tempDir) throws IOException
    {
        Files.createDirectories(tempDir.resolve("directory-to-remove"));
        Files.createFile(tempDir.resolve("file-to-remove"));
        startContextListener.setCleanableDirectories(Optional.of(List.of(tempDir.toFile())));
        startContextListener.onApplicationEvent(mock(ContextStartedEvent.class));
        assertTrue(Files.exists(tempDir));
        assertEquals(0, Files.list(tempDir).count());
    }

    @Test
    void shouldIgnoreNonExistentCleanableDirectories(@TempDir Path tempDir)
    {
        startContextListener.setCleanableDirectories(Optional.of(List.of(tempDir.resolve("non-existent").toFile())));
        startContextListener.onApplicationEvent(mock(ContextStartedEvent.class));
        assertTrue(Files.exists(tempDir));
    }

    @Test
    void shouldThrowIllegalStateExceptionIfErrorIsOccurredAtFileRemoval(@TempDir Path tempDir) throws IOException
    {
        Path dir = tempDir.resolve("will-fail-to-delete");
        Files.createDirectories(dir);
        File cleanableDirectory = dir.toFile();
        try (MockedStatic<FileUtils> fileUtils = mockStatic(FileUtils.class))
        {
            IOException ioException = new IOException();
            fileUtils.when(() -> FileUtils.cleanDirectory(cleanableDirectory)).thenThrow(ioException);
            startContextListener.setCleanableDirectories(Optional.of(List.of(cleanableDirectory)));
            IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
                    () -> startContextListener.onApplicationEvent(mock(ContextStartedEvent.class)));
            assertEquals(ioException, illegalStateException.getCause());
        }
        assertTrue(Files.exists(tempDir));
    }
}
