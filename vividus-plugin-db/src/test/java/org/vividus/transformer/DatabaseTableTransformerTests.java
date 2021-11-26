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
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.vividus.db.DataSourceManager;

@ExtendWith(MockitoExtension.class)
class DatabaseTableTransformerTests
{
    private static final String EMPTY_EXAMPLES_TABLE = "";
    private static final String DB_KEY = "testdb";
    private static final String QUERY = "select * from table";
    private static final String COLUMN = "column";
    private static final String TABLE_PROPERTIES = "dbKey=testdb, sqlQuery=";
    private static final String VALUE = "value";
    private static final String VALUE_2 = "value2";

    private final Keywords keywords = new Keywords();
    private final ParameterConverters converters = new ParameterConverters();

    private final DataSourceManager dataSourceManager = mock(DataSourceManager.class);

    @InjectMocks private final DatabaseTableTransformer transformer = new DatabaseTableTransformer(dataSourceManager);

    static Stream<Arguments> matchingResultToTable()
    {
        Map<String, Object> row = new HashMap<>();
        row.put(COLUMN, null);
        return Stream.of(
                arguments(
                        List.of(Map.of(COLUMN, VALUE)),
                        "\\|column\\|\n\\|value\\|"
                ),
                arguments(
                        List.of(row),
                        "\\|column\\|\n\\|null\\|"
                ),
                arguments(
                        List.of(Map.of(COLUMN, VALUE, "column2", VALUE_2)),
                        "(\\|column2\\|column\\|\\n\\|value2\\|value\\|)"
                                + "|(\\|column\\|column2\\|\\n\\|value\\|value2\\|)"
                ),
                arguments(
                        List.of(Map.of(COLUMN, VALUE), Map.of(COLUMN, VALUE_2)),
                        "\\|column\\|\n\\|value\\|\n\\|value2\\|"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("matchingResultToTable")
    void shouldCreateExamplesTableFromDb(List<Map<String, Object>> result, String expectedTablePattern)
    {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(dataSourceManager.getJdbcTemplate(DB_KEY)).thenReturn(jdbcTemplate);
        when(jdbcTemplate.queryForList(QUERY)).thenReturn(result);
        var tableProperties = new TableProperties(TABLE_PROPERTIES + QUERY, keywords, converters);
        String actual = transformer.transform(EMPTY_EXAMPLES_TABLE, null, tableProperties);
        assertThat(actual, matchesPattern(expectedTablePattern));
    }

    @ParameterizedTest
    @CsvSource({
            "'',                        'dbKey' is not set in ExamplesTable properties",
            "'dbKey= ',                 ExamplesTable property 'dbKey' is blank",
            "'dbKey=testdb',            'sqlQuery' is not set in ExamplesTable properties",
            "'dbKey=testdb,sqlQuery= ', ExamplesTable property 'sqlQuery' is blank"
    })
    void shouldThrowErrorIfInvalidParametersAreProvided(String propertiesAsString, String errorMessage)
    {
        var tableProperties = new TableProperties(propertiesAsString, keywords, converters);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> transformer.transform(EMPTY_EXAMPLES_TABLE, null, tableProperties));
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void shouldEmptyThrowResultDataException()
    {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(dataSourceManager.getJdbcTemplate(DB_KEY)).thenReturn(jdbcTemplate);
        var tableProperties = new TableProperties(TABLE_PROPERTIES + QUERY, keywords, converters);
        var exception = assertThrows(EmptyResultDataAccessException.class,
                () -> transformer.transform(EMPTY_EXAMPLES_TABLE, null, tableProperties));
        assertEquals("Result was expected to have at least one row", exception.getMessage());
    }

    @Test
    void shouldCreateExamplesTableFromDbWithNullReplacement()
    {
        Map<String, Object> row = new HashMap<>();
        row.put(COLUMN, null);

        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(dataSourceManager.getJdbcTemplate(DB_KEY)).thenReturn(jdbcTemplate);
        when(jdbcTemplate.queryForList(QUERY)).thenReturn(List.of(row));
        var tableProperties = new TableProperties(TABLE_PROPERTIES + QUERY + ", nullReplacement=\"\"",
                keywords, converters);
        String actual = transformer.transform(EMPTY_EXAMPLES_TABLE, null, tableProperties);
        assertEquals(actual, "|column|\n|\"\"|");
    }
}
