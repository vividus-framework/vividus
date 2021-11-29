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

package org.vividus.transformer;

import static java.util.Map.entry;
import static java.util.function.Function.identity;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;

class ExtendedTableTransformerTests
{
    static Stream<Arguments> processValues()
    {
        return Stream.of(
                arguments("One of ExamplesTable properties must be set: either 'key-1' or 'key-2'",
                          "1=value-1, 2=value-2"),
                arguments("Only one ExamplesTable property must be set, but found both 'key-1' and 'key-2'",
                          "key-1=value-1, key-2=value-2")
             );
    }

    @ParameterizedTest
    @MethodSource("processValues")
    void testProcessCompetingMandatoryProperties(String expected, String properties)
    {
        var exception = assertThrows(IllegalArgumentException.class,
                () -> processCompetingMandatoryProperties(properties));
        assertEquals(expected, exception.getMessage());
    }

    @ParameterizedTest
    @CsvSource({
            "value-2, 'key=value-1, key-2=value-2'",
            "value-1, 'key-1=value-1, non-existent=value-2'"
    })
    void testProcessCompetingMandatoryOneNullProperties(String expected, String properties)
    {
        assertEquals(expected, processCompetingMandatoryProperties(properties));
    }

    private String processCompetingMandatoryProperties(String propertiesAsString)
    {
        var tableProperties = new TableProperties(propertiesAsString, new Keywords(), new ParameterConverters());
        ExtendedTableTransformer transformer = (tableAsString, tableParsers, properties) -> null;
        return transformer.processCompetingMandatoryProperties(tableProperties, entry("key-1", identity()),
                entry("key-2", identity()));
    }
}
