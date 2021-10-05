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

package org.vividus.bdd.steps;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.jbehave.core.embedder.StoryControls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.bdd.DryRunAwareExecutor;
import org.vividus.bdd.expression.IExpressionProcessor;

public class ExpressionAdaptor implements DryRunAwareExecutor
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ExpressionAdaptor.class);

    private static final Pattern GREEDY_EXPRESSION_PATTERN = Pattern.compile("#\\{((?:(?!#\\{|\\$\\{).)*)}",
            Pattern.DOTALL);
    private static final Pattern RELUCTANT_EXPRESSION_PATTERN = Pattern.compile(
            "#\\{((?:(?!#\\{|\\$\\{).)*?\\)|(?:(?!#\\{|\\$\\{).)*?)}", Pattern.DOTALL);

    private static final String REPLACEMENT_PATTERN = "\\#\\{%s\\}";

    private final StoryControls storyControls;

    private List<IExpressionProcessor<?>> processors;

    public ExpressionAdaptor(StoryControls storyControls)
    {
        this.storyControls = storyControls;
    }

    /**
     * Processes the expression including nested ones.
     * <br>
     * Syntax:
     * <br>
     * <code>
     * #{expression(arguments...)}
     * #{expression(arguments..., #{expression(arguments...)})}
     * </code>
     * <br>
     * Example:
     * <br>
     * <code>
     * #{shiftDate("1942-12-02T01:23:40+04:00", "yyyy-MM-dd'T'HH:mm:ssz", "P43Y4M3W3D")}
     * <br>
     * #{encodeToBase64(#{fromEpochSecond(-523641111)})}
     * </code>
     *
     * @param expression the expression to process
     * @return the result of processed expression
     */
    public Object processRawExpression(String expression)
    {
        return execute(() -> processExpression(expression, () -> processExpression(expression,
                List.of(RELUCTANT_EXPRESSION_PATTERN, GREEDY_EXPRESSION_PATTERN).iterator())), expression);
    }

    /**
     * Processes the expression excluding nested ones.
     * <br>
     * Syntax:
     * <br>
     * <code>
     * expression(arguments...)
     * </code>
     * <br>
     * Example:
     * <br>
     * <code>
     * shiftDate("1942-12-02T01:23:40+04:00", "yyyy-MM-dd'T'HH:mm:ssz", "P43Y4M3W3D")
     * </code>
     *
     * @param expression the expression to process
     * @return the result of processed expression
     */
    public Object processExpression(String expression)
    {
        return processExpression(expression, () -> apply(expression));
    }

    private Object processExpression(String value, Supplier<Object> expressionResolver)
    {
        try
        {
            return expressionResolver.get();
        }
        catch (RuntimeException e)
        {
            LOGGER.error("Unable to process expression '{}'", value);
            throw e;
        }
    }

    private Object processExpression(String value, Iterator<Pattern> expressionPatterns)
    {
        String processedValue = value;
        Matcher expressionMatcher = expressionPatterns.next().matcher(processedValue);
        boolean expressionFound = false;
        while (expressionMatcher.find())
        {
            expressionFound = true;
            String expression = expressionMatcher.group(1);
            Object expressionResult = apply(expression);
            if (!(expressionResult instanceof String) && ("#{" + expression + "}").equals(processedValue))
            {
                return expressionResult;
            }
            if (!expressionResult.equals(expression))
            {
                String regex = String.format(REPLACEMENT_PATTERN, Pattern.quote(expression));
                processedValue = processedValue.replaceFirst(regex,
                    Matcher.quoteReplacement(String.valueOf(expressionResult)));
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

    private Object apply(String expression)
    {
        for (IExpressionProcessor<?> processor : processors)
        {
            Optional<?> optional = processor.execute(expression);
            if (optional.isPresent())
            {
                return optional.get();
            }
        }
        return expression;
    }

    @Inject
    public void setProcessors(List<IExpressionProcessor<?>> processors)
    {
        this.processors = processors;
    }

    @Override
    public StoryControls getStoryControls()
    {
        return storyControls;
    }
}
