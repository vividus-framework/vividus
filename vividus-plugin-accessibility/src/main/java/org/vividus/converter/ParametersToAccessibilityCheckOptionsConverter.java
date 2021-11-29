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

package org.vividus.converter;

import static java.util.stream.Collectors.collectingAndThen;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.reflect.TypeLiteral;
import org.jbehave.core.steps.ParameterConverters.AbstractParameterConverter;
import org.jbehave.core.steps.Parameters;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.accessibility.model.AccessibilityCheckOptions;
import org.vividus.accessibility.model.AccessibilityStandard;
import org.vividus.accessibility.model.ViolationLevel;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.context.UiContext;
import org.vividus.ui.web.action.ICssSelectorFactory;

public class ParametersToAccessibilityCheckOptionsConverter
    extends AbstractParameterConverter<Parameters, AccessibilityCheckOptions>
{
    private static final String LEVEL = "level";

    private static final Logger LOGGER = LoggerFactory.getLogger(
            ParametersToAccessibilityCheckOptionsConverter.class);

    private static final String STANDARD = "standard";

    private final UiContext uiContext;
    private final ICssSelectorFactory cssSelectorFactory;
    private final ISearchActions searchActions;

    public ParametersToAccessibilityCheckOptionsConverter(UiContext uiContext, ICssSelectorFactory cssSelectorFactory,
            ISearchActions searchActions)
    {
        this.uiContext = uiContext;
        this.cssSelectorFactory = cssSelectorFactory;
        this.searchActions = searchActions;
    }

    @Override
    public AccessibilityCheckOptions convertValue(Parameters row, Type type)
    {
        AccessibilityStandard standardParameter = row.valueAs(STANDARD, AccessibilityStandard.class, null);
        checkNotNull(standardParameter, STANDARD);
        ViolationLevel level = row.valueAs(LEVEL, ViolationLevel.class, null);
        checkNotNull(level, LEVEL);
        Set<Locator> locators = row.valueAs("elementsToCheck", new TypeLiteral<Set<Locator>>() { }.getType(), Set.of());
        Set<Locator> elementsToIgnore = row.valueAs("elementsToIgnore", new TypeLiteral<Set<Locator>>() { }.getType(),
                Set.of());
        List<String> violationsToIgnore = row.valueAs("violationsToIgnore",
                new TypeLiteral<List<String>>() { }.getType(), List.of());
        List<String> violationsToCheck = row.valueAs("violationsToCheck",
                new TypeLiteral<List<String>>() { }.getType(), null);
        return createOptions(standardParameter, violationsToIgnore, violationsToCheck, locators, elementsToIgnore,
                level);
    }

    private AccessibilityCheckOptions createOptions(AccessibilityStandard standard, List<String> violationsToIgnore,
            List<String> violationsToCheck, Set<Locator> elementsToCheck,
            Set<Locator> elementsToIgnore, ViolationLevel level)
    {
        List<String> ignore = new ArrayList<>(violationsToIgnore);
        Stream.of(ViolationLevel.values())
              .filter(l -> l.getCode() > level.getCode())
              .map(ViolationLevel::toString)
              .map(String::toLowerCase)
              .forEach(ignore::add);
        AccessibilityCheckOptions options = new AccessibilityCheckOptions(standard);
        SearchContext context = uiContext.getSearchContext();
        if (context instanceof WebElement)
        {
            options.setRootElement(cssSelectorFactory.getCssSelector((WebElement) context));
        }
        options.setIgnore(ignore);
        options.setInclude(violationsToCheck);
        options.setElementsToCheck(generateCssSelector(elementsToCheck));
        options.setHideElements(generateCssSelector(elementsToIgnore));
        options.setLevel(level);
        return options;
    }

    private String generateCssSelector(Set<Locator> locator)
    {
        if (locator.isEmpty())
        {
            return null;
        }
        return locator.stream()
                      .map(s -> {
                          s.getSearchParameters().setVisibility(Visibility.ALL);
                          return s;
                      })
                      .map(this::findElementsToCheck)
                      .flatMap(List::stream)
                      .collect(collectingAndThen(Collectors.toList(), cssSelectorFactory::getCssSelector));
    }

    private List<WebElement> findElementsToCheck(Locator locator)
    {
        List<WebElement> elements = searchActions.findElements(uiContext.getSearchContext(), locator);
        if (elements.isEmpty())
        {
            LOGGER.info("No elements found by {}", locator);
        }
        return elements;
    }

    private <T> void checkNotNull(T toCheck, String fieldName)
    {
        Validate.isTrue(toCheck != null, fieldName + " should be set");
    }
}
