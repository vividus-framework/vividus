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

package org.vividus.ui.web.action;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.inject.Inject;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.ui.web.action.search.ActionAttributeType;
import org.vividus.ui.web.action.search.DefaultSearch;
import org.vividus.ui.web.action.search.IActionAttributeType;
import org.vividus.ui.web.action.search.IElementAction;
import org.vividus.ui.web.action.search.IElementFilterAction;
import org.vividus.ui.web.action.search.IElementSearchAction;
import org.vividus.ui.web.action.search.SearchAttributes;
import org.vividus.ui.web.action.search.SearchParameters;
import org.vividus.ui.web.action.search.Visibility;
import org.vividus.ui.web.context.IWebUiContext;

public class SearchActions implements ISearchActions
{
    private static final String EXCEPTION_MESSAGE = "There is no mapped search action for attribute: ";

    @Inject private IWebUiContext webUiContext;

    private Map<IActionAttributeType, IElementAction> elementActions;

    @Override
    public List<WebElement> findElements(SearchContext searchContext, By locator)
    {
        return getDefaultSearchAction().findElements(searchContext, locator,
                new SearchParameters().setVisibility(Visibility.VISIBLE));
    }

    @Override
    public List<WebElement> findElements(By locator)
    {
        return findElements(webUiContext.getSearchContext(), locator);
    }

    @Override
    public List<WebElement> findElements(SearchContext searchContext, SearchAttributes searchAttributes)
    {
        IActionAttributeType searchAttributeType = searchAttributes.getSearchAttributeType();
        SearchParameters searchParameters = searchAttributes.getSearchParameters();
        List<WebElement> foundElements;
        if (searchAttributeType.getSearchLocatorBuilder() != null)
        {
            By locator = searchAttributeType.getSearchLocatorBuilder().apply(searchParameters);
            foundElements = getDefaultSearchAction().findElements(searchContext, locator, searchParameters);
        }
        else
        {
            IElementSearchAction searchAction = getAction(searchAttributeType);
            foundElements = searchAction.search(searchContext, searchParameters);
        }
        for (Entry<IActionAttributeType, List<String>> entry : searchAttributes.getFilterAttributes().entrySet())
        {
            IElementFilterAction filterAction = getAction(entry.getKey());
            for (String filterValue : entry.getValue())
            {
                foundElements = filterAction.filter(foundElements, filterValue);
            }
        }
        List<SearchAttributes> childSearchAttributes = searchAttributes.getChildSearchAttributes();
        for (SearchAttributes attributes : childSearchAttributes)
        {
            foundElements = searchInChildElements(foundElements, attributes);
        }
        return foundElements;
    }

    @Override
    public List<WebElement> findElements(SearchAttributes searchAttributes)
    {
        return findElements(webUiContext.getSearchContext(), searchAttributes);
    }

    private List<WebElement> searchInChildElements(List<WebElement> foundElements, SearchAttributes attributes)
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
    public Optional<WebElement> findElement(SearchAttributes attributes)
    {
        List<WebElement> elements = findElements(webUiContext.getSearchContext(), attributes);
        return elements.isEmpty() ? Optional.empty() : Optional.of(elements.get(0));
    }

    private DefaultSearch getDefaultSearchAction()
    {
        return getAction(ActionAttributeType.DEFAULT);
    }

    @SuppressWarnings("unchecked")
    private <T extends IElementAction> T getAction(IActionAttributeType actionAttributeType)
    {
        IElementAction elementAction = elementActions.get(actionAttributeType);
        if (elementAction == null)
        {
            throw new UnsupportedOperationException(EXCEPTION_MESSAGE + actionAttributeType.getAttributeName());
        }
        return (T) elementAction;
    }

    public void setElementActions(Map<IActionAttributeType, IElementAction> elementActions)
    {
        this.elementActions = elementActions;
    }
}
