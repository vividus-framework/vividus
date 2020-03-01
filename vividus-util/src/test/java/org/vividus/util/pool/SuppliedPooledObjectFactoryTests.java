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

package org.vividus.util.pool;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;
import org.junit.jupiter.api.Test;

class SuppliedPooledObjectFactoryTests
{
    private static final String VALUE = "value";

    private final SuppliedPooledObjectFactory<String> factory = new SuppliedPooledObjectFactory<>(() -> VALUE);

    @Test
    void testCreate()
    {
        assertEquals(VALUE, factory.create());
    }

    @Test
    void testWrap()
    {
        PooledObject<String> actual = factory.wrap(VALUE);
        assertThat(actual, instanceOf(DefaultPooledObject.class));
        assertEquals(VALUE, actual.getObject());
    }
}
