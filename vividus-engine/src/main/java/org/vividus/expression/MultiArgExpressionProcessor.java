/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.expression;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MultiArgExpressionProcessor<T> implements IExpressionProcessor<T>
{
    private static final int ARGS_GROUP = 1;

    private final Pattern pattern;
    private final String expressionName;
    private final int minArgNumber;
    private final int maxArgNumber;
    private final Function<String, ExpressionArguments> argsParser;
    private final Function<List<String>, T> evaluator;

    public MultiArgExpressionProcessor(String expressionName, int minArgNumber, int maxArgNumber,
            Function<String, ExpressionArguments> argsParser, Function<List<String>, T> evaluator)
    {
        this.pattern = Pattern.compile("^" + expressionName + "\\((.*)\\)$", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        this.expressionName = expressionName;
        this.minArgNumber = minArgNumber;
        this.maxArgNumber = maxArgNumber;
        this.argsParser = argsParser;
        this.evaluator = evaluator;
    }

    public MultiArgExpressionProcessor(String expressionName, int minArgNumber, int maxArgNumber,
            Function<List<String>, T> evaluator)
    {
        this(expressionName, minArgNumber, maxArgNumber, ExpressionArguments::new, evaluator);
    }

    public MultiArgExpressionProcessor(String expressionName, int expectedArgNumber,
            Function<List<String>, T> evaluator)
    {
        this(expressionName, expectedArgNumber, expectedArgNumber, evaluator);
    }

    public MultiArgExpressionProcessor(String expressionName, int expectedArgNumber,
            Function<String, ExpressionArguments> argsParser, Function<List<String>, T> evaluator)
    {
        this(expressionName, expectedArgNumber, expectedArgNumber, argsParser, evaluator);
    }

    @Override
    public Optional<T> execute(String expression)
    {
        Matcher expressionMatcher = pattern.matcher(expression);
        if (expressionMatcher.find())
        {
            List<String> args = parseArgs(expressionMatcher.group(ARGS_GROUP));
            T expressionResult = evaluator.apply(args);
            return Optional.of(expressionResult);
        }
        return Optional.empty();
    }

    private List<String> parseArgs(String argsAsString)
    {
        if (minArgNumber == 1 && maxArgNumber == 1)
        {
            return List.of(argsAsString);
        }
        List<String> args = argsParser.apply(argsAsString).getArguments();
        int argsNumber = args.size();
        if (minArgNumber == maxArgNumber)
        {
            if (argsNumber != minArgNumber)
            {
                throwException(argsAsString, argsNumber, error -> error.append(minArgNumber));
            }
        }
        else if (argsNumber < minArgNumber || argsNumber > maxArgNumber)
        {
            throwException(argsAsString, argsNumber, error -> error.append("from ").append(minArgNumber).append(" to ")
                    .append(maxArgNumber));
        }
        return args;
    }

    private void throwException(String argsAsString, int argsNumber, Consumer<StringBuilder> expectationsAppender)
    {
        StringBuilder errorMessageBuilder = new StringBuilder("The expected number of arguments for '")
                .append(expressionName)
                .append("' expression is ");
        expectationsAppender.accept(errorMessageBuilder);
        errorMessageBuilder.append(", but found ")
                .append(argsNumber)
                .append(" argument");
        if (argsNumber != 1)
        {
            errorMessageBuilder.append('s');
        }
        if (argsNumber > 0)
        {
            errorMessageBuilder.append(": '").append(argsAsString).append('\'');
        }
        throw new IllegalArgumentException(errorMessageBuilder.toString());
    }
}
