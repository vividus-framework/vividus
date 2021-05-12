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

package org.vividus.bdd.transformer;

import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.Validate.isTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.jbehave.core.configuration.Configuration;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.ExamplesTableFactory;

public abstract class AbstractTableLoadingTransformer implements ExtendedTableTransformer
{
    @Inject private Configuration configuration;

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

        Stream<String> examplesTablesStream = tables.stream();
        if (tableAsString.isBlank())
        {
            isTrue(tables.size() > 1, "Please, specify more than one unique table paths");
        }
        else
        {
            isTrue(!tables.isEmpty(), "Please, specify at least one table path");
            examplesTablesStream = Stream.concat(examplesTablesStream, Stream.of(tableAsString));
        }

        ExamplesTableFactory factory = configuration.examplesTableFactory();
        return examplesTablesStream.map(factory::createExamplesTable)
                                   .collect(Collectors.toCollection(LinkedList::new));
    }
}
