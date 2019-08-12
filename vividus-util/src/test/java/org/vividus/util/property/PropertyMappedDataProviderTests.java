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

package org.vividus.util.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PropertyMappedDataProviderTests
{
    private static final String KEY = "key";
    private static final String VALUE = "value";

    @Mock
    private IPropertyMapper propertyMapper;

    private PropertyMappedDataProvider<String> provider;

    @BeforeEach
    void beforeEach() throws IOException
    {
        String propertyPrefix = "prefix";
        when(propertyMapper.readValues(propertyPrefix, String.class)).thenReturn(Collections.singletonMap(KEY, VALUE));
        provider = new PropertyMappedDataProvider<>(propertyMapper, propertyPrefix, String.class);
    }

    @Test
    void testSuccesfulGet()
    {
        assertEquals(VALUE, provider.get(KEY));
    }

    @Test
    void testFailedGet()
    {
        String key = "unknown";
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> provider.get(key));
        assertEquals("No entry is found for key: " + key, exception.getMessage());
    }

    @Test
    void testGetData()
    {
        assertEquals(Collections.singletonMap(KEY, VALUE), provider.getData());
    }
}
