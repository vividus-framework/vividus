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

package org.vividus.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.expression.ParseException;
import org.springframework.expression.ParserContext;
import org.springframework.expression.spel.SpelEvaluationException;
import org.springframework.expression.spel.standard.SpelExpressionParser;

public class SpelExpressionResolver
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SpelExpressionResolver.class);
    private static final SpelExpressionParser PARSER = new SpelExpressionParser();

    public Object resolve(String expressionString) throws ParseException
    {
        try
        {
            return PARSER.parseExpression(expressionString, ParserContext.TEMPLATE_EXPRESSION).getValue();
        }
        catch (SpelEvaluationException e)
        {
            LOGGER.atInfo()
                  .addArgument(expressionString)
                  .setCause(e)
                  .log("Unable to evaluate the string '{}' as SpEL expression, it'll be used as is");
            return expressionString;
        }
    }
}
