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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class ThreadedTestContextTests
{
    private static final String KEY = "key";

    private final ThreadedTestContext runContext = new ThreadedTestContext();

    @Test
    void testGetWithTypeCasting()
    {
        String value = "value";
        runContext.put(KEY, value);
        String result = runContext.get(KEY, String.class);
        assertEquals(value, result);
    }

    @Test
    void testRemove()
    {
        runContext.put(KEY, new Object());
        runContext.remove(KEY);
        assertNull(runContext.get(KEY));
        assertEquals(0, runContext.size());
    }

    @Test
    void testPutAll()
    {
        Object value = new Object();
        runContext.putAll(Collections.singletonMap(KEY, value));
        assertEquals(value, runContext.get(KEY));
        assertEquals(1, runContext.size());
    }

    @Test
    void testClear()
    {
        runContext.put(KEY, new Object());
        runContext.clear();
        assertEquals(0, runContext.size());
    }

    @Test
    void testGetAllWithNewInitMapValue()
    {
        Object initObject = new Object();
        runContext.putInitValueSupplier(KEY, () -> initObject);
        Map<Object, Object> runContextData = new HashMap<>();
        runContext.copyAllTo(runContextData);
        assertTrue(runContextData.containsKey(KEY));
        assertEquals(runContextData.get(KEY), initObject);
        assertEquals(1, runContextData.size());
    }

    @Test
    void testGetAllWithExistedInitMapValue()
    {
        Object initObject = new Object();
        runContext.putInitValueSupplier(KEY, () -> initObject);
        Object object = new Object();
        runContext.put(KEY, object);
        Map<Object, Object> runContextData = new HashMap<>();
        runContext.copyAllTo(runContextData);
        assertTrue(runContextData.containsKey(KEY));
        assertEquals(runContextData.get(KEY), object);
        assertEquals(1, runContextData.size());
    }
}
