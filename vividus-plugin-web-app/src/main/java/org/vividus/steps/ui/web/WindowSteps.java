/*
 * Copyright 2019-2022 the original author or authors.
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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.not;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.monitor.TakeScreenshotOnFailure;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;
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
     * Changes the current browser window size to the specified one.
     * NOTE: The specified browser window size should be smaller than the current screen resolution.
     *
     * @param targetSize The desired browser window size in pixels, e.g. `800x600`,
     *                   where the first measure is window width, the last one is window height.
     */
    @When("I change window size to `$targetSize`")
    public void resizeCurrentWindow(Dimension targetSize)
    {
        if (canTryToResize(targetSize))
        {
            getWebDriver().manage().window().setSize(targetSize);
        }
    }

    /**
     * Closes <b>current window</b> and switches to the window from which rederection to current window was performed
     * <p>
     * Each browser <b>window</b> or <b>tab</b> is considered to be a separate <b>window object</b>. This object holds
     * corresponding <b>Document</b> object, which itself is a html page. So this method applies to both windows and
     * tabs.
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Receives all opened browser windows
     * <li>Identifies current window and closes it
     * <li>Switches back to the window from which rederection to current window was performed
     * </ul>
     * @see <a href="https://html.spec.whatwg.org/#browsing-context"><i>Browsing context (Window &amp; Document)</i></a>
     * @see <a href="https://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     */
    @When("I close the current window")
    public void closeCurrentWindow()
    {
        tryToCloseCurrentWindow(false);
    }

    /**
     * Trying to close the <b>current window</b> with JavaScript method 'close()'.
     * If an alert window via 'onbeforeunload' event is opened, it must be checked and handled in the subsequent steps.
     * If an alert window is not opened, the step closes the current window and switches to the previous window.
     * <p>
     * Each browser <b>window</b> or <b>tab</b> is considered to be a separate <b>window object</b>. This object holds
     * corresponding <b>Document</b> object, which itself is a html page. So this method applies to both windows and
     * tabs.
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Receives all opened browser windows
     * <li>Identifies current window and try to close it
     * <li>If an alert window via 'onbeforeunload' event opens, step completes execution
     * (alert is expected to be handled in next steps)
     * <li>If an alert window does not open, closes current window and
     *  switches back to the window from which rederection to current window was performed
     * </ul>
     * <p>
     * Note that this step can only be used if the current window was opened using the step
     * 'When I open URL `$pageUrl` in new window'.
     * <p>
     * @see <a href="https://html.spec.whatwg.org/#browsing-context"><i>Browsing context (Window &amp; Document)</i></a>
     * @see <a href="https://www.w3schools.com/tags/default.asp"><i>HTML Element Reference</i></a>
     * @see <a href="https://www.w3schools.com/jsref/event_onbeforeunload.asp"><i>Event 'onbeforeunload'</i></a>
     * @see <a href="https://www.w3schools.com/js/js_popup.asp"><i>JavaScript Popup Boxes</i></a>
     */
    @When("I attempt to close current window with possibility to handle alert")
    public void closeCurrentWindowWithAlertsHandling()
    {
        tryToCloseCurrentWindow(true);
    }

    private void tryToCloseCurrentWindow(boolean handleAlerts)
    {
        WebDriver driver = getWebDriver();
        String currentWindow = driver.getWindowHandle();
        for (String window : driver.getWindowHandles())
        {
            if (!window.equals(currentWindow))
            {
                if (handleAlerts)
                {
                    javascriptActions.closeCurrentWindow();
                    if (!alertActions.isAlertPresent())
                    {
                        driver.switchTo().window(window);
                    }
                    else
                    {
                        LOGGER.info("Alert dialog box is shown and must be handled in the subsequent steps.");
                    }
                }
                else
                {
                    driver.close();
                    driver.switchTo().window(window);
                }
                break;
            }
        }
        softAssert.assertThat("Current window has been closed", driver.getWindowHandles(),
                not(contains(currentWindow)));
    }

    private boolean canTryToResize(Dimension targetSize)
    {
        return webDriverManager.checkWindowFitsScreen(targetSize, (fitsScreen, size) -> {
            String assertionDescription = String.format(
                    "The desired browser window size %dx%d fits the screen size (%dx%d)", targetSize.getWidth(),
                    targetSize.getHeight(), size.getWidth(), size.getHeight());
            softAssert.assertTrue(assertionDescription, fitsScreen);
        }).orElse(true);
    }

    private WebDriver getWebDriver()
    {
        return webDriverProvider.get();
    }
}
