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

package org.vividus.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.convert.TypeDescriptor;

class StringToEditorGenericConverterTests
{
    private final EagerlyInitializingPropertyEditorRegistrySupport propertyEditorRegistry
        = new EagerlyInitializingPropertyEditorRegistrySupport();
    private final StringToEditorGenericConverter converter = new StringToEditorGenericConverter();

    @BeforeEach
    void setUp()
    {
        converter.setPropertyEditorRegistry(propertyEditorRegistry);
        converter.init();
    }

    @Test
    void shouldNotReturnConvertiblePairsForCollections()
    {
        assertTrue(converter.getConvertibleTypes().stream()
                .allMatch(p -> !p.getClass().isAssignableFrom(Collection.class)));
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldConvertValueToPattern()
    {
        String regex = ".+";
        TypeDescriptor targetType = mock(TypeDescriptor.class);
        when((Class<Pattern>) targetType.getObjectType()).thenReturn(Pattern.class);
        assertEquals(regex, ((Pattern) converter.convert(regex, null, targetType)).pattern());
    }
}
