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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.Sequence;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.context.IUiContext;
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

    private static final String ID = "id";
    private static final String TYPE = "type";
    private static final String ACTIONS = "actions";
    private static final String DURATION = "duration";
    private static final String X = "x";
    private static final String Y = "y";
    private static final String ORIGIN = "origin";
    private static final String BUTTON = "button";

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
        InOrder ordered = inOrder(alertActions, waitActions, eventBus, uiContext);
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
    void clickElementExpectedException()
    {
        mockBodySearch();
        testClick(false, false);
        verify(alertActions).waitForAlert(webDriver);
        verifyNoInteractions(uiContext);
    }

    @Test
    void clickElementNotClickableStaleReferenceException()
    {
        mockBodySearch();

        WebDriverException e = new WebDriverException(OTHER_ELEMENT_WOULD_RECEIVE_CLICK);
        WebDriverException e2 = new WebDriverException(STALE_EXCEPTION);
        doThrow(e).doThrow(e2).when(webElement).click();
        ClickResult result = mouseActions.click(webElement);
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
        WebDriverException exception = assertThrows(WebDriverException.class, () -> mouseActions.click(webElement));
        assertThat(exception.getMessage(), containsString(exceptionMessage));
        verifyNoInteractions(uiContext);
    }

    @Test
    void clickElementNotClickableExceptionNoExceptionChrome()
    {
        mockBodySearch();

        WebDriverException e = new WebDriverException(OTHER_ELEMENT_WOULD_RECEIVE_CLICK);
        doThrow(e).doNothing().when(webElement).click();
        testClickWithElementNotClickableException();
    }

    @Test
    void shouldNotRetryClickOnMatchingError()
    {
        mockBodySearch();

        WebDriverException e = new WebDriverException(OTHER_ELEMENT_WOULD_RECEIVE_CLICK);
        doThrow(e).doNothing().when(webElement).click();
        when(alertActions.isAlertPresent(webDriver)).thenReturn(Boolean.FALSE);
        testClickWithElementNotClickableException();
    }

    @Test
    void shouldNotRetryClickOnNonMatchingError()
    {
        mockBodySearch();

        WebDriverException e = new WebDriverException(ELEMENT_IS_NOT_CLICKABLE_AT_POINT);
        doThrow(e).doNothing().when(webElement).click();
        WebDriverException exception = assertThrows(WebDriverException.class, () -> mouseActions.click(webElement));
        assertThat(exception.getMessage(), containsString(ELEMENT_IS_NOT_CLICKABLE_AT_POINT));
    }

    private WebElement mockBodySearch()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        WebElement body = mock(WebElement.class);
        when(webDriver.findElement(BODY_XPATH_LOCATOR)).thenReturn(body);
        return body;
    }

    @Test
    void shouldPerformContextClick()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        mouseActions.contextClick(webElement);
        verify((Interactive) webDriver).perform(sequencesCaptor.capture());
        Collection<Sequence> sequences = sequencesCaptor.getValue();
        assertEquals(1, sequences.size());
        assertEquals(mouseSequence(List.of(
                pointerMoveAction(),
                Map.of(BUTTON, 2, TYPE, "pointerDown"),
                Map.of(BUTTON, 2, TYPE, "pointerUp"))
            ), sequences.iterator().next().toJson()
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
        verify((Interactive) webDriver, times(2)).perform(sequencesCaptor.capture());
        List<Collection<Sequence>> sequences = sequencesCaptor.getAllValues();
        assertEquals(2, sequences.size());
        assertEquals(Map.of(
                ID, "default wheel",
                TYPE, "wheel",
                ACTIONS, List.of(
                        Map.of(
                                DURATION, 250L,
                                X, 0,
                                Y, 0,
                                "deltaX", 0,
                                "deltaY", 0,
                                TYPE, "scroll",
                                ORIGIN, webElement
                        )
                )), sequences.get(0).iterator().next().toJson());
        assertEquals(mouseSequence(List.of(pointerMoveAction())), sequences.get(1).iterator().next().toJson());
    }

    private static Map<String, Object> mouseSequence(List<Map<String, ?>> mouseActions)
    {
        return Map.of(
                ID, "default mouse",
                TYPE, "pointer",
                "parameters", Map.of("pointerType", "mouse"), ACTIONS, mouseActions
        );
    }

    private Map<String, Object> pointerMoveAction()
    {
        return Map.of(
                DURATION, 100L,
                X, 0,
                Y, 0,
                TYPE, "pointerMove",
                ORIGIN, webElement
        );
    }

    @Test
    void shouldNotPerformActionsAtAttemptToMoveToNullElement()
    {
        mouseActions.moveToElement(null);
        verifyNoInteractions(webDriverProvider, softAssert);
    }
}
