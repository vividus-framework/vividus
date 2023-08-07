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

package org.vividus.json.converter;

import java.lang.reflect.Type;
import java.util.List;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.jbehave.core.steps.ParameterConverters.FromStringParameterConverter;

import net.javacrumbs.jsonunit.core.Option;
import net.javacrumbs.jsonunit.core.internal.Options;

public class JsonComparisonOptionsConverter extends FromStringParameterConverter<Options>
{
    private final FluentEnumListConverter fluentEnumListConverter;

    public JsonComparisonOptionsConverter(FluentEnumListConverter fluentEnumListConverter)
    {
        this.fluentEnumListConverter = fluentEnumListConverter;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public Options convertValue(String value, Type type)
    {
        Type listOfOptions = new TypeLiteral<List<Option>>() { }.getType();
        List options = fluentEnumListConverter.convertValue(value, listOfOptions);
        Options jsonComparisonOptions = Options.empty();
        for (Option option : (Iterable<Option>) options)
        {
            jsonComparisonOptions = jsonComparisonOptions.with(option);
        }
        return jsonComparisonOptions;
    }
}
