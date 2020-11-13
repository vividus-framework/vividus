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

package org.vividus.ui.action;

import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import javax.inject.Inject;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.ui.action.search.ElementActionService;
import org.vividus.ui.action.search.IElementFilterAction;
import org.vividus.ui.action.search.IElementSearchAction;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.LocatorType;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.context.IUiContext;

public class SearchActions implements ISearchActions
{
    @Inject private IUiContext uiContext;
    @Inject private ElementActionService elementActionService;

    public List<WebElement> findElements(SearchContext searchContext, Locator locator)
    {
        SearchParameters searchParameters = locator.getSearchParameters();
        IElementSearchAction searchAction = elementActionService.find(locator.getLocatorType());
        List<WebElement> foundElements = searchAction.search(searchContext, searchParameters);
        for (Entry<LocatorType, List<String>> entry : locator.getFilterAttributes().entrySet())
        {
            IElementFilterAction filterAction = elementActionService.find(entry.getKey());
            for (String filterValue : entry.getValue())
            {
                foundElements = filterAction.filter(foundElements, filterValue);
            }
        }
        List<Locator> childLocators = locator.getChildLocators();
        for (Locator attributes : childLocators)
        {
            foundElements = searchInChildElements(foundElements, attributes);
        }
        return foundElements;
    }

    @Override
    public List<WebElement> findElements(Locator locator)
    {
        return findElements(uiContext.getSearchContext(), locator);
    }

    private List<WebElement> searchInChildElements(List<WebElement> foundElements, Locator attributes)
    {
        Iterator<WebElement> iterator = foundElements.iterator();
        while (iterator.hasNext())
        {
            WebElement element = iterator.next();
            List<WebElement> childElements = findElements(element, attributes);
            if (childElements.isEmpty())
            {
                iterator.remove();
            }
        }
        return foundElements;
    }

    @Override
    public Optional<WebElement> findElement(Locator attributes)
    {
        return findElement(uiContext.getSearchContext(), attributes);
    }

    @Override
    public Optional<WebElement> findElement(SearchContext searchContext, Locator attributes)
    {
        List<WebElement> elements = findElements(searchContext, attributes);
        return elements.isEmpty() ? Optional.empty() : Optional.of(elements.get(0));
    }
}
