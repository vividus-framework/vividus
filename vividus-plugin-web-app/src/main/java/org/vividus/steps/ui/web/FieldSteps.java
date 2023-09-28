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

import java.util.Map;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.Browser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;
import org.vividus.ui.web.action.IFieldActions;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.util.FormatUtils;

@TakeScreenshotOnFailure
public class FieldSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FieldSteps.class);

    private static final String FIELD_TO_CLEAR = "The field to clear";
    private static final int TEXT_TYPING_ATTEMPTS_LIMIT = 5;

    private final IWebDriverManager webDriverManager;
    private final IFieldActions fieldActions;
    private final WebJavascriptActions javascriptActions;
    private final ISoftAssert softAssert;
    private final IBaseValidations baseValidations;

    public FieldSteps(IWebDriverManager webDriverManager, IFieldActions fieldActions,
            WebJavascriptActions javascriptActions, ISoftAssert softAssert, IBaseValidations baseValidations)
    {
        this.webDriverManager = webDriverManager;
        this.fieldActions = fieldActions;
        this.javascriptActions = javascriptActions;
        this.softAssert = softAssert;
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
        enterTextInField(normalizedText, locator, false);
    }

    private void enterTextInField(String text, Locator locator, boolean retry)
    {
        baseValidations.assertElementExists("The field to enter text", locator).ifPresent(
                element -> enterTextInField(element, text, retry, () -> enterTextInField(text, locator, true)));
    }

    private void enterTextInField(WebElement element, String text, boolean retry, Runnable retryRunnable)
    {
        try
        {
            element.clear();
            LOGGER.info("Entering text \"{}\" in element", text);
            if (webDriverManager.isBrowserAnyOf(Browser.SAFARI) && fieldActions.isElementContenteditable(element))
            {
                javascriptActions.executeScript("var element = arguments[0];element.innerHTML = arguments[1];", element,
                        text);
                return;
            }

            fieldActions.typeText(element, text);
            applyWorkaroundIfIE(element, text);
        }
        catch (StaleElementReferenceException e)
        {
            if (retry)
            {
                throw e;
            }
            LOGGER.info("An element is stale. One more attempt to type text into it");
            retryRunnable.run();
        }
    }

    @SuppressWarnings("unchecked")
    private void applyWorkaroundIfIE(WebElement element, String normalizedText)
    {
        // Workaround for IExplore: https://github.com/seleniumhq/selenium/issues/805
        if (webDriverManager.isBrowserAnyOf(Browser.IE) && Boolean.TRUE.equals(
                ((Map<String, Object>) webDriverManager.getCapabilities().getCapability(
                        InternetExplorerOptions.IE_OPTIONS)).get(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS)))
        {
            int iterationsCounter = TEXT_TYPING_ATTEMPTS_LIMIT;
            while (iterationsCounter > 0 && isValueNotEqualTo(element, normalizedText))
            {
                element.clear();
                LOGGER.info("Re-typing text \"{}\" to element", normalizedText);
                fieldActions.typeText(element, normalizedText);
                iterationsCounter--;
            }
            if (iterationsCounter == 0 && isValueNotEqualTo(element, normalizedText))
            {
                softAssert.recordFailedAssertion(String.format("The element is not filled correctly"
                        + " after %d typing attempt(s)", TEXT_TYPING_ATTEMPTS_LIMIT + 1));
            }
        }
    }

    private boolean isValueNotEqualTo(WebElement element, String expectedValue)
    {
        return !expectedValue.equals(javascriptActions.executeScript("return arguments[0].value;", element));
    }
}
