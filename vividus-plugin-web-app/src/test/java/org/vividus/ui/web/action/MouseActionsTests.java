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

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.common.eventbus.EventBus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.interactions.Coordinates;
import org.openqa.selenium.interactions.HasInputDevices;
import org.openqa.selenium.interactions.Locatable;
import org.openqa.selenium.interactions.Mouse;
import org.openqa.selenium.interactions.MoveTargetOutOfBoundsException;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverType;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.action.AlertActions.Action;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.ui.web.event.PageLoadEndEvent;

@ExtendWith(MockitoExtension.class)
class MouseActionsTests
{
    private static final By BODY_XPATH_LOCATOR = By.xpath("//body");
    private static final String STALE_EXCEPTION = "StaleException";
    private static final String ELEMENT_IS_NOT_CLICKABLE_AT_POINT = "Element <element> is not clickable at point";
    private static final String OTHER_ELEMENT_WOULD_RECEIVE_CLICK = ELEMENT_IS_NOT_CLICKABLE_AT_POINT
            + " (75, 975). Other element would receive the click: <div..";
    private static final String COULD_NOT_CLICK_ERROR_MESSAGE = "Could not click on the element: ";
    private static final String COULD_NOT_MOVE_TO_ERROR_MESSAGE = "Could not move to the element because of an error: ";

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock(extraInterfaces = Locatable.class)
    private WebElement locatableWebElement;

    @Mock(extraInterfaces = { HasCapabilities.class, HasInputDevices.class })
    private WebDriver webDriver;

    @Mock
    private JavascriptActions javascriptActions;

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private Mouse mouse;

    @Mock
    private IWebDriverManager webDriverManager;

    @Mock
    private IWaitActions waitActions;

    @Mock
    private IAlertActions alertActions;

    @Mock
    private WebElement webElement;

    @Mock
    private EventBus eventBus;

    @Mock
    private IWebUiContext webUiContext;

    @InjectMocks
    private MouseActions mouseActions;

    private void verifyWebElement(MouseActions mouseActionsSpy, boolean alertPresent, boolean newPageLoaded,
            ClickResult result)
    {
        verify(webElement).click();
        assertTrue(result.isClicked());
        verify(mouseActionsSpy).moveToElement(webElement);
        assertEquals(newPageLoaded, result.isNewPageLoaded());
        if (!alertPresent)
        {
            verify(waitActions).waitForPageLoad();
            verify(eventBus)
                    .post(ArgumentMatchers.<PageLoadEndEvent>argThat(arg -> arg.isNewPageLoaded() == newPageLoaded));
        }
        else
        {
            verifyNoInteractions(eventBus);
        }
    }

    private MouseActions mockMoveToElement()
    {
        MouseActions mouseActionsSpy = spy(this.mouseActions);
        doNothing().when(mouseActionsSpy).moveToElement(webElement);
        return mouseActionsSpy;
    }

    private void testClick(boolean alertPresent, boolean newPageLoaded)
    {
        MouseActions mouseActionsSpy = mockMoveToElement();
        ClickResult result = mouseActionsSpy.click(webElement);
        verifyWebElement(mouseActionsSpy, alertPresent, newPageLoaded, result);
    }

    @Test
    void clickElementWithoutAlert()
    {
        WebElement body = mockBodySearch();
        doThrow(WebDriverException.class).when(body).isDisplayed();
        when(alertActions.waitForAlert(webDriver)).thenReturn(Boolean.FALSE);
        testClick(false, true);
        verify(webUiContext).reset();
    }

    @Test
    void clickElementWithNewAlert()
    {
        mockBodySearch();
        when(alertActions.waitForAlert(webDriver)).thenReturn(Boolean.TRUE);
        testClick(false, false);
        verifyNoInteractions(webUiContext);
    }

    @Test
    void clickElementWithAlert()
    {
        boolean alertPresent = true;
        mockBodySearch();
        when(alertActions.isAlertPresent()).thenReturn(alertPresent);
        testClick(alertPresent, false);
        verifyNoInteractions(webUiContext);
    }

    @Test
    void clickElementNull()
    {
        mouseActions.click((WebElement) null);
        verifyNoInteractions(webDriverProvider);
        verifyNoInteractions(webUiContext);
    }

    @Test
    void clickWrapsElement()
    {
        WrapsElement wrapsElement = mock(WrapsElement.class);
        WebElement webElement = mock(WebElement.class);
        when(wrapsElement.getWrappedElement()).thenReturn(webElement);
        MouseActions spy = spy(mouseActions);
        ClickResult expectedResult = new ClickResult();
        doReturn(expectedResult).when(spy).click(webElement);
        ClickResult actualResult = spy.click(wrapsElement);
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void clickWrapsElementNull()
    {
        mouseActions.click((WrapsElement) null);
        verifyNoInteractions(webDriverProvider);
        verifyNoInteractions(webUiContext);
    }

    @Test
    void clickElementExpectedException()
    {
        mockBodySearch();
        testClick(false, false);
        verify(alertActions).waitForAlert(webDriver);
        verifyNoInteractions(webUiContext);
    }

    @Test
    void clickElementNotClickableStaleReferenceExceptionNotChrome()
    {
        when(webDriverManager.isTypeAnyOf(WebDriverType.CHROME)).thenReturn(false);
        mockBodySearch();

        WebDriverException e = new WebDriverException(ELEMENT_IS_NOT_CLICKABLE_AT_POINT);
        WebDriverException e2 = new WebDriverException(STALE_EXCEPTION);
        doThrow(e).doThrow(e2).when(webElement).click();
        MouseActions mouseActionsSpy = mockMoveToElement();
        ClickResult result = mouseActionsSpy.click(webElement);
        verify(webElement, never()).sendKeys("");
        assertFalse(result.isNewPageLoaded());
        verify(softAssert).recordFailedAssertion(COULD_NOT_CLICK_ERROR_MESSAGE + e2);
        verify(alertActions, never()).waitForAlert(webDriver);
        verifyNoInteractions(webUiContext);
    }

    @Test
    void clickElementTimeoutExceptionNotChrome()
    {
        mockBodySearch();

        WebDriverException e = new WebDriverException(
                "TimeoutException : timeout: Timed out receiving message from renderer ");
        doThrow(e).doThrow(e).when(webElement).click();
        mouseActions.click(webElement);
        verify(webElement, never()).sendKeys("");
        verify(alertActions, never()).waitForAlert(webDriver);
        verifyNoInteractions(webUiContext);
    }

    @Test
    void clickElementTimeoutExceptionWaitForPageLoad()
    {
        mockBodySearch();
        WebDriverException e = new WebDriverException("Timed out waiting for page to load");
        doThrow(e).doThrow(e).when(webElement).click();
        assertTrue(mouseActions.click(webElement).isClicked());
        verifyNoInteractions(webUiContext);
    }

    @Test
    void clickElementGenericWebDriverException()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        String exceptionMessage = "Something goes wrong";
        doThrow(new WebDriverException(exceptionMessage)).when(webElement).click();
        WebDriverException exception = assertThrows(WebDriverException.class, () ->  mouseActions.click(webElement));
        assertThat(exception.getMessage(), containsString(exceptionMessage));
        verifyNoInteractions(webUiContext);
    }

    @Test
    void clickElementNotClickableExceptionNoExceptionNotChrome()
    {
        when(webDriverManager.isTypeAnyOf(WebDriverType.CHROME)).thenReturn(false);
        mockBodySearch();

        WebDriverException e = new WebDriverException(ELEMENT_IS_NOT_CLICKABLE_AT_POINT);
        doThrow(e).doNothing().when(webElement).click();
        when(alertActions.isAlertPresent()).thenReturn(Boolean.FALSE);
        MouseActions mouseActionsSpy = mockMoveToElement();
        ClickResult result = mouseActionsSpy.click(webElement);
        verify(webElement, never()).sendKeys("");
        assertFalse(result.isNewPageLoaded());
        verify(alertActions).waitForAlert(webDriver);
        verifyNoInteractions(webUiContext);
    }

    @Test
    void clickElementNotClickableExceptionStaleExceptionChrome()
    {
        when(webDriverManager.isTypeAnyOf(WebDriverType.CHROME)).thenReturn(true);
        mockBodySearch();

        WebDriverException e = new WebDriverException(ELEMENT_IS_NOT_CLICKABLE_AT_POINT);
        WebDriverException e2 = new WebDriverException(STALE_EXCEPTION);
        doThrow(e).doThrow(e2).when(webElement).click();
        MouseActions mouseActionsSpy = mockMoveToElement();
        mouseActionsSpy.click(webElement);
        verify(softAssert).recordFailedAssertion(COULD_NOT_CLICK_ERROR_MESSAGE + e2);
        verify(alertActions, never()).waitForAlert(webDriver);
        verifyNoInteractions(webUiContext);
    }

    @Test
    void clickElementNotClickableExceptionNoExceptionChrome()
    {
        when(webDriverManager.isTypeAnyOf(WebDriverType.CHROME)).thenReturn(true);
        mockBodySearch();

        WebDriverException e = new WebDriverException(ELEMENT_IS_NOT_CLICKABLE_AT_POINT);
        doThrow(e).doNothing().when(webElement).click();
        MouseActions mouseActionsSpy = mockMoveToElement();
        ClickResult result = mouseActionsSpy.click(webElement);
        assertFalse(result.isNewPageLoaded());
        verify(alertActions).waitForAlert(webDriver);
        verify(eventBus).post(any(PageLoadEndEvent.class));
    }

    @Test
    void clickElementNotClickableExceptionWithJsChrome()
    {
        when(webDriverManager.isTypeAnyOf(WebDriverType.CHROME)).thenReturn(true);
        mockBodySearch();

        WebDriverException e = new WebDriverException(OTHER_ELEMENT_WOULD_RECEIVE_CLICK);
        doThrow(e).when(webElement).click();
        MouseActions mouseActionsSpy = mockMoveToElement();
        ClickResult result = mouseActionsSpy.click(webElement);
        verify(javascriptActions).click(webElement);
        assertFalse(result.isNewPageLoaded());
        verify(alertActions).waitForAlert(webDriver);
        verify(eventBus).post(any(PageLoadEndEvent.class));
        verifyNoInteractions(webUiContext);
    }

    @Test
    void clickElementNotClickableExceptionWithoutJsNotChrome()
    {
        when(webDriverManager.isTypeAnyOf(WebDriverType.CHROME)).thenReturn(false);
        mockBodySearch();

        WebDriverException e = new WebDriverException(OTHER_ELEMENT_WOULD_RECEIVE_CLICK);
        doThrow(e).doNothing().when(webElement).click();
        MouseActions mouseActionsSpy = mockMoveToElement();
        ClickResult result = mouseActionsSpy.click(webElement);
        verifyNoInteractions(javascriptActions);
        assertFalse(result.isNewPageLoaded());
        verify(alertActions).waitForAlert(webDriver);
        verify(eventBus).post(any(PageLoadEndEvent.class));
        verifyNoInteractions(webUiContext);
    }

    @Test
    void clickElementNotClickableExceptionAndWebDriverExceptionInChromeWorkaround()
    {
        when(webDriverManager.isTypeAnyOf(WebDriverType.CHROME)).thenReturn(true);
        mockBodySearch();

        WebDriverException e = new WebDriverException(ELEMENT_IS_NOT_CLICKABLE_AT_POINT);
        doThrow(e).doNothing().when(webElement).click();
        MouseActions mouseActionsSpy = mockMoveToElement();
        ClickResult result = mouseActionsSpy.click(webElement);
        assertFalse(result.isNewPageLoaded());
        verify(alertActions).waitForAlert(webDriver);
        verify(eventBus).post(any(PageLoadEndEvent.class));
        verifyNoInteractions(webUiContext);
    }

    @Test
    void testClickViaJavascript()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        WebElement body = mock(WebElement.class);
        WebDriverEventListener webDriverEventListener = mock(WebDriverEventListener.class);
        List<WebDriverEventListener> webDriverEventListeners = new ArrayList<>();
        webDriverEventListeners.add(webDriverEventListener);
        mouseActions.setWebDriverEventListeners(webDriverEventListeners);
        when(webDriver.findElement(BODY_XPATH_LOCATOR)).thenReturn(body);
        ClickResult result = mouseActions.clickViaJavascript(webElement);
        verify(webDriverEventListener).beforeClickOn(webElement, webDriver);
        verify(webDriverEventListener).afterClickOn(webElement, webDriver);
        assertFalse(result.isNewPageLoaded());
        verify(alertActions).waitForAlert(webDriver);
        verify(eventBus).post(any(PageLoadEndEvent.class));
        verifyNoInteractions(webUiContext);
    }

    @Test
    void testClickViaJavascriptNullElement()
    {
        ClickResult result = mouseActions.clickViaJavascript(null);
        assertFalse(result.isClicked());
        verify(alertActions, never()).waitForAlert(webDriver);
        verify(eventBus, never()).post(any(PageLoadEndEvent.class));
        verifyNoInteractions(webUiContext);
    }

    @Test
    void clickElementWithAcceptAlert()
    {
        boolean alertPresent = true;
        mockBodySearch();
        when(alertActions.isAlertPresent()).thenReturn(alertPresent);
        MouseActions mouseActionsSpy = mockMoveToElement();
        ClickResult result = mouseActionsSpy.click(webElement, Optional.of(Action.ACCEPT));
        verifyWebElement(mouseActionsSpy, alertPresent, false, result);
        verifyNoInteractions(webUiContext);
    }

    private WebElement mockBodySearch()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        WebElement body = mock(WebElement.class);
        when(webDriver.findElement(BODY_XPATH_LOCATOR)).thenReturn(body);
        return body;
    }

    @Test
    void clickInvisibleElementIsNull()
    {
        mouseActions.moveToAndClick(null);
        verifyNoInteractions(mouse);
    }

    @Test
    void clickInvisibleElement()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(((HasInputDevices) webDriver).getMouse()).thenReturn(mouse);
        Coordinates coordinates = mock(Coordinates.class);
        when(((Locatable) locatableWebElement).getCoordinates()).thenReturn(coordinates);
        mouseActions.moveToAndClick(locatableWebElement);
        verify(mouse).mouseMove(coordinates);
        verify(mouse).click(null);
    }

    @Test
    void testContextClick()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(((HasInputDevices) webDriver).getMouse()).thenReturn(mouse);
        Coordinates coordinates = mock(Coordinates.class);
        when(((Locatable) locatableWebElement).getCoordinates()).thenReturn(coordinates);
        mouseActions.contextClick(locatableWebElement);
        verify(mouse).contextClick(coordinates);
    }

    @Test
    void testContextClickWebElementIsNull()
    {
        mouseActions.contextClick(null);
        verifyNoInteractions(mouse);
    }

    @Test
    void testMoveToElement()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(((HasInputDevices) webDriver).getMouse()).thenReturn(mouse);
        Coordinates coordinates = mock(Coordinates.class);
        when(((Locatable) locatableWebElement).getCoordinates()).thenReturn(coordinates);
        mouseActions.moveToElement(locatableWebElement);
        verify(mouse).mouseMove(coordinates);
    }

    @Test
    void testMoveToNullElement()
    {
        mouseActions.moveToElement(null);
        verifyNoInteractions(webDriverProvider);
        verifyNoInteractions(javascriptActions);
        verifyNoInteractions(softAssert);
    }

    @Test
    void testMoveToElementMoveTargetOutOfBoundsException()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(((HasInputDevices) webDriver).getMouse()).thenReturn(mouse);
        MoveTargetOutOfBoundsException boundsException = new MoveTargetOutOfBoundsException(
                COULD_NOT_MOVE_TO_ERROR_MESSAGE);
        Coordinates coordinates = mock(Coordinates.class);
        when(((Locatable) locatableWebElement).getCoordinates()).thenReturn(coordinates);
        doThrow(boundsException).when(mouse).mouseMove(coordinates);
        mouseActions.moveToElement(locatableWebElement);
        verify(softAssert).recordFailedAssertion(COULD_NOT_MOVE_TO_ERROR_MESSAGE + boundsException);
    }

    @Test
    void testMoveToElementOnMobile()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(((HasInputDevices) webDriver).getMouse()).thenReturn(mouse);
        when(webDriverManager.isMobile()).thenReturn(true);
        mouseActions.moveToElement(locatableWebElement);
        verify(javascriptActions).scrollIntoView(locatableWebElement, true);
    }

    @Test
    void testMoveToElementOnSafari()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(((HasInputDevices) webDriver).getMouse()).thenReturn(mouse);
        when(webDriverManager.isMobile()).thenReturn(false);
        when(webDriverManager.isTypeAnyOf(WebDriverType.SAFARI)).thenReturn(true);
        mouseActions.moveToElement(locatableWebElement);
        verify(javascriptActions).scrollIntoView(locatableWebElement, true);
    }
}
