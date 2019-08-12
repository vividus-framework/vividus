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

package org.vividus.bdd.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.time.Duration;

import org.junit.jupiter.api.Test;

class DurationConverterTests
{
    private static final Type TYPE = Duration.class;

    private final DurationConverter converter = new DurationConverter();

    @Test
    void acceptTestTrue()
    {
        assertTrue(converter.accept(Duration.class));
    }

    @Test
    void acceptTestFalse()
    {
        assertFalse(converter.accept(String.class));
    }

    @Test
    void convertTest()
    {
        Duration expected = Duration.ZERO;
        assertEquals(expected, converter.convertValue("PT0S", TYPE));
    }
}
