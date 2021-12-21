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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableParsers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TableLoadingTransformerTests
{
    private static final String PATH = "path";
    private static final String INPUT_TABLE = "|input_table_col|";
    private static final String DEFAULT_SEPARATOR = "|";

    @Mock private Configuration configuration;
    @Mock private TableProperties tableProperties;
    @Mock private ExamplesTableFactory factory;

    @BeforeEach
    void init()
    {
        when(configuration.examplesTableFactory()).thenReturn(factory);
    }

    @Test
    void shouldLoadTablesWithForbiddenEmptyOnes() throws IllegalAccessException
    {
        String table = "|col1|col2|\n|val11|val12|";

        when(tableProperties.getProperties()).thenReturn(createProperties(PATH));
        when(factory.createExamplesTable(PATH)).thenReturn(new ExamplesTable(table));
        when(factory.createExamplesTable(INPUT_TABLE)).thenReturn(new ExamplesTable(table));
        mockSeparators();

        TestTableLoadingTransformer transformer = createTransformer(true);

        List<ExamplesTable> tables = transformer.loadTables(INPUT_TABLE, tableProperties);
        assertThat(tables, hasSize(2));
        String tableAsString = new ExamplesTable(table).asString();
        assertEquals(tableAsString, tables.get(0).asString());
        assertEquals(tableAsString, tables.get(1).asString());
    }

    @Test
    void shouldLoadTablesWithForbiddenEmptyOnesNotDefaultSeparators() throws IllegalAccessException
    {
        String table = "~col1~col2~\n~val11~val12~";
        String separator = "~";

        when(tableProperties.getProperties()).thenReturn(createProperties(PATH));
        when(factory.createExamplesTable(PATH)).thenReturn(new ExamplesTable(table));
        when(factory.createExamplesTable("{valueSeparator=~, headerSeparator=~, ignorableSeparator=~--}\n"
                + INPUT_TABLE)).thenReturn(new ExamplesTable(table));
        when(tableProperties.getHeaderSeparator()).thenReturn(separator);
        when(tableProperties.getValueSeparator()).thenReturn(separator);
        when(tableProperties.getIgnorableSeparator()).thenReturn("~--");

        TestTableLoadingTransformer transformer = createTransformer(true);

        List<ExamplesTable> tables = transformer.loadTables(INPUT_TABLE, tableProperties);
        assertThat(tables, hasSize(2));
        String tableAsString = new ExamplesTable(table).asString();
        assertEquals(tableAsString, tables.get(0).asString());
        assertEquals(tableAsString, tables.get(1).asString());
    }

    @Test
    void shouldForbidEmptyTablesOnTableLoading() throws IllegalAccessException
    {
        String tablePath2 = "path2";
        String tablePath3 = "path3";

        when(tableProperties.getProperties()).thenReturn(
                createProperties(Stream.of(PATH, tablePath2, tablePath3).collect(Collectors.joining(";"))));
        when(factory.createExamplesTable(PATH)).thenReturn(ExamplesTable.EMPTY);
        when(factory.createExamplesTable(tablePath2)).thenReturn(new ExamplesTable("|col1|\n|row1|"));
        when(factory.createExamplesTable(tablePath3)).thenReturn(ExamplesTable.EMPTY);
        when(factory.createExamplesTable(INPUT_TABLE)).thenReturn(new ExamplesTable(INPUT_TABLE));
        mockSeparators();

        TestTableLoadingTransformer transformer = createTransformer(true);

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
            () -> transformer.loadTables(INPUT_TABLE, tableProperties));
        assertEquals("Empty ExamplesTable-s are not allowed, but [table at index 1, table at index 3, input table] "
                + "is/are empty", thrown.getMessage());
    }

    @Test
    void shouldNotForbidEmptyTablesOnTableLoading() throws IllegalAccessException
    {
        when(tableProperties.getProperties()).thenReturn(createProperties(PATH));
        when(factory.createExamplesTable(PATH)).thenReturn(ExamplesTable.EMPTY);
        when(factory.createExamplesTable(INPUT_TABLE)).thenReturn(new ExamplesTable(INPUT_TABLE));
        mockSeparators();

        TestTableLoadingTransformer transformer = createTransformer(false);

        List<ExamplesTable> tables = transformer.loadTables(INPUT_TABLE, tableProperties);
        assertThat(tables, hasSize(2));
    }

    private TestTableLoadingTransformer createTransformer(boolean allowEmptyTables) throws IllegalAccessException
    {
        TestTableLoadingTransformer transformer = new TestTableLoadingTransformer(allowEmptyTables);
        Field configurationField = ReflectionUtils.findFields(TestTableLoadingTransformer.class,
                f -> "configuration".equals(f.getName()), HierarchyTraversalMode.BOTTOM_UP).get(0);
        ReflectionUtils.makeAccessible(configurationField);
        configurationField.set(transformer, configuration);
        return transformer;
    }

    private void mockSeparators()
    {
        when(tableProperties.getHeaderSeparator()).thenReturn(DEFAULT_SEPARATOR);
        when(tableProperties.getValueSeparator()).thenReturn(DEFAULT_SEPARATOR);
        when(tableProperties.getIgnorableSeparator()).thenReturn("|--");
    }

    private static Properties createProperties(String tables)
    {
        Properties properties = new Properties();
        properties.put("tables", tables);
        return properties;
    }

    private static final class TestTableLoadingTransformer extends AbstractTableLoadingTransformer
    {
        private TestTableLoadingTransformer(boolean forbidEmptyTables)
        {
            super(forbidEmptyTables);
        }

        @Override
        public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
        {
            throw new UnsupportedOperationException();
        }
    }
}
