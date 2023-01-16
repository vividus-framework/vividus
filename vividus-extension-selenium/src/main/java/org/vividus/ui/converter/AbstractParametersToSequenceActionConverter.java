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

package org.vividus.ui.converter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jbehave.core.steps.ParameterConverters.AbstractParameterConverter;
import org.jbehave.core.steps.Parameters;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.vividus.converter.ui.StringToLocatorConverter;
import org.vividus.ui.action.SequenceAction;
import org.vividus.ui.action.SequenceActionType;

public abstract class AbstractParametersToSequenceActionConverter<T extends SequenceActionType<? extends Actions>>
        extends AbstractParameterConverter<Parameters, SequenceAction<T>>
{
    private static final String ARGUMENT = "argument";

    private final StringToLocatorConverter stringToLocatorConverter;

    protected AbstractParametersToSequenceActionConverter(StringToLocatorConverter stringToLocatorConverter, Type type)
    {
        super(Parameters.class, type);
        this.stringToLocatorConverter = stringToLocatorConverter;
    }

    @SuppressWarnings("PMD.NullAssignment")
    @Override
    public SequenceAction<T> convertValue(Parameters parameters, Type type)
    {
        T actionType = parameters.valueAs("type", ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0]);
        String argumentAsString = parameters.values().get(ARGUMENT);
        Object argument;
        if (StringUtils.isEmpty(argumentAsString))
        {
            Validate.isTrue(actionType.isNullable(), "Argument is mandatory for action '%s'", actionType);
            argument = null;
        }
        else
        {
            Type argumentType = actionType.getArgumentType();
            argument = argumentType.equals(WebElement.class) ? stringToLocatorConverter.convertValue(argumentAsString,
                    null) : parameters.valueAs(ARGUMENT, argumentType);
        }
        return new SequenceAction<>(actionType, argument);
    }
}
