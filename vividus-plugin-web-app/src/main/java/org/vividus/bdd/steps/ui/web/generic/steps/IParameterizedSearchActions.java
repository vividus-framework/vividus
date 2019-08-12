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

package org.vividus.bdd.steps.ui.web.generic.steps;

import java.util.List;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

/**
 * Search of elements for generic parameterized steps
 */
public interface IParameterizedSearchActions
{
    /**
     * Finds elements according to specified search-parameters
     * @param searchContext Search context
     * @param searchInputData Search input data
     * @return List of found elements
     */
    List<WebElement> findElements(SearchContext searchContext, SearchInputData searchInputData);

    /**
     * Finds or filters elements list according to specified WebElement attributes
     * @param searchContext Search context
     * @param searchInputData Search input data
     * @param elements List of elements
     * @return List of found or filtered elements
     */
    List<WebElement> findElementsWithAttributes(SearchContext searchContext, SearchInputData searchInputData,
            List<WebElement> elements);

    /**
     * Filters elements list according to specified WebElement CSS properties
     * @param searchInputData Search input data
     * @param elements List of elements
     * @return List of found or filtered elements
     */
    List<WebElement> filterElementsWithCssProperties(SearchInputData searchInputData, List<WebElement> elements);
}
