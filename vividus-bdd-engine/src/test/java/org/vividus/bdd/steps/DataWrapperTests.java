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

package org.vividus.bdd.steps;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;

class DataWrapperTests
{
    @Test
    void shouldReturnStringAsBytes()
    {
        String data = "data";
        assertArrayEquals(data.getBytes(StandardCharsets.UTF_8), new DataWrapper(data).getBytes());
    }

    @Test
    void shouldReturnBytesAsBytes()
    {
        byte[] data = { 0, 1, 2 };
        assertArrayEquals(data, new DataWrapper(data).getBytes());
    }

    @Test
    void shouldFailOnUnsupportedContentType()
    {
        DataWrapper dataWrapper = new DataWrapper(new Object());
        var exception = assertThrows(IllegalArgumentException.class, dataWrapper::getBytes);
        assertEquals("Unsupported content type: class java.lang.Object", exception.getMessage());
    }
}
