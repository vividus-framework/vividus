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

package org.vividus.util.pool;

import java.util.function.Supplier;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

public class SuppliedPooledObjectFactory<T> extends BasePooledObjectFactory<T>
{
    private final Supplier<T> supplier;

    public SuppliedPooledObjectFactory(Supplier<T> supplier)
    {
        this.supplier = supplier;
    }

    @Override
    public T create()
    {
        return supplier.get();
    }

    @Override
    public PooledObject<T> wrap(T obj)
    {
        return new DefaultPooledObject<>(obj);
    }
}
