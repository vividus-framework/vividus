/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.exporter.converter;

import static java.lang.System.lineSeparator;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.vividus.model.jbehave.Parameters;

class CucumberExamplesConverterTests
{
    private static final String KEY_1_KEY_2 = "|key 1|key 2|";
    private static final String KEY = "key ";
    private static final String VALUE = "value ";
    private static final String VALUES_FIRST_LINE = "|value 11|value 12|";
    private static final String VALUES_SECOND_LINE = "|value 21|value 22|";

    @Test
    void shouldConvertExamplesWithExampleName()
    {
        Parameters parameters = new Parameters();
        parameters.setNames(List.of(KEY + 1, KEY + 2));
        parameters.setValues(List.of(
                List.of(VALUE + 11, VALUE + 12),
                List.of(VALUE + 21, VALUE + 22)
        ));

        String examplesAsString =
                  "Examples:" + lineSeparator()
                + KEY_1_KEY_2 + lineSeparator()
                + VALUES_FIRST_LINE + lineSeparator()
                + VALUES_SECOND_LINE + lineSeparator();
        assertEquals(examplesAsString, CucumberExamplesConverter.buildScenarioExamplesTable(parameters));
    }

    @Test
    void shouldConvertExamplesWithoutExampleName()
    {
        Parameters parameters = new Parameters();
        parameters.setNames(List.of(KEY + 1, KEY + 2));
        parameters.setValues(List.of(
                List.of(VALUE + 11, VALUE + 12),
                List.of(VALUE + 21, VALUE + 22)
        ));

        String examplesAsString =
                        KEY_1_KEY_2 + lineSeparator()
                        + VALUES_FIRST_LINE + lineSeparator()
                        + VALUES_SECOND_LINE + lineSeparator();
        assertEquals(examplesAsString, CucumberExamplesConverter.buildScenarioExamplesTableWithoutName(parameters));
    }
}
