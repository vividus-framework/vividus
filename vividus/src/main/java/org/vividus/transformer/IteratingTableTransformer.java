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

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.Validate;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.vividus.util.ExamplesTableProcessor;

public class IteratingTableTransformer implements ExtendedTableTransformer
{
    private static final String START_INCLUSIVE = "startInclusive";
    private static final String END_INCLUSIVE = "endInclusive";

    private static final List<String> ITERATOR = List.of("iterator");

    @Override
    public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
    {
        checkTableEmptiness(tableAsString);

        int startInclusive = properties.getMandatoryNonBlankProperty(START_INCLUSIVE, int.class);
        int endInclusive = properties.getMandatoryNonBlankProperty(END_INCLUSIVE, int.class);
        Validate.isTrue(endInclusive >= startInclusive, "'%s' value must be less than or equal to '%s' value",
                START_INCLUSIVE, END_INCLUSIVE);

        List<String> column = IntStream.rangeClosed(startInclusive, endInclusive)
                .mapToObj(String::valueOf)
                .collect(Collectors.toList());
        return ExamplesTableProcessor.buildExamplesTableFromColumns(ITERATOR, List.of(column), properties);
    }
}
