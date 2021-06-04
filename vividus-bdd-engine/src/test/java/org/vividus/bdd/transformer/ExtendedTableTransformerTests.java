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

package org.vividus.bdd.transformer;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ExtendedTableTransformerTests
{
    private static final String VALUE_1 = "value-1";
    private static final String VALUE_2 = "value-2";
    private static final String KEY_1 = "key-1";
    private static final String KEY_2 = "key-2";

    private static Function<String, String> func = Function.identity();
    private final ExtendedTableTransformer transformer = new ExtendedTableTransformer()
    {
        @Override
        public String transform(String tableAsString, TableParsers tableParsers, TableProperties properties)
        {
            return null;
        }
    };
    private final ParameterConverters parameterConverters = new ParameterConverters();

    static Stream<Arguments> processValues()
    {
        return Stream.of(
                arguments("One of ExamplesTable properties must be set: either 'key-1' or 'key-2'",
                          Map.of("1", VALUE_1, "2", VALUE_2)),
                arguments("Only one ExamplesTable property must be set, but found both 'key-1' and 'key-2'",
                          Map.of(KEY_1, VALUE_1, KEY_2, VALUE_2))
             );
    }

    static Stream<Arguments> processValuesExpected()
    {
        return Stream.of(
                arguments(VALUE_2, Map.of("key", VALUE_1, KEY_2, VALUE_2)),
                arguments(VALUE_1, Map.of(KEY_1, VALUE_1, "non-existent", VALUE_2))
         );
    }

    @ParameterizedTest
    @MethodSource("processValues")
    void testProcessCompetingMandatoryProperties(String expected, Map<String, String> values)
    {
        IllegalArgumentException exception =
                    assertThrows(IllegalArgumentException.class, () ->
                          transformer.processCompetingMandatoryProperties(createProperties(values),
                                      entry(KEY_1, func), entry(KEY_2, func)));
        assertEquals(expected, exception.getMessage());
    }

    @ParameterizedTest
    @MethodSource("processValuesExpected")
    void testProcessCompetingMandatoryOneNullProperties(String expected, Map<String, String> values)
    {
        assertEquals(expected, transformer.processCompetingMandatoryProperties(createProperties(values),
                entry(KEY_1, func), entry(KEY_2, func)).toString());
    }

    private TableProperties createProperties(Map<String, String> values)
    {
        Properties properties = new Properties();
        properties.putAll(values);
        return new TableProperties(parameterConverters, properties);
    }
}
