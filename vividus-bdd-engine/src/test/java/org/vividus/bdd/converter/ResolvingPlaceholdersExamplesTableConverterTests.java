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

package org.vividus.bdd.converter;

import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.model.TableTransformers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.steps.PlaceholderResolver;

@ExtendWith(MockitoExtension.class)
class ResolvingPlaceholdersExamplesTableConverterTests
{
    private final ExamplesTableFactory examplesTableFactory = new ExamplesTableFactory(null, new TableTransformers());
    @Mock private PlaceholderResolver placeholderResolver;
    private ResolvingPlaceholdersExamplesTableConverter converter;

    @BeforeEach
    void beforeEach()
    {
        converter = new ResolvingPlaceholdersExamplesTableConverter(examplesTableFactory, placeholderResolver);
    }

    @ParameterizedTest
    @CsvSource({
            "value,  value",
            "${var}, varValue"
    })
    void shouldResolvePlaceholdersInExamplesTable(String cellValue, String resolvedValue)
    {
        var examplesTable = "|key  |\n|" + cellValue + "|";
        when(placeholderResolver.resolvePlaceholders(cellValue, String.class)).thenReturn(resolvedValue);
        var result = converter.convertValue(examplesTable, null);
        assertEquals(List.of(Map.of("key", resolvedValue)), result.getRows());
    }

    @Test
    void shouldSkipResolvingOfNullCells()
    {
        var examplesTable = "{nullPlaceholder=NULL}\n|columnWithNull|\n|NULL|";
        var result = converter.convertValue(examplesTable, null);
        assertEquals(List.of(singletonMap("columnWithNull", null)), result.getRows());
        verifyNoInteractions(placeholderResolver);
    }
}
