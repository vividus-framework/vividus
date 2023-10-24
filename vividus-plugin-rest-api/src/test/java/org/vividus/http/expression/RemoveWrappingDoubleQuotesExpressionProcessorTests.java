/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.http.expression;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.jbehave.core.expressions.ExpressionProcessor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

@ExtendWith(TestLoggerFactoryExtension.class)
class RemoveWrappingDoubleQuotesExpressionProcessorTests
{
    private final TestLogger logger = TestLoggerFactory.getTestLogger(
            RemoveWrappingDoubleQuotesExpressionProcessor.class);

    private final ExpressionProcessor<String> processor = new RemoveWrappingDoubleQuotesExpressionProcessor();

    @Test
    void testExecuteWithUnsupportedException()
    {
        assertEquals(Optional.empty(), processor.execute("removeWrappingDoubleQuote(value)"));
        assertThat(logger.getLoggingEvents(), is(empty()));
    }

    @ParameterizedTest(name = "{index}: for expression \"{0}\", result is \"{1}\"")
    @CsvSource({
        "removeWrappingDoubleQuotes(\"value\"),     value",
        "removeWrappingDoubleQuotes(value),         value",
        "removeWrappingDoubleQuotes(),              ''",
        "removeWrappingDoubleQuotes(\"\"),          ''",
        "removeWrappingDoubleQuotes(\"\"\"),        \"",
        "removeWrappingDoubleQuotes(\"value),       \"value",
        "removeWrappingDoubleQuotes(v\"alu\"e),     v\"alu\"e",
        "removeWrappingDoubleQuotes(\"va\"lu\"e\"), va\"lu\"e",
        "removeWrappingDoubleQuotes(\"va\"lu\"e),   \"va\"lu\"e",
        "removeWrappingDoubleQuotes(\"va\"lue),     \"va\"lue",
        "removeWrappingDoubleQuotes(va\"lue\"),     va\"lue\"",
        "RemoveWrappingdOUBLEQuotes(\"value\"),     value",
        "RemoveWrappingdOUBLEQuotes(\"value),       \"value",
        "removeWrappingDoubleQuotes(), ''"
        })
    void testExecute(String expression, String expected)
    {
        assertEquals(expected, processor.execute(expression).get());
        assertThat(logger.getLoggingEvents(), equalTo(List.of(
                warn("#{removeWrappingDoubleQuotes(..)} expression is deprecated and will be removed in VIVIDUS 0.7.0."
                        + " Please use JSON steps validating and saving JSON element values instead"))));
    }
}
