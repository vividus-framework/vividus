/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.datetime.expression;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.vividus.util.DateUtils;

class EpochExpressionProcessorsTests
{
    private final EpochExpressionProcessors processor = new EpochExpressionProcessors(new DateUtils(ZoneId.of("UTC")));

    private void assertExpressionResult(String expected, String expressionName, String input)
    {
        assertEquals(Optional.of(expected), processor.execute(String.format(expressionName + "(%s)", input)));
    }

    @ParameterizedTest
    @CsvSource({
            "1993-04-16T00:00:00,           734918400,  734918400000",
            "1993-04-16T00:00:00.123,       734918400,  734918400123",
            "2020-12-11T18:43:05+05:30,     1607692385, 1607692385000",
            "2020-12-11T18:43:05.987+05:30, 1607692385, 1607692385987"
    })
    void shouldExecuteMatchingExpressionToEpoch(String date, String expectedEpochSecond, String expectedEpochMilli)
    {
        assertAll(
                () -> assertExpressionResult(expectedEpochSecond, "toEpochSecond", date),
                () -> assertExpressionResult(expectedEpochMilli, "toEpochMilli", date)
        );
    }

    @ParameterizedTest
    @CsvSource({
            "1.669640468E9, 2022-11-28T13:01:08",
            "734918400,     1993-04-16T00:00:00"
    })
    void shouldExecuteMatchingExpressionFromEpochSecond(String epoch, String expectedDate)
    {
        assertExpressionResult(expectedDate, "fromEpochSecond", epoch);
    }

    @ParameterizedTest
    @CsvSource({
            "1.669640468123E12, 2022-11-28T13:01:08.123",
            "734918400987,     1993-04-16T00:00:00.987",
            "734918400980,     1993-04-16T00:00:00.980",
            "734918400900,     1993-04-16T00:00:00.900",
            "734918400000,     1993-04-16T00:00:00.000"
    })
    void shouldExecuteMatchingExpressionFromEpochMilli(String epoch, String expectedDate)
    {
        assertExpressionResult(expectedDate, "fromEpochMilli", epoch);
    }

    @ParameterizedTest
    @ValueSource(strings = { "fromEpoch", "toEpoch" })
    void testExecuteNonMatchingExpression(String invalidExpressionName)
    {
        assertEquals(Optional.empty(),
                processor.execute(String.format("%s(%s)", invalidExpressionName, "1993-04-16T00:00:00")));
    }
}
