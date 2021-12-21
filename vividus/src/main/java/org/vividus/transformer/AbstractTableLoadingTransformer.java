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

import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.Validate.isTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTableFactory;

public abstract class AbstractTableLoadingTransformer implements ExtendedTableTransformer
{
    @Inject private Configuration configuration;

    private final boolean forbidEmptyTables;

    protected AbstractTableLoadingTransformer(boolean forbidEmptyTables)
    {
        this.forbidEmptyTables = forbidEmptyTables;
    }

    protected List<ExamplesTable> loadTables(String tableAsString, TableProperties tableProperties)
    {
        List<String> tables = Optional.ofNullable(tableProperties.getProperties().getProperty("tables"))
                .stream()
                .map(t -> t.split("(?<!\\\\);"))
                .flatMap(Stream::of)
                .map(t -> t.replace("\\;", ";").trim())
                .distinct()
                .filter(not(String::isBlank))
                .collect(Collectors.toList());

        ExamplesTableFactory factory = configuration.examplesTableFactory();

        List<DescriptiveTable> descriptiveTables = new ArrayList<>();
        if (tableAsString.isBlank())
        {
            isTrue(tables.size() > 1, "Please, specify more than one unique table paths");
        }
        else
        {
            isTrue(!tables.isEmpty(), "Please, specify at least one table path");

            descriptiveTables.add(new DescriptiveTable("input table",
                    factory.createExamplesTable(getSeparatorsAsString(tableProperties) + tableAsString)));
        }

        List<DescriptiveTable> pathTables = IntStream.range(0, tables.size())
                 .mapToObj(index -> new DescriptiveTable(String.format("table at index %d", index + 1),
                        factory.createExamplesTable(tables.get(index))))
                 .collect(Collectors.toList());

        descriptiveTables.addAll(0, pathTables);

        if (forbidEmptyTables)
        {
            checkEmptyTables(descriptiveTables);
        }

        return descriptiveTables.stream()
                                .map(DescriptiveTable::getTable)
                                .collect(Collectors.toList());
    }

    private String getSeparatorsAsString(TableProperties tableProperties)
    {
        String headerSeparator = tableProperties.getHeaderSeparator();
        String valueSeparator = tableProperties.getValueSeparator();
        String ignorableSeparator = tableProperties.getIgnorableSeparator();
        String propertiesAsString = "";
        if (!headerSeparator.equals(valueSeparator) || !"|".equals(headerSeparator)
                || !"|--".equals(ignorableSeparator))
        {
            propertiesAsString = "{valueSeparator=" + valueSeparator + ", headerSeparator=" + headerSeparator
                    + ", ignorableSeparator=" + ignorableSeparator + "}\n";
        }
        return propertiesAsString;
    }

    private void checkEmptyTables(List<DescriptiveTable> tables)
    {
        List<String> emptyTables = tables.stream()
                                         .filter(w -> w.getTable().getRowCount() == 0)
                                         .map(DescriptiveTable::getDescription)
                                         .collect(Collectors.toList());

        isTrue(emptyTables.isEmpty(), "Empty ExamplesTable-s are not allowed, but %s is/are empty", emptyTables);
    }

    protected Configuration getConfiguration()
    {
        return configuration;
    }

    private static final class DescriptiveTable
    {
        private final String description;
        private final ExamplesTable table;

        DescriptiveTable(String description, ExamplesTable table)
        {
            this.description = description;
            this.table = table;
        }

        String getDescription()
        {
            return description;
        }

        ExamplesTable getTable()
        {
            return table;
        }
    }
}
