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

package org.vividus.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ParametersToMapConverterTests
{
    private final ParametersToMapConverter converter = new ParametersToMapConverter();

    @Test
    void shouldAcceptType()
    {
        assertTrue(converter.canConvertTo(new TypeLiteral<Map<String, String>>() { }.getType()));
    }

    static Stream<Type> notAcceptableTypes()
    {
        return Stream.of(
                new TypeLiteral<Map<String, Object>>() { }.getType(),
                new TypeLiteral<Map<Object, String>>() { }.getType(),
                new TypeLiteral<Map<?, ?>>() { }.getType(),
                new TypeLiteral<Set<String>>() { }.getType(),
                new TypeLiteral<String>() { }.getType(),
                String.class
        );
    }

    @ParameterizedTest
    @MethodSource("notAcceptableTypes")
    void shouldNotAcceptType(Type type)
    {
        assertFalse(converter.canConvertTo(type));
    }

    @Test
    void shouldConvertExamplesTable()
    {
        ExamplesTable table = new ExamplesTable("|key0|key1|\n|value0|value1|");
        Map<String, String> actual = converter.convertValue(table.getRowAsParameters(0), null);
        Map<String, String> expected = Map.of("key0", "value0", "key1", "value1");
        assertEquals(expected, actual);
    }
}
