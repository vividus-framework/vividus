/*
 * Copyright 2019-2021 the original author or authors.
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

import javax.inject.Inject;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.monitor.TakeScreenshotOnFailure;
import org.vividus.selenium.WebDriverType;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.web.action.IFieldActions;
import org.vividus.ui.web.action.IWebElementActions;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.util.FormatUtil;

@TakeScreenshotOnFailure
public class FieldSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(FieldSteps.class);

    private static final String A_FIELD_WITH_NAME = "A field with attributes%1$s";
    private static final int TEXT_TYPING_ATTEMPTS_LIMIT = 5;

    @Inject private IBaseValidations baseValidations;
    @Inject private IWebElementActions webElementActions;
    @Inject private IFieldActions fieldActions;
    @Inject private ISoftAssert softAssert;
    @Inject private WebJavascriptActions javascriptActions;
    @Inject private IWebDriverManager webDriverManager;

    /**
     * Clears an <b>element</b> located by <b>locator</b>
     * <p>
     * Can be used to delete the text from elements like <i>{@literal <input>}</i>, <i>{@literal <textarea>}</i>
     * (or {@literal <body>} if you work with a CKE editor - a field to enter and edit text,
     * that is contained in a frame as a separate html document), don't trigger keyboard or mouse events on the field.
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Finds the <b>text field</b>
     * <li>Clears it
     * </ul>
     * @param locator to locate field
     * @return WebElement An element that was cleared
     * @see <a href="https://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @When("I clear field located `$locator`")
    public WebElement clearFieldLocatedBy(Locator locator)
    {
        WebElement element = findFieldBy(locator);
        if (element != null)
        {
            element.clear();
        }
        return element;
    }

    /**
     * Clears an <b>element</b> located by <b>locator</b> using keyboard
     * <p>
     * Can be used to delete the text from elements like <i>{@literal <input>}</i>, <i>{@literal <textarea>}</i>
     * (or {@literal <body>} if you work with a CKE editor - a field to enter and edit text,
     * that is contained in a frame as a separate html document). Simulates an user action by pressing buttons
     * Ctrl+a and Backspace, what allows to trigger keyboard or mouse events on the field.
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Finds the <b>text field</b>
     * <li>Clears it by pressing Ctrl+a, Backspace
     * </ul>
     * @param locator to locate element
     * @return WebElement An element that was cleared
     * @see <a href="https://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @When("I clear field located `$locator` using keyboard")
    public WebElement clearFieldLocatedByUsingKeyboard(Locator locator)
    {
        WebElement element = findFieldBy(locator);
        fieldActions.clearFieldUsingKeyboard(element);
        return element;
    }

    /**
     * Enters a 'text' in a <b>field</b> located by locator without clearing its previous content
     * <p>
     * Can be used for typing text into elements like <i>{@literal <input>}</i>, <i>{@literal <textarea>}</i> (or
     * {@literal <body>} if you work with a CKE editor - a field to enter and edit text, that is contained in a frame as
     * a separate html document).
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Finds the <b>element</b>
     * <li>Types the text into it
     * </ul>
     * @param locator to locate element
     * @param text A text to type into the <b>element</b>
     * @see <a href="https://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @When("I add `$text` to field located `$locator`")
    public void addTextToField(String text, Locator locator)
    {
        WebElement field = findFieldBy(locator);
        webElementActions.addText(field, text);
    }

    /**
     * Enters text in the field located by locator
     * @param locator to locate element
     * @param text A text to type into the <b>element</b>
     * @see <a href="https://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @SuppressWarnings("unchecked")
    @When("I enter `$text` in field located `$locator`")
    public void enterTextInField(String text, Locator locator)
    {
        WebElement element = findElement(locator);
        if (element != null)
        {
            String normalizedText = FormatUtil.normalizeLineEndings(text);
            element.clear();
            LOGGER.info("Entering text \"{}\" in element", normalizedText);
            if (webDriverManager.isTypeAnyOf(WebDriverType.SAFARI)
                    && webElementActions.isElementContenteditable(element))
            {
                javascriptActions.executeScript("var element = arguments[0];element.innerHTML = arguments[1];", element,
                        normalizedText);
                return;
            }
            element = sendKeysRetryingIfStale(locator, element, normalizedText);
            applyWorkaroundIfIE(element, normalizedText);
        }
    }

    private WebElement sendKeysRetryingIfStale(Locator locator, WebElement element, String normalizedText)
    {
        WebElement toSendKeys = element;
        try
        {
            toSendKeys.sendKeys(normalizedText);
        }
        catch (StaleElementReferenceException e)
        {
            LOGGER.info("An element is stale. One more attempt to type text into it");
            toSendKeys = findElement(locator);
            toSendKeys.sendKeys(normalizedText);
        }
        return toSendKeys;
    }

    private void applyWorkaroundIfIE(WebElement element, String normalizedText)
    {
        // Workaround for IExplore: https://github.com/seleniumhq/selenium/issues/805
        if (webDriverManager.isTypeAnyOf(WebDriverType.IEXPLORE) && Boolean.TRUE.equals(
                ((Map<String, Object>) webDriverManager.getCapabilities().getCapability(WebDriverType.IE_OPTIONS))
                        .get("requireWindowFocus")))
        {
            int iterationsCounter = TEXT_TYPING_ATTEMPTS_LIMIT;
            while (iterationsCounter > 0 && !isValueEqualTo(element, normalizedText))
            {
                element.clear();
                LOGGER.info("Re-typing text \"{}\" to element", normalizedText);
                element.sendKeys(normalizedText);
                iterationsCounter--;
            }
            if (iterationsCounter == 0 && !isValueEqualTo(element, normalizedText))
            {
                softAssert.recordFailedAssertion(String.format("The element is not filled correctly"
                        + " after %d typing attempt(s)", TEXT_TYPING_ATTEMPTS_LIMIT + 1));
            }
        }
    }

    private WebElement findElement(Locator locator)
    {
        return baseValidations.assertIfElementExists(String.format("An element with attributes%1$s", locator), locator);
    }

    private boolean isValueEqualTo(WebElement element, String expectedValue)
    {
        return expectedValue.equals(javascriptActions.executeScript("return arguments[0].value;", element));
    }

    /**
     * Checks that a field located by <b>locator</b> does not exist in the previously set content
     * <p>
     * <b>Field</b> is a {@literal <input> or <textarea>} tags in the table (or a {@literal <body>} tag if you work with
     * CKE editor - a field to enter and edit text, that is contained in a {@literal <frame>} as a separate
     * html-document)
     * <p>
     * @param locator to locate field
     */
    @Then("field located `$locator` does not exist")
    public void doesNotFieldExist(Locator locator)
    {
        baseValidations.assertIfElementDoesNotExist(String.format(A_FIELD_WITH_NAME, locator), locator);
    }

    /**
     * Checks that previously set searchContext contains a field located by <b>locator</b>
     * <p>
     * <b>Field</b> is a {@literal <input> or <textarea>} tags in the table (or a {@literal <body>} tag if you work with
     * CKE editor - a field to enter and edit text, that is contained in a {@literal <frame>} as a separate
     * html-document)
     * <p>
     * @param locator to locate field
     * @return WebElement
     */
    @Then("field located `$locator` exists")
    public WebElement findFieldBy(Locator locator)
    {
        return baseValidations.assertIfElementExists(String.format(A_FIELD_WITH_NAME, locator), locator);
    }
}
