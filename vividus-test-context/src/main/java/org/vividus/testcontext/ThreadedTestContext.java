/*
 * Copyright 2019-2020 the original author or authors.
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class ThreadedTestContext implements TestContext
{
    private final ThreadLocal<TestContext> context = ThreadLocal.withInitial(SimpleTestContext::new);

    private final Map<Object, Supplier<Object>> initMap = new ConcurrentHashMap<>();

    @Override
    public void put(Object key, Object value)
    {
        getContext().put(key, value);
    }

    @Override
    public void remove(Object key)
    {
        getContext().remove(key);
    }

    @Override
    public <T> T get(Object key)
    {
        return getContext().get(key);
    }

    @Override
    public <T> T get(Object key, Supplier<T> initialValueSupplier)
    {
        return getContext().get(key, initialValueSupplier);
    }

    @Override
    public <T> T get(Object key, Class<T> type)
    {
        return getContext().get(key, type);
    }

    @Override
    public int size()
    {
        return getContext().size();
    }

    @Override
    public void copyAllTo(Map<Object, Object> destination)
    {
        initMap.forEach(this::get);
        getContext().copyAllTo(destination);
    }

    @Override
    public void putAll(Map<Object, Object> source)
    {
        getContext().putAll(source);
    }

    @Override
    public void putInitValueSupplier(Object key, Supplier<Object> initialValueSupplier)
    {
        initMap.put(key, initialValueSupplier);
    }

    @Override
    public void clear()
    {
        getContext().clear();
        context.remove();
    }

    private TestContext getContext()
    {
        return context.get();
    }
}
