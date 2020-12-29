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

package org.vividus.bdd.expression;

import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

@Named
public class RandomIntExpressionProcessor implements IExpressionProcessor<Integer>
{
    private static final String INT_NUMBER_REGEX = "(-?[1-9]\\d*|0)";
    private static final Pattern RANDOM_VALUE_PATTERN = Pattern.compile(
            "^randomInt\\(" + INT_NUMBER_REGEX + ",\\s*" + INT_NUMBER_REGEX + "\\)$", Pattern.CASE_INSENSITIVE);

    @Override
    public Optional<Integer> execute(String expression)
    {
        Matcher expressionMatcher = RANDOM_VALUE_PATTERN.matcher(expression);
        if (expressionMatcher.find())
        {
            int minInclusive = Integer.parseInt(expressionMatcher.group(1));
            int maxInclusive = Integer.parseInt(expressionMatcher.group(2));
            int randomInt = ThreadLocalRandom.current().nextInt(minInclusive, maxInclusive + 1);
            return Optional.of(randomInt);
        }
        return Optional.empty();
    }
}
