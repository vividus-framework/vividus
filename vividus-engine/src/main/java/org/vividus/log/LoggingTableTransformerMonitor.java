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

package org.vividus.log;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.model.TableTransformerMonitor;
import org.jbehave.core.model.TableTransformers.Formatting;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingTableTransformerMonitor implements TableTransformerMonitor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingTableTransformerMonitor.class);

    private static final Formatting FORMATTING_TRANSFORMER = new Formatting();
    private static final int MAX_ROWS_LOGGING = 50;

    private final TableParsers tableParsers;

    public LoggingTableTransformerMonitor(TableParsers tableParsers)
    {
        this.tableParsers = tableParsers;
    }

    @Override
    public void beforeTransformerApplying(String transformerName, TableProperties properties, String inputTable)
    {
        logTransformerInfo("Input table body", transformerName, properties, inputTable);
    }

    @Override
    public void afterTransformerApplying(String transformerName, TableProperties properties, String outputTable)
    {
        logTransformerInfo("Output (transformed) table", transformerName, properties, outputTable);
    }

    private void logTransformerInfo(String tableTitle, String transformerName, TableProperties tableProperties,
            String table)
    {
        LOGGER.atInfo()
                .addArgument(transformerName)
                .addArgument(() -> tableProperties.getPropertiesAsString().trim())
                .addArgument(() -> prepareExampleTableForLogging(table, tableProperties))
                .log(String.format("Applying table transformer: {}%nProperties: {{}}%n%s:%n{}", tableTitle));
    }

    private String prepareExampleTableForLogging(String originExampleTable, TableProperties tableProperties)
    {
        if (StringUtils.isBlank(originExampleTable))
        {
            return ">>>  EMPTY  <<<";
        }

        AtomicInteger totalNumberOrRows = new AtomicInteger();
        String rowSeparator = tableProperties.getRowSeparator();
        String resultingTable = originExampleTable.lines()
                .filter(ignored -> totalNumberOrRows.incrementAndGet() <= MAX_ROWS_LOGGING)
                .collect(Collectors.collectingAndThen(
                        Collectors.joining(rowSeparator),
                        table -> FORMATTING_TRANSFORMER.transform(table, tableParsers, tableProperties)
                ))
                .trim();
        int numberOfTruncatedRows = totalNumberOrRows.get() - MAX_ROWS_LOGGING;
        if (numberOfTruncatedRows > 0)
        {
            return String.format("%s%s...%d row(s) truncated", resultingTable, rowSeparator, numberOfTruncatedRows);
        }
        return resultingTable;
    }
}
