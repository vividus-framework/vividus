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

import static java.util.function.Predicate.not;
import static org.apache.commons.lang3.Validate.isTrue;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableProperties;
import org.vividus.bdd.spring.ExtendedConfiguration;

@Named("MERGING")
public class MergingTableTransformer implements ExtendedTableTransformer
{
    @Inject private ExtendedConfiguration configuration;

    @Override
    public String transform(String tableAsString, ExamplesTableProperties properties)
    {
        MergeMode mergeMode = getMandatoryEnumProperty(properties, "mergeMode", MergeMode.class);

        List<String> tables = Optional.ofNullable(properties.getProperties().getProperty("tables"))
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
            isTrue(tables.size() > 0, "Please, specify at least one table path");
            examplesTablesStream = Stream.concat(examplesTablesStream, Stream.of(tableAsString));
        }

        List<ExamplesTable> examplesTables = examplesTablesStream
                .map(p -> configuration.examplesTableFactory().createExamplesTable(p))
                .collect(Collectors.toCollection(LinkedList::new));
        String fillerValue = properties.getProperties().getProperty("fillerValue");
        return mergeMode.merge(examplesTables, properties, Optional.ofNullable(fillerValue));
    }
}
