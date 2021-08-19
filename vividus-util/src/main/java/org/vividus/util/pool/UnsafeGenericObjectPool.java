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

package org.vividus.util.pool;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;

public class UnsafeGenericObjectPool<T> extends GenericObjectPool<T>
{
    private static final Duration MAX_WAIT_DURATION = Duration.ofMinutes(5);

    public UnsafeGenericObjectPool(PooledObjectFactory<T> factory)
    {
        super(factory);
        setMaxWait(MAX_WAIT_DURATION);
    }

    public UnsafeGenericObjectPool(Supplier<T> supplier)
    {
        this(new SuppliedPooledObjectFactory<>(supplier));
    }

    public <R> R apply(Function<T, R> function)
    {
        try
        {
            T obj = borrowObject();
            try
            {
                return function.apply(obj);
            }
            finally
            {
                returnObject(obj);
            }
        }
        catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e)
        {
            throw new IllegalStateException(e);
        }
    }

    public void accept(Consumer<T> consumer)
    {
        apply(obj ->
        {
            consumer.accept(obj);
            return null;
        });
    }
}
