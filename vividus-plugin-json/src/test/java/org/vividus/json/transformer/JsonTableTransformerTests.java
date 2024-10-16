/*
 * Copyright 2019-2024 the original author or authors.
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
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;

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
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class JsonTableTransformerTests
{
    private static final String EXAMPLE_TABLE_COLUMNS_CONFIG = ",columns=column_code=$.superCodes..code;"
            + "column_codeSystem=$.superCodes..codeSystem;column_type=$.superCodes..type";
    private static final String EXPECTED_TABLE = """
            |column_code|column_codeSystem|column_type|
            |107214|VIVIDUS|A|
            |107224|VIVIDUS|B|
            |107314|VIVIDUS|C|
            |107324|VIVIDUS|D|
            |107XX4|VIVIDUS|E|
            |1|true|F|""";

    @Mock private VariableContext variableContext;
    @InjectMocks private JsonTableTransformer jsonTableTransformer;

    @Test
    void shouldTransformFromVariableToComplexTable()
    {
        when(variableContext.getVariable("varName")).thenReturn(readJsonData());

        var tableProperties = createProperties("variableName=varName" + EXAMPLE_TABLE_COLUMNS_CONFIG);
        var table = jsonTableTransformer.transform(StringUtils.EMPTY, null, tableProperties);
        assertEquals(EXPECTED_TABLE, table);
    }

    @Test
    void shouldTransformFromVariableToSimpleTable()
    {
        when(variableContext.getVariable("json")).thenReturn(readJsonData());

        var tableProperties = createProperties("variableName=json,columns=key=$.key");
        var table = jsonTableTransformer.transform(StringUtils.EMPTY, null, tableProperties);
        assertEquals("|key|\n|value|", table);
    }

    @Test
    void shouldTransformFromPathToComplexTable()
    {
        var tableProperties = createProperties("path=org/vividus/json/transformer/data.json"
                + EXAMPLE_TABLE_COLUMNS_CONFIG);
        var table = jsonTableTransformer.transform(StringUtils.EMPTY, null, tableProperties);
        assertEquals(EXPECTED_TABLE, table);
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
            "'columns=key=value\\,url', 'One of either ''variableName'' or ''path'' should be specified'",
            "variableName=any,          '''columns'' is not set in ExamplesTable properties'",
            "'path= ',                  'ExamplesTable property ''path'' is blank'",
            "'variableName= ',          'ExamplesTable property ''variableName'' is blank'"
    })
    void shouldThrowAnErrorOnMissingParameters(String propertiesAsString, String expectedErrorMsg)
    {
        var properties = createProperties(propertiesAsString);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> jsonTableTransformer.transform(StringUtils.EMPTY, null, properties));
        assertEquals(expectedErrorMsg, exception.getMessage());
    }

    @Test
    void shouldThrowExceptionOnFailedAttemptToLoadResource()
    {
        try (MockedStatic<ResourceUtils> resourceUtilsStaticMock = mockStatic(ResourceUtils.class))
        {
            var path = "org/vividus/json/transformer/lost.json";
            var tableProperties = createProperties("path=" + path + EXAMPLE_TABLE_COLUMNS_CONFIG);
            var ioException = new IOException();
            resourceUtilsStaticMock.when(() -> ResourceUtils.loadResourceOrFileAsStream(path)).thenThrow(ioException);
            var uncheckedIOException = assertThrows(UncheckedIOException.class,
                    () -> jsonTableTransformer.transform(StringUtils.EMPTY, null, tableProperties));
            assertEquals(ioException, uncheckedIOException.getCause());
        }
    }

    private TableProperties createProperties(String propertiesAsString)
    {
        return new TableProperties(propertiesAsString, new Keywords(), new ParameterConverters());
    }

    private String readJsonData()
    {
        return ResourceUtils.loadResource(this.getClass(), "data.json");
    }
}
