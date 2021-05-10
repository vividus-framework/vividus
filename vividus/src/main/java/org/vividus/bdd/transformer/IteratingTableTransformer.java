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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Named;

import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.vividus.bdd.util.ExamplesTableProcessor;

@Named("ITERATING")
public class IteratingTableTransformer implements ExtendedTableTransformer
{
    private static final List<String> ITERATOR = List.of("iterator");

    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
    {
        checkTableEmptiness(tableAsString);
        int limit = properties.getMandatoryIntProperty("limit");
        List<String> column = Stream.iterate(0, i -> i + 1)
                .limit(limit)
                .map(String::valueOf)
                .collect(Collectors.toList());
        return ExamplesTableProcessor.buildExamplesTableFromColumns(ITERATOR, List.of(column), properties);
    }
}
