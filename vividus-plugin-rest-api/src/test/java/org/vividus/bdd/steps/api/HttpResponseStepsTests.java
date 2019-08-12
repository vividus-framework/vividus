/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd.steps.api;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.api.IApiTestContext;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.model.ArchiveVariable;
import org.vividus.bdd.model.NamedEntry;
import org.vividus.bdd.model.OutputFormat;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.http.client.HttpResponse;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class HttpResponseStepsTests
{
    private static final String FILE_JSON = "file.json";
    private static final String IMAGE_PNG = "images/image.png";

    @Mock
    private IApiTestContext apiTestContext;

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private IBddVariableContext bddVariableContext;

    @InjectMocks
    private HttpResponseSteps httpResponseSteps;

    @Test
    void testSaveFilesContentToVariables()
    {
        mockHttpResponse();

        String json = "json";
        String image = "image";
        String base64 = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABAQMAAAAl21bKAAAABlBMVEUAAAD///+l2Z/dAAAACXBIWXMAAA7EAAAOxAGVK"
                + "w4bAAAACklEQVQImWNgAAAAAgAB9HFkpgAAAABJRU5ErkJggg==";

        httpResponseSteps.saveFilesContentToVariables(
                List.of(createVariable(IMAGE_PNG, image, OutputFormat.BASE64),
                        createVariable(FILE_JSON, json, OutputFormat.TEXT)));
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        verify(bddVariableContext).putVariable(scopes, image, base64);
        verify(bddVariableContext).putVariable(scopes, json,
                "{\"plugin\": \"vividus-plugin-rest-api\"}\n");
        verifyZeroInteractions(softAssert);
        verifyNoMoreInteractions(bddVariableContext);
    }

    @Test
    void testSaveFilesContentToVariablesInvalidPath()
    {
        mockHttpResponse();

        String path = "path";
        httpResponseSteps.saveFilesContentToVariables(List.of(createVariable(path, path, OutputFormat.BASE64)));
        verify(softAssert)
                .recordFailedAssertion(String.format("Unable to find entry by name %s in response archive", path));
        verifyZeroInteractions(bddVariableContext);
        verifyNoMoreInteractions(softAssert);
    }

    @Test
    void testVerifyArhiveContainsEntries()
    {
        mockHttpResponse();
        Set<String> archiveEntries = Set.of(IMAGE_PNG, FILE_JSON);
        String dummy = "dummy";
        String message = "The response archive contains entry with name ";
        httpResponseSteps.verifyArhiveContainsEntries(List.of(
                createEntry(FILE_JSON),
                createEntry(IMAGE_PNG),
                createEntry(dummy)
                ));
        verify(softAssert).assertThat(eq(message + FILE_JSON), eq(archiveEntries),
                argThat(e -> e.matches(archiveEntries)));
        verify(softAssert).assertThat(eq(message + IMAGE_PNG), eq(archiveEntries),
                argThat(e -> e.matches(archiveEntries)));
        verify(softAssert).assertThat(eq(message + dummy), eq(archiveEntries),
                argThat(e ->  !e.matches(archiveEntries)));
        verifyNoMoreInteractions(softAssert);
    }

    private static NamedEntry createEntry(String name)
    {
        NamedEntry entry = new NamedEntry();
        entry.setName(name);
        return entry;
    }

    private void mockHttpResponse()
    {
        byte[] data = ResourceUtils.loadResourceAsByteArray(getClass(), "/org/vividus/bdd/steps/api/archive.zip");
        HttpResponse response = mock(HttpResponse.class);
        when(apiTestContext.getResponse()).thenReturn(response);
        when(response.getResponseBody()).thenReturn(data);
    }

    private static ArchiveVariable createVariable(String path, String variableName, OutputFormat outputFormat)
    {
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        ArchiveVariable variable = new ArchiveVariable();
        variable.setPath(path);
        variable.setOutputFormat(outputFormat);
        variable.setScopes(scopes);
        variable.setVariableName(variableName);
        return variable;
    }
}
