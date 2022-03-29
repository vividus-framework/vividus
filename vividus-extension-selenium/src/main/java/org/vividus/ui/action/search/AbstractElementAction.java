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

package org.vividus.ui.action.search;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.ui.action.ElementActions;
import org.vividus.ui.action.IExpectedConditions;
import org.vividus.ui.action.IWaitActions;

public abstract class AbstractElementAction implements IElementAction
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractElementAction.class);

    private IWaitActions waitActions;
    @Inject private IExpectedConditions<By> expectedConditions;
    @Inject private ElementActions elementActions;
    @Inject private ElementActionService elementActionService;
    private Duration waitForElementTimeout;
    private boolean retrySearchIfStale;

    private final LocatorType type;

    public AbstractElementAction(LocatorType type)
    {
        this.type = type;
    }

    @Override
    public LocatorType getType()
    {
        return type;
    }

    protected List<WebElement> findElements(SearchContext searchContext, By locator, SearchParameters parameters,
            Map<LocatorType, List<String>> filters)
    {
        if (searchContext != null)
        {
            return findElements(searchContext, locator, parameters, filters, false);
        }
        LOGGER.error(IElementAction.NOT_SET_CONTEXT);
        return List.of();
    }

    private List<WebElement> findElements(SearchContext searchContext, By locator, SearchParameters parameters,
            Map<LocatorType, List<String>> filters, boolean retry)
    {
        List<WebElement> elements = parameters.isWaitForElement()
                ? waitForElement(searchContext, locator)
                : searchContext.findElements(locator);
        boolean elementsFound = null != elements;
        LOGGER.atInfo().addArgument(locator)
                       .addArgument(() -> elementsFound ? elements.size() : 0)
                       .log("Total number of elements found {} is {}");
        if (elementsFound)
        {
            try
            {
                List<WebElement> filteredElements = filterElementsByTypes(elements, filters);
                Visibility visibility = parameters.getVisibility();
                return Visibility.ALL == visibility ? filteredElements
                        : filterElementsByVisibility(filteredElements, visibility, retry);
            }
            catch (StaleElementReferenceException e)
            {
                return findElements(searchContext, locator, parameters, filters, true);
            }
        }
        return List.of();
    }

    protected List<WebElement> filterElementsByVisibility(List<WebElement> elements, Visibility visibility,
            boolean retry)
    {
        boolean visible = visibility == Visibility.VISIBLE;
        return elements.stream().filter(element -> {
            try
            {
                return visible == elementActions.isElementVisible(element);
            }
            catch (StaleElementReferenceException e)
            {
                if (retrySearchIfStale && !retry)
                {
                    throw e;
                }
                LOGGER.warn(e.getMessage(), e);
                return false;
            }
        }).collect(Collectors.collectingAndThen(Collectors.toList(), list ->
        {
            LOGGER.atInfo().addArgument(visibility::getDescription)
                           .addArgument(list::size)
                           .log("Number of {} elements is {}");
            return list;
        }));
    }

    private List<WebElement> filterElementsByTypes(List<WebElement> foundElements,
            Map<LocatorType, List<String>> filters)
    {
        List<WebElement> filteredElements = foundElements;
        for (Map.Entry<LocatorType, List<String>> entry : filters.entrySet())
        {
            IElementFilterAction filterAction = elementActionService.find(entry.getKey());
            for (String filterValue : entry.getValue())
            {
                int size = filteredElements.size();
                if (size == 0)
                {
                    break;
                }
                try
                {
                    filteredElements = filterAction.filter(filteredElements, filterValue);
                }
                catch (StaleElementReferenceException e)
                {
                    if (retrySearchIfStale)
                    {
                        throw e;
                    }
                    LOGGER.warn(e.getMessage(), e);
                }

                int filteredElementsCount = size - filteredElements.size();
                LOGGER.atInfo().addArgument(filteredElementsCount).addArgument(size)
                        .addArgument(entry::getKey).addArgument(filterValue)
                        .log("{} of {} elements were filtered out by {} filter with '{}' value");
            }
        }
        return filteredElements;
    }

    private List<WebElement> waitForElement(SearchContext searchContext, By locator)
    {
        return waitActions.wait(searchContext, waitForElementTimeout,
                expectedConditions.presenceOfAllElementsLocatedBy(locator), false).getData();
    }

    public void setWaitActions(IWaitActions waitActions)
    {
        this.waitActions = waitActions;
    }

    public void setWaitForElementTimeout(Duration waitForElementTimeout)
    {
        this.waitForElementTimeout = waitForElementTimeout;
    }

    public void setRetrySearchIfStale(boolean retrySearchIfStale)
    {
        this.retrySearchIfStale = retrySearchIfStale;
    }
}
