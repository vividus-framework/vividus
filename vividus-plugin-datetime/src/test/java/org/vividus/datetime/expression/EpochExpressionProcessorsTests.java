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

package org.vividus.datetime.expression;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.vividus.util.DateUtils;

class EpochExpressionProcessorsTests
{
    private static final String ISO_DATE_TIME = "1993-04-16T00:00:00";

    private final EpochExpressionProcessors processor = new EpochExpressionProcessors(new DateUtils(ZoneId.of("UTC")));

    @ParameterizedTest
    @CsvSource({
            "1993-04-16T00:00:00,       734918400",
            "2020-12-11T18:43:05+05:30, 1607692385"
    })
    void testExecuteMatchingExpressionToEpoch(String date, String expectedEpoch)
    {
        assertEquals(Optional.of(expectedEpoch), processor.execute(String.format("toEpochSecond(%s)", date)));
    }

    @ParameterizedTest
    @CsvSource({
            "1.669640468E9, 2022-11-28T13:01:08",
            "734918400,     1993-04-16T00:00:00"
    })
    void testExecuteMatchingExpressionFromEpoch(String epoch, String expectedDate)
    {
        assertEquals(Optional.of(expectedDate), processor.execute(String.format("fromEpochSecond(%s)", epoch)));
    }

    @ParameterizedTest
    @ValueSource(strings = { "fromEpoch", "toEpoch" })
    void testExecuteNonMatchingExpression(String invalidExpressionName)
    {
        assertEquals(Optional.empty(),
                processor.execute(String.format("%s(%s)", invalidExpressionName, ISO_DATE_TIME)));
    }
}
