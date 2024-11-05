/*
 * Copyright 2019-2024 the original author or authors.
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

import com.google.common.eventbus.EventBus;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.web.event.PageLoadEndEvent;

import jakarta.inject.Inject;

public class MouseActions implements IMouseActions
{
    private static final String COULD_NOT_CLICK_ERROR_MESSAGE = "Could not click on the element: ";
    private static final By BODY_XPATH_LOCATOR = By.xpath("//body");

    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IWebDriverManager webDriverManager;
    @Inject private ISoftAssert softAssert;
    @Inject private WebJavascriptActions javascriptActions;
    @Inject private IWebWaitActions waitActions;
    @Inject private IAlertActions alertActions;
    @Inject private EventBus eventBus;
    @Inject private IUiContext uiContext;

    @Override
    public ClickResult click(WebElement element)
    {
        ClickResult clickResult = new ClickResult();
        if (element != null)
        {
            WebDriver webDriver = getWebDriver();
            WebElement page = webDriver.findElement(BODY_XPATH_LOCATOR);
            try
            {
                element.click();
                afterClick(clickResult, page, webDriver);
            }
            catch (WebDriverException webDriverException)
            {
                tryToWorkaroundException(element, clickResult, webDriver, page, webDriverException);
            }
        }
        return clickResult;
    }

    private void tryToWorkaroundException(WebElement element, ClickResult clickResult, WebDriver webDriver,
            WebElement page, WebDriverException webDriverException)
    {
        String message = webDriverException.getMessage();
        /*
         - Element is overlapped with another one: Other element would receive the click
         - Page has dynamically loaded content which results in invalid (sometimes negative) coordinates for click, e.g:
             Element is not clickable at point (498, -377)
             Element is not clickable at point (550, -11228)
        */
        if (message.contains("is not clickable at point"))
        {
            try
            {
                javascriptActions.scrollElementIntoViewportCenter(element);
                element.click();
                afterClick(clickResult, page, webDriver);
            }
            catch (WebDriverException e)
            {
                softAssert.recordFailedAssertion(COULD_NOT_CLICK_ERROR_MESSAGE + e);
            }
        }
        else if (message.contains("timeout: Timed out receiving message from renderer"))
        {
            softAssert.recordFailedAssertion(COULD_NOT_CLICK_ERROR_MESSAGE + webDriverException);
        }
        else if (message.contains("Timed out waiting for page to load"))
        {
            afterClick(clickResult, page, webDriver);
        }
        else
        {
            throw webDriverException;
        }
    }

    private void afterClick(ClickResult clickResult, WebElement page, WebDriver webDriver)
    {
        if (!webDriverManager.isElectronApp())
        {
            waitActions.waitForPageLoad();
            resetContextIfNeeded(clickResult, page);
            eventBus.post(new PageLoadEndEvent(clickResult.isNewPageLoaded(), webDriver));
        }
        clickResult.setClicked(true);
    }

    private void resetContextIfNeeded(ClickResult clickResult, WebElement page)
    {
        if (!alertActions.waitForAlert(getWebDriver()))
        {
            try
            {
                page.isDisplayed();
            }
            catch (@SuppressWarnings("unused") WebDriverException e)
            {
                clickResult.setNewPageLoaded(true);
                uiContext.reset();
            }
        }
    }

    @Override
    public void moveToElement(WebElement element)
    {
        if (element != null)
        {
            try
            {
                new Actions(getWebDriver()).scrollToElement(element).perform();
            }
            catch (MoveTargetOutOfBoundsException e)
            {
                // This is workaround for some Chrome/Chromium driver/browser bug:
                // https://github.com/w3c/webdriver/issues/1635#issuecomment-1196434722
                // https://stackoverflow.com/q/69975806/2067574
                javascriptActions.scrollElementIntoViewportCenter(element);
            }

            new Actions(getWebDriver()).moveToElement(element).perform();
        }
    }

    @Override
    public void contextClick(WebElement element)
    {
        if (element != null)
        {
            new Actions(getWebDriver()).contextClick(element).perform();
        }
    }

    private WebDriver getWebDriver()
    {
        return webDriverProvider.get();
    }
}
