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

package org.vividus.ui.web.listener;

import static com.github.valfirst.slf4jtest.LoggingEvent.debug;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.ui.context.IUiContext;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class})
class WebSourceCodePublishingOnFailureListenerTests
{
    private static final String INNER_HTML = "innerHTML";

    @Mock private IUiContext uiContext;
    @InjectMocks private WebSourceCodePublishingOnFailureListener listener;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(WebSourceCodePublishingOnFailureListener.class);

    @Test
    void shouldReturnWholePageForDriverContext()
    {
        WebDriver webDriver = mock(WebDriver.class);
        when(uiContext.getSearchContext()).thenReturn(webDriver);
        String pageSource = "<html/>";
        when(webDriver.getPageSource()).thenReturn(pageSource);
        assertEquals(Optional.of(pageSource), listener.getSourceCode());
    }

    @Test
    void shouldReturnWholePageForElementContext()
    {
        WebElement webElement = mock(WebElement.class);
        when(uiContext.getSearchContext()).thenReturn(webElement);
        String elementSource = "<div/>";
        when(webElement.getAttribute(INNER_HTML)).thenReturn(elementSource);
        assertEquals(Optional.of(elementSource), listener.getSourceCode());
    }

    @Test
    void shouldHandleStaleElementsCorrectly()
    {
        WebElement webElement = mock(WebElement.class);
        when(uiContext.getSearchContext()).thenReturn(webElement);
        when(webElement.getAttribute(INNER_HTML)).thenThrow(StaleElementReferenceException.class);
        assertEquals(Optional.empty(), listener.getSourceCode());
        assertEquals(logger.getLoggingEvents(), List.of(debug("Unable to get sources of the stale element")));
    }

    @Test
    void shouldReturnEmptyValueForNullSearchContext()
    {
        when(uiContext.getSearchContext()).thenReturn(null);
        assertEquals(Optional.empty(), listener.getSourceCode());
    }
}
