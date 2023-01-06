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

package org.vividus.expression;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.doThrow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.stream.Stream;
import java.util.zip.GZIPOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;

class Base64ExpressionProcessorsTests
{
    private static final String BLACK_B64_PIXEL = "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAAAXNSR0IAr"
            + "s4c6QAAAA1JREFUGFdjYGBg+A8AAQQBAHAgZQsAAAAASUVORK5CYII=";

    private static final String BASE_64 = "YmlnIGRhdGE=";

    private final Base64ExpressionProcessors processors = new Base64ExpressionProcessors();

    static Stream<Arguments> expressionSource()
    {
        // CHECKSTYLE:OFF
        return Stream.of(
                arguments("resourceToBase64(/org/vividus/expressions/resource.txt)",       BASE_64),
                arguments("resourceToBase64(org/vividus/expressions/resource.txt)",        BASE_64),
                arguments("decodeFromBase64(QmFydWNo)",                                    "Baruch"),
                arguments("encodeToBase64(Baruch)",                                        "QmFydWNo"),
                arguments("toBase64Gzip(vividus)",                                         "H4sIAAAAAAAA/yvLLMtMKS0GANIHCdkHAAAA")
        );
        // CHECKSTYLE:ON
    }

    @ParameterizedTest(name = "{index}: for expression \"{0}\", result is \"{1}\"")
    @MethodSource("expressionSource")
    void shouldResolveBase64Expression(String expression, String expected)
    {
        assertEquals(expected, processors.execute(expression).get());
    }

    @Test
    void shouldDecodeBase64ToBinaryData()
    {
        var blackPixelBytes = new byte[] {
            -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 1, 0, 0, 0, 1, 8, 6, 0, 0, 0, 31,
            21, -60, -119, 0, 0, 0, 1, 115, 82, 71, 66, 0, -82, -50, 28, -23, 0, 0, 0, 13, 73, 68, 65, 84, 24, 87, 99,
            96, 96, 96, -8, 15, 0, 1, 4, 1, 0, 112, 32, 101, 11, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126 };
        assertArrayEquals(blackPixelBytes,
                processors.execute("decodeFromBase64ToBinary(" + BLACK_B64_PIXEL + ")").map(byte[].class::cast).get());
    }

    @Test
    void shouldWrapIoException()
    {
        var ioe = new IOException();
        try (MockedConstruction<GZIPOutputStream> ignored = Mockito.mockConstruction(GZIPOutputStream.class,
                Mockito.withSettings().useConstructor(ByteArrayOutputStream.class), (gzipos, context) ->
                    doThrow(ioe).when(gzipos).write(new byte[] { 48 })))
        {
            var uioe = assertThrows(UncheckedIOException.class, () -> processors.execute("toBase64Gzip(0)"));
            assertEquals(ioe, uioe.getCause());
        }
    }
}
