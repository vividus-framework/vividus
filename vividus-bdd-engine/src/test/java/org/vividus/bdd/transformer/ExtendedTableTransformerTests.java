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

package org.vividus.bdd.transformer;

import static java.util.Map.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.spy;

import java.util.Map;
import java.util.Properties;
import java.util.function.Function;

import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ExtendedTableTransformerTests
{
    private static final String VALUE_1 = "value-1";
    private static final String VALUE_2 = "value-2";
    private static final String KEY_1 = "key-1";
    private static final String KEY_2 = "key-2";

    private static Function<String, String> func = key -> { return key; };
    private final ExtendedTableTransformer transformer = spy(ExtendedTableTransformer.class);

    @Test
    void testProcessCompetingMandatoryNotNullProperties()
    {
        Map<String, String> values = Map.of(KEY_1, VALUE_1, KEY_2, VALUE_2);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                transformer.processCompetingMandatoryProperties(createProperties(values),
                    entry(KEY_1, func), entry(KEY_2, func)));
        assertEquals("Only one ExamplesTable property must be set, but found both 'key-1' and 'key-2'",
                     exception.getMessage());
    }

    @Test
    void testProcessCompetingMandatoryNullProperties()
    {
        Map<String, String> values = Map.of("1", VALUE_1, "2", VALUE_2);
        IllegalArgumentException exception =
               assertThrows(IllegalArgumentException.class, () ->
                     transformer.processCompetingMandatoryProperties(createProperties(values),
                                 entry(KEY_1, func), entry(KEY_2, func)));
        assertEquals("One of ExamplesTable properties must be set: either 'key-1' or 'key-2'", exception.getMessage());
    }

    @Test
    void testProcessCompetingMandatoryFirstNullProperties()
    {
        Map<String, String> values = Map.of("key", VALUE_1, KEY_2, VALUE_2);
        assertEquals(VALUE_2, transformer.processCompetingMandatoryProperties(createProperties(values),
                entry(KEY_1, func), entry(KEY_2, func)).toString());
    }

    @Test
    void testProcessCompetingMandatorySecondNullProperties()
    {
        Map<String, String> values = Map.of(KEY_1, VALUE_1, "non-existent", VALUE_2);
        assertEquals(VALUE_1, transformer.processCompetingMandatoryProperties(createProperties(values),
                entry(KEY_1, func), entry(KEY_2, func)).toString());
    }

    private static TableProperties createProperties(Map<String, String> values)
    {
        Properties properties = new Properties();
        properties.putAll(values);
        return new TableProperties(properties);
    }
}
