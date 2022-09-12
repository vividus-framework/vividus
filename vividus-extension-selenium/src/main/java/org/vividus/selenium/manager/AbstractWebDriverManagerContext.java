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

package org.vividus.selenium.manager;

import java.util.function.Supplier;

import org.vividus.testcontext.SimpleTestContext;
import org.vividus.testcontext.TestContext;

public abstract class AbstractWebDriverManagerContext<K> implements WebDriverManagerContext<K>
{
    private final TestContext testContext;
    private final Class<?> storageType;

    protected AbstractWebDriverManagerContext(TestContext testContext, Class<?> storageType)
    {
        this.testContext = testContext;
        this.storageType = storageType;
    }

    @Override
    public <T> T get(K key)
    {
        return getData().get(key);
    }

    @Override
    public <T> void put(K key, T value)
    {
        getData().put(key, value);
    }

    public <T> T get(K key, Supplier<T> initialValueSupplier)
    {
        return getData().get(key, initialValueSupplier);
    }

    protected void reset()
    {
        getData().clear();
    }

    protected void reset(K key)
    {
        getData().remove(key);
    }

    private SimpleTestContext getData()
    {
        return testContext.get(storageType, SimpleTestContext::new);
    }
}
