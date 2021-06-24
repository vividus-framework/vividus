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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;

class RepeatingTableTransformerTests
{
    private static final String TABLE_AS_STRING = "|h1|h2|\n|r1v1|r1v2|\n|r2v1|r2v2|";
    private static final String ROW2 = "|r2v1|r2v2|\n";
    private static final String ROW1 = "|r1v1|r1v2|\n";
    private final RepeatingTableTranformer transformer = new RepeatingTableTranformer();
    private final Keywords keywords = new Keywords();
    private final ParameterConverters parameterConverters = new ParameterConverters();

    @Test
    void shouldGenerateRepeatedTable()
    {
        var expected = "|h1|h2|\n"
                + ROW1
                + ROW2
                + ROW1
                + ROW2
                + ROW1
                + ROW2
                + ROW1
                + ROW2
                + ROW1
                + "|r2v1|r2v2|";
        var tableParsers = new TableParsers(parameterConverters);
        var tableProperties = new TableProperties("times=5", keywords, parameterConverters);
        assertEquals(expected, transformer.transform(TABLE_AS_STRING, tableParsers, tableProperties));
    }

    @Test
    void shouldThrowAnExceptionIfNoPropertyPresent()
    {
        var tableParsers = new TableParsers(parameterConverters);
        var tableProperties = new TableProperties("", keywords, parameterConverters);
        var exception = assertThrows(IllegalArgumentException.class,
            () -> transformer.transform(TABLE_AS_STRING, tableParsers, tableProperties));
        assertEquals("'times' is not set in ExamplesTable properties", exception.getMessage());
    }
}
