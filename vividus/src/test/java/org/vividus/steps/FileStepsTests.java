/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class FileStepsTests
{
    private static final String FILE_CONTENT = "content";
    private static final String FILE_DOES_NOT_EXIST = "File '%s' does not exist";
    private static final String FILE_NAME = "test.txt";

    @Mock
    private VariableContext variableContext;

    @Mock
    private ISoftAssert softAssert;

    @InjectMocks
    private FileSteps fileSteps;

    @Test
    void testSaveResponseBodyToFile() throws IOException
    {
        String pathVariable = "path";
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        fileSteps.createTemporaryFile(FILE_NAME, new DataWrapper(FILE_CONTENT), scopes, pathVariable);
        verify(variableContext).putVariable(eq(scopes), eq(pathVariable), argThat(path ->
        {
            try
            {
                return FILE_CONTENT.equals(FileUtils.readFileToString(new File((String) path), StandardCharsets.UTF_8));
            }
            catch (IOException e)
            {
                throw new UncheckedIOException(e);
            }
        }));
    }

    @Test
    void testSaveStringVariableToFile(@TempDir Path tempDir) throws IOException
    {
        String tempFilePath = tempDir.resolve("temp").resolve("any.txt").toString();
        fileSteps.createFile(FILE_CONTENT, tempFilePath);
        assertEquals(FILE_CONTENT, FileUtils.readFileToString(new File(tempFilePath), StandardCharsets.UTF_8));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = "   ")
    void shouldFailOnInvalidPath(String path)
    {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> fileSteps.assertPathExists(path)
        );
        assertEquals("File path must not be null, empty or blank", exception.getMessage());
    }

    @Test
    void testShouldPassWhenPathExists(@TempDir Path tempDir) throws IOException
    {
        Path filePath = tempDir.resolve(FILE_NAME);
        Files.writeString(filePath, FILE_CONTENT, StandardCharsets.UTF_8);
        fileSteps.assertPathExists(filePath.toString());
        verify(softAssert)
                .recordPassedAssertion("File '" + filePath + "' exists");
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void testShouldFailWhenPathDoesNotExist(@TempDir Path tempDir)
    {
        Path invalidFilePath = tempDir.resolve("non-existent.txt");
        fileSteps.assertPathExists(invalidFilePath.toString());
        verify(softAssert).recordFailedAssertion(String.format(FILE_DOES_NOT_EXIST, invalidFilePath));
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void testShouldFailWhenPathIsDirectory(@TempDir Path tempDir) throws IOException
    {
        Path dirPath = tempDir.resolve("test-directory");
        Files.createDirectory(dirPath);
        fileSteps.assertPathExists(dirPath.toString());
        verify(softAssert).recordFailedAssertion(String.format(FILE_DOES_NOT_EXIST, dirPath));
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void shouldFailWhenPathIsInvalid()
    {
        String invalidPath = "invalid\0path";
        fileSteps.assertPathExists(invalidPath);
        verify(softAssert).recordFailedAssertion(startsWith("Invalid path '" + invalidPath + "':"));
        verifyNoMoreInteractions(softAssert);
    }
}
