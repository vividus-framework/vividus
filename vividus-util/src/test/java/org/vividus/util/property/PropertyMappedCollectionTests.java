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

package org.vividus.util.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class PropertyMappedCollectionTests
{
    private static final String KEY = "key";
    private static final String VALUE = "value";
    private static final String UNKNOWN = "unknown";

    private final PropertyMappedCollection<String> provider = new PropertyMappedCollection<>(Map.of(KEY, VALUE));

    @Test
    void shouldReturnValueByKey()
    {
        assertEquals(VALUE, provider.get(KEY, "Error message"));
    }

    @Test
    void shouldReturnOptionalValueByKey()
    {
        assertEquals(Optional.of(VALUE), provider.getNullable(KEY));
    }

    @Test
    void shouldReturnEmptyOptionalWhenNoValueIsPresent()
    {
        assertEquals(Optional.empty(), provider.getNullable(UNKNOWN));
    }

    @Test
    void shouldThrowExceptionWhenNoValueIsPresent()
    {
        String errorMessage = "No entry is found for key: ";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> provider.get(UNKNOWN, errorMessage + "%s", UNKNOWN));
        assertEquals(errorMessage + UNKNOWN, exception.getMessage());
    }

    @Test
    void shouldReturnAllData()
    {
        assertEquals(Collections.singletonMap(KEY, VALUE), provider.getData());
    }
}
