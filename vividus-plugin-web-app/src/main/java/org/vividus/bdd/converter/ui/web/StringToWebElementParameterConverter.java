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

package org.vividus.bdd.converter.ui.web;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.function.Supplier;

import javax.inject.Named;

import org.jbehave.core.steps.ParameterConverters.AbstractParameterConverter;
import org.openqa.selenium.WebElement;
import org.vividus.ui.action.SearchActions;
import org.vividus.ui.web.util.ElementUtil;

@Named
public class StringToWebElementParameterConverter extends AbstractParameterConverter<Supplier<Optional<WebElement>>>
{
    private final SearchActions searchActions;
    private final ElementUtil elementUtil;

    public StringToWebElementParameterConverter(SearchActions searchActions, ElementUtil elementUtil)
    {
        this.searchActions = searchActions;
        this.elementUtil = elementUtil;
    }

    @Override
    public Supplier<Optional<WebElement>> convertValue(String value, Type type)
    {
        return elementUtil.getElement(value, searchActions);
    }
}
