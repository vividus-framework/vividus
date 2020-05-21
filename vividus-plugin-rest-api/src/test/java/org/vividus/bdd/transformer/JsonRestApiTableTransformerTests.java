/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.bdd.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.util.ResourceUtils;
import org.vividus.util.UriUtils;

@ExtendWith(MockitoExtension.class)
class JsonRestApiTableTransformerTests
{
    private static final String JSON_DATA = ResourceUtils.loadResource(JsonRestApiTableTransformerTests.class,
            "data.json");
    private static final String URL = "url";
    private static final String URL_VALUE = "https://example.com/";
    private static final String COLUMNS = "columns";

    @Mock
    private IBddVariableContext bddVariableContext;

    @Mock
    private IHttpClient httpClient;

    @InjectMocks
    private JsonRestApiTableTransformer jsonTableGenerator;

    @Test
    void testTransformFromUrl() throws IOException
    {
        HttpResponse httpResponse = mock(HttpResponse.class);
        when(httpClient.doHttpGet(UriUtils.createUri(URL_VALUE))).thenReturn(httpResponse);
        when(httpResponse.getResponseBodyAsString()).thenReturn(JSON_DATA);
        testTransform(Map.entry(URL, URL_VALUE));
    }

    @Test
    void testTransformFromVariable()
    {
        String varName = "varName";
        when(bddVariableContext.getVariable(varName)).thenReturn(JSON_DATA);
        testTransform(Map.entry("variable", varName));
    }

    private void testTransform(Entry<String, String> source)
    {
        Entry<String, String> columns = Map.entry(COLUMNS, "column_code=$.superCodes..code;"
                + "column_codeSystem=$.superCodes..codeSystem;column_type=$.superCodes..type");
        Map<String, String> keyToJsonPathValue = Map.ofEntries(columns, source);
        String expectedTable = "|column_code|column_codeSystem|column_type|\n|107214|VIVIDUS|A|\n|107224|VIVIDUS|B|"
                + "\n|107314|VIVIDUS|C|\n|107324|VIVIDUS|D|\n|107XX4|VIVIDUS|E|\n|1|true|F|";
        String table = jsonTableGenerator.transform(StringUtils.EMPTY, null, createProperties(keyToJsonPathValue));
        assertEquals(expectedTable, table);
    }

    @Test
    void testSourceTransformPropertyIsNotSpecifiedException()
    {
        TableProperties properties = createProperties(Collections.singletonMap(COLUMNS, "key=value,url"));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> jsonTableGenerator.transform(StringUtils.EMPTY, null, properties));
        assertEquals("One of ExamplesTable properties must be set: either 'url' or 'variable'", exception.getMessage());
    }

    @Test
    void testColumnsTransformPropertyIsNotSpecifiedException()
    {
        TableProperties properties = createProperties(Collections.singletonMap(URL, URL_VALUE));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> jsonTableGenerator.transform(StringUtils.EMPTY, null, properties));
        assertEquals("'columns' is not set in ExamplesTable properties", exception.getMessage());
    }

    private static TableProperties createProperties(Map<String, String> keyToJsonPathValue)
    {
        Properties properties = new Properties();
        properties.putAll(keyToJsonPathValue);
        return new TableProperties(properties);
    }
}
