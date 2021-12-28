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

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.ui.action.search.IElementSearchAction;
import org.vividus.ui.action.search.LocatorType;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.web.util.LocatorUtil;

public class ElementNameSearch extends AbstractWebElementSearchAction implements IElementSearchAction
{
    public ElementNameSearch(LocatorType elementActionType)
    {
        super(elementActionType);
    }

    @Override
    public List<WebElement> search(SearchContext searchContext, SearchParameters parameters,
            Map<LocatorType, List<String>> filters)
    {
        String elementName = parameters.getValue();
        return findElementsByText(searchContext,
                LocatorUtil.getXPathLocator(".//*[@*=%1$s or text()=%1$s]", elementName), parameters, "*");
    }
}
