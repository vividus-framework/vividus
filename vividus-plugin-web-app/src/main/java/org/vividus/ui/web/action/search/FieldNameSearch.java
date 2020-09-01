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

import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.ui.action.search.AbstractElementAction;
import org.vividus.ui.action.search.IElementSearchAction;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.web.util.LocatorUtil;

public class FieldNameSearch extends AbstractElementAction implements IElementSearchAction
{
    public FieldNameSearch()
    {
        super(WebLocatorType.FIELD_NAME);
    }

    @Override
    public List<WebElement> search(SearchContext searchContext, SearchParameters parameters)
    {
        // due to firefox bug, we can't use name() and must use local-name() as workaround 'body' represents CKE editor
        By locator = LocatorUtil.getXPathLocator(".//*[(local-name() = 'input' or local-name() = 'textarea' or "
                + "local-name()='body') and ((@* | text())=%s)]", parameters.getValue());
        return findElements(searchContext, locator, parameters);
    }
}
