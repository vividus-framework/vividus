/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.ui.context;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;

public interface IUiContext
{
    /**
     * Returns the current search context or null if it's not set
     *
     * @return Search context or null if it's not set
     * @deprecated Use {@link #getOptionalSearchContext()} instead
     */
    @Deprecated(since = "0.4.8", forRemoval = true)
    SearchContext getSearchContext();

    Optional<SearchContext> getOptionalSearchContext();

    <T extends SearchContext> Optional<T> getSearchContext(Class<T> clazz);

    SearchContextSetter getSearchContextSetter();

    void putSearchContext(SearchContext searchContext, SearchContextSetter setter);

    void reset();

    void clear();

    List<WebElement> getAssertingWebElements();

    boolean withAssertingWebElements(List<WebElement> elements, BooleanSupplier asserter);
}
