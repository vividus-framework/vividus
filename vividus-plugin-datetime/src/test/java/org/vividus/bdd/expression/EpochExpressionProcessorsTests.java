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

import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.vividus.util.DateUtils;

class EpochExpressionProcessorsTests
{
    private static final String ISO_DATE_TIME = "1993-04-16T00:00:00";
    private static final String EPOCH = "734918400";

    private final EpochExpressionProcessors processor = new EpochExpressionProcessors(new DateUtils(ZoneId.of("UTC")));

    @ParameterizedTest
    @CsvSource({
            ISO_DATE_TIME + ", " + EPOCH,
            "2020-12-11T18:43:05+05:30, 1607692385"
    })
    void testExecuteMatchingExpressionToEpoch(String date, String expectedEpoch)
    {
        assertEquals(Optional.of(expectedEpoch), processor.execute(String.format("toEpochSecond(%s)", date)));
    }

    @Test
    void testExecuteMatchingExpressionFromEpoch()
    {
        assertEquals(Optional.of(ISO_DATE_TIME), processor.execute(String.format("fromEpochSecond(%s)", EPOCH)));
    }

    @Test
    void testExecuteNonMatchingExpression()
    {
        assertEquals(Optional.empty(), processor.execute(String.format("toEpoch(%s)", ISO_DATE_TIME)));
    }
}
