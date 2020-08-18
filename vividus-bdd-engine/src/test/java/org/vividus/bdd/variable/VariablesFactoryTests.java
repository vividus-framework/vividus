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
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.util.property.IPropertyMapper;
import org.vividus.util.property.PropertyMapper;
import org.vividus.util.property.PropertyParser;

@ExtendWith(MockitoExtension.class)
class VariablesFactoryTests
{
    private static final String GLOBAL_PROPERTY_PREFIX = "bdd.variables.global.";
    private static final String BATCH_PROPERTY_FAMILY = "bdd.variables.batch-";
    private static final String KEY1 = "key1";
    private static final String KEY2 = "key2";
    private static final String KEY3 = "key3";

    private static final String GLOBAL = "global";
    private static final String BATCH = "batch";
    private static final String NEXT_BATCHES = "next-batches";

    private static final Map<String, String> GLOBAL_VARIABLES = Map.of(
            KEY1, GLOBAL,
            KEY2, GLOBAL,
            KEY3, GLOBAL
    );

    @Mock private PropertyParser propertyParser;
    @Mock private IBddRunContext bddRunContext;
    private VariablesFactory variablesFactory;

    @BeforeEach
    void beforeEach()
    {
        MockitoAnnotations.initMocks(this);
        IPropertyMapper propertyMapper = new PropertyMapper(propertyParser, Set.of());
        variablesFactory = new VariablesFactory(propertyParser, propertyMapper, bddRunContext);

        String batch1PropertyPrefix = BATCH_PROPERTY_FAMILY + "1.";

        when(propertyParser.getPropertyValuesByPrefix(GLOBAL_PROPERTY_PREFIX)).thenReturn(GLOBAL_VARIABLES);
        when(propertyParser.getPropertiesByPrefix(BATCH_PROPERTY_FAMILY)).thenReturn(Map.of(
            batch1PropertyPrefix + KEY1, BATCH,
            batch1PropertyPrefix + KEY2, BATCH
        ));
    }

    @Test
    void shouldCreateVariables() throws IOException
    {
        variablesFactory.init();
        when(bddRunContext.getRunningBatchKey()).thenReturn("batch-1");
        variablesFactory.addNextBatchesVariable(KEY1, NEXT_BATCHES);
        Variables variables = variablesFactory.createVariables();

        assertEquals(NEXT_BATCHES, variables.getVariable(KEY1));
        assertEquals(BATCH, variables.getVariable(KEY2));
        assertEquals(GLOBAL, variables.getVariable(KEY3));
    }

    @Test
    void shouldProvideGlobalVariables() throws IOException
    {
        variablesFactory.init();
        assertEquals(GLOBAL_VARIABLES, variablesFactory.getGlobalVariables());
    }
}
