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

package org.vividus.mobileapp.action;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.List;
import java.util.function.Consumer;

import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.mobile.action.search.AppiumLocatorType;

import io.appium.java_client.HasOnScreenKeyboard;
import io.appium.java_client.HidesKeyboard;

public class KeyboardActions
{
    private static final Logger LOGGER = LoggerFactory.getLogger(KeyboardActions.class);

    private final TouchActions touchActions;
    private final IWebDriverProvider webDriverProvider;
    private final GenericWebDriverManager genericWebDriverManager;
    private final ISearchActions searchActions;
    private final boolean realDevice;

    public KeyboardActions(boolean realDevice, TouchActions touchActions,
            IWebDriverProvider webDriverProvider, GenericWebDriverManager genericWebDriverManager,
            ISearchActions searchActions)
    {
        this.realDevice = realDevice;
        this.touchActions = touchActions;
        this.webDriverProvider = webDriverProvider;
        this.genericWebDriverManager = genericWebDriverManager;
        this.searchActions = searchActions;
    }

    /**
     * Type <b>text</b> into the <b>element</b>
     * <br>
     * The atomic actions performed are:
     * <ol>
     * <li>type text into the element</li>
     * <li>hide keyboard</li>
     * </ol>
     * @param element element to type text, must not be {@code null}
     * @param text text to type into the element, must not be {@code null}
     * @param hideKeyboard flag showing whether to hide the keyboard after typing
     */
    public void typeText(WebElement element, String text, boolean hideKeyboard)
    {
        LOGGER.atInfo().addArgument(text).log("Typing text '{}' into the field");
        if (hideKeyboard)
        {
            performWithHideKeyboard(element, e -> e.sendKeys(text));
        }
        else
        {
            element.sendKeys(text);
        }
    }

    /**
     * Clear the <b>element</b>'s text
     * <br>
     * The atomic actions performed are:
     * <ol>
     * <li>clear the element's text</li>
     * <li>hide keyboard</li>
     * </ol>
     * @param element element to clear, must not be {@code null}
     */
    public void clearText(WebElement element)
    {
        performWithHideKeyboard(element, WebElement::clear);
    }

    private void performWithHideKeyboard(WebElement webElement, Consumer<WebElement> performOnElement)
    {
        performOnElement.accept(webElement);

        // 1. https://github.com/appium/WebDriverAgent/blob/master/WebDriverAgentLib/Commands/FBCustomCommands.m#L107
        // 2. The keyboard is not shown in some cases: e.g. when trying to clear an empty field. So we need to check
        // whether the keyboard is shown at first
        if (genericWebDriverManager.isIOSNativeApp() && webDriverProvider.getUnwrapped(HasOnScreenKeyboard.class)
                .isKeyboardShown())
        {
            String tagName = getTagNameSafely(webElement);
            /*
             * Tap on 'Return' doesn't close the keyboard for XCUIElementTypeTextView element, the only way to
             * close the keyboard is to tap on any element outside the text view.
             */
            if (!"XCUIElementTypeTextView".equals(tagName))
            {
                /*
                 * Handle closing the keyboard as it's done in WDA
                 * https://github.com/appium/WebDriverAgent/pull/453/files
                 */
                Locator dismissKeyboardButtonLocator = new Locator(AppiumLocatorType.XPATH,
                        new SearchParameters("(//XCUIElementTypeKeyboard//XCUIElementTypeButton)[last()]",
                                Visibility.VISIBLE, false));
                List<WebElement> buttons = searchActions.findElements(webDriverProvider.get(),
                        dismissKeyboardButtonLocator);
                isTrue(!buttons.isEmpty(), "Unable to find a button to close the keyboard");
                touchActions.tap(buttons.get(0));
                return;
            }
            else if (realDevice)
            {
                LOGGER.atWarn().addArgument(tagName).log("Skip hiding keyboard for {}. Use the tap step to tap"
                        + " outside the {} to hide the keyboard");
                return;
            }
        }
        webDriverProvider.getUnwrapped(HidesKeyboard.class).hideKeyboard();
    }

    private static String getTagNameSafely(WebElement webElement)
    {
        try
        {
            return webElement.getTagName();
        }
        catch (StaleElementReferenceException exception)
        {
            // Swallow exception quietly
            return null;
        }
    }
}
