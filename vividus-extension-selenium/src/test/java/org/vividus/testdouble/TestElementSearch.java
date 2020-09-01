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

package org.vividus.testdouble;

import java.util.List;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.ui.action.search.IElementSearchAction;
import org.vividus.ui.action.search.LocatorType;
import org.vividus.ui.action.search.SearchParameters;

public class TestElementSearch implements IElementSearchAction
{
    @Override
    public List<WebElement> search(SearchContext searchContext, SearchParameters parameters)
    {
        return List.of();
    }

    @Override
    public LocatorType getType()
    {
        return TestLocatorType.SEARCH;
    }
}
