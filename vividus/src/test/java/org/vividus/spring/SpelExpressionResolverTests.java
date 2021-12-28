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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.expression.spel.SpelEvaluationException;

@ExtendWith(TestLoggerFactoryExtension.class)
class SpelExpressionResolverTests
{
    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(SpelExpressionResolver.class);
    private static final SpelExpressionResolver PARSER = new SpelExpressionResolver();

    @Test
    void shouldResolveSpellExpression()
    {
        assertEquals(2, PARSER.resolve("#{1 + 1}"));
    }

    @Test
    void shouldLogAnExceptionAndReturnOriginalValue()
    {
        var expressionString = "#{anyOf(1,2,3)}";
        assertEquals(expressionString, PARSER.resolve(expressionString));
        var loggingEvents = LOGGER.getLoggingEvents();
        assertThat(loggingEvents, Matchers.hasSize(1));
        LoggingEvent event = loggingEvents.get(0);
        assertEquals("Unable to evaluate the string '#{anyOf(1,2,3)}' as SpEL expression, it'll be used as is",
            event.getFormattedMessage());
        assertInstanceOf(SpelEvaluationException.class, event.getThrowable().get());
    }
}
