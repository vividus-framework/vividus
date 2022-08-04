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

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.params.provider.Arguments.of;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BinaryExpressionProcessorTests
{
    private static final byte[] BIG_DATA_BYTES = "big data".getBytes(StandardCharsets.UTF_8);

    @InjectMocks private BinaryExpressionProcessor processor;

    static Stream<Arguments> expressionSource()
    {
        // CHECKSTYLE:OFF
        return Stream.of(
                of("loadBinaryResource(/org/vividus/expressions/resource.txt)",               BIG_DATA_BYTES),
                of("loadBinaryFile(src/test/resources/org/vividus/expressions/resource.txt)", BIG_DATA_BYTES)
        );
        // CHECKSTYLE:ON
    }

    @ParameterizedTest(name = "{index}: for expression \"{0}\", result is \"{1}\"")
    @MethodSource("expressionSource")
    void testExecute(String expression, byte[] expected)
    {
        assertArrayEquals(expected, processor.execute(expression).get());
    }
}
