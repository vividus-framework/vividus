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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

class UnsafeGenericObjectPoolTests
{
    private static final String VALUE = "value";

    @Test
    void shouldSetMaxWait()
    {
        testSimplePool(pool -> assertEquals(Duration.ofMinutes(5), pool.getMaxWaitDuration()));
    }

    @Test
    void testApply()
    {
        testSimplePool(pool -> assertEquals(VALUE, pool.apply(pooled -> pooled)));
    }

    @Test
    void testAccept()
    {
        testSimplePool(pool -> pool.accept(pooled -> assertEquals(VALUE, pooled)));
    }

    @Test
    void testApplyWithExceptionAtCreation()
    {
        var exception = new IllegalArgumentException();
        try (var pool = new UnsafeGenericObjectPool<>(() -> { throw exception; }))
        {
            var actual = assertThrows(IllegalStateException.class, () -> pool.apply(pooled -> pooled));
            assertEquals(exception, actual.getCause());
        }
    }

    @Test
    void testApplyWithExceptionAtAppliance()
    {
        testSimplePool(pool ->
        {
            IllegalArgumentException exception = new IllegalArgumentException();
            var actual = assertThrows(IllegalStateException.class, () -> pool.apply(pooled -> {
                throw exception;
            }));
            assertEquals(exception, actual.getCause());
        });
    }

    private void testSimplePool(Consumer<UnsafeGenericObjectPool<String>> test)
    {
        try (var pool = new UnsafeGenericObjectPool<>(() -> VALUE))
        {
            test.accept(pool);
        }
    }
}
