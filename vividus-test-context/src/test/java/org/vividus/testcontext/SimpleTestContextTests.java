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

package org.vividus.testcontext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.Collections;

import org.junit.jupiter.api.Test;

class SimpleTestContextTests
{
    private static final String KEY = "key";

    private final SimpleTestContext simpleTestContext = new SimpleTestContext();

    @Test
    void testGetWithTypeCasting()
    {
        String value = "value";
        simpleTestContext.put(KEY, value);
        String result = simpleTestContext.get(KEY, String.class);
        assertEquals(value, result);
    }

    @Test
    void testRemove()
    {
        simpleTestContext.put(KEY, new Object());
        simpleTestContext.remove(KEY);
        assertNull(simpleTestContext.get(KEY));
        assertEquals(0, simpleTestContext.size());
    }

    @Test
    void testPutAll()
    {
        Object value = new Object();
        simpleTestContext.putAll(Collections.singletonMap(KEY, value));
        assertEquals(value, simpleTestContext.get(KEY));
        assertEquals(1, simpleTestContext.size());
    }

    @Test
    void testPutInitValueSupplier()
    {
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
            () -> simpleTestContext.putInitValueSupplier(KEY, () -> null));
        assertEquals("Method is not supported", exception.getMessage());
    }

    @Test
    void testGetWithInitialValueSupplier()
    {
        Object value = new Object();
        simpleTestContext.get(KEY, () -> value);
        assertEquals(value, simpleTestContext.get(KEY));
    }

    @Test
    void testClear()
    {
        simpleTestContext.put(KEY, new Object());
        simpleTestContext.clear();
        assertEquals(0, simpleTestContext.size());
    }

    @Test
    void testClearValueNotImplementingClearable()
    {
        Object value = mock(Object.class);
        simpleTestContext.put(KEY, value);
        simpleTestContext.clear();
        verifyNoInteractions(value);
        assertEquals(0, simpleTestContext.size());
    }
}
