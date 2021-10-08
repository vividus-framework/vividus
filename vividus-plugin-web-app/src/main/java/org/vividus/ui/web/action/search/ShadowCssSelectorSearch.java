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

import com.codeborne.selenide.selector.ByShadow;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.How;
import org.vividus.ui.action.search.IElementSearchAction;
import org.vividus.ui.action.search.LocatorType;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;

public class ShadowCssSelectorSearch extends BySeleniumLocatorSearch implements IElementSearchAction
{
    public ShadowCssSelectorSearch(LocatorType type, How how)
    {
        super(type, how);
    }

    @Override
    public List<WebElement> search(SearchContext searchContext, SearchParameters parameters)
    {
        if (parameters instanceof ShadowDomSearchParameters)
        {
            ShadowDomSearchParameters shadowParameters = (ShadowDomSearchParameters) parameters;
            List<WebElement> elements = ByShadow.cssSelector(shadowParameters.getValue(),
                    shadowParameters.getUpperShadowHost(), shadowParameters.getInnerShadowHosts())
                    .findElements(searchContext);

            if (!elements.isEmpty())
            {
                Visibility visibility = parameters.getVisibility();
                return Visibility.ALL == visibility
                        ? elements
                        : filterElementsByVisibility(elements, visibility, false);
            }
        }
        return List.of();
    }
}
