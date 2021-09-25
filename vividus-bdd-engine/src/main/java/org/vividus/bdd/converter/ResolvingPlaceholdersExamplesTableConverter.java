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

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.ExamplesTableFactory;
import org.jbehave.core.steps.ParameterConverters;
import org.vividus.bdd.steps.PlaceholderResolver;

public class ResolvingPlaceholdersExamplesTableConverter extends ParameterConverters.ExamplesTableConverter
{
    private final PlaceholderResolver placeholderResolver;

    public ResolvingPlaceholdersExamplesTableConverter(ExamplesTableFactory factory,
            PlaceholderResolver placeholderResolver)
    {
        super(factory);
        this.placeholderResolver = placeholderResolver;
    }

    @Override
    public ExamplesTable convertValue(String value, Type type)
    {
        ExamplesTable examplesTable = super.convertValue(value, type);
        List<Map<String, String>> rows = examplesTable.getRows();
        rows.forEach(row -> row.entrySet().forEach(cell -> {
            String cellValue = cell.getValue();
            if (cellValue != null)
            {
                cell.setValue((String) placeholderResolver.resolvePlaceholders(cellValue, String.class));
            }
        }));
        return examplesTable.withRows(rows);
    }
}
