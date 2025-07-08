/*
 * Copyright 2019-2024 the original author or authors.
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
import static org.mockito.Mockito.when;

import org.jbehave.core.configuration.Keywords;
import org.jbehave.core.model.ExamplesTable.TableProperties;
import org.jbehave.core.model.TableParsers;
import org.jbehave.core.steps.ParameterConverters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.steps.PlaceholderResolver;

@ExtendWith(MockitoExtension.class)
class ResolvingVariablesEagerlyTransformerTests
{
    private final ParameterConverters parameterConverters = new ParameterConverters();

    @Mock private PlaceholderResolver resolver;
    @InjectMocks private ResolvingVariablesEagerlyTransformer transformer;

    @Test
    void shouldResolveDataRowsTest()
    {
        String table = "|header|\n|${variable}|";
        when(resolver.resolvePlaceholders("${variable}", String.class)).thenReturn("resolver_variable");
        String actual = transformer.transform(table, new TableParsers(parameterConverters), createTableProperties());
        assertEquals("|header|\n|resolver_variable|", actual);
    }

    private TableProperties createTableProperties()
    {
        return new TableProperties("", new Keywords(), parameterConverters);
    }
}
