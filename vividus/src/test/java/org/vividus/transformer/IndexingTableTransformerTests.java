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

package org.vividus.transformer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class IndexingTableTransformerTests
{
    @ParameterizedTest
    @CsvSource({
        "order=ASCENDING,  '|k|index|\n|value|0|\n|value2|1|'",
        "order=DESCENDING, '|k|index|\n|value|1|\n|value2|0|'"
    })
    void shouldAddIndexColumnAccordingWithOrder(String properties, String expectedTable)
    {
        var transformer = new IndexingTableTransformer();
        var converters = new ParameterConverters();
        var tableProperties = new TableProperties(properties, new Keywords(), converters);

        assertEquals(expectedTable, transformer.transform("|k|\n|value|\n|value2|",
            new TableParsers(new ParameterConverters()), tableProperties));
    }

    @Test
    void shouldThrowAnExceptionIfIndexColumnIsInTheTable()
    {
        var transformer = new IndexingTableTransformer();
        var converters = new ParameterConverters();
        var tableProperties = new TableProperties("", new Keywords(), converters);
        var tableParsers = new TableParsers(new ParameterConverters());

        var iae = assertThrows(IllegalArgumentException.class, () -> transformer.transform("|index|\n|v|",
            tableParsers, tableProperties));
        assertEquals("Unable to add column with row indices to the table, because it has `index` column.",
            iae.getMessage());
    }
}
