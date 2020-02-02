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

import java.io.IOException;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.util.property.IPropertyMapper;
import org.vividus.util.property.IPropertyParser;

class VariablesFactoryTests
{
    private static final String GLOBAL_PROPERTY_FAMILY = "bdd.variables.global";
    private static final String BATCH_PROPERTY_FAMILY = "bdd.variables.batch-";
    private static final String SCOPE_KEY = "scopeKey";
    private static final String BATCH = "batch";
    private static final String KEY = "key";
    private static final String VALUE = "value";

    @Test
    void testFactoryInitialization() throws IOException
    {
        Map<String, String> globals = Map.of(KEY, VALUE, SCOPE_KEY, "global");
        Map<String, String> batches = Map.of(SCOPE_KEY, BATCH);

        IPropertyParser propertyParser = mock(IPropertyParser.class);
        IPropertyMapper propertyMapper = mock(IPropertyMapper.class);
        IBddRunContext bddRunContext = mock(IBddRunContext.class);

        VariablesFactory variablesFactory = new VariablesFactory(propertyParser, propertyMapper, bddRunContext);
        when(propertyParser.getPropertyValuesByFamily(GLOBAL_PROPERTY_FAMILY)).thenReturn(globals);
        when(propertyMapper.readValues(BATCH_PROPERTY_FAMILY, Map.class)).thenReturn(Map.of("1", batches));
        when(bddRunContext.getRunningBatchKey()).thenReturn("batch-1");
        variablesFactory.init();
        variablesFactory.addNextBatchesVariable(KEY, VALUE);
        Variables variables = variablesFactory.createVariables();

        assertEquals(Map.of(KEY, VALUE, SCOPE_KEY, BATCH), variables.getVariables(VariableScope.GLOBAL));
        assertEquals(Map.of(KEY, VALUE), variables.getVariables(VariableScope.NEXT_BATCHES));
    }
}
