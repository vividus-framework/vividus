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

package org.vividus.bdd.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class FileStepsTests
{
    @Mock
    private IBddVariableContext bddVariableContext;

    @InjectMocks
    private FileSteps fileSteps;

    @Test
    void testSaveResponseBodyToFile() throws IOException
    {
        String content = "content";
        String pathVariable = "path";
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        fileSteps.saveResponseBodyToFile("test.txt", content, scopes, pathVariable);
        verify(bddVariableContext).putVariable(eq(scopes), eq(pathVariable), argThat(path ->
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
}
