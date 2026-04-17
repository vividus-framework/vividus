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

package org.vividus.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;
import java.util.regex.Pattern;

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
    private static final Pattern FILE_PATTERN = Pattern.compile("report-\\d+\\.csv");
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.SCENARIO);
    private static final String VARIABLE = "file-path";
    private static final Duration TIMEOUT = Duration.ZERO;

    @Mock private VariableContext variableContext;
    @Mock private ISoftAssert softAssert;

    @InjectMocks
    private FileSteps fileSteps;

    @Test
    void testSaveResponseBodyToFile() throws IOException
    {
        String content = "content";
        String pathVariable = "path";
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        fileSteps.createTemporaryFile("test.txt", new DataWrapper(content), scopes, pathVariable);
        verify(variableContext).putVariable(eq(scopes), eq(pathVariable), argThat(path ->
        {
            try
            {
                return FileUtils.readFileToString(new File((String) path), StandardCharsets.UTF_8).equals(content);
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
        String fileContent = "file-content";
        fileSteps.createFile(fileContent, tempFilePath);
        assertEquals(fileContent, FileUtils.readFileToString(new File(tempFilePath), StandardCharsets.UTF_8));
    }

    @Test
    void shouldFailWhenDirectoryDoesNotExist()
    {
        var path = Path.of("non/existent/dir");
        var exception = assertThrows(IllegalArgumentException.class,
                () -> fileSteps.waitForFileAndSavePath(TIMEOUT, TIMEOUT, FILE_PATTERN, path, SCOPES, VARIABLE));
        assertEquals("The directory '%s' does not exist".formatted(path), exception.getMessage());
        verifyNoInteractions(variableContext, softAssert);
    }

    @Test
    void shouldSaveFilePathWhenExactlyOneFileMatches(@TempDir Path tempDir) throws IOException
    {
        String csv = "report-42.csv";
        Files.createFile(tempDir.resolve(csv));
        fileSteps.waitForFileAndSavePath(TIMEOUT, TIMEOUT, FILE_PATTERN, tempDir, SCOPES, VARIABLE);
        verify(variableContext).putVariable(SCOPES, VARIABLE, tempDir.resolve(csv).toAbsolutePath().toString());
        verifyNoInteractions(softAssert);
    }

    @Test
    void shouldFailWhenNoFileAppearsWithinTimeout(@TempDir Path tempDir) throws IOException
    {
        fileSteps.waitForFileAndSavePath(TIMEOUT, TIMEOUT, FILE_PATTERN, tempDir, SCOPES, VARIABLE);
        verify(softAssert).recordFailedAssertion(
                "No file matching `%s` appeared in directory '%s' within %s"
                        .formatted(FILE_PATTERN, tempDir, TIMEOUT));
        verifyNoInteractions(variableContext);
    }

    @Test
    void shouldFailWhenMultipleFilesMatch(@TempDir Path tempDir) throws IOException
    {
        Files.createFile(tempDir.resolve("report-1.csv"));
        Files.createFile(tempDir.resolve("report-2.csv"));
        fileSteps.waitForFileAndSavePath(TIMEOUT, TIMEOUT, FILE_PATTERN, tempDir, SCOPES, VARIABLE);
        var messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(softAssert).recordFailedAssertion(messageCaptor.capture());
        assertEquals(
                "Expected exactly 1 file matching `%s` in directory '%s', but found 2: report-1.csv, report-2.csv."
                        .formatted(FILE_PATTERN, tempDir),
                messageCaptor.getValue());
        verifyNoInteractions(variableContext);
    }
}
