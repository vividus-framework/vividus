/*
 * Copyright 2019 the original author or authors.
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.inject.Inject;

import com.google.common.eventbus.EventBus;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverType;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.action.AlertActions.Action;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.ui.web.event.PageLoadEndEvent;

public class MouseActions implements IMouseActions
{
    private static final String COULD_NOT_MOVE_ERROR_MESSAGE = "Could not move to the element because of an error: ";
    private static final String COULD_NOT_CLICK_ERROR_MESSAGE = "Could not click on the element: ";
    private static final By BODY_XPATH_LOCATOR = By.xpath("//body");

    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IWebDriverManager webDriverManager;
    @Inject private ISoftAssert softAssert;
    @Inject private JavascriptActions javascriptActions;
    @Inject private IWaitActions waitActions;
    @Inject private IAlertActions alertActions;
    @Inject private EventBus eventBus;
    @Inject private IWebUiContext webUiContext;
    private List<WebDriverEventListener> webDriverEventListeners;

    @Override
    public ClickResult click(WebElement element)
    {
        return click(element, Optional.empty());
    }

    @Override
    public ClickResult click(WebElement element, Optional<Action> defaultAlertAction)
    {
        ClickResult clickResult = new ClickResult();
        if (element != null)
        {
            WebElement page = getWebDriver().findElement(BODY_XPATH_LOCATOR);
            try
            {
                moveToElement(element);
                element.click();
                afterClick(clickResult, page, defaultAlertAction);
            }
            catch (WebDriverException webDriverException)
            {
                String message = webDriverException.getMessage();
                if (message.contains("is not clickable at point"))
                {
                    try
                    {
                        if (webDriverManager.isTypeAnyOf(WebDriverType.CHROME)
                                && message.contains(". Other element would receive the click"))
                        {
                            javascriptActions.click(element);
                        }
                        else
                        {
                            element.click();
                        }
                        afterClick(clickResult, page, defaultAlertAction);
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
                    afterClick(clickResult, page, defaultAlertAction);
                }
                else
                {
                    throw webDriverException;
                }
            }
        }
        return clickResult;
    }

    @Override
    public ClickResult click(WrapsElement element)
    {
        if (element != null)
        {
            return click(element.getWrappedElement());
        }
        return new ClickResult();
    }

    @Override
    public void moveToAndClick(WebElement element)
    {
        if (element != null)
        {
            new Actions(getWebDriver()).moveToElement(element).click().perform();
        }
    }

    @Override
    public ClickResult clickViaJavascript(WebElement element)
    {
        ClickResult clickResult = new ClickResult();
        if (element != null)
        {
            WebDriver webDriver = getWebDriver();
            WebElement page = webDriver.findElement(BODY_XPATH_LOCATOR);
            webDriverEventListeners.forEach(listener -> listener.beforeClickOn(element, webDriver));
            javascriptActions.click(element);
            webDriverEventListeners.forEach(listener -> listener.afterClickOn(element, webDriver));
            afterClick(clickResult, page, Optional.empty());
        }
        return clickResult;
    }

    private void afterClick(ClickResult clickResult, WebElement page, Optional<Action> defaultAlertAction)
    {
        defaultAlertAction.ifPresent(alertActions::processAlert);
        if (!alertActions.isAlertPresent())
        {
            waitActions.waitForPageLoad();
            resetContextIfNeeded(clickResult, page);
            eventBus.post(new PageLoadEndEvent(clickResult.isNewPageLoaded()));
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
                webUiContext.reset();
            }
        }
    }

    @Override
    public void moveToElement(WebElement element)
    {
        if (element != null)
        {
            //Safari Driver doesn't scroll to element before moveTo action
            if (webDriverManager.isMobile() || webDriverManager.isTypeAnyOf(WebDriverType.SAFARI))
            {
                javascriptActions.scrollIntoView(element, true);
            }
            try
            {
                new Actions(getWebDriver()).moveToElement(element).perform();
            }
            catch (MoveTargetOutOfBoundsException ex)
            {
                softAssert.recordFailedAssertion(COULD_NOT_MOVE_ERROR_MESSAGE + ex);
            }
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

    public void setWebDriverEventListeners(List<WebDriverEventListener> webDriverEventListeners)
    {
        this.webDriverEventListeners = Collections.unmodifiableList(webDriverEventListeners);
    }
}
