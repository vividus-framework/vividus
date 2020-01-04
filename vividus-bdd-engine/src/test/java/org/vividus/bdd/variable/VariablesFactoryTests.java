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

package org.vividus.bdd.variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.vividus.util.property.IPropertyParser;

class VariablesFactoryTests
{
    private static final String VARIABLES_PROPERTY_FAMILY = "bdd.variables.global";
    private static final String KEY = "key";
    private static final String VALUE = "value";

    @Test
    void testFactoryInitialization()
    {
        Map<String, String> values = new HashMap<>();
        values.put(KEY, VALUE);

        IPropertyParser propertyParser = mock(IPropertyParser.class);

        VariablesFactory variablesFactory = new VariablesFactory();
        variablesFactory.setPropertyParser(propertyParser);
        when(propertyParser.getPropertyValuesByFamily(VARIABLES_PROPERTY_FAMILY)).thenReturn(values);
        variablesFactory.init();
        variablesFactory.addNextBatchesVariable(KEY, VALUE);
        Variables variables = variablesFactory.createVariables();

        assertEquals(Map.of(KEY, VALUE), variables.getVariables(VariableScope.GLOBAL));
        assertEquals(Map.of(KEY, VALUE), variables.getVariables(VariableScope.NEXT_BATCHES));
    }
}
