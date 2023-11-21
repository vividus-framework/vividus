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

package org.vividus.steps.ui.web;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.locator.Locator;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;
import org.vividus.ui.web.action.IFieldActions;
import org.vividus.ui.web.util.FormatUtils;

@TakeScreenshotOnFailure
public class FieldSteps
{
    private static final String FIELD_TO_CLEAR = "The field to clear";

    private final IFieldActions fieldActions;
    private final IBaseValidations baseValidations;

    public FieldSteps(IFieldActions fieldActions, IBaseValidations baseValidations)
    {
        this.fieldActions = fieldActions;
        this.baseValidations = baseValidations;
    }

    /**
     * Clears the field found by the specified locator.
     * <p>
     * It's allowed to delete the text from elements declared using <i>{@literal <input>}</i> or <i>{@literal
     * <textarea>}</i> tags and from CKE editors (they usually should be located via {@literal <body>} tag, that is
     * contained in a frame as a separate HTML document).
     * <p>
     * The step does not trigger any keyboard or mouse events on the field.
     *
     * @param locator The locator used to find field.
     * @see <a href="https://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @When("I clear field located by `$locator`")
    public void clearField(Locator locator)
    {
        baseValidations.assertElementExists(FIELD_TO_CLEAR, locator).ifPresent(WebElement::clear);
    }

    /**
     * Clears the field found by the specified locator using keyboard.
     * <p>
     * It's allowed to delete the text from elements declared using <i>{@literal <input>}</i> or <i>{@literal
     * <textarea>}</i> tags and from CKE editors (they usually should be located via {@literal <body>} tag, that is
     * contained in a frame as a separate HTML document).
     * <p>
     * The step simulates user action by pressing buttons Ctrl+A and Backspace, that allows to trigger keyboard
     * events on the field.
     *
     * @param locator The locator used to find field.
     * @see <a href="https://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @When("I clear field located by `$locator` using keyboard")
    public void clearFieldUsingKeyboard(Locator locator)
    {
        baseValidations.assertElementExists(FIELD_TO_CLEAR, locator).ifPresent(fieldActions::clearFieldUsingKeyboard);
    }

    /**
     * Enters the text in the field found by the specified locator without clearing of the previous content.
     * <p>
     * It's allowed to add the text to elements declared using <i>{@literal <input>}</i> or <i>{@literal
     * <textarea>}</i> tags and from CKE editors (they usually should be located via {@literal <body>} tag, that is
     * contained in a frame as a separate HTML document).
     * </p>
     *
     * @param text    The text to add to the field.
     * @param locator The locator used to find field.
     * @see <a href="https://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @When("I add `$text` to field located by `$locator`")
    public void addTextToField(String text, Locator locator)
    {
        baseValidations.assertElementExists("The field to add text", locator).ifPresent(
                field -> fieldActions.addText(field, text));
    }

    /**
     * Enters the text in a field found by the specified locator.
     * <p>
     * It's allowed to enter the text in elements declared using <i>{@literal <input>}</i> or <i>{@literal
     * <textarea>}</i> tags and from CKE editors (they usually should be located via {@literal <body>} tag, that is
     * contained in a frame as a separate HTML document).
     * <p>The atomic actions performed are:</p>
     * <ul>
     * <li>find the field by the locator;</li>
     * <li>clear the field if it is found, otherwise the whole step is failed and its execution stops;</li>
     * <li>type the text in the field;</li>
     * <li>the first three actions are retried once if the field becomes stale during actions execution in other words
     * <a href="https://www.selenium.dev/exceptions/#stale_element_reference">StaleElementReferenceException</a>
     * is thrown at any atomic action.</li>
     * </ul>
     *
     * @param text    The text to enter in the field.
     * @param locator The locator used to find field.
     * @see <a href="https://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @When("I enter `$text` in field located by `$locator`")
    public void enterTextInField(String text, Locator locator)
    {
        String normalizedText = FormatUtils.normalizeLineEndings(text);
        baseValidations.assertElementExists("The field to enter text", locator).ifPresent(
                element -> fieldActions.typeText(element, normalizedText));
    }
}
