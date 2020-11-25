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

package org.vividus.bdd.converter;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.apache.commons.lang3.reflect.TypeUtils;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.ParameterConverters.AbstractChainableParameterConverter;
import org.jbehave.core.steps.Parameters;

@Named
public class ExamplesTableToListOfMapsConverter
        extends AbstractChainableParameterConverter<ExamplesTable, List<Map<String, String>>>
{
    @Override
    public boolean accept(Type type)
    {
        TypeLiteral<List<Map<String, String>>> typeLiteral = new TypeLiteral<>() { };
        return TypeUtils.equals(type, typeLiteral.getType());
    }

    @Override
    public List<Map<String, String>> convertValue(ExamplesTable examplesTable, Type type)
    {
        return examplesTable.getRowsAsParameters().stream().map(Parameters::values).collect(Collectors.toList());
    }
}
