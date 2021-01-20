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

package org.vividus.bdd.expression;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FunctionalExpressionProcessor<T> extends AbstractExpressionProcessor<T>
{
    private static final int INPUT_DATA_GROUP = 1;

    private final Function<String, T> transformer;

    public FunctionalExpressionProcessor(String functionName, Function<String, T> transformer)
    {
        super(Pattern.compile("^" + functionName + "\\((.*)\\)$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL));
        this.transformer = transformer;
    }

    @Override
    protected T evaluateExpression(Matcher expressionMatcher)
    {
        String inputData = expressionMatcher.group(INPUT_DATA_GROUP);
        return transformer.apply(inputData);
    }
}
