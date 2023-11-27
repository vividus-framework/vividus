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

package org.vividus.ui.web.playwright.steps;

import com.microsoft.playwright.Locator;

import org.jbehave.core.annotations.When;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

public class FieldSteps
{
    private final UiContext uiContext;

    public FieldSteps(UiContext uiContext)
    {
        this.uiContext = uiContext;
    }

    /**
     * Enter the text in the field found by the given locator.
     * <p>
     * It works for elements declared using <code>&lt;input&gt;</code> or <code>&lt;textarea&gt;</code> tags and for
     * <code>[contenteditable]</code> elements (e.g. CKE editors, they are usually located via <code>&lt;body&gt;
     * </code> tag, that is placed in a frame as a separate HTML document).
     * <p>The actions performed are:</p>
     * <ul>
     * <li>find the field by the locator;</li>
     * <li>clear the field if it is found, otherwise the whole step is failed and its execution stops;</li>
     * <li>type the text in the field.</li>
     * </ul>
     *
     * @param text    The text to enter in the field.
     * @param locator The locator used to find the field to enter the text.
     */
    @When("I enter `$text` in field located by `$locator`")
    public void enterTextInField(String text, PlaywrightLocator locator)
    {
        uiContext.locateElement(locator).fill(text);
    }

    /**
     * Enters the text in the field, found by the given locator, without clearing the previous content.
     * <p>
     * It works for elements declared using <code>&lt;input&gt;</code> or <code>&lt;textarea&gt;</code> tags and for
     * <code>[contenteditable]</code> elements (e.g. CKE editors, they are usually located via <code>&lt;body&gt;
     * </code> tag, that is placed in a frame as a separate HTML document).
     *
     * @param text    The text to add to the field.
     * @param locator The locator used to find the field to add the text.
     */
    @When("I add `$text` to field located by `$locator`")
    public void addTextToField(String text, PlaywrightLocator locator)
    {
        Locator field = uiContext.locateElement(locator);
        field.fill(field.inputValue() + text);
    }

    /**
     * Finds the field by the given locator and clears it if it's found.
     * <p>
     * It works for elements declared using <code>&lt;input&gt;</code> or <code>&lt;textarea&gt;</code> tags and for
     * <code>[contenteditable]</code> elements (e.g. CKE editors, they are usually located via <code>&lt;body&gt;
     * </code> tag, that is placed in a frame as a separate HTML document).
     *
     * @param locator The locator used to find the field to clear.
     */
    @When("I clear field located by `$locator`")
    public void clearField(PlaywrightLocator locator)
    {
        uiContext.locateElement(locator).clear();
    }
}
