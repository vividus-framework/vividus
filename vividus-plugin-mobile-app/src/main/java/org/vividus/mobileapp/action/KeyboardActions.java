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

package org.vividus.mobileapp.action;

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
     */
    public void typeText(WebElement element, String text)
    {
        LOGGER.atInfo().addArgument(text).log("Typing text '{}' into the field");
        performWithHideKeyboard(() -> element.sendKeys(text));
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
        performWithHideKeyboard(element::clear);
    }

    private void performWithHideKeyboard(Runnable runnable)
    {
        runnable.run();

        // https://github.com/appium/WebDriverAgent/blob/master/WebDriverAgentLib/Commands/FBCustomCommands.m#L107
        if (genericWebDriverManager.isIOSNativeApp() && realDevice)
        {
            Locator keyboardReturnLocator = new Locator(AppiumLocatorType.XPATH, new SearchParameters(
                    "//XCUIElementTypeKeyboard//XCUIElementTypeButton[@name='Return']", Visibility.VISIBLE, false));
            searchActions.findElement(webDriverProvider.get(), keyboardReturnLocator).ifPresentOrElse(touchActions::tap,
                () ->
                {
                    throw new IllegalStateException("Unable to find 'Return' button to close the keyboard");
                });
        }
        else
        {
            webDriverProvider.getUnwrapped(HidesKeyboard.class).hideKeyboard();
        }
    }
}
