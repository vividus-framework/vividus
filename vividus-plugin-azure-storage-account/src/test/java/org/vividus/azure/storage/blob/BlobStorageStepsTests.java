/*
 * Copyright 2021 the original author or authors.
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

package org.vividus.azure.storage.blob;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.BddVariableContext;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.bdd.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class BlobStorageStepsTests
{
    private static final String SUFFIX = "result.json";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.SCENARIO);
    private static final String VALUE = "value";
    private static final byte[] BYTES = VALUE.getBytes(StandardCharsets.UTF_8);
    private static final String VARIABLE_NAME = "variableName";
    private static final String KEY = "key";
    private static final String PATH = "path";

    @Mock private BddVariableContext bddVariableContext;

    @InjectMocks
    private BlobStorageSteps blobStorageSteps;

    @Test
    void shouldReadTextContent()
    {
        when(blobStorageService.readBlob(KEY, PATH)).thenReturn(BYTES);
        blobStorageSteps.readBlob(PATH, KEY, SCOPES, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(SCOPES, VARIABLE_NAME, VALUE);
    }

    @Test
    void shouldSafeAFileToATempDirectory() throws IOException
    {
        when(blobStorageService.readBlob(KEY, PATH)).thenReturn(BYTES);
        blobStorageSteps.readBlobToFile(PATH, KEY, SUFFIX, SCOPES, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(eq(SCOPES), eq(VARIABLE_NAME), argThat(path ->
        {
            return path.toString().endsWith(SUFFIX) && VALUE.equals(readString((Path) path));
        }));
    }

    @Test
    void shouldDeleteFile()
    {
        blobStorageSteps.deleteAFile(PATH, KEY);
        verify(blobStorageService).delete(KEY, PATH);
        verifyNoInteractions(bddVariableContext);
    }

    @Test
    void shouldFindFilesFiles()
    {
        List<Path> pathes = List.of(mock(Path.class));
        when(blobStorageService.findFiles(argThat(m -> "\"path\"".equals(m.toString())),
            eq(KEY))).thenReturn(pathes);
        blobStorageSteps.findFiles(StringComparisonRule.IS_EQUAL_TO, PATH, KEY, SCOPES, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(SCOPES, VARIABLE_NAME, pathes);
    }

    private String readString(Path path)
    {
        try
        {
            return Files.readString(path, StandardCharsets.UTF_8);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
