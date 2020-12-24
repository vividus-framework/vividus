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

package org.vividus.bdd.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class ExamplesTableToListOfMapsConverterTests
{
    private final ExamplesTableToListOfMapsConverter converter = new ExamplesTableToListOfMapsConverter();

    @Test
    void shouldAcceptType()
    {
        assertTrue(converter.accept(new TypeLiteral<List<Map<String, String>>>() { }.getType()));
    }

    static Stream<Type> notAcceptableTypes()
    {
        return Stream.of(
                new TypeLiteral<List<Map<String, Object>>>() { }.getType(),
                new TypeLiteral<List<Map<Object, String>>>() { }.getType(),
                new TypeLiteral<List<Map<?, ?>>>() { }.getType(),
                new TypeLiteral<List<Set<String>>>() { }.getType(),
                new TypeLiteral<List<String>>() { }.getType(),
                new TypeLiteral<List<?>>() { }.getType(),
                String.class
        );
    }

    @ParameterizedTest
    @MethodSource("notAcceptableTypes")
    void shouldNotAcceptType(Type type)
    {
        assertFalse(converter.accept(type));
    }

    @Test
    void shouldConvertExamplesTable()
    {
        ExamplesTable table = new ExamplesTable("|key0|key1|\n|value0|value1|");
        List<Map<String, String>> actual = converter.convertValue(table, null);
        List<Map<String, String>> expected = List.of(Map.of("key0", "value0", "key1", "value1"));
        assertEquals(expected, actual);
    }
}
