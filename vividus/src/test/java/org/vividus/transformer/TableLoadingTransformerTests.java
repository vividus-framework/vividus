/*
 * Copyright 2019-2022 the original author or authors.
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

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableParsers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TableLoadingTransformerTests
{
    private static final String PATH = "/inner/table.table";
    private static final String INPUT_TABLE = "/input/table.table";

    private static final String TABLES_KEY = "tables=";

    private static final String TABLE = "|col1|col2|\n|val11|val12|";

    private final Keywords keywords = new Keywords();
    @Mock private Configuration configuration;
    @Mock private ExamplesTableFactory factory;

    @BeforeEach
    void init()
    {
        when(configuration.examplesTableFactory()).thenReturn(factory);
        when(configuration.keywords()).thenReturn(keywords);
    }

    @Test
    void shouldLoadTablesWithForbiddenEmptyOnes() throws IllegalAccessException
    {
        var tableProperties = new TableProperties(TABLES_KEY + PATH, keywords, null);
        when(factory.createExamplesTable(PATH)).thenReturn(new ExamplesTable(TABLE));
        when(factory.createExamplesTable(INPUT_TABLE)).thenReturn(new ExamplesTable(TABLE));

        testTableLoading(true, INPUT_TABLE, tableProperties, TABLE, TABLE);
    }

    @ParameterizedTest
    @CsvSource({
            "~, ~, ~--'",
            "|, ~, ~--'",
            "|, |, ~--'"
    })
    void shouldLoadTablesWithForbiddenEmptyOnesNotDefaultSeparators(String headerSeparator, String valueSeparator,
            String ignorableSeparator) throws IllegalAccessException
    {
        var tableBody = String.format("%1$scol1%1$scol2%1$s%n%2$sval11%2$sval12%2$s%3$scomment", headerSeparator,
                valueSeparator, ignorableSeparator);
        var separators = String.format("headerSeparator=%s, valueSeparator=%s, ignorableSeparator=%s", headerSeparator,
                valueSeparator, ignorableSeparator);
        var table = String.format("{%s}%n%s", separators, tableBody);

        var tableProperties = new TableProperties(TABLES_KEY + PATH + ", " + separators, keywords, null);
        when(factory.createExamplesTable(PATH)).thenReturn(new ExamplesTable(table));
        when(factory.createExamplesTable(table)).thenReturn(new ExamplesTable(table));

        testTableLoading(true, tableBody, tableProperties, table, table);
    }

    @Test
    void shouldForbidEmptyTablesOnTableLoading() throws IllegalAccessException
    {
        var tablePath2 = "path2";
        var tablePath3 = "path3";

        var tableProperties = new TableProperties(TABLES_KEY + String.join(";", PATH, tablePath2, tablePath3), keywords,
                null);
        when(factory.createExamplesTable(PATH)).thenReturn(ExamplesTable.EMPTY);
        when(factory.createExamplesTable(tablePath2)).thenReturn(new ExamplesTable("|col1|\n|row1|"));
        when(factory.createExamplesTable(tablePath3)).thenReturn(ExamplesTable.EMPTY);
        when(factory.createExamplesTable(INPUT_TABLE)).thenReturn(new ExamplesTable(INPUT_TABLE));

        var transformer = createTransformer(true);

        var exception = assertThrows(IllegalArgumentException.class,
            () -> transformer.loadTables(INPUT_TABLE, tableProperties));
        assertEquals("Empty ExamplesTable-s are not allowed, but [table at index 1, table at index 3, input table] "
                + "is/are empty", exception.getMessage());
    }

    @Test
    void shouldNotForbidEmptyTablesOnTableLoading() throws IllegalAccessException
    {
        var tableProperties = new TableProperties(TABLES_KEY + PATH, keywords, null);
        when(factory.createExamplesTable(PATH)).thenReturn(ExamplesTable.EMPTY);
        when(factory.createExamplesTable(INPUT_TABLE)).thenReturn(new ExamplesTable(TABLE));

        testTableLoading(false, INPUT_TABLE, tableProperties, "", TABLE);
    }

    private TestTableLoadingTransformer createTransformer(boolean allowEmptyTables) throws IllegalAccessException
    {
        var transformer = new TestTableLoadingTransformer(allowEmptyTables);
        var configurationField = ReflectionUtils.findFields(TestTableLoadingTransformer.class,
                f -> "configuration".equals(f.getName()), HierarchyTraversalMode.BOTTOM_UP).get(0);
        ReflectionUtils.makeAccessible(configurationField);
        configurationField.set(transformer, configuration);
        return transformer;
    }

    private void testTableLoading(boolean forbidEmptyTables, String inputTable, TableProperties tableProperties,
            String resultingTable1, String resultingTable2) throws IllegalAccessException
    {
        var transformer = createTransformer(forbidEmptyTables);

        var tables = transformer.loadTables(inputTable, tableProperties);
        assertThat(tables, hasSize(2));
        assertEquals(new ExamplesTable(resultingTable1).asString(), tables.get(0).asString());
        assertEquals(new ExamplesTable(resultingTable2).asString(), tables.get(1).asString());
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
