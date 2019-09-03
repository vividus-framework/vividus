/*
 * Copyright 2019 the original author or authors.
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

import java.util.List;
import java.util.Optional;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.ui.web.action.search.SearchAttributes;

public interface ISearchActions
{
    List<WebElement> findElements(SearchContext searchContext, By locator);

    List<WebElement> findElements(By locator);

    List<WebElement> findElements(SearchContext searchContext, SearchAttributes searchAttributes);

    List<WebElement> findElements(SearchAttributes searchAttributes);

    Optional<WebElement> findElement(SearchAttributes searchAttributes);
}
