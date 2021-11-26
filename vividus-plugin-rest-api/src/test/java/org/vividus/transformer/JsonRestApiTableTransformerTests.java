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

package org.vividus.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.util.ResourceUtils;
import org.vividus.util.UriUtils;

@ExtendWith(MockitoExtension.class)
class JsonRestApiTableTransformerTests
{
    private static final String JSON_DATA = ResourceUtils.loadResource(JsonRestApiTableTransformerTests.class,
            "data.json");
    private static final String URL_VALUE = "https://example.com/";
    private static final String URL_PARAMETER = "url=";
    private static final String VARIABLE_PARAMETER = "variable=";

    @Mock private VariableContext variableContext;
    @Mock private IHttpClient httpClient;
    @InjectMocks private JsonRestApiTableTransformer jsonTableGenerator;

    private final Keywords keywords = new Keywords();
    private final ParameterConverters parameterConverters =  new ParameterConverters();

    @Test
    void testTransformFromUrl() throws IOException
    {
        var httpResponse = mock(HttpResponse.class);
        when(httpClient.doHttpGet(UriUtils.createUri(URL_VALUE))).thenReturn(httpResponse);
        when(httpResponse.getResponseBodyAsString()).thenReturn(JSON_DATA);
        testTransform(URL_PARAMETER + URL_VALUE);
    }

    @Test
    void testTransformFromVariable()
    {
        var variableName = "varName";
        when(variableContext.getVariable(variableName)).thenReturn(JSON_DATA);
        testTransform(VARIABLE_PARAMETER + variableName);
    }

    private void testTransform(String source)
    {
        var tableProperties = createProperties(source + ",columns=column_code=$.superCodes..code;"
                        + "column_codeSystem=$.superCodes..codeSystem;"
                        + "column_type=$.superCodes..type");
        String table = jsonTableGenerator.transform(StringUtils.EMPTY, null, tableProperties);
        var expectedTable =
                "|column_code|column_codeSystem|column_type|\n"
                + "|107214|VIVIDUS|A|\n"
                + "|107224|VIVIDUS|B|\n"
                + "|107314|VIVIDUS|C|\n"
                + "|107324|VIVIDUS|D|\n"
                + "|107XX4|VIVIDUS|E|\n"
                + "|1|true|F|";
        assertEquals(expectedTable, table);
    }

    @Test
    void testTransformFromVariableWithComplexJsonPath()
    {
        var variableName = "varWithJson";
        when(variableContext.getVariable(variableName)).thenReturn(JSON_DATA);
        var columns = "columns=type=$..[?(@.codes[0].code==\"107214\")].type";
        var tableProperties = createProperties(columns + "," + VARIABLE_PARAMETER + variableName);
        String table = jsonTableGenerator.transform(StringUtils.EMPTY, null, tableProperties);
        var expectedTable = "|type|\n|A|";
        assertEquals(expectedTable, table);
    }

    @Test
    void testSourceTransformPropertyIsNotSpecifiedException()
    {
        TableProperties properties = createProperties("columns=key=value\\,url");
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> jsonTableGenerator.transform(StringUtils.EMPTY, null, properties));
        assertEquals("One of ExamplesTable properties must be set: either 'url' or 'variable'", exception.getMessage());
    }

    @Test
    void testColumnsTransformPropertyIsNotSpecifiedException()
    {
        TableProperties properties = createProperties(URL_PARAMETER + URL_VALUE);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> jsonTableGenerator.transform(StringUtils.EMPTY, null, properties));
        assertEquals("'columns' is not set in ExamplesTable properties", exception.getMessage());
    }

    private TableProperties createProperties(String propertiesAsString)
    {
        return new TableProperties(propertiesAsString, keywords, parameterConverters);
    }
}
