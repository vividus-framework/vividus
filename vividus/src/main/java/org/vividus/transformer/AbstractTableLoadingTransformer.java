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

package org.vividus.transformer;

import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.Validate.isTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTableFactory;

import jakarta.inject.Inject;

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
                .toList();

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
                 .toList();

        descriptiveTables.addAll(0, pathTables);

        if (forbidEmptyTables)
        {
            checkEmptyTables(descriptiveTables);
        }

        return descriptiveTables.stream().map(DescriptiveTable::table).toList();
    }

    private String getSeparatorsAsString(TableProperties tableProperties)
    {
        Keywords keywords = configuration.keywords();
        String headerSeparator = tableProperties.getHeaderSeparator();
        String valueSeparator = tableProperties.getValueSeparator();
        String ignorableSeparator = tableProperties.getIgnorableSeparator();
        if (keywords.examplesTableHeaderSeparator().equals(headerSeparator)
                && keywords.examplesTableValueSeparator().equals(valueSeparator)
                && keywords.examplesTableIgnorableSeparator().equals(ignorableSeparator))
        {
            return "";
        }
        return String.format("{headerSeparator=%s, valueSeparator=%s, ignorableSeparator=%s}%n", headerSeparator,
                valueSeparator, ignorableSeparator);
    }

    private void checkEmptyTables(List<DescriptiveTable> tables)
    {
        List<String> emptyTables = tables.stream()
                                         .filter(w -> w.table().getRowCount() == 0)
                                         .map(DescriptiveTable::description)
                                         .toList();

        isTrue(emptyTables.isEmpty(), "Empty ExamplesTable-s are not allowed, but %s is/are empty", emptyTables);
    }

    protected Configuration getConfiguration()
    {
        return configuration;
    }

    private record DescriptiveTable(String description, ExamplesTable table)
    {
    }
}
