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

package org.vividus.converter;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.reflect.TypeLiteral;
import org.jbehave.core.steps.ParameterConverters.AbstractParameterConverter;
import org.jbehave.core.steps.Parameters;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.accessibility.model.AbstractAccessibilityCheckOptions;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.Visibility;

public abstract class AbstractAccessibilityCheckOptionsConverter<T extends AbstractAccessibilityCheckOptions>
        extends AbstractParameterConverter<Parameters, T>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractAccessibilityCheckOptionsConverter.class);

    private final ISearchActions searchActions;

    protected AbstractAccessibilityCheckOptionsConverter(ISearchActions searchActions)
    {
        this.searchActions = searchActions;
    }

    protected List<String> getViolationsToIgnore(Parameters params)
    {
        return params.valueAs("violationsToIgnore", new TypeLiteral<List<String>>() { }.getType(), List.of());
    }

    protected List<String> getViolationsToCheck(Parameters params)
    {
        return params.valueAs("violationsToCheck", new TypeLiteral<List<String>>() { }.getType(), null);
    }

    protected <R> R getStandard(Parameters params, Class<R> type)
    {
        return params.valueAs("standard", type, null);
    }

    protected void configureElements(Parameters params, AbstractAccessibilityCheckOptions object)
    {
        object.setElementsToCheck(getElements(params, "elementsToCheck", true));
        object.setHideElements(getElements(params, "elementsToIgnore", false));
    }

    private List<WebElement> getElements(Parameters params, String key, boolean nullOnEmpty)
    {
        Set<Locator> locatorsToChecks = params.valueAs(key, new TypeLiteral<Set<Locator>>() { }.getType(),
                Set.of());
        return findElementsToCheck(locatorsToChecks, nullOnEmpty);
    }

    @SuppressWarnings("NoNullForCollectionReturn")
    private List<WebElement> findElementsToCheck(Set<Locator> locator, boolean nullOnEmpty)
    {
        if (locator.isEmpty())
        {
            return List.of();
        }

        List<WebElement> elements = locator.stream()
                                           .map(s -> {
                                               s.getSearchParameters().setVisibility(Visibility.ALL);
                                               return s;
                                           })
                                           .map(this::findElementsToCheck)
                                           .flatMap(List::stream)
                                           .collect(Collectors.toList());

        return elements.isEmpty() && nullOnEmpty ? null : elements;
    }

    private List<WebElement> findElementsToCheck(Locator locator)
    {
        List<WebElement> elements = searchActions.findElements(locator);
        if (elements.isEmpty())
        {
            LOGGER.info("No elements found by {}", locator);
        }
        return elements;
    }
}
