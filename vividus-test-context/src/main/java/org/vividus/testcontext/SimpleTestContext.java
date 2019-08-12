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

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class SimpleTestContext implements TestContext
{
    private final Map<Object, Object> map = new HashMap<>();

    @Override
    public void put(Object key, Object value)
    {
        map.put(key, value);
    }

    @Override
    public void remove(Object key)
    {
        map.remove(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key)
    {
        return (T) map.get(key);
    }

    @Override
    public <T> T get(Object key, Supplier<T> initialValueSupplier)
    {
        T value = get(key);
        if (value == null)
        {
            value = initialValueSupplier.get();
            put(key, value);
        }
        return value;
    }

    @Override
    public <T> T get(Object key, Class<T> type)
    {
        return type.cast(map.get(key));
    }

    @Override
    public int size()
    {
        return map.size();
    }

    @Override
    public void copyAllTo(Map<Object, Object> destination)
    {
        destination.putAll(this.map);
    }

    @Override
    public void putAll(Map<Object, Object> source)
    {
        this.map.putAll(source);
    }

    @Override
    public void putInitValueSupplier(Object key, Supplier<Object> initialValueSupplier)
    {
        throw new UnsupportedOperationException("Method is not supported");
    }

    @Override
    public void clear()
    {
        map.clear();
    }
}
