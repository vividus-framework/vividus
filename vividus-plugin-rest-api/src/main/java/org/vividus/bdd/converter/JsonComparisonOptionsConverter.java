/*
 * Copyright 2019 the original author or authors.
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.jbehave.core.steps.ParameterConverters.AbstractParameterConverter;

import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Options;

@Named
public class JsonComparisonOptionsConverter extends AbstractParameterConverter<Options>
{
    private final FluentEnumListConverter fluentEnumListConverter;

    @Inject
    public JsonComparisonOptionsConverter(FluentEnumListConverter fluentEnumListConverter)
    {
        this.fluentEnumListConverter = fluentEnumListConverter;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Options convertValue(String value, Type type)
    {
        Type listOfOption = new TypeLiteral<List<Option>>() { }.getType();
        Set options = new HashSet<>(fluentEnumListConverter.convertValue(value, listOfOption));
        if (options.isEmpty())
        {
            return Options.empty();
        }
        Iterator<Option> iterator = options.iterator();
        Options jsonComparisonOptions = new Options(iterator.next());
        while (iterator.hasNext())
        {
            jsonComparisonOptions = jsonComparisonOptions.with(iterator.next());
        }
        return jsonComparisonOptions;
    }
}
