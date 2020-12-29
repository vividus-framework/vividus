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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RemoveWrappingDoubleQuotesExpressionProcessorTests
{
    private final IExpressionProcessor<String> processor = new RemoveWrappingDoubleQuotesExpressionProcessor();

    @Test
    void testExecuteWithUnsupportedException()
    {
        assertEquals(Optional.empty(), processor.execute("removeWrappingDoubleQuote(value)"));
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
    }
}
