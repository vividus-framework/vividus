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

package org.vividus.ui.web.action.search;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.ui.action.search.AbstractElementAction;
import org.vividus.ui.action.search.LocatorType;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.util.LocatorUtil;

public abstract class AbstractWebElementSearchAction extends AbstractElementAction
{
    protected static final String TRANSLATE_TO_LOWER_CASE = "translate(%s,"
            + " 'ABCDEFGHIJKLMNOPQRSTUVWXYZ', 'abcdefghijklmnopqrstuvwxyz')";
    private static final String TRANSLATE_TO_LOWER_CASE_FORMATTED = String.format(TRANSLATE_TO_LOWER_CASE, ".");
    private static final String ELEMENT_WITH_ANY_ATTRIBUTE_OR_TEXT_CASE_INSENSITIVE = String
            .format("[text()[%2$s=%1$s] or @*[%2$s=%1$s] or *[%2$s=%1$s]", "%1$s", TRANSLATE_TO_LOWER_CASE_FORMATTED);

    @Inject private IWebElementActions webElementActions;

    AbstractWebElementSearchAction(LocatorType elementActionType)
    {
        super(elementActionType);
    }

    protected List<WebElement> findElementsByText(SearchContext searchContext, By defaultLocator,
            SearchParameters parameters, String... tagNames)
    {
        List<WebElement> elements = findElements(searchContext, defaultLocator, parameters, Map.of());
        if (elements.isEmpty())
        {
            String text = parameters.getValue();
            By newLocator = generateCaseInsensitiveLocator(text, tagNames);
            return findElements(searchContext, newLocator, parameters, Map.of())
                    .stream()
                    .filter(element -> matchesToText(element, text))
                    .collect(Collectors.toList());
        }
        return elements;
    }

    protected static By generateCaseInsensitiveLocator(String text, String... tagNames)
    {
        @SuppressWarnings("PMD.InsufficientStringBufferDeclaration")
        StringBuilder locator = new StringBuilder();
        for (String tagName : tagNames)
        {
            locator.append(".//")
                    .append(tagName)
                    .append(ELEMENT_WITH_ANY_ATTRIBUTE_OR_TEXT_CASE_INSENSITIVE)
                    .append(" and not(.//")
                    .append(tagName)
                    .append(ELEMENT_WITH_ANY_ATTRIBUTE_OR_TEXT_CASE_INSENSITIVE)
                    .append("])]|");
        }
        return LocatorUtil.getXPathLocator(locator.substring(0, locator.length() - 1), text.toLowerCase());
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
}
