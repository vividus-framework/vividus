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

import static java.util.stream.Collectors.toMap;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jbehave.core.steps.ParameterConverters.AbstractParameterConverter;
import org.jbehave.core.steps.Parameters;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.vividus.converter.ui.StringToLocatorConverter;
import org.vividus.ui.action.AtomicAction;
import org.vividus.ui.action.AtomicActionFactory;

public class ParametersToAtomicActionConverter<T extends Actions>
        extends AbstractParameterConverter<Parameters, AtomicAction<T>>
{
    private static final String ARGUMENT = "argument";

    private final StringToLocatorConverter stringToLocatorConverter;
    private final Map<String, AtomicActionFactory<T, ?>> actionFactories;

    public ParametersToAtomicActionConverter(StringToLocatorConverter stringToLocatorConverter,
            Set<AtomicActionFactory<T, ?>> atomicActionFactories)
    {
        super(Parameters.class, new TypeToken<AtomicAction<T>>() { }.getType());
        this.stringToLocatorConverter = stringToLocatorConverter;
        this.actionFactories = atomicActionFactories.stream()
                .collect(toMap(AtomicActionFactory::getName, Function.identity()));
    }

    @Override
    public boolean canConvertTo(Type targetType)
    {
        return targetType instanceof ParameterizedType && ((ParameterizedType) targetType).getRawType().equals(
                AtomicAction.class);
    }

    @SuppressWarnings("PMD.NullAssignment")
    @Override
    public AtomicAction<T> convertValue(Parameters parameters, Type type)
    {
        String actionName = parameters.<String>valueAs("type", String.class).trim()
                .replaceAll("\\W", "_")
                .toUpperCase();
        AtomicActionFactory<T, ?> actionFactory = actionFactories.get(actionName);
        String argumentAsString = parameters.values().get(ARGUMENT);
        Object argument;
        if (StringUtils.isEmpty(argumentAsString))
        {
            Validate.isTrue(!actionFactory.isArgumentRequired(), "Argument is mandatory for action '%s'", actionName);
            argument = null;
        }
        else
        {
            Type argumentType = actionFactory.getArgumentType();
            argument = argumentType.equals(WebElement.class) ? stringToLocatorConverter.convertValue(argumentAsString,
                    null) : parameters.valueAs(ARGUMENT, argumentType);
        }
        return new AtomicAction<>(actionFactory, argument);
    }
}
