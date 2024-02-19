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

package org.vividus.ui.action;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.locator.Locator;
import org.vividus.ui.action.search.ElementActionService;
import org.vividus.ui.action.search.IElementFilterAction;
import org.vividus.ui.action.search.IElementSearchAction;
import org.vividus.ui.action.search.LocatorType;
import org.vividus.ui.context.IUiContext;

import jakarta.inject.Inject;

public class SearchActions implements ISearchActions
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchActions.class);

    @Inject private IUiContext uiContext;
    @Inject private ElementActionService elementActionService;

    @Override
    public List<WebElement> findElements(SearchContext searchContext, Locator locator)
    {
        IElementSearchAction searchAction = elementActionService.find(locator.getLocatorType());
        return findElements(searchContext, locator, searchAction, false);
    }

    @Override
    public List<WebElement> findElements(Locator locator)
    {
        return findElements(uiContext.getSearchContext(), locator);
    }

    @Override
    public Optional<WebElement> findElement(Locator attributes)
    {
        return findElement(uiContext.getSearchContext(), attributes);
    }

    @Override
    public Optional<WebElement> findElement(SearchContext searchContext, Locator locator)
    {
        List<WebElement> elements = findElements(searchContext, locator);
        return elements.isEmpty() ? Optional.empty() : Optional.of(elements.get(0));
    }

    private List<WebElement> findElements(SearchContext searchContext, Locator locator,
            IElementSearchAction searchAction, boolean retry)
    {
        List<WebElement> foundElements = searchAction.search(searchContext, locator.getSearchParameters());
        try
        {
            return filterElements(locator, foundElements);
        }
        catch (StaleElementReferenceException e)
        {
            if (!retry)
            {
                return findElements(searchContext, locator, searchAction, true);
            }
            throw e;
        }
    }

    private List<WebElement> filterElements(Locator locator, List<WebElement> foundElements)
    {
        List<WebElement> result = foundElements;
        for (Entry<LocatorType, List<String>> filterAttribute : locator.getFilterAttributes().entrySet())
        {
            LocatorType filterType = filterAttribute.getKey();
            IElementFilterAction filterAction = elementActionService.find(filterType);
            for (String filterValue : filterAttribute.getValue())
            {
                int size = result.size();
                if (size == 0)
                {
                    break;
                }

                List<WebElement> filteredElements = filterAction.filter(result, filterValue);

                LOGGER.atInfo().addArgument(() -> size - filteredElements.size())
                               .addArgument(size)
                               .addArgument(filterType)
                               .addArgument(filterValue)
                               .log("{} of {} elements were filtered out by {} filter with '{}' value");

                result = filteredElements;
            }
        }
        return result;
    }
}
