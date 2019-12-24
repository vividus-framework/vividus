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
    private static final String PATH_1 = "/test1.table";
    private static final String PATH_2 = "/test2.table";
    private static final String PATH_3 = "/test3.table";
    private static final String TABLES_DELIMITER = "; ";
    private static final String TWO_PATHS = String.join(TABLES_DELIMITER, PATH_1, PATH_2);
    private static final String THREE_PATHS = String.join(TABLES_DELIMITER, PATH_1, PATH_2, PATH_3);
    private static final String TRANSFORMER = "{transformer=FROM_EXCEL, path=test.csv, sheet=test, ";
    private static final String TRANSFORMER_ONE = TRANSFORMER + "range=A2:A4, column=country}";
    private static final String TRANSFORMER_TWO = TRANSFORMER + "range=B2:B4, column=capital}";
    private static final String FILLER_VALUE_PARAMETER_NAME = "fillerValue";
    private static final String FILLER_VALUE_PARAMETER_VALUE = "value";
    private static final String HEADER_SEPARATOR = "headerSeparator";
    private static final String VALUE_SEPARATOR = "valueSeparator";
    private static final String SEPARATOR = "!";

    private static final String TABLE_1_FOR_COLUMNS_MERGE = "|var6|var5|\n|b|a|\n|d|c|";
    private static final String TABLE_2_FOR_COLUMNS_MERGE = "|var3|var4|\n|e|f|\n|g|h|";
    private static final String TABLE_3_FOR_COLUMNS_MERGE = "|var1|var2|\n|j|i|\n|l|k|";
    private static final String MERGED_TABLE_COLUMNS = "|var1|var2|var3|var4|var5|var6|\n|j|i|e|f|a|b|\n|l|k|g|h|c|d|";

    private static final String TABLE_1_FOR_ROWS_MERGE = "|var2|var1|\n|b|a|\n|d|c|";
    private static final String TABLE_2_FOR_ROWS_MERGE = "|var1|var2|\n|e|f|\n|g|h|";
    private static final String TABLE_3_FOR_ROWS_MERGE = "|var1|var2|\n|i|j|\n|a|b|";
    private static final String MERGED_TABLE_ROWS = "|var1|var2|\n|a|b|\n|c|d|\n|e|f|\n|g|h|\n|i|j|\n|a|b|";

    private static final String ERROR_MORE_THAN_ONE_TABLE_PATHS = "Please, specify more than one unique table paths";
    private static final String ERROR_AT_LEAST_ONE_TABLE_PATH = "Please, specify at least one table path";

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
        verifyIllegalArgumentException(properties, ERROR_MORE_THAN_ONE_TABLE_PATHS);
    }

    @Test
    void testNoTablesToMerge()
    {
        Properties properties = new Properties();
        properties.setProperty(MERGE_MODE, ROWS);
        verifyIllegalArgumentException(properties, ERROR_MORE_THAN_ONE_TABLE_PATHS);
    }

    @Test
    void testNoTablesToMergeHavingTableBody()
    {
        Properties properties = new Properties();
        properties.setProperty(MERGE_MODE, ROWS);
        verifyIllegalArgumentException(TABLE_1_FOR_ROWS_MERGE, properties, ERROR_AT_LEAST_ONE_TABLE_PATH);
    }

    @Test
    void testNotEnoughTablesToMerge()
    {
        Properties properties = createProperties(ROWS, "");
        verifyIllegalArgumentException(properties, ERROR_MORE_THAN_ONE_TABLE_PATHS);
    }

    @Test
    void testNotEnoughTablesToMergeHavingTableBody()
    {
        Properties properties = createProperties(ROWS, "");
        verifyIllegalArgumentException(TABLE_1_FOR_ROWS_MERGE, properties, ERROR_AT_LEAST_ONE_TABLE_PATH);
    }

    @Test
    void testRowsModeDifferentTableKeys()
    {
        Properties properties = createProperties(ROWS, TWO_PATHS);
        when(configuration.examplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(PATH_1, "|var1|var2|\n|a|b|");
        mockCreateExamplesTable(PATH_2, "|var1|var3|\n|c|d|");
        verifyIllegalArgumentException(properties, "Please, specify tables with the same sets of headers");
    }

    @Test
    void testColumnsModeDifferentRowsNumber()
    {
        Properties properties = createProperties(COLUMNS, TWO_PATHS);
        when(configuration.examplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(PATH_1, "|var1|var2|\n|a|b|\n|c|d|");
        mockCreateExamplesTable(PATH_2, "|var3|var4|\n|e|f|");
        verifyIllegalArgumentException(properties, "Please, specify tables with the same number of rows");
    }

    @Test
    void testColumnsModeNotUniqueKeySets()
    {
        Properties properties = createProperties(COLUMNS, TWO_PATHS);
        when(configuration.examplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(PATH_1, "|var2|var1|\n|b|a|");
        mockCreateExamplesTable(PATH_2, "|var2|var3|\n|c|d|");
        verifyIllegalArgumentException(properties, "Please, specify tables with the unique sets of headers");
    }

    @Test
    void testTransformInRowsMode()
    {
        Properties properties = createProperties(ROWS, THREE_PATHS);
        when(configuration.examplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(PATH_1, TABLE_1_FOR_ROWS_MERGE);
        mockCreateExamplesTable(PATH_2, TABLE_2_FOR_ROWS_MERGE);
        mockCreateExamplesTable(PATH_3, TABLE_3_FOR_ROWS_MERGE);
        assertMerge(properties, MERGED_TABLE_ROWS);
    }

    @Test
    void testTransformInRowsModeHavingTableBody()
    {
        Properties properties = createProperties(ROWS, TWO_PATHS);
        when(configuration.examplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(PATH_1, TABLE_1_FOR_ROWS_MERGE);
        mockCreateExamplesTable(PATH_2, TABLE_2_FOR_ROWS_MERGE);
        mockCreateExamplesTable(TABLE_3_FOR_ROWS_MERGE);
        assertMerge(TABLE_3_FOR_ROWS_MERGE, properties, MERGED_TABLE_ROWS);
    }

    @Test
    void testTransformsInColumnsMode()
    {
        Properties properties = createProperties(COLUMNS, THREE_PATHS);
        when(configuration.examplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(PATH_1, TABLE_1_FOR_COLUMNS_MERGE);
        mockCreateExamplesTable(PATH_2, TABLE_2_FOR_COLUMNS_MERGE);
        mockCreateExamplesTable(PATH_3, TABLE_3_FOR_COLUMNS_MERGE);
        assertMerge(properties, MERGED_TABLE_COLUMNS);
    }

    @Test
    void testTransformsInColumnsModeHavingTableBody()
    {
        Properties properties = createProperties(COLUMNS, TWO_PATHS);
        when(configuration.examplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(PATH_1, TABLE_1_FOR_COLUMNS_MERGE);
        mockCreateExamplesTable(PATH_2, TABLE_2_FOR_COLUMNS_MERGE);
        mockCreateExamplesTable(TABLE_3_FOR_COLUMNS_MERGE);
        assertMerge(TABLE_3_FOR_COLUMNS_MERGE, properties, MERGED_TABLE_COLUMNS);
    }

    @Test
    void testTransformsInColumnsModeByRangeWithNestedTransformers()
    {
        Properties properties = createProperties(COLUMNS,
                TRANSFORMER_ONE + TABLES_DELIMITER + TRANSFORMER_TWO);
        when(configuration.examplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(TRANSFORMER_ONE, "|country|\n|Belarus|\n|USA|\n|Armenia|");
        mockCreateExamplesTable(TRANSFORMER_TWO, "|capital|\n|Minsk|\n|Washington|\n|Yerevan|");
        assertMerge(properties, "|capital|country|\n|Minsk|Belarus|\n|Washington|USA|\n|Yerevan|Armenia|");
    }

    @Test
    void testTransformsInColumnsModeByAddressesWithNestedTransformers()
    {
        Properties properties = createProperties(COLUMNS, TRANSFORMER + "addresses=A2\\;A4, column=country}"
                + TABLES_DELIMITER + TRANSFORMER + "addresses=B2\\;B4, column=capital}");
        when(configuration.examplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(TRANSFORMER + "addresses=A2;A4, column=country}", "|country|\n|Belarus|\n|Armenia|");
        mockCreateExamplesTable(TRANSFORMER + "addresses=B2;B4, column=capital}", "|capital|\n|Minsk|\n|Yerevan|");
        assertMerge(properties, "|capital|country|\n|Minsk|Belarus|\n|Yerevan|Armenia|");
    }

    @Test
    void testColumnsModeDifferentRowsNumberWithValueToFill()
    {
        Properties properties = createProperties(COLUMNS, THREE_PATHS);
        properties.setProperty(FILLER_VALUE_PARAMETER_NAME, FILLER_VALUE_PARAMETER_VALUE);
        when(configuration.examplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(PATH_1, "|var1|var2|\n|a|b|\n|c|d|\n|e|f|");
        mockCreateExamplesTable(PATH_2, "|var5|\n|i|\n|j|");
        mockCreateExamplesTable(PATH_3, "|var3|var4|\n|g|h|");
        assertMerge(properties,
                "|var1|var2|var3|var4|var5|\n|a|b|g|h|i|\n|c|d|value|value|j|\n|e|f|value|value|value|");
    }

    @Test
    void testRowsModeDifferentTableKeysWithValueToFill()
    {
        Properties properties = createProperties(ROWS, THREE_PATHS);
        properties.setProperty(FILLER_VALUE_PARAMETER_NAME, FILLER_VALUE_PARAMETER_VALUE);
        when(configuration.examplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(PATH_1, "|var1|var3|\n|e|f|");
        mockCreateExamplesTable(PATH_2, "|var1|var2|\n|b|a|\n|d|c|");
        mockCreateExamplesTable(PATH_3, "|var1|var2|var3|\n|r|g|h|");
        assertMerge(properties, "|var1|var2|var3|\n|e|value|f|\n|b|a|value|\n|d|c|value|\n|r|g|h|");
    }

    @Test
    void testRowsModeWithValueToFillHeaderSeparatorValueSeparator()
    {
        Properties properties = createProperties(ROWS, TWO_PATHS);
        properties.setProperty(FILLER_VALUE_PARAMETER_NAME, FILLER_VALUE_PARAMETER_VALUE);
        properties.setProperty(HEADER_SEPARATOR, SEPARATOR);
        properties.setProperty(VALUE_SEPARATOR, SEPARATOR);
        when(configuration.examplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(PATH_1, "{headerSeparator=!,valueSeparator=!}\n!var1!var2!\n!A!B|C!");
        mockCreateExamplesTable(PATH_2, "{headerSeparator=!,valueSeparator=!}\n!var1!\n!D|E!");
        assertMerge(properties, "!var1!var2!\n!A!B|C!\n!D|E!value!");
    }

    @Test
    void testColumnsModeWithValueToFillHeaderSeparatorValueSeparator()
    {
        Properties properties = createProperties(COLUMNS, TWO_PATHS);
        properties.setProperty(FILLER_VALUE_PARAMETER_NAME, FILLER_VALUE_PARAMETER_VALUE);
        properties.setProperty(HEADER_SEPARATOR, SEPARATOR);
        properties.setProperty(VALUE_SEPARATOR, SEPARATOR);
        when(configuration.examplesTableFactory()).thenReturn(factory);
        mockCreateExamplesTable(PATH_1, "{headerSeparator=!,valueSeparator=!}\n!var1!var3!\n!B!A|E!");
        mockCreateExamplesTable(PATH_2, "{headerSeparator=!,valueSeparator=!}\n!var2!\r\n!D|E!\n!F!");
        assertMerge(properties, "!var1!var2!var3!\n!B!D|E!A|E!\n!value!F!value!");
    }

    private void mockCreateExamplesTable(String table)
    {
        mockCreateExamplesTable(table, table);
    }

    private void mockCreateExamplesTable(String input, String table)
    {
        lenient().when(factory.createExamplesTable(input)).thenReturn(new ExamplesTable(table));
    }

    private void assertMerge(Properties properties, String expected)
    {
        assertMerge(StringUtils.EMPTY, properties, expected);
    }

    private void assertMerge(String tableBody, Properties properties, String expected)
    {
        ExamplesTableProperties examplesTableProperties = new ExamplesTableProperties(properties);
        assertEquals(expected, mergingTableTransformer.transform(tableBody, examplesTableProperties));
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
        String tableBody = StringUtils.EMPTY;
        verifyIllegalArgumentException(tableBody, properties, message);
    }

    private void verifyIllegalArgumentException(String tableBody, Properties properties, String message)
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> mergingTableTransformer.transform(tableBody, new ExamplesTableProperties(properties)));
        assertEquals(message, exception.getMessage());
    }
}
