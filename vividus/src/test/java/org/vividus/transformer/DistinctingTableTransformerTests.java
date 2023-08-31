/*
 * Copyright 2019-2023 the original author or authors.
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

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;

class DistinctingTableTransformerTests
{
    @Test
    void testDistinct()
    {
        var parameterConverters = new ParameterConverters();
        var tableParsers = new TableParsers(parameterConverters);
        var tableProperties = new TableProperties("byColumnNames=key1;key3", new Keywords(), parameterConverters);
        var inputTable = """
                |key1|key2|key3|
                |a   |x   |a   |
                |a   |y   |a   |
                |b   |x   |a   |
                |b   |y   |a   |
                |b   |x   |b   |
                |b   |y   |b   |
                |a   |x   |b   |
                |a   |y   |b   |""";
        String result = new DistinctingTableTransformer().transform(inputTable, tableParsers, tableProperties);
        var distinctTable = """
                |key1|key3|
                |a|a|
                |b|a|
                |b|b|
                |a|b|""";
        assertEquals(distinctTable, result);
    }
}
