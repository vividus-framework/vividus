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

package org.vividus.ui.web.action;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.eventbus.EventBus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.events.WebDriverEventListener;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.web.action.AlertActions.Action;
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

    private static final String BUTTON = "button";
    private static final String TYPE = "type";

    @Mock(extraInterfaces = { HasCapabilities.class, Interactive.class })
    private WebDriver webDriver;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private WebJavascriptActions javascriptActions;
    @Mock private ISoftAssert softAssert;
    @Mock private IWebDriverManager webDriverManager;
    @Mock private IWebWaitActions waitActions;
    @Mock private IAlertActions alertActions;
    @Mock private WebElement webElement;
    @Mock private EventBus eventBus;
    @Mock private IUiContext uiContext;
    @InjectMocks private MouseActions mouseActions;
    @Captor private ArgumentCaptor<Collection<Sequence>> sequencesCaptor;

    private void verifyWebElement(int clickAttempts, boolean alertPresent, boolean newPageLoaded, ClickResult result)
    {
        verify(webElement, times(clickAttempts)).click();
        assertTrue(result.isClicked());
        assertEquals(newPageLoaded, result.isNewPageLoaded());
        InOrder ordered = inOrder(javascriptActions, alertActions, waitActions, eventBus, uiContext);
        ordered.verify(javascriptActions).scrollElementIntoViewportCenter(webElement);
        ordered.verify(alertActions).isAlertPresent(webDriver);
        if (!alertPresent)
        {
            ordered.verify(waitActions).waitForPageLoad();
            ordered.verify(alertActions).waitForAlert(webDriver);
            ordered.verify(eventBus)
                    .post(ArgumentMatchers.<PageLoadEndEvent>argThat(arg -> arg.isNewPageLoaded() == newPageLoaded));
        }
        ordered.verifyNoMoreInteractions();
    }

    private void testClick(boolean alertPresent, boolean newPageLoaded)
    {
        ClickResult result = mouseActions.click(webElement);
        verifyWebElement(1, alertPresent, newPageLoaded, result);
    }

    private void testClickWithElementNotClickableException()
    {
        ClickResult result = mouseActions.click(webElement);
        verifyWebElement(2, false, false, result);
    }

    @Test
    void clickElementWithoutAlert()
    {
        WebElement body = mockBodySearch();
        doThrow(WebDriverException.class).when(body).isDisplayed();
        when(alertActions.waitForAlert(webDriver)).thenReturn(Boolean.FALSE);
        testClick(false, true);
        verify(uiContext).reset();
    }

    @Test
    void clickElementWithNewAlert()
    {
        mockBodySearch();
        when(alertActions.waitForAlert(webDriver)).thenReturn(Boolean.TRUE);
        testClick(false, false);
        verifyNoInteractions(uiContext);
    }

    @Test
    void clickElementWithAlert()
    {
        boolean alertPresent = true;
        mockBodySearch();
        when(alertActions.isAlertPresent(webDriver)).thenReturn(alertPresent);
        testClick(alertPresent, false);
        verifyNoInteractions(uiContext);
    }

    @Test
    void clickElementNull()
    {
        mouseActions.click((WebElement) null);
        verifyNoInteractions(webDriverProvider);
        verifyNoInteractions(uiContext);
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
        verifyNoInteractions(uiContext);
    }

    @Test
    void clickElementExpectedException()
    {
        mockBodySearch();
        testClick(false, false);
        verify(alertActions).waitForAlert(webDriver);
        verifyNoInteractions(uiContext);
    }

    @Test
    void clickElementNotClickableStaleReferenceExceptionNotChrome()
    {
        mockBodySearch();

        WebDriverException e = new WebDriverException(ELEMENT_IS_NOT_CLICKABLE_AT_POINT);
        WebDriverException e2 = new WebDriverException(STALE_EXCEPTION);
        doThrow(e).doThrow(e2).when(webElement).click();
        ClickResult result = mouseActions.click(webElement);
        verify(webElement, never()).sendKeys("");
        assertFalse(result.isNewPageLoaded());
        InOrder ordered = inOrder(javascriptActions, alertActions, eventBus, uiContext, softAssert);
        ordered.verify(javascriptActions).scrollElementIntoViewportCenter(webElement);
        ordered.verify(softAssert).recordFailedAssertion(COULD_NOT_CLICK_ERROR_MESSAGE + e2);
        ordered.verifyNoMoreInteractions();
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
        verifyNoInteractions(uiContext);
    }

    @Test
    void clickElementTimeoutExceptionWaitForPageLoad()
    {
        mockBodySearch();
        WebDriverException e = new WebDriverException("Timed out waiting for page to load");
        doThrow(e).doThrow(e).when(webElement).click();
        assertTrue(mouseActions.click(webElement).isClicked());
        verifyNoInteractions(uiContext);
    }

    @Test
    void clickElementGenericWebDriverException()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        String exceptionMessage = "Something goes wrong";
        doThrow(new WebDriverException(exceptionMessage)).when(webElement).click();
        WebDriverException exception = assertThrows(WebDriverException.class, () ->  mouseActions.click(webElement));
        assertThat(exception.getMessage(), containsString(exceptionMessage));
        verifyNoInteractions(uiContext);
    }

    @Test
    void clickElementNotClickableExceptionNoExceptionNotChrome()
    {
        mockBodySearch();

        WebDriverException e = new WebDriverException(ELEMENT_IS_NOT_CLICKABLE_AT_POINT);
        doThrow(e).doNothing().when(webElement).click();
        when(alertActions.isAlertPresent(webDriver)).thenReturn(Boolean.FALSE);
        testClickWithElementNotClickableException();
        verify(webElement, never()).sendKeys("");
    }

    @Test
    void clickElementNotClickableExceptionStaleExceptionChrome()
    {
        mockBodySearch();

        WebDriverException e = new WebDriverException(ELEMENT_IS_NOT_CLICKABLE_AT_POINT);
        WebDriverException e2 = new WebDriverException(STALE_EXCEPTION);
        doThrow(e).doThrow(e2).when(webElement).click();
        mouseActions.click(webElement);
        InOrder ordered = inOrder(javascriptActions, alertActions, eventBus, uiContext, softAssert);
        ordered.verify(javascriptActions).scrollElementIntoViewportCenter(webElement);
        ordered.verify(softAssert).recordFailedAssertion(COULD_NOT_CLICK_ERROR_MESSAGE + e2);
        ordered.verifyNoMoreInteractions();
    }

    @Test
    void clickElementNotClickableExceptionNoExceptionChrome()
    {
        mockBodySearch();

        WebDriverException e = new WebDriverException(ELEMENT_IS_NOT_CLICKABLE_AT_POINT);
        doThrow(e).doNothing().when(webElement).click();
        testClickWithElementNotClickableException();
    }

    @Test
    void shouldRecordFailedAssertionWhenClickOverlappedElement()
    {
        mockBodySearch();

        var exception = new WebDriverException(OTHER_ELEMENT_WOULD_RECEIVE_CLICK);
        doThrow(exception).when(webElement).click();
        testClick(false, false);
        verify(softAssert).recordFailedAssertion(COULD_NOT_CLICK_ERROR_MESSAGE + exception);
    }

    @Test
    void clickElementNotClickableExceptionAndWebDriverExceptionInChromeWorkaround()
    {
        mockBodySearch();

        WebDriverException e = new WebDriverException(ELEMENT_IS_NOT_CLICKABLE_AT_POINT);
        doThrow(e).doNothing().when(webElement).click();
        testClickWithElementNotClickableException();
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
        verifyNoInteractions(uiContext);
    }

    @Test
    void shouldNotProcessAlertsAfterClickForElectronApps()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        WebElement body = mock(WebElement.class);
        WebDriverEventListener webDriverEventListener = mock(WebDriverEventListener.class);
        mouseActions.setWebDriverEventListeners(List.of(webDriverEventListener));
        when(webDriver.findElement(BODY_XPATH_LOCATOR)).thenReturn(body);
        when(webDriverManager.isElectronApp()).thenReturn(true);
        ClickResult result = mouseActions.clickViaJavascript(webElement);
        verify(webDriverEventListener).beforeClickOn(webElement, webDriver);
        verify(webDriverEventListener).afterClickOn(webElement, webDriver);
        assertFalse(result.isNewPageLoaded());
        verifyNoInteractions(uiContext, alertActions, eventBus);
    }

    @Test
    void testClickViaJavascriptNullElement()
    {
        ClickResult result = mouseActions.clickViaJavascript(null);
        assertFalse(result.isClicked());
        verify(alertActions, never()).waitForAlert(webDriver);
        verify(eventBus, never()).post(any(PageLoadEndEvent.class));
        verifyNoInteractions(uiContext);
    }

    @Test
    void clickElementWithAcceptAlert()
    {
        boolean alertPresent = true;
        mockBodySearch();
        when(alertActions.isAlertPresent(webDriver)).thenReturn(alertPresent);
        ClickResult result = mouseActions.click(webElement, Optional.of(Action.ACCEPT));
        verifyWebElement(1, alertPresent, false, result);
    }

    private WebElement mockBodySearch()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        WebElement body = mock(WebElement.class);
        when(webDriver.findElement(BODY_XPATH_LOCATOR)).thenReturn(body);
        return body;
    }

    @Test
    void shouldNotPerformActionsAtAttemptToClickNullElement()
    {
        mouseActions.moveToAndClick(null);
        verifyNoInteractions(webDriver);
    }

    @Test
    void clickInvisibleElement()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        mouseActions.moveToAndClick(webElement);
        verifyActionsWithMove(verify((Interactive) webDriver), createClickActions(0));
    }

    @Test
    void shouldPerformContextClick()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        mouseActions.contextClick(webElement);
        verifyActionsWithMove(verify((Interactive) webDriver), createClickActions(2));
    }

    private List<Map<String, Object>> createClickActions(int button)
    {
        return List.of(
                Map.of(BUTTON, button, TYPE, "pointerDown"),
                Map.of(BUTTON, button, TYPE, "pointerUp")
        );
    }

    @Test
    void testContextClickWebElementIsNull()
    {
        mouseActions.contextClick(null);
        verifyNoInteractions(webDriver);
    }

    @Test
    void shouldMoveToElement()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        mouseActions.moveToElement(webElement);
        InOrder ordered = inOrder(javascriptActions, webDriver);
        ordered.verify(javascriptActions).scrollIntoView(webElement, true);
        verifyActionsWithMove(ordered.verify((Interactive) webDriver), List.of());
    }

    @Test
    void shouldNotPerformActionsAtAttemptToMoveToNullElement()
    {
        mouseActions.moveToElement(null);
        verifyNoInteractions(webDriverProvider, javascriptActions, softAssert);
    }

    private void verifyActionsWithMove(Interactive interactiveVerification, List<Map<String, Object>> actions)
    {
        interactiveVerification.perform(sequencesCaptor.capture());
        Collection<Sequence> sequences = sequencesCaptor.getValue();
        assertEquals(1, sequences.size());
        List<Map<String, Object>> allActions = new ArrayList<>();
        allActions.add(Map.of(
                "duration", 100L,
                "x", 0,
                "y", 0,
                TYPE, "pointerMove",
                "origin", webElement));
        allActions.addAll(actions);
        assertEquals(Map.of(
                "id", "default mouse",
                TYPE, "pointer",
                "parameters", Map.of("pointerType", "mouse"),
                "actions", allActions
            ), sequences.iterator().next().toJson()
        );
    }
}
