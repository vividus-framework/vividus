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

package org.vividus.ui.web.expression;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class DecodeDataUrlExpressionTests
{
    private static final String BLACK_B64_PIXEL = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAAXNSR0IAr"
        + "s4c6QAAAA1JREFUGFdjYGBg+A8AAQQBAHAgZQsAAAAASUVORK5CYII=";

    private final DecodeDataUrlExpression expression = new DecodeDataUrlExpression();

    @Test
    void shouldParseBinaryData()
    {
        var blackPixelBytes = new byte[] {
            -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0, 31,
            21, -60, -119, 0, 0, 0, 1, 115, 82, 71, 66, 0, -82, -50, 28, -23, 0, 0, 0, 13, 73, 68, 65, 84, 24, 87, 99,
            96, 96, 96, -8, 15, 0, 1, 4, 1, 0, 112, 32, 101, 11, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126 };
        assertArrayEquals(blackPixelBytes,
                (byte[]) expression.execute("decodeDataUrl(data:image/png;base64," + BLACK_B64_PIXEL + ")").get());
    }

    @Test
    void shouldThrowAnExceptionInCaseOfInvalidArgument()
    {
        var iae = assertThrows(IllegalArgumentException.class, () -> expression.execute("decodeDataUrl(incorrect)"));
        assertEquals("Supplied argument `incorrect` is invalid Data URL", iae.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "'decodeDataUrl(data:,value)',                                  value",
            "'decodeDataUrl(data:text/plain,value)',                        value",
            "'decodeDataUrl(data:text/plain;base64,SGVsbG8sIFdvcmxkIQ==)', 'Hello, World!'",
            "'decodeDataUrl(data:;base64,SGVsbG8sIFdvcmxkIQ==)',           'Hello, World!'"
    })
    void shouldParseData(String dataUrl, Object expectedValue)
    {
        assertEquals(expectedValue, expression.execute(dataUrl).get());
    }
}
