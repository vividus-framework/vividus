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

package org.vividus.converter.ui;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.function.Supplier;

import org.jbehave.core.steps.ParameterConverters.AbstractParameterConverter;
import org.jbehave.core.steps.Parameters;
import org.openqa.selenium.WebElement;
import org.vividus.steps.ui.model.SequenceAction;
import org.vividus.steps.ui.model.SequenceActionType;

public class AbstractParametersToSequenceActionConverter<T extends SequenceActionType<?>>
        extends AbstractParameterConverter<Parameters, SequenceAction<T>>
{
    private final StringToLocatorConverter stringToLocatorConverter;

    public AbstractParametersToSequenceActionConverter(StringToLocatorConverter stringToLocatorConverter,
            Type type)
    {
        super(Parameters.class, type);
        this.stringToLocatorConverter = stringToLocatorConverter;
    }

    @Override
    public SequenceAction<T> convertValue(Parameters parameters, Type type)
    {
        T actionType = parameters.valueAs("type", ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0]);
        String argumentAsString = argumentAs(parameters, String.class);
        if (argumentAsString.isEmpty() && actionType.isNullable())
        {
            return new SequenceAction<T>(actionType, null);
        }
        Type argumentType = actionType.getArgumentType();
        return new SequenceAction<T>(actionType, convertArgument(argumentAsString,
                argumentType, () -> argumentAs(parameters, argumentType)));
    }

    private <P> P argumentAs(Parameters parameters, Type type)
    {
        return parameters.valueAs("argument", type);
    }

    private Object convertArgument(String argumentValue, Type argumentType, Supplier<Object> defaultConverter)
    {
        if (argumentType.equals(WebElement.class))
        {
            return stringToLocatorConverter.convertValue(argumentValue, null);
        }
        return defaultConverter.get();
    }
}
