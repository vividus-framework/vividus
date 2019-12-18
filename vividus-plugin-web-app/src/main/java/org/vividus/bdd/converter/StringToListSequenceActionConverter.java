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
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Named;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.ParameterConverters.AbstractParameterConverter;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.web.model.SequenceAction;
import org.vividus.bdd.steps.ui.web.model.SequenceActionType;

@Named
public class StringToListSequenceActionConverter extends AbstractParameterConverter<List<SequenceAction>>
{
    private final StringToSearchAttributesConverter stringToSearchAttributesConverter;
    private final PointConverter pointConverter;

    public StringToListSequenceActionConverter(StringToSearchAttributesConverter stringToSearchAttributesConverter,
            PointConverter pointConverter)
    {
        this.stringToSearchAttributesConverter = stringToSearchAttributesConverter;
        this.pointConverter = pointConverter;
    }

    @Override
    public List<SequenceAction> convertValue(String value, Type type)
    {
        return new ExamplesTable(value).getRowsAsParameters().stream()
            .map(params ->
            {
                SequenceActionType actionType = params.valueAs("type", SequenceActionType.class);
                String argumentValue = params.valueAs("argument", String.class);
                return new SequenceAction(actionType, convertArgument(argumentValue, actionType.getArgumentType()));
            })
            .collect(Collectors.toList());
    }

    private Object convertArgument(String argumentValue, Class<?> argumentType)
    {
        if (argumentType.equals(WebElement.class))
        {
            return stringToSearchAttributesConverter.convertValue(argumentValue, null);
        }
        else if (argumentType.equals(Point.class))
        {
            return pointConverter.convertValue(argumentValue, null);
        }
        return argumentValue;
    }
}
