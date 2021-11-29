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

package org.vividus.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.Point;

class PointConverterTests
{
    private final PointConverter converter = new PointConverter();

    @ParameterizedTest
    @CsvSource({
        "0, 0",
        "1, 1",
        "-1, -1"
        })
    void convertTest(int x, int y)
    {
        Point expected = new Point(x, y);
        assertEquals(expected, converter.convertValue("(" + x + ", " + y + ")", Point.class));
    }

    @Test
    void testConvertValueInvalidPointFormat()
    {
        assertThrows(IllegalArgumentException.class,
            () -> converter.convertValue("[0,0]", Point.class),
            "Unable to parse point: [0,0]. It should match regular experession: \\((-?\\d+),\\s*(-?\\d+)\\)");
    }
}
