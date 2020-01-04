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

package org.vividus.ui.web.action;

import java.util.Collection;
import java.util.stream.Stream;

import org.openqa.selenium.WebElement;

/**
 * <a href="https://www.w3schools.com/cssref/css_selectors.asp">CSS Selector</a> factory
 */
public interface ICssSelectorFactory
{
    /**
     * Creates <a href="https://www.w3schools.com/cssref/css_selectors.asp">CSS Selector</a> for a given WebElement
     * @param element WebElement to create CSS selector for
     * @return CSS selector
     */
    String getCssSelector(WebElement element);

    /**
     * Creates a single <a href="https://www.w3schools.com/cssref/css_selectors.asp">CSS Selector</a> for a given
     * collection of WebElement-s
     * @param elements collection of WebElement-s to create CSS selector for
     * @return CSS selector
     */
    String getCssSelector(Collection<WebElement> elements);

    /**
     * Creates a stream of <a href="https://www.w3schools.com/cssref/css_selectors.asp">CSS Selector</a> for a given
     * collection of WebElement-s
     * @param elements collection of WebElement-s to create CSS selector for
     * @return CSS selectors of elements
     */
    Stream<String> getCssSelectors(Collection<WebElement> elements);
}
