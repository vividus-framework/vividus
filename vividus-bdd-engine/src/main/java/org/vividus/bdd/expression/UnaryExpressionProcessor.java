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
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UnaryExpressionProcessor implements IExpressionProcessor
{
    private static final int INPUT_DATA_GROUP = 1;

    private final Pattern pattern;
    private final UnaryOperator<String> transformer;

    public UnaryExpressionProcessor(String functionName, UnaryOperator<String> transformer)
    {
        pattern = Pattern.compile("^" + functionName + "\\((.*)\\)$", Pattern.CASE_INSENSITIVE);
        this.transformer = transformer;
    }

    @Override
    public Optional<String> execute(String expression)
    {
        Matcher expressionMatcher = pattern.matcher(expression);
        if (expressionMatcher.find())
        {
            String inputData = expressionMatcher.group(INPUT_DATA_GROUP);
            return Optional.of(transformer.apply(inputData));
        }
        return Optional.empty();
    }
}
