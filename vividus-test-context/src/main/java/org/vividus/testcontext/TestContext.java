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

import java.util.Map;
import java.util.function.Supplier;

public interface TestContext
{
    void put(Object key, Object value);

    void remove(Object key);

    <T> T get(Object key);

    <T> T get(Object key, Supplier<T> initialValueSupplier);

    <T> T get(Object key, Class<T> type);

    int size();

    void clear();

    void copyAllTo(Map<Object, Object> destination);

    void putAll(Map<Object, Object> source);

    void putInitValueSupplier(Object key, Supplier<Object> initialValueSupplier);
}
