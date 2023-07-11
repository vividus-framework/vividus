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

package org.vividus.log;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.event.Level;

@SuppressWarnings({"MultipleStringLiterals", "MultipleStringLiteralsExtended"})
@ExtendWith(TestLoggerFactoryExtension.class)
class LoggingTableTransformerMonitorTests
{
    private static final String LINE_SEPARATOR = System.lineSeparator();
    private static final String TRANSFORMER_NAME = "TEST_TRANSFORMER";
    private static final String BEFORE_APPLYING_FORMAT = "Applying table transformer: {}" + LINE_SEPARATOR
                                                       + "Properties: {{}}" + LINE_SEPARATOR
                                                       + "Input table body:" + LINE_SEPARATOR
                                                       + "{}";
    private static final String AFTER_APPLYING_FORMAT = "Applying table transformer: {}" + LINE_SEPARATOR
                                                      + "Properties: {{}}" + LINE_SEPARATOR
                                                      + "Output (transformed) table:" + LINE_SEPARATOR
                                                      + "{}";
    private static final String EXAMPLE_TABLE = "|Earth|Moon  |\n"
                                              + "!Mars !Deimos!\n";
    private static final String LOGGED_EXAMPLE_TABLE = "|Earth|Moon  |\n"
                                                     + "!Mars !Deimos!";
    private static final String LOGGED_PROPERTIES = "transformer=TEST_TRANSFORMER, valueSeparator=!";
    private static final int MAX_ROWS_LOGGING = 50;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(LoggingTableTransformerMonitor.class);
    private final Keywords keywords = new Keywords();
    private final ParameterConverters parameterConverters = new ParameterConverters();
    private final TableParsers tableParsers = new TableParsers(parameterConverters);
    private final LoggingTableTransformerMonitor monitor = new LoggingTableTransformerMonitor(tableParsers);
    private final TableProperties tableProperties = new TableProperties(
            "transformer=TEST_TRANSFORMER, valueSeparator=!", keywords, parameterConverters);

    @Test
    void shouldLogTableTransformerBeforeAndAfter()
    {
        monitor.beforeTransformerApplying(TRANSFORMER_NAME, tableProperties, EXAMPLE_TABLE);
        monitor.afterTransformerApplying(TRANSFORMER_NAME, tableProperties, EXAMPLE_TABLE);

        assertThat(logger.getLoggingEvents(), is(List.of(
                info(BEFORE_APPLYING_FORMAT, TRANSFORMER_NAME, LOGGED_PROPERTIES, LOGGED_EXAMPLE_TABLE),
                info(AFTER_APPLYING_FORMAT, TRANSFORMER_NAME, LOGGED_PROPERTIES, LOGGED_EXAMPLE_TABLE)
        )));
    }

    @ParameterizedTest
    @CsvSource({
            "'',       |header|, >>>  EMPTY  <<<,    |header|",
            "|header|, '',       |header|,           >>>  EMPTY  <<<"
    })
    void shouldLogTableTransformerBeforeAndAfterIfTablesEmpty(String inputTable, String outputTable,
            String loggedInputTable, String loggedOutputTable)
    {
        monitor.beforeTransformerApplying(TRANSFORMER_NAME, tableProperties, inputTable);
        monitor.afterTransformerApplying(TRANSFORMER_NAME, tableProperties, outputTable);

        assertThat(logger.getLoggingEvents(), is(List.of(
                info(BEFORE_APPLYING_FORMAT, TRANSFORMER_NAME, LOGGED_PROPERTIES, loggedInputTable),
                info(AFTER_APPLYING_FORMAT, TRANSFORMER_NAME, LOGGED_PROPERTIES, loggedOutputTable)
        )));
    }

    @Test
    void shouldLogTableTransformerWithTruncatedTable()
    {
        var rowsCountWithoutHeader = 51;
        var rowHeader = "|header|\n";
        var rowLine = "!row   !\n";
        var transformerTable = rowHeader + rowLine.repeat(rowsCountWithoutHeader);
        var loggedTable = rowHeader + rowLine.repeat(MAX_ROWS_LOGGING - 1).trim() + "\n...2 row(s) truncated";

        monitor.afterTransformerApplying(TRANSFORMER_NAME, tableProperties, transformerTable);

        assertThat(logger.getLoggingEvents(), is(List.of(
                info(AFTER_APPLYING_FORMAT, TRANSFORMER_NAME, LOGGED_PROPERTIES, loggedTable)
        )));
    }

    @Test
    void shouldNotProcessTransformerIfLoggerLevelInfoNotUsing()
    {
        var tablePropertiesMocked = mock(TableProperties.class);

        logger.setEnabledLevels(Level.DEBUG);
        monitor.beforeTransformerApplying(TRANSFORMER_NAME, tablePropertiesMocked, EXAMPLE_TABLE);
        monitor.afterTransformerApplying(TRANSFORMER_NAME, tablePropertiesMocked, EXAMPLE_TABLE);

        verifyNoInteractions(tablePropertiesMocked);
    }
}
