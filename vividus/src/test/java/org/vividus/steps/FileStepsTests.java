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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class FileStepsTests
{
    private static final String FILE_CONTENT = "content";
    private static final String FILE_DOES_NOT_EXIST = "Path '%s' does not exist";
    private static final String FILE_NAME = "test.txt";
    private static final String FILE_PATH_MUST_NOT_BE_NULL_EMPTY_OR_BLANK = "Path must not be null, empty or blank";

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

    @Test
    void testShouldFailOnNullPath()
    {
        fileSteps.assertPathExists(null);
        verify(softAssert).recordFailedAssertion(FILE_PATH_MUST_NOT_BE_NULL_EMPTY_OR_BLANK);
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void testShouldFailOnEmptyPath()
    {
        fileSteps.assertPathExists("");
        verify(softAssert).recordFailedAssertion(FILE_PATH_MUST_NOT_BE_NULL_EMPTY_OR_BLANK);
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void testShouldFailOnBlankPath()
    {
        fileSteps.assertPathExists("   ");
        verify(softAssert).recordFailedAssertion(FILE_PATH_MUST_NOT_BE_NULL_EMPTY_OR_BLANK);
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void testShouldPassWhenPathExists(@TempDir Path tempDir) throws IOException
    {
        Path filePath = tempDir.resolve(FILE_NAME);
        Files.writeString(filePath, FILE_CONTENT, StandardCharsets.UTF_8);
        fileSteps.assertPathExists(filePath.toString());
        var messageCaptor = ArgumentCaptor.forClass(String.class);
        var booleanCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(softAssert).assertTrue(messageCaptor.capture(), booleanCaptor.capture());
        assertEquals(
                String.format(FILE_DOES_NOT_EXIST, filePath.toAbsolutePath()),
                messageCaptor.getValue()
        );
        assertEquals(true, booleanCaptor.getValue());
    }

    @Test
    void testShouldFailWhenPathDoesNotExist(@TempDir Path tempDir)
    {
        Path invalidFilePath = tempDir.resolve("non-existent.txt");
        fileSteps.assertPathExists(invalidFilePath.toString());
        var messageCaptor = ArgumentCaptor.forClass(String.class);
        var booleanCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(softAssert).assertTrue(messageCaptor.capture(), booleanCaptor.capture());
        assertEquals(
                String.format(FILE_DOES_NOT_EXIST, invalidFilePath.toAbsolutePath()),
                messageCaptor.getValue()
        );
        assertEquals(false, booleanCaptor.getValue());
    }

    @Test
    void testShouldFailWhenPathIsDirectory(@TempDir Path tempDir) throws IOException
    {
        Path dirPath = tempDir.resolve("test-directory");
        Files.createDirectory(dirPath);
        fileSteps.assertPathExists(dirPath.toString());
        var messageCaptor = ArgumentCaptor.forClass(String.class);
        var booleanCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(softAssert).assertTrue(messageCaptor.capture(), booleanCaptor.capture());
        assertEquals(
                String.format(FILE_DOES_NOT_EXIST, dirPath.toAbsolutePath()),
                messageCaptor.getValue()
        );
        assertEquals(false, booleanCaptor.getValue());
    }

    @Test
    void testShouldFailWhenPathIsInvalid()
    {
        try (MockedStatic<Paths> mockedPaths = mockStatic(Paths.class))
        {
            String invalidPath = "bad::path";
            mockedPaths.when(() -> Paths.get(invalidPath))
                    .thenThrow(new InvalidPathException(invalidPath, "Mock invalid path"));
            fileSteps.assertPathExists(invalidPath);
            verify(softAssert).recordFailedAssertion("Invalid path: Mock invalid path: " + invalidPath);
            verifyNoMoreInteractions(softAssert);
        }
    }
}
