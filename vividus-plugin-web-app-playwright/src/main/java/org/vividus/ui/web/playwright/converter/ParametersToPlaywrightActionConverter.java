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

package org.vividus.ui.web.playwright.converter;

import java.lang.reflect.Type;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jbehave.core.steps.ParameterConverters.AbstractParameterConverter;
import org.jbehave.core.steps.Parameters;
import org.vividus.ui.web.playwright.action.AbstractPlaywrightActions;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

public class ParametersToPlaywrightActionConverter
        extends AbstractParameterConverter<Parameters, AbstractPlaywrightActions>
{
    private static final String ARGUMENT = "argument";

    private final StringToPlaywrightLocatorConverter stringToLocatorConverter;

    private final List<AbstractPlaywrightActions> actions;

    public ParametersToPlaywrightActionConverter(StringToPlaywrightLocatorConverter stringToLocatorConverter,
            List<AbstractPlaywrightActions> actions)
    {
        this.stringToLocatorConverter = stringToLocatorConverter;
        this.actions = actions;
    }

    @Override
    public AbstractPlaywrightActions convertValue(Parameters parameters, Type type)
    {
        String actionName = parameters.<String>valueAs("type", String.class).trim()
                .replaceAll("\\W", "_")
                .toUpperCase();
        AbstractPlaywrightActions action = actions.stream().filter(a -> a.getName().equals(actionName)).findFirst()
                .map(AbstractPlaywrightActions::createAction)
                .orElseThrow(() -> new IllegalArgumentException("There is no action: " + actionName));
        String argumentAsString = parameters.values().get(ARGUMENT);
        Object argument;
        if (StringUtils.isEmpty(argumentAsString))
        {
            Validate.isTrue(!action.isArgumentRequired(), "Argument is mandatory for action '%s'", actionName);
            argument = null;
        }
        else
        {
            Type argumentType = action.getArgumentType();
            argument = argumentType.equals(PlaywrightLocator.class)
                    ? stringToLocatorConverter.convertValue(argumentAsString, null)
                    : parameters.valueAs(ARGUMENT, argumentType);
        }
        action.setArgument(argument);
        return action;
    }
}
