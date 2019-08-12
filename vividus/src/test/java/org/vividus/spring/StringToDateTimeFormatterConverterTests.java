/*
 * Copyright 2019 the original author or authors.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.format.DateTimeFormatter;

import org.junit.jupiter.api.Test;

class StringToDateTimeFormatterConverterTests
{
    private final StringToDateTimeFormatterConverter stringToDateTimeFormatterConverter =
            new StringToDateTimeFormatterConverter();

    @Test
    void testConvert()
    {
        String format = "hh:mm";
        assertEquals(DateTimeFormatter.ofPattern(format).toString(),
                stringToDateTimeFormatterConverter.convert(format).toString());
    }

    @Test
    void testConvertIllegalPattern()
    {
        assertThrows(IllegalArgumentException.class, () -> stringToDateTimeFormatterConverter.convert("hhh"));
    }
}
