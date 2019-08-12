/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd.expression;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ReplaceByRegExpProcessor implements IExpressionProcessor
{
    private static final Map<Pattern, Function<Matcher, Function<String, String>>> EVALUATE_REG_EXP = Map.of(
            buildPattern("First"), matcher -> matcher::replaceFirst,
            buildPattern("All"),   matcher -> matcher::replaceAll);

    private static final int REG_EXP_INDEX = 0;
    private static final int REPLACEMENT_INDEX = 1;
    private static final int INPUT_INDEX = 2;

    private static Pattern buildPattern(String functionName)
    {
        return Pattern.compile("^replace" + functionName + "ByRegExp\\((?:(?=[\"]{3})([\"]{3}.*?[\"]{3})|(.+?)), "
                + "(?:(?=[\"]{3})([\"]{3}.*?[\"]{3})|(.+?)), (?:(?=[\"]{3})([\"]{3}.*?[\"]{3})|(.+?))\\)$",
                Pattern.DOTALL);
    }

    @Override
    public Optional<String> execute(String expression)
    {
        for (Entry<Pattern, Function<Matcher, Function<String, String>>> entry : EVALUATE_REG_EXP.entrySet())
        {
            Matcher expressionMatcher = entry.getKey().matcher(expression);
            if (expressionMatcher.find())
            {
                List<String> groupValues = processMatchExpressionResult(expressionMatcher);
                String regExp = groupValues.get(REG_EXP_INDEX);
                String input = groupValues.get(INPUT_INDEX);
                String replacement = groupValues.get(REPLACEMENT_INDEX);
                return Optional.of(entry.getValue().apply(Pattern.compile(regExp, Pattern.DOTALL).matcher(input))
                        .apply(replacement));
            }
        }
        return Optional.empty();
    }

    private static List<String> processMatchExpressionResult(Matcher expressionMatcher)
    {
        return IntStream.rangeClosed(1, expressionMatcher.groupCount())
                .mapToObj(expressionMatcher::group)
                .filter(Objects::nonNull)
                .map(value -> value.replaceAll("^\"\"\"|\"\"\"$", ""))
                .collect(Collectors.toList());
    }
}
