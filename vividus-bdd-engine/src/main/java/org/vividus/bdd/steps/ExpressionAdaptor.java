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

package org.vividus.bdd.steps;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.vividus.bdd.expression.IExpressionProcessor;

public class ExpressionAdaptor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionAdaptor.class);

    private static final Pattern GREEDY_EXPRESSION_PATTERN = Pattern.compile("#\\{((?:(?!#\\{|\\$\\{).)*)}",
            Pattern.DOTALL);
    private static final Pattern RELUCTANT_EXPRESSION_PATTERN = Pattern.compile(
            "#\\{((?:(?!#\\{|\\$\\{).)*?\\)|(?:(?!#\\{|\\$\\{).)*?)}", Pattern.DOTALL);

    private static final String REPLACEMENT_PATTERN = "\\#\\{%s\\}";

    private List<IExpressionProcessor> processors;

    public String process(String value)
    {
        try
        {
            return processExpression(value,
                    List.of(RELUCTANT_EXPRESSION_PATTERN, GREEDY_EXPRESSION_PATTERN).iterator());
        }
        catch (RuntimeException e)
        {
            LOGGER.error("Unable to process expression '{}'", value);
            throw e;
        }
    }

    private String processExpression(String value, Iterator<Pattern> expressionPatterns)
    {
        String processedValue = value;
        Matcher expressionMatcher = expressionPatterns.next().matcher(processedValue);
        boolean expressionFound = false;
        while (expressionMatcher.find())
        {
            expressionFound = true;
            String expression = expressionMatcher.group(1);
            String expressionResult = apply(expression);
            if (!expressionResult.equals(expression))
            {
                String regex = String.format(REPLACEMENT_PATTERN, Pattern.quote(expression));
                processedValue = processedValue.replaceFirst(regex, Matcher.quoteReplacement(expressionResult));
                expressionFound = false;
                expressionMatcher.reset(processedValue);
            }
        }
        if (expressionFound && expressionPatterns.hasNext())
        {
            return processExpression(processedValue, expressionPatterns);
        }
        return processedValue;
    }

    private String apply(String expression)
    {
        for (IExpressionProcessor processor : processors)
        {
            Optional<String> optional = processor.execute(expression);
            if (optional.isPresent())
            {
                return optional.get();
            }
        }
        return expression;
    }

    @Autowired
    public void setProcessors(List<IExpressionProcessor> processors)
    {
        this.processors = processors;
    }
}
