/*
 * Copyright 2019-2023 the original author or authors.
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

import org.openqa.selenium.WebElement;
import org.vividus.ui.action.ElementActions;

public interface IWebElementActions extends ElementActions
{
    /**
     * Gets css value and removes all occurrences of quotes and apostrophes
     * @param element WebElement to get value from
     * @param propertyName Desired css property
     * @return css property value without quotes and apostrophes
     */
    String getCssValue(WebElement element, String propertyName);

    /**
     * Gets the text from css 'content' value of the chosen element
     * @param element Web element which contains ':before' or ':after' pseudo-element with 'content' value
     * @return the text from the css 'content' value of the chosen element or empty string if no content found
     */
    String getPseudoElementContent(WebElement element);

    /**
     * Gets the text from css 'content' value of all pseudo-elements on the page
     * @return the list of text values from the css 'content' value of all the pseudo-elements
     * or empty list if no content found
     */
    List<String> getAllPseudoElementsContent();

    /**
     * Gets the text content from whole page
     * @return the inner text from whole page
     * or empty string if no content found
     */
    String getPageText();
}
