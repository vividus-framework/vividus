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

package org.vividus.ui.action.search;

import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.SPACE;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.locators.RelativeLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.ui.action.ElementActions;
import org.vividus.ui.action.IExpectedSearchContextCondition;
import org.vividus.ui.action.IWaitActions;

import jakarta.inject.Inject;

public abstract class AbstractElementAction implements IElementAction
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractElementAction.class);

    private static final String NUMBER_OF_ELEMENTS_MESSAGE = "The total number of elements found by \"{}\" is {}";
    private static final String NUMBER_OF_FILTERED_ELEMENTS_MESSAGE =
            NUMBER_OF_ELEMENTS_MESSAGE + ", the number of {} elements is {}";
    private static final String UNKNOWN_LOCATOR = "unknown locator";

    private IWaitActions waitActions;
    @Inject private ElementActions elementActions;
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

    protected List<WebElement> findElements(SearchContext searchContext, By locator, SearchParameters parameters)
    {
        if (searchContext != null)
        {
            return findElements(searchContext, locator, parameters.getVisibility(), parameters.isWaitForElement(),
                    false);
        }
        LOGGER.error(NOT_SET_CONTEXT);
        return List.of();
    }

    private List<WebElement> findElements(SearchContext searchContext, By locator, Visibility visibility,
            boolean waitForElement, boolean retry)
    {
        if (waitForElement)
        {
            List<WebElement> value = waitForElement(searchContext, locator, visibility);
            return Objects.requireNonNullElseGet(value, List::of);
        }
        List<WebElement> elements = searchContext.findElements(locator);

        if (Visibility.ALL == visibility || elements.isEmpty())
        {
            LOGGER.atInfo()
                    .addArgument(() -> convertLocatorToReadableForm(locator))
                    .addArgument(elements::size)
                    .log(NUMBER_OF_ELEMENTS_MESSAGE);
            return elements;
        }
        try
        {
            List<WebElement> filteredElements = filterElementsByVisibility(elements, visibility, retry);
            LOGGER.atInfo()
                    .addArgument(() -> convertLocatorToReadableForm(locator))
                    .addArgument(elements::size)
                    .addArgument(visibility::getDescription)
                    .addArgument(filteredElements::size)
                    .log(NUMBER_OF_FILTERED_ELEMENTS_MESSAGE);
            return filteredElements;
        }
        catch (StaleElementReferenceException e)
        {
            return findElements(searchContext, locator, visibility, false, true);
        }
    }

    private static String convertLocatorToReadableForm(By locator)
    {
        if (locator instanceof RelativeLocator.RelativeBy)
        {
            return formatRelativeLocator((RelativeLocator.RelativeBy) locator);
        }
        return StringUtils.removeStart(locator.toString(), "By.");
    }

    private static String formatRelativeLocator(RelativeLocator.RelativeBy locator)
    {
        String relativeElementParametersValue = locator.getRemoteParameters().value().toString();

        Pattern rootElementPattern = Pattern.compile("root=\\{([^}]+)}");
        Matcher rootElementMatcher = rootElementPattern.matcher(relativeElementParametersValue);
        String rootElement = rootElementMatcher.find() ? rootElementMatcher.group(1) : UNKNOWN_LOCATOR;

        StringBuilder result = new StringBuilder();
        result.append(rootElement).append(" and placed");

        List<String> filters = getRelativeFilters(relativeElementParametersValue);
        int numberOfFilters = filters.size();
        if (numberOfFilters == 1)
        {
            result.append(SPACE).append(filters.get(0));
        }
        else if (numberOfFilters > 1)
        {
            result.append(':').append(System.lineSeparator());
            filters.forEach(f -> result.append("  - ").append(f).append(System.lineSeparator()));
        }
        else
        {
            return "Unable to find relative filters for the element located " + rootElement;
        }
        return result.toString();
    }

    private static List<String> getRelativeFilters(String filtersString)
    {
        Pattern filterPattern = Pattern.compile("\\{(.*?)},?");
        Pattern kindPattern = Pattern.compile("kind=(\\w+)");

        Matcher filterMatcher = filterPattern.matcher(filtersString);
        List<String> filters = new ArrayList<>();
        while (filterMatcher.find())
        {
            String filter = filterMatcher.group(1);
            Matcher kindMatcher = kindPattern.matcher(filter);
            if (kindMatcher.find())
            {
                String kind = kindMatcher.group(1);
                String filterString = formatRelativeFilterLocators(filter);
                filters.add(kind + filterString);
            }
        }
        return filters;
    }

    private static String formatRelativeFilterLocators(String filter)
    {
        Pattern filterLocatorPattern = Pattern.compile("->\\s(.+:.+)](?:, (\\d+))?]");
        Matcher filterLocatorMatcher = filterLocatorPattern.matcher(filter);
        String filterLocator = UNKNOWN_LOCATOR;
        String nearPixels = EMPTY;
        if (filterLocatorMatcher.find())
        {
            filterLocator = filterLocatorMatcher.group(1);
            nearPixels = filterLocatorMatcher.group(2);
            nearPixels = nearPixels != null ? SPACE + nearPixels + "px" : EMPTY;
        }
        return nearPixels + " of the element with " + filterLocator;
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
                LOGGER.atWarn().addArgument(e::getMessage).setCause(e).log("{}");
                return false;
            }
        }).toList();
    }

    private List<WebElement> waitForElement(SearchContext searchContext, By locator, Visibility visibility)
    {
        IExpectedSearchContextCondition<List<WebElement>> condition = new IExpectedSearchContextCondition<>()
        {
            @Override
            @SuppressWarnings("checkstyle:NoNullForCollectionReturn")
            public List<WebElement> apply(SearchContext searchContext)
            {
                List<WebElement> elements = findElements(searchContext, locator, visibility, false, false);
                return !elements.isEmpty() ? elements : null;
            }
        };
        return waitActions.wait(searchContext, waitForElementTimeout, condition, false).getData();
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
