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

import java.util.Properties;

import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;

class DistinctingTableTransformerTests
{
    private static final String TABLE =
                      "|key1|key2|key3|\n"
                    + "|a   |x   |a   |\n"
                    + "|a   |y   |a   |\n"
                    + "|b   |x   |a   |\n"
                    + "|b   |y   |a   |\n"
                    + "|b   |x   |b   |\n"
                    + "|b   |y   |b   |\n"
                    + "|a   |x   |b   |\n"
                    + "|a   |y   |b   |";

    private static final String DISTINCT_TABLE =
                      "|key1|key3|\n"
                    + "|a|a|\n"
                    + "|b|a|\n"
                    + "|b|b|\n"
                    + "|a|b|";

    @Test
    void testDistinct()
    {
        DistinctingTableTransformer transformer = new DistinctingTableTransformer();
        Properties properties = new Properties();
        properties.setProperty("byColumnNames", "key1;key3");
        ParameterConverters parameterConverters = new ParameterConverters();
        String result = transformer.transform(TABLE, new TableParsers(parameterConverters),
                new TableProperties(parameterConverters, properties));
        assertEquals(DISTINCT_TABLE, result);
    }
}
