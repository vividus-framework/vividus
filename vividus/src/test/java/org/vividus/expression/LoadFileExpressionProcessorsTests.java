/*
 * Copyright 2019-2026 the original author or authors.
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

package org.vividus.expression;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mockito;

class LoadFileExpressionProcessorsTests
{
    private static final String LOAD_FILE = "loadFile(%s)";
    private static final String LOAD_BINARY_FILE = "loadBinaryFile(%s)";
    private static final String ERROR_MESSAGE = "The file by '%s' path does not exist or is a directory";

    private final LoadFileExpressionProcessors processors = new LoadFileExpressionProcessors();

    @Test
    void shouldReadFileAsString(@TempDir Path tempDir) throws IOException
    {
        var content = "hello world";
        var file = tempDir.resolve("test.txt").toFile();
        FileUtils.writeStringToFile(file, content, StandardCharsets.UTF_8);
        assertEquals(content, processors.execute(LOAD_FILE.formatted(file.getAbsolutePath())).get());
    }

    @Test
    void shouldReadFileAsBinary(@TempDir Path tempDir) throws IOException
    {
        var content = new byte[] { 1, 2, 3 };
        var file = tempDir.resolve("test.bin").toFile();
        FileUtils.writeByteArrayToFile(file, content);
        assertArrayEquals(content,
                processors.execute(LOAD_BINARY_FILE.formatted(file.getAbsolutePath())).map(byte[].class::cast).get());
    }

    @Test
    void shouldThrowExceptionWhenFileDoesNotExist(@TempDir Path tempDir)
    {
        var path = tempDir.resolve("missing.txt").toFile().getAbsolutePath();
        var exception = assertThrows(IllegalArgumentException.class,
                () -> processors.execute(LOAD_FILE.formatted(path)));
        assertEquals(ERROR_MESSAGE.formatted(path), exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenPathIsDirectory(@TempDir Path tempDir)
    {
        var path = tempDir.toFile().getAbsolutePath();
        var exception = assertThrows(IllegalArgumentException.class,
                () -> processors.execute(LOAD_FILE.formatted(path)));
        assertEquals(ERROR_MESSAGE.formatted(path), exception.getMessage());
    }

    @Test
    void shouldWrapIoException(@TempDir Path tempDir) throws IOException
    {
        var file = tempDir.resolve("exception.txt").toFile();
        file.createNewFile();
        var ioe = new IOException();
        try (var fileUtils = Mockito.mockStatic(FileUtils.class))
        {
            fileUtils.when(() -> FileUtils.readFileToString(file, StandardCharsets.UTF_8)).thenThrow(ioe);
            var uioe = assertThrows(UncheckedIOException.class,
                    () -> processors.execute(LOAD_FILE.formatted(file.getAbsolutePath())));
            assertEquals(ioe, uioe.getCause());
        }
    }
}
