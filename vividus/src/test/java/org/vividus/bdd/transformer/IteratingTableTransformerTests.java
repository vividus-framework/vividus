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
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;

class IteratingTableTransformerTests
{
    private static final String TABLE = "";
    private final IteratingTableTransformer tableTransformer = new IteratingTableTransformer();
    private final Keywords  keywords = new Keywords();
    private final ParameterConverters parameterConverters = new ParameterConverters();

    @Test
    void shouldFailIfNoLimitSet()
    {
        var tableProperties = new TableProperties("", keywords, parameterConverters);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> tableTransformer.transform(TABLE, null, tableProperties));
        assertEquals("'limit' is not set in ExamplesTable properties", exception.getMessage());
    }

    @Test
    void shouldReturnTableWithDesiredQuantityOfRows()
    {
        var tableProperties = new TableProperties("limit=2", keywords, parameterConverters);
        assertEquals("|iterator|\n|0|\n|1|", tableTransformer.transform(TABLE, null, tableProperties));
    }
}
