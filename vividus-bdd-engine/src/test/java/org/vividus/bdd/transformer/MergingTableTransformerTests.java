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

package org.vividus.bdd.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.ExamplesTableProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.spring.Configuration;

@ExtendWith(MockitoExtension.class)
class MergingTableTransformerTests
{
    private static final String MERGE_MODE = "mergeMode";
    private static final String ROWS = "rows";
    private static final String COLUMNS = "columns";
    private static final String PATH_ONE = "/test1.table";
    private static final String PATH_TWO = "/test2.table";
    private static final String PATH_THREE = "/test3.table";
    private static final String TABLES_DELIMITER = "; ";
    private static final String TWO_PATHS = String.join(TABLES_DELIMITER, PATH_ONE, PATH_TWO);
    private static final String THREE_PATHS = String.join(TABLES_DELIMITER, PATH_ONE, PATH_TWO, PATH_THREE);
    private static final String TRANSFORMER = "{transformer=FROM_EXCEL, path=test.csv, sheet=test, ";
    private static final String TRANSFORMER_ONE = TRANSFORMER + "range=A2:A4, column=country}";
    private static final String TRANSFORMER_TWO = TRANSFORMER + "range=B2:B4, column=capital}";
    private static final String FILLER_VALUE_PARAMETER_NAME = "fillerValue";
    private static final String FILLER_VALUE_PARAMETER_VALUE = "value";
    private static final String HEADER_SEPARATOR = "headerSeparator";
    private static final String VALUE_SEPARATOR = "valueSeparator";
    private static final String SEPARATOR = "!";

    @Mock
    private ExamplesTableFactory factory;

    @Mock
    private Configuration configuration;

    @InjectMocks
    private MergingTableTransformer mergingTableTransformer;

    @Test
    void testInvalidMergeMode()
    {
        Properties properties = new Properties();
        properties.setProperty(MERGE_MODE, "invalidMode");
        verifyIllegalArgumentException(properties,
                "Value of ExamplesTable property '" + MERGE_MODE + "' must be from range [ROWS, COLUMNS]");
    }

    @Test
    void testNotUniqueTablePaths()
    {
        Properties properties = createProperties(ROWS, "/test.table; /test.table");
        verifyIllegalArgumentException(properties, "Please, specify more than one unique table paths");
    }

    @Test
    void testRowsModeDifferentTableKeys()
    {
        Properties properties = createProperties(ROWS, TWO_PATHS);
        when(configuration.getExamplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(PATH_ONE, "|var1|var2|\n|a|b|");
        mockCreateExamplesTable(PATH_TWO, "|var1|var3|\n|c|d|");
        verifyIllegalArgumentException(properties, "Please, specify tables with the same sets of headers");
    }

    @Test
    void testColumnsModeDifferentRowsNumber()
    {
        Properties properties = createProperties(COLUMNS, TWO_PATHS);
        when(configuration.getExamplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(PATH_ONE, "|var1|var2|\n|a|b|\n|c|d|");
        mockCreateExamplesTable(PATH_TWO, "|var3|var4|\n|e|f|");
        verifyIllegalArgumentException(properties, "Please, specify tables with the same number of rows");
    }

    @Test
    void testColumnsModeNotUniqueKeySets()
    {
        Properties properties = createProperties(COLUMNS, TWO_PATHS);
        when(configuration.getExamplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(PATH_ONE, "|var2|var1|\n|b|a|");
        mockCreateExamplesTable(PATH_TWO, "|var2|var3|\n|c|d|");
        verifyIllegalArgumentException(properties, "Please, specify tables with the unique sets of headers");
    }

    @Test
    void testTransformInRowsMode()
    {
        Properties properties = createProperties(ROWS, THREE_PATHS);
        when(configuration.getExamplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(PATH_ONE, "|var2|var1|\n|b|a|\n|d|c|");
        mockCreateExamplesTable(PATH_TWO, "|var1|var2|\n|e|f|\n|g|h|");
        mockCreateExamplesTable(PATH_THREE, "|var1|var2|\n|i|j|\n|a|b|");
        assertMerge(properties, "|var1|var2|\n|a|b|\n|c|d|\n|e|f|\n|g|h|\n|i|j|\n|a|b|");
    }

    @Test
    void testTransformsInColumnsMode()
    {
        Properties properties = createProperties(COLUMNS, THREE_PATHS);
        when(configuration.getExamplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(PATH_ONE, "|var6|var5|\n|b|a|\n|d|c|");
        mockCreateExamplesTable(PATH_TWO, "|var3|var4|\n|e|f|\n|g|h|");
        mockCreateExamplesTable(PATH_THREE, "|var1|var2|\n|j|i|\n|l|k|");
        assertMerge(properties, "|var1|var2|var3|var4|var5|var6|\n|j|i|e|f|a|b|\n|l|k|g|h|c|d|");
    }

    @Test
    void testTransformsInColumnsModeByRangeWithNestedTransformers()
    {
        Properties properties = createProperties(COLUMNS,
                TRANSFORMER_ONE + TABLES_DELIMITER + TRANSFORMER_TWO);
        when(configuration.getExamplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(TRANSFORMER_ONE, "|country|\n|Belarus|\n|USA|\n|Armenia|");
        mockCreateExamplesTable(TRANSFORMER_TWO, "|capital|\n|Minsk|\n|Washington|\n|Yerevan|");
        assertMerge(properties, "|capital|country|\n|Minsk|Belarus|\n|Washington|USA|\n|Yerevan|Armenia|");
    }

    @Test
    void testTransformsInColumnsModeByAddressesWithNestedTransformers()
    {
        Properties properties = createProperties(COLUMNS, TRANSFORMER + "addresses=A2\\;A4, column=country}"
                + TABLES_DELIMITER + TRANSFORMER + "addresses=B2\\;B4, column=capital}");
        when(configuration.getExamplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(TRANSFORMER + "addresses=A2;A4, column=country}", "|country|\n|Belarus|\n|Armenia|");
        mockCreateExamplesTable(TRANSFORMER + "addresses=B2;B4, column=capital}", "|capital|\n|Minsk|\n|Yerevan|");
        assertMerge(properties, "|capital|country|\n|Minsk|Belarus|\n|Yerevan|Armenia|");
    }

    @Test
    void testColumnsModeDifferentRowsNumberWithValueToFill()
    {
        Properties properties = createProperties(COLUMNS, THREE_PATHS);
        properties.setProperty(FILLER_VALUE_PARAMETER_NAME, FILLER_VALUE_PARAMETER_VALUE);
        when(configuration.getExamplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(PATH_ONE, "|var1|var2|\n|a|b|\n|c|d|\n|e|f|");
        mockCreateExamplesTable(PATH_TWO, "|var5|\n|i|\n|j|");
        mockCreateExamplesTable(PATH_THREE, "|var3|var4|\n|g|h|");
        assertMerge(properties,
                "|var1|var2|var3|var4|var5|\n|a|b|g|h|i|\n|c|d|value|value|j|\n|e|f|value|value|value|");
    }

    @Test
    void testRowsModeDifferentTableKeysWithValueToFill()
    {
        Properties properties = createProperties(ROWS, THREE_PATHS);
        properties.setProperty(FILLER_VALUE_PARAMETER_NAME, FILLER_VALUE_PARAMETER_VALUE);
        when(configuration.getExamplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(PATH_ONE, "|var1|var3|\n|e|f|");
        mockCreateExamplesTable(PATH_TWO, "|var1|var2|\n|b|a|\n|d|c|");
        mockCreateExamplesTable(PATH_THREE, "|var1|var2|var3|\n|r|g|h|");
        assertMerge(properties, "|var1|var2|var3|\n|e|value|f|\n|b|a|value|\n|d|c|value|\n|r|g|h|");
    }

    @Test
    void testRowsModeWithValueToFillHeaderSeparatorValueSeparator()
    {
        Properties properties = createProperties(ROWS, TWO_PATHS);
        properties.setProperty(FILLER_VALUE_PARAMETER_NAME, FILLER_VALUE_PARAMETER_VALUE);
        properties.setProperty(HEADER_SEPARATOR, SEPARATOR);
        properties.setProperty(VALUE_SEPARATOR, SEPARATOR);
        when(configuration.getExamplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(PATH_ONE, "{headerSeparator=!,valueSeparator=!}\n!var1!var2!\n!A!B|C!");
        mockCreateExamplesTable(PATH_TWO, "{headerSeparator=!,valueSeparator=!}\n!var1!\n!D|E!");
        assertMerge(properties, "!var1!var2!\n!A!B|C!\n!D|E!value!");
    }

    @Test
    void testColumnsModeWithValueToFillHeaderSeparatorValueSeparator()
    {
        Properties properties = createProperties(COLUMNS, TWO_PATHS);
        properties.setProperty(FILLER_VALUE_PARAMETER_NAME, FILLER_VALUE_PARAMETER_VALUE);
        properties.setProperty(HEADER_SEPARATOR, SEPARATOR);
        properties.setProperty(VALUE_SEPARATOR, SEPARATOR);
        when(configuration.getExamplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(PATH_ONE, "{headerSeparator=!,valueSeparator=!}\n!var1!var3!\n!B!A|E!");
        mockCreateExamplesTable(PATH_TWO, "{headerSeparator=!,valueSeparator=!}\n!var2!\r\n!D|E!\n!F!");
        assertMerge(properties, "!var1!var2!var3!\n!B!D|E!A|E!\n!value!F!value!");
    }

    private void mockCreateExamplesTable(String path, String table)
    {
        lenient().when(factory.createExamplesTable(path)).thenReturn(new ExamplesTable(table));
    }

    private void assertMerge(Properties properties, String expected)
    {
        ExamplesTableProperties examplesTableProperties = new ExamplesTableProperties(properties);
        assertEquals(expected, mergingTableTransformer.transform(StringUtils.EMPTY, examplesTableProperties));
    }

    private Properties createProperties(String mergeMode, String tables)
    {
        Properties properties = new Properties();
        properties.setProperty(MERGE_MODE, mergeMode);
        properties.setProperty("tables", tables);
        return properties;
    }

    private void verifyIllegalArgumentException(Properties properties, String message)
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> mergingTableTransformer.transform(StringUtils.EMPTY, new ExamplesTableProperties(properties)));
        assertEquals(message, exception.getMessage());
    }
}
