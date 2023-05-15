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

import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

public interface IFieldActions
{
    void selectItemInDropDownList(Select select, String text, boolean addition);

    void clearFieldUsingKeyboard(WebElement field);

    /**
     * Enters text in any element without clearing its previous content
     * @param element Any element to type
     * @param text Text to type
     */
    void addText(WebElement element, String text);

    /**
     * Types the text in the element
     * @param element The element to type text in
     * @param text The text to type
     */
    void typeText(WebElement element, String text);

    /**
     * Checks that the content of the element is editable
     * @param element element to check
     * @return true - if the element's content is editable or false - if it's not
     */
    boolean isElementContenteditable(WebElement element);
}
