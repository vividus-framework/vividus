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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Properties;

import org.jbehave.core.model.ExamplesTable.ExamplesTableProperties;
import org.junit.jupiter.api.Test;

class IteratingTableTransformerTests
{
    private static final String TABLE = "";
    private final IteratingTableTransformer tableTransformer = new IteratingTableTransformer();

    @Test
    void shouldFailIfNoLimitSet()
    {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> tableTransformer.transform(TABLE, null, new ExamplesTableProperties(new Properties())));
        assertEquals("'limit' is not set in ExamplesTable properties", exception.getMessage());
    }

    @Test
    void shouldReturnTableWithDesiredQuantityOfRows()
    {
        Properties properties = new Properties();
        properties.put("limit", "2");
        assertEquals("|iterator|\n|0|\n|1|",
                tableTransformer.transform(TABLE, null, new ExamplesTableProperties(properties)));
    }
}
