/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.json.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class JsonTableTransformerTests
{
    @Mock private VariableContext variableContext;
    @InjectMocks private JsonTableTransformer jsonTableTransformer;

    @Test
    void testTransformFromVariable()
    {
        when(variableContext.getVariable("varName")).thenReturn(readJsonData());

        var tableProperties = createProperties("variableName=varName"
                + ",columns=column_code=$.superCodes..code;"
                + "column_codeSystem=$.superCodes..codeSystem;"
                + "column_type=$.superCodes..type");
        var table = jsonTableTransformer.transform(StringUtils.EMPTY, null, tableProperties);
        var expectedTable = """
                |column_code|column_codeSystem|column_type|
                |107214|VIVIDUS|A|
                |107224|VIVIDUS|B|
                |107314|VIVIDUS|C|
                |107324|VIVIDUS|D|
                |107XX4|VIVIDUS|E|
                |1|true|F|""";
        assertEquals(expectedTable, table);
    }

    @Test
    void testTransformFromVariableWithComplexJsonPath()
    {
        when(variableContext.getVariable("varWithJson")).thenReturn(readJsonData());
        var tableProperties = createProperties("columns=type=$..[?(@.codes[0].code==\"107214\")].type,"
                + "variableName=varWithJson");
        var table = jsonTableTransformer.transform(StringUtils.EMPTY, null, tableProperties);
        var expectedTable = "|type|\n|A|";
        assertEquals(expectedTable, table);
    }

    @ParameterizedTest
    @CsvSource({
            "'columns=key=value\\,url', variableName",
            "variableName=any,          columns"
    })
    void shouldThrowAnErrorOnMissingParameters(String propertiesAsString,
            String nameOfMissingProperty)
    {
        var properties = createProperties(propertiesAsString);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> jsonTableTransformer.transform(StringUtils.EMPTY, null, properties));
        assertEquals("'" + nameOfMissingProperty + "' is not set in ExamplesTable properties",
                exception.getMessage());
    }

    private TableProperties createProperties(String propertiesAsString)
    {
        return new TableProperties(propertiesAsString, new Keywords(), new ParameterConverters());
    }

    private static String readJsonData()
    {
        return ResourceUtils.loadResource(JsonTableTransformerTests.class, "data.json");
    }
}
