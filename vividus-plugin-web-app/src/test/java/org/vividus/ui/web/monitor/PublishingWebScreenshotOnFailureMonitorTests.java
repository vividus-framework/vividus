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

package org.vividus.ui.web.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.google.common.eventbus.EventBus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.context.RunContext;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.screenshot.WebScreenshotTaker;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.screenshot.Screenshot;

@ExtendWith(MockitoExtension.class)
class PublishingWebScreenshotOnFailureMonitorTests
{
    private static final String SCREENSHOT_NAME = "screenshot-name";

    @Mock private EventBus eventBus;
    @Mock private RunContext runContext;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IUiContext uiContext;
    @Mock private WebScreenshotTaker webScreenshotTaker;
    @Mock private Screenshot screenshotMock;
    @InjectMocks private PublishingWebScreenshotOnFailureMonitor monitor;

    @AfterEach
    void afterEach()
    {
        verifyNoMoreInteractions(eventBus, runContext, webDriverProvider, screenshotMock, webScreenshotTaker);
    }

    @Test
    void shouldTakeScreenshotOfWebElement()
    {
        WebElement webElement = mock(WebElement.class);

        when(uiContext.getSearchContext()).thenReturn(webElement);
        when(webScreenshotTaker.takeScreenshot(SCREENSHOT_NAME, List.of(webElement)))
                .thenReturn(Optional.of(screenshotMock));

        Optional<Screenshot> screenshot = monitor.takeScreenshot(SCREENSHOT_NAME);
        assertTrue(screenshot.isPresent());
        assertEquals(screenshotMock, screenshot.get());
    }

    @Test
    void shouldTakeScreenshotOfAssertingWebElement()
    {
        WebElement webElement = mock(WebElement.class);
        WebDriver webDriver = mock(WebDriver.class);

        when(uiContext.getSearchContext()).thenReturn(webDriver);
        when(uiContext.getAssertingWebElements()).thenReturn(List.of(webElement));
        when(webScreenshotTaker.takeScreenshot(SCREENSHOT_NAME, List.of(webElement)))
                .thenReturn(Optional.of(screenshotMock));

        Optional<Screenshot> screenshot = monitor.takeScreenshot(SCREENSHOT_NAME);
        assertTrue(screenshot.isPresent());
        assertEquals(screenshotMock, screenshot.get());
    }
}
