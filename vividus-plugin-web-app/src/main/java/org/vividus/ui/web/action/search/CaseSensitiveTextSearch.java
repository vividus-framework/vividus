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

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.ui.action.search.IElementFilterAction;
import org.vividus.ui.action.search.IElementSearchAction;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.web.util.LocatorUtil;

public class CaseSensitiveTextSearch extends AbstractWebElementSearchAction
        implements IElementSearchAction, IElementFilterAction
{
    public CaseSensitiveTextSearch()
    {
        super(WebLocatorType.CASE_SENSITIVE_TEXT);
    }

    @Override
    public List<WebElement> search(SearchContext searchContext, SearchParameters parameters)
    {
        String value = parameters.getValue();
        List<WebElement> elements = findElements(searchContext, LocatorUtil.getXPathLocatorByFullInnerText(value),
                parameters);
        if (elements.isEmpty())
        {
            By locator = LocatorUtil.getXPathLocatorByInnerText(value);
            return findElementsByText(searchContext, locator, parameters, "*");
        }
        return elements;
    }

    @Override
    public boolean matches(WebElement element, String text)
    {
        String elementText = getWebElementActions().getElementText(element);
        return text.equals(elementText) || StringUtils.equalsIgnoreCase(elementText, text) && matchesToText(element,
                text);
    }
}
