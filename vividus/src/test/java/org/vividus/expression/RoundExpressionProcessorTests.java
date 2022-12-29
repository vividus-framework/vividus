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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Optional;

import org.jbehave.core.steps.ParameterConverters.FluentEnumConverter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class RoundExpressionProcessorTests
{
    private final RoundExpressionProcessor processor = new RoundExpressionProcessor(new FluentEnumConverter());

    @ParameterizedTest
    @CsvSource({
        "'round(ABC)',          Invalid value to round: 'ABC'",
        "'round(1, A)',         Invalid max fraction digits value: 'A'",
        "'round(,12)',          Invalid value to round: ''",
        "'round(12,)',          Invalid max fraction digits value: ''",
        "'round(1,2,3)',        Invalid rounding mode: '3'",
        "'round(1.2,3.1.3,1)',  Invalid max fraction digits value: '3.1.3'",
        "'round(1,2,left)',     Invalid rounding mode: 'left'",
        "'round(0.15237E)',     Invalid value to round: '0.15237E'",
        "'round(0.15237E-+2)',  Invalid value to round: '0.15237E-+2'",
        "'round(0.15237-2)',    Invalid value to round: '0.15237-2'",
        "'round(0.15237+2)',    Invalid value to round: '0.15237+2'"
    })
    void shouldHandleInvalidValues(String expression, String error)
    {
        var exception = assertThrows(IllegalArgumentException.class, () -> processor.execute(expression));
        assertEquals(error, exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
        "'round(-5.9, 0)',         -6",
        "'round(-5.5, 0)',         -5",
        "'round(-5.1, 0)',         -5",
        "'round(5.1, 0)',           5",
        "'round(5.5, 0)',           6",
        "'round(5.9, 0)',           6",
        "'round(-5.59, 1)',        -5.6",
        "'round(-5.55, 1)',        -5.5",
        "'round(-5.51, 1)',        -5.5",
        "'round(5.51, 1)',          5.5",
        "'round(5.55, 1)',          5.6",
        "'round(5.59, 1)',          5.6",
        "'round(-5.559, 2)',       -5.56",
        "'round(-5.555, 2)',       -5.55",
        "'round(-5.551, 2)',       -5.55",
        "'round(5.551, 2)',         5.55",
        "'round(5.555, 2)',         5.56",
        "'round(5.559, 2)',         5.56",
        "'round(-5.559)',          -5.56",
        "'round(-5.555)',          -5.55",
        "'round(-5.551)',          -5.55",
        "'round(5.551)',            5.55",
        "'round(5.555)',            5.56",
        "'round(5.559)',            5.56",
        "'round(-5.5559, 3)',      -5.556",
        "'round(-5.5555, 3)',      -5.555",
        "'round(-5.5551, 3)',      -5.555",
        "'round(5.5551, 3)',        5.555",
        "'round(5.5555, 3)',        5.556",
        "'round(5.5559, 3)',        5.556",
        "'round(0.0)',              0",
        "'round(5.0)',              5",
        "'round(5.00000)',          5",
        "'round(-0.9, 0)',         -1",
        "'round(-.9, 0)',          -1",
        "'round(-0.5, 0)',          0",
        "'round(-.5, 0)',           0",
        "'round(-0.1, 0)',          0",
        "'round(-.1, 0)',           0",
        "'round(-0, 0)',            0",
        "'round(0, 0)',             0",
        "'round(0.1, 0)',           0",
        "'round(.1, 0)',            0",
        "'round(0.5, 0)',           1",
        "'round(.5, 0)',            1",
        "'round(0.9, 0)',           1",
        "'round(.9, 0)',            1",
        "'round(-4.9999)',         -5",
        "'round(-5.0001)',         -5",
        "'round(4.9999)',           5",
        "'round(5.0001)',           5",
        "'round(25.009, 0)',        25",
        "'round(5)',                5",
        "'round(+5)',               5",
        "'round(-5)',              -5",
        "'round(5.5)',              5.5",
        "'round(5.55, 2)',          5.55",
        "'round(5.55)',             5.55",
        "'round(5.5555, 4)',        5.5555",
        "'round(2.9467868767, 1)',  2.9",
        "'round(2.9467868768, 2)',  2.95",
        "'round(2.9467868769, 0)',  3",
        "'round(024.059, 1)',       24.1",
        "'round(24.501, 1)',        24.5",
        "'round(9999999999999999999999999999999999999999.9, 0)', 10000000000000000000000000000000000000000",
        "'round(9.9999999999999999999999999999999999999999999999999999999999999999999999999999999, 0)', 10",
        "'round(1.4, 0, ceiling)',       2",
        "'round(1.6, 0, ceiling)',       2",
        "'round(-1.4, 0, ceiling)',     -1",
        "'round(-1.6, 0, ceiling)',     -1",
        "'round(1.4, 0, floor)',         1",
        "'round(1.6, 0, floor)',         1",
        "'round(-1.4, 0, floor)',       -2",
        "'round(-1.6, 0, floor)',       -2",
        "'round(1.4444, 3, ceiling)',    1.445",
        "'round(1.6666, 3, ceiling)',    1.667",
        "'round(-1.4444, 3, ceiling)',  -1.444",
        "'round(-1.6666, 3, ceiling)',  -1.666",
        "'round(1.4444, 3, floor)',      1.444",
        "'round(1.6666, 3, floor)',      1.666",
        "'round(-1.4444, 3, floor)',    -1.445",
        "'round(-1.6666, 3, floor)',    -1.667",
        "'round(-1.4444, 3, up)',       -1.445",
        "'round(-1.4444, 3, down)',     -1.444",
        "'round(-1.4444, 3, half_up)',  -1.444",
        "'round(-1.4444, 3, half_down)',-1.444",
        "'round(-1.4444, 3, half_even)',-1.444",
        "'round(-1.1, 2, unnecessary)', -1.1",
        "'round(-1.10, 2, unnecessary)', -1.1",
        "'round(0.15237E3, 0)', 152",
        "'round(0.15237E+3, 0)', 152",
        "'round(15237E-3, 2)', 15.24",
        "'round(-1.4444, 3, half up)',  -1.444",
        "'round(-1.4444, 3, half down)',-1.444",
        "'round(-1.4444, 3, half even)',-1.444"
    })
    void testRound(String expression, String expected)
    {
        assertEquals(Optional.of(expected), processor.execute(expression));
    }
}
