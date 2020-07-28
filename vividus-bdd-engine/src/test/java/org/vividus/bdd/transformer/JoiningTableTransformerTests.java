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
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTableFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.spring.ExtendedConfiguration;

@ExtendWith(MockitoExtension.class)
class JoiningTableTransformerTests
{
    private static final String JOIN_MODE = "joinMode";
    private static final String ROWS = "rows";
    private static final String COLUMNS = "columns";
    private static final String PATH = "/test.table";
    private static final String MERGING_TRANSFORMER = "{transformer=MERGING, mergeMode=columns, tables="
            + "\\{transformer=FROM_EXCEL\\, path=test.csv\\, sheet=data\\, range=A2:A3\\, column=test1\\};"
            + "\\{transformer=FROM_EXCEL\\, path=test.csv\\, sheet=data\\, range=B2:B3\\, column=test2\\}}";
    private static final String COLUMNS_TO_JOIN = "columnsToJoin";
    private static final String JOINED_COLUMN = "joinedColumn";

    @Mock
    private ExamplesTableFactory factory;

    @Mock
    private ExtendedConfiguration configuration;

    @InjectMocks
    private JoiningTableTransformer joiningTableTransformer;

    @Test
    void testInvalidJoinMode()
    {
        Properties properties = new Properties();
        properties.setProperty(JOIN_MODE, "invalidMode");
        verifyIllegalArgumentException(properties, "Value of ExamplesTable property 'joinMode' must be from range "
                + "[ROWS, COLUMNS], but got 'invalidMode'");
    }

    @Test
    void testTransformInColumnsModeWithoutColumnName()
    {
        Properties properties = createProperties(COLUMNS);
        when(configuration.examplesTableFactory()).thenReturn(factory);
        verifyIllegalArgumentException(properties, "'" + JOINED_COLUMN + "' is not set in ExamplesTable properties");
    }

    @Test
    void testTransformInColumnsModeWithOneColumnName()
    {
        Properties properties = createProperties(COLUMNS);
        properties.setProperty(COLUMNS_TO_JOIN, "var8");
        properties.setProperty(JOINED_COLUMN, "var9");
        mockCreateExamplesTable(PATH, "|var7|var8|\n|z|t|\n|y|e|");
        assertJoin(PATH, properties, "|var7|var9|\n|z|t|\n|y|e|");
    }

    @Test
    void testTransformInColumnsMode()
    {
        Properties properties = createProperties(COLUMNS);
        properties.setProperty(JOINED_COLUMN, "var5");
        mockCreateExamplesTable(PATH, "|var1|var2|\n|a|b|\n|c|d|");
        assertJoin(PATH, properties, "|var5|\n|a b|\n|c d|");
    }

    @Test
    void testTransformInRowsMode()
    {
        Properties properties = createProperties(ROWS);
        mockCreateExamplesTable(PATH, "|var3|var4|\n|e|f|\n|g|h|");
        assertJoin(PATH, properties, "|var3|var4|\n|e g|f h|");
    }

    @Test
    void testRowsModeWithValueSeparator()
    {
        Properties properties = createProperties(ROWS);
        mockCreateExamplesTable(PATH, "{valueSeparator=!}\n|var1|var2|\n!A!B|C!\n!D!E|F!");
        assertJoin(PATH, properties, "|var1|var2|\n!A D!B|C E|F!");
    }

    @Test
    void testTransformsInRowsModeByRangeWithNestedTransformers()
    {
        Properties properties = createProperties(ROWS);
        mockCreateExamplesTable(MERGING_TRANSFORMER, "|var5|var6|\n|i|j|\n|k|l|");
        assertJoin(MERGING_TRANSFORMER, properties, "|var5|var6|\n|i k|j l|");
    }

    @Test
    void testTransformInColumnsModeWithColumnsNames()
    {
        Properties properties = createProperties(COLUMNS);
        properties.setProperty(COLUMNS_TO_JOIN, "var1;var4");
        properties.setProperty(JOINED_COLUMN, "var3");
        mockCreateExamplesTable(PATH, "|var1|var2|var4|\n|q|r|s|\n|t|u|v|");
        assertJoin(PATH, properties, "|var3|var2|\n|q s|r|\n|t v|u|");
    }

    private void assertJoin(String table, Properties properties, String expected)
    {
        when(configuration.examplesTableFactory()).thenReturn(factory);
        TableProperties tableProperties = new TableProperties(properties);
        assertEquals(expected, joiningTableTransformer.transform(table, null, tableProperties));
    }

    private void mockCreateExamplesTable(String path, String table)
    {
        lenient().when(factory.createExamplesTable(path)).thenReturn(new ExamplesTable(table));
    }

    private Properties createProperties(String joinMode)
    {
        Properties properties = new Properties();
        properties.setProperty(JOIN_MODE, joinMode);
        return properties;
    }

    private void verifyIllegalArgumentException(Properties properties, String message)
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> joiningTableTransformer.transform(PATH, null, new TableProperties(properties)));
        assertEquals(message, exception.getMessage());
    }
}
