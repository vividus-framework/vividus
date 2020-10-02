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

package org.vividus.beans;

import java.beans.PropertyEditorSupport;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.vividus.model.IntegerRange;

public class IntegerRangePropertyEditor extends PropertyEditorSupport
{
    private static final Map<Pattern, Function<Matcher, List<Integer>>> REGEX_HANDLERS = Map.of(
            Pattern.compile("(-?+\\d++)\\.\\.(-?+\\d++)"), parseRange(),
            Pattern.compile("-?+\\d++"), m -> List.of(Integer.valueOf(m.group())));

    private static Function<Matcher, List<Integer>> parseRange()
    {
        return m ->
        {
            int start = Integer.parseInt(m.group(1));
            int end = Integer.parseInt(m.group(2));

            return IntStream.rangeClosed(start, end)
                    .boxed()
                    .collect(Collectors.toList());
        };
    }

    @Override
    public void setAsText(String source)
    {
        IntegerRange intRange = Stream.of(source.split(",")).map(value ->
        {
            for (Map.Entry<Pattern, Function<Matcher, List<Integer>>> handler : REGEX_HANDLERS.entrySet())
            {
                Matcher matcher = handler.getKey().matcher(value);
                if (matcher.matches())
                {
                    return handler.getValue().apply(matcher);
                }
            }
            throw new IllegalArgumentException(
                    "Expected integers in format 'number' or 'number..number' but got: " + value);
        })
        .flatMap(List::stream)
        .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), IntegerRange::new));
        setValue(intRange);
    }
}
