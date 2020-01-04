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

package org.vividus.ui.web.action.search;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.ui.web.action.IExpectedConditions;
import org.vividus.ui.web.action.IJavascriptActions;
import org.vividus.ui.web.action.IWaitActions;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.util.LocatorUtil;

public abstract class AbstractElementSearchAction
{
    protected static final String TRANSLATE_TO_LOWER_CASE = "translate(%s,"
            + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')";
    private static final String TRANSLATE_TO_LOWER_CASE_FORMATTED = String.format(TRANSLATE_TO_LOWER_CASE, ".");
    private static final String ELEMENT_WITH_ANY_ATTRIBUTE_OR_TEXT_CASE_INSENSITIVE = "[text()["
            + TRANSLATE_TO_LOWER_CASE_FORMATTED + "=%1$s] or @*[" + TRANSLATE_TO_LOWER_CASE_FORMATTED + "=%1$s] or *["
            + TRANSLATE_TO_LOWER_CASE_FORMATTED + "=%1$s]]";
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractElementSearchAction.class);

    @Inject private IWebElementActions webElementActions;
    @Inject private IJavascriptActions javascriptActions;
    @Inject private IWaitActions waitActions;
    @Inject private IExpectedConditions<By> expectedConditions;
    private Duration waitForElementTimeout;

    public List<WebElement> findElements(SearchContext searchContext, By locator, SearchParameters parameters)
    {
        if (searchContext != null)
        {
            List<WebElement> elements = parameters.isWaitForElement() ? waitForElement(searchContext, locator)
                    : searchContext.findElements(locator);
            boolean elementsFound = null != elements;
            LOGGER.info("Total number of elements found {} is equal to {}", locator,
                    elementsFound ? elements.size() : 0);
            if (elementsFound)
            {
                Visibility visibility = parameters.getVisibility();
                return Visibility.ALL == visibility ? elements : filterElementsByVisibility(elements,
                        visibility == Visibility.VISIBLE);
            }
        }
        return List.of();
    }

    private List<WebElement> filterElementsByVisibility(List<WebElement> elements, boolean visible)
    {
        return elements.stream().filter(element -> {
            try
            {
                return visible == isElementVisible(element, false);
            }
            catch (StaleElementReferenceException e)
            {
                LOGGER.warn(e.getMessage(), e);
                return false;
            }
        }).collect(Collectors.toList());
    }

    private boolean isElementVisible(WebElement element, boolean scrolled)
    {
        if (!element.isDisplayed())
        {
            if (!scrolled)
            {
                javascriptActions.scrollIntoView(element, true);
                return isElementVisible(element, true);
            }
            return false;
        }
        return true;
    }

    protected List<WebElement> findElementsByText(SearchContext searchContext, By defaultLocator,
            SearchParameters parameters, String... tagNames)
    {
        List<WebElement> elements = findElements(searchContext, defaultLocator, parameters);
        if (elements.isEmpty())
        {
            String text = parameters.getValue();
            By newLocator = generateCaseInsensitiveLocator(text, tagNames);
            return findElements(searchContext, newLocator, parameters)
                    .stream()
                    .filter(element -> matchesToText(element, text))
                    .collect(Collectors.toList());
        }
        return elements;
    }

    protected static By generateCaseInsensitiveLocator(String text, String... tagNames)
    {
        StringBuilder locator = new StringBuilder();
        for (String tagName : tagNames)
        {
            locator.append(".//").append(tagName).append(ELEMENT_WITH_ANY_ATTRIBUTE_OR_TEXT_CASE_INSENSITIVE)
                    .append('|');
        }
        return LocatorUtil.getXPathLocator(locator.substring(0, locator.length() - 1), text.toLowerCase());
    }

    private List<WebElement> waitForElement(SearchContext searchContext, By locator)
    {
        return waitActions.wait(searchContext, waitForElementTimeout,
                expectedConditions.presenceOfAllElementsLocatedBy(locator), false).getData();
    }

    protected boolean matchesToText(WebElement element, final String text)
    {
        String textTransform = webElementActions.getCssValue(element, "text-transform");
        return Stream.of(text.split("\\W")).allMatch(word -> matchesToTextTransform(word, textTransform));
    }

    private boolean matchesToTextTransform(String word, String textTransform)
    {
        if (word.isEmpty())
        {
            return true;
        }
        switch (textTransform)
        {
            case "uppercase":
                return StringUtils.isAllUpperCase(word);
            case "lowercase":
                return StringUtils.isAllLowerCase(word);
            case "capitalize":
                return Character.isUpperCase(word.charAt(0));
            default:
                return false;
        }
    }

    protected IWebElementActions getWebElementActions()
    {
        return this.webElementActions;
    }

    protected IJavascriptActions getJavascriptActions()
    {
        return this.javascriptActions;
    }

    public void setWaitForElementTimeout(Duration waitForElementTimeout)
    {
        this.waitForElementTimeout = waitForElementTimeout;
    }
}
