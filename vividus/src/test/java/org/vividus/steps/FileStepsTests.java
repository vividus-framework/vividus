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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
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
    private static final String FILE_EXIST = "File '%s' does not exist at path '%s'";
    private static final String FILE_NAME = "test.txt";
    private static final String FILE_NAME_AND_FILE_PATH_MUST_NOT_BE_NULL = "fileName and filePath must not be null";

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
    void testDoesFileExistWithNullFileName(@TempDir Path tempDir)
    {
        fileSteps.doesFileExist(null, tempDir.toString());
        verify(softAssert).recordFailedAssertion(FILE_NAME_AND_FILE_PATH_MUST_NOT_BE_NULL);
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void testDoesFileExistWithNullFilePath()
    {
        fileSteps.doesFileExist(FILE_NAME, null);
        verify(softAssert).recordFailedAssertion(FILE_NAME_AND_FILE_PATH_MUST_NOT_BE_NULL);
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void shouldReturnTrueWhenFileExists(@TempDir Path tempDir) throws IOException
    {
        File file = tempDir.resolve(FILE_NAME).toFile();
        FileUtils.writeStringToFile(file, FILE_CONTENT, StandardCharsets.UTF_8);
        fileSteps.doesFileExist(FILE_NAME, tempDir.toString());
        var messageCaptor = ArgumentCaptor.forClass(String.class);
        var booleanCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(softAssert).assertTrue(messageCaptor.capture(), booleanCaptor.capture());
        assertEquals(String.format(FILE_EXIST, FILE_NAME, tempDir.toString()), messageCaptor.getValue());
        assertEquals(true, booleanCaptor.getValue());
    }

    @Test
    void shouldReturnFalseWhenFileDoesNotExist(@TempDir Path tempDir)
    {
        String fileName = "non-existent.txt";
        fileSteps.doesFileExist(fileName, tempDir.toString());
        var messageCaptor = ArgumentCaptor.forClass(String.class);
        var booleanCaptor = ArgumentCaptor.forClass(Boolean.class);
        verify(softAssert).assertTrue(messageCaptor.capture(), booleanCaptor.capture());
        assertEquals(String.format(FILE_EXIST, fileName, tempDir.toString()), messageCaptor.getValue());
        assertEquals(false, booleanCaptor.getValue());
    }
}
