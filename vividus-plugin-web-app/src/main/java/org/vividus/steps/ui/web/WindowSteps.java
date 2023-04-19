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

import java.util.Optional;
import java.util.function.BiConsumer;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;
import org.vividus.ui.web.action.IAlertActions;
import org.vividus.ui.web.action.WebJavascriptActions;

@TakeScreenshotOnFailure
public class WindowSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(WindowSteps.class);

    private final IWebDriverProvider webDriverProvider;
    private final IWebDriverManager webDriverManager;
    private final WebJavascriptActions javascriptActions;
    private final IAlertActions alertActions;
    private final ISoftAssert softAssert;

    public WindowSteps(IWebDriverProvider webDriverProvider, IWebDriverManager webDriverManager,
            WebJavascriptActions javascriptActions, IAlertActions alertActions, ISoftAssert softAssert)
    {
        this.webDriverProvider = webDriverProvider;
        this.webDriverManager = webDriverManager;
        this.javascriptActions = javascriptActions;
        this.alertActions = alertActions;
        this.softAssert = softAssert;
    }

    /**
     * Opens a new browser tab and switches the focus for future commands to this tab.
     */
    @When("I open new tab")
    public void openNewTab()
    {
        getWebDriver().switchTo().newWindow(WindowType.TAB);
    }

    /**
     * Changes the current browser window size to the specified one.
     * NOTE: The specified browser window size should be smaller than the current screen resolution.
     *
     * @param targetSize The desired browser window size in pixels, e.g. `800x600`,
     *                   where the first measure is window width, the last one is window height.
     */
    @When("I change window size to `$targetSize`")
    public void resizeCurrentWindow(Dimension targetSize)
    {
        boolean resizingPossible = true;
        Optional<Dimension> optionalScreenResolution = webDriverManager.getScreenResolution();
        if (optionalScreenResolution.isPresent())
        {
            Dimension screenResolution = optionalScreenResolution.get();

            boolean fitsScreen = targetSize.getWidth() <= screenResolution.getWidth()
                    && targetSize.getHeight() <= screenResolution.getHeight();
            String assertionDescription = String.format(
                    "The desired browser window size %dx%d fits the screen size (%dx%d)", targetSize.getWidth(),
                    targetSize.getHeight(), screenResolution.getWidth(), screenResolution.getHeight());
            resizingPossible = softAssert.assertTrue(assertionDescription, fitsScreen);
        }
        if (resizingPossible)
        {
            getWebDriver().manage().window().setSize(targetSize);
        }
    }

    /**
     * Closes <b>current tab</b> and switches to the tab from which redirection to current tab was performed
     * Actions performed at this step:
     * <ul>
     * <li>Receives all opened browser tabs
     * <li>Identifies current tab and closes it
     * <li>Switches back to the tab from which redirection to current tab was performed
     * </ul>
     * @see <a href="https://html.spec.whatwg.org/#browsing-context"><i>Browsing context</i></a>
     * @see <a href="https://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @When("I close current tab")
    public void closeCurrentTab()
    {
        boolean tabClosed = tryToCloseCurrentTab((driver, tab) ->
        {
            driver.close();
            driver.switchTo().window(tab);
        });
        softAssert.recordAssertion(tabClosed, "Current tab has been closed");
    }

    /**
     * Trying to close the <b>current tab</b> with JavaScript method 'close()'.
     * If an alert window is opened via 'onbeforeunload' event, it must be checked and handled in the subsequent steps.
     * If an alert window is not opened, the step closes the current tab and switches to the previous tab.
     * Actions performed at this step:
     * <ul>
     * <li>Receives all opened browser tabs
     * <li>Identifies current tab and try to close it
     * <li>If an alert window via 'onbeforeunload' event opens, step completes execution
     * (alert is expected to be handled in next steps)
     * <li>If an alert window does not open, closes current tab and
     *  switches back to the tab from which redirection to current tab was performed
     * </ul>
     * <p>
     * Note that this step can only be used if the current tab was opened using the step
     * 'When I open URL `$pageUrl` in new tab'.
     * </p>
     * @see <a href="https://html.spec.whatwg.org/#browsing-context"><i>Browsing context</i></a>
     * @see <a href="https://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     * @see <a href="https://www.w3schools.com/jsref/event_onbeforeunload.asp"><i>Event 'onbeforeunload'</i></a>
     * @see <a href="https://www.w3schools.com/js/js_popup.asp"><i>JavaScript Popup Boxes</i></a>
     */
    @When("I attempt to close current tab with possibility to handle alert")
    public void closeCurrentTabWithAlertsHandling()
    {
        tryToCloseCurrentTab((driver, tab) ->
        {
            javascriptActions.closeCurrentTab();
            if (!alertActions.isAlertPresent(driver))
            {
                driver.switchTo().window(tab);
            }
            else
            {
                LOGGER.info("Alert dialog box is shown and must be handled in the subsequent steps.");
            }
        });
    }

    private boolean tryToCloseCurrentTab(BiConsumer<WebDriver, String> closeExecutor)
    {
        WebDriver driver = getWebDriver();
        String currentTab = driver.getWindowHandle();
        for (String tab : driver.getWindowHandles())
        {
            if (!tab.equals(currentTab))
            {
                closeExecutor.accept(driver, tab);
                break;
            }
        }
        return !driver.getWindowHandles().contains(currentTab);
    }

    private WebDriver getWebDriver()
    {
        return webDriverProvider.get();
    }
}
