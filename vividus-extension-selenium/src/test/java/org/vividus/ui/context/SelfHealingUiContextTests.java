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

package org.vividus.ui.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.WrapsElement;
import org.openqa.selenium.remote.RemoteWebElement;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class SelfHealingUiContextTests extends UiContextTestsBase
{
    public static final String MESSAGE = "Message";
    public static final String RETRY_MESSAGE = "Got an exception trying to reset context";

    public static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(SelfHealingUiContext.class);

    @Mock private SearchContextSetter searchContextSetter;

    @InjectMocks
    private SelfHealingUiContext uiContext;

    @BeforeEach
    void beforeEach()
    {
        uiContext.setTestContext(getContext());
    }

    @Test
    void shouldReturnValueIfContextNotStale()
    {
        var webElement = mock(WebElement.class);
        uiContext.putSearchContext(webElement, searchContextSetter);
        doNothing().when(webElement).click();
        uiContext.getSearchContext(WebElement.class).click();
        verify(webElement).click();
        verifyNoInteractions(searchContextSetter);
        assertEquals(LOGGER.getLoggingEvents(), List.of());
    }

    @Test
    void shouldNotWrapWebDriver()
    {
        var webDriver = mock(WebDriver.class);
        uiContext.putSearchContext(webDriver, searchContextSetter);
        when(webDriver.getTitle()).thenThrow(new StaleElementReferenceException(MESSAGE));
        var searchContext = (WebDriver) uiContext.getSearchContext();
        assertThrows(StaleElementReferenceException.class, searchContext::getTitle);
    }

    @Test
    void shouldTryResetContext()
    {
        var webElement = mock(WebElement.class);
        uiContext.putSearchContext(webElement, searchContextSetter);
        var exception = new StaleElementReferenceException(MESSAGE);
        doThrow(exception).doNothing().when(webElement).click();
        ((WebElement) uiContext.getSearchContext()).click();
        verify(webElement, times(2)).click();
        verify(searchContextSetter).setSearchContext();
        assertEquals(LOGGER.getLoggingEvents(), List.of(LoggingEvent.info(exception, RETRY_MESSAGE)));
    }

    @Test
    void shouldReturnUnwrappedObject()
    {
        var webElement = mock(WebElement.class);
        uiContext.putSearchContext(webElement, searchContextSetter);
        var searchContext = uiContext.getSearchContext();
        assertNotSame(webElement, searchContext);
        assertSame(webElement, ((WrapsElement) searchContext).getWrappedElement());
    }

    @Test
    void shouldRethrowAnExceptionIfResetDidntHelp()
    {
        var webElement = mock(WebElement.class);
        uiContext.putSearchContext(webElement, searchContextSetter);
        var exception = new StaleElementReferenceException(MESSAGE);
        doThrow(exception).when(webElement).click();
        var searchContext = (WebElement) uiContext.getSearchContext();
        assertThrows(StaleElementReferenceException.class, searchContext::click);
        verify(webElement, times(2)).click();
        verify(searchContextSetter).setSearchContext();
        assertEquals(LOGGER.getLoggingEvents(), List.of(LoggingEvent.info(exception, RETRY_MESSAGE)));
    }

    @Test
    void shouldNotRetryInvalidExceptions()
    {
        var webElement = mock(WebElement.class);
        uiContext.putSearchContext(webElement, searchContextSetter);
        var exception = new IllegalArgumentException(MESSAGE);
        doThrow(exception).when(webElement).click();
        var searchContext = (WebElement) uiContext.getSearchContext();
        assertThrows(IllegalArgumentException.class, searchContext::click);
        verify(webElement).click();
        verifyNoInteractions(searchContextSetter);
        assertEquals(LOGGER.getLoggingEvents(), List.of());
    }

    @Test
    void shouldTryResetContextForCustomInterface()
    {
        var webElement = mock(CustomWebElement.class);
        uiContext.putSearchContext(webElement, searchContextSetter);
        var exception = new StaleElementReferenceException(MESSAGE);
        doThrow(exception).doNothing().when(webElement).doSomething();
        (uiContext.getSearchContext(CustomWebElement.class)).doSomething();
        verify(webElement, times(2)).doSomething();
        verify(searchContextSetter).setSearchContext();
        assertEquals(LOGGER.getLoggingEvents(), List.of(LoggingEvent.info(exception, RETRY_MESSAGE)));
    }

    @Test
    void shouldTryResetContextReceivedByClass()
    {
        var webElement = mock(WebElement.class);
        uiContext.putSearchContext(webElement, searchContextSetter);
        var exception = new StaleElementReferenceException(MESSAGE);
        doThrow(exception).doNothing().when(webElement).click();
        uiContext.getSearchContext(WebElement.class).click();
        verify(webElement, times(2)).click();
        verify(searchContextSetter).setSearchContext();
        assertEquals(LOGGER.getLoggingEvents(), List.of(LoggingEvent.info(exception, RETRY_MESSAGE)));
    }

    @Test
    void shouldNotWrapClasses()
    {
        var webElement = mock(RemoteWebElement.class);
        uiContext.putSearchContext(webElement, searchContextSetter);
        doThrow(new StaleElementReferenceException(MESSAGE)).doNothing().when(webElement).click();
        var searchContext = uiContext.getSearchContext(RemoteWebElement.class);
        assertThrows(StaleElementReferenceException.class, searchContext::click);
        assertEquals(LOGGER.getLoggingEvents(), List.of());
    }

    private interface CustomWebElement extends WebElement
    {
        void doSomething();
    }
}
