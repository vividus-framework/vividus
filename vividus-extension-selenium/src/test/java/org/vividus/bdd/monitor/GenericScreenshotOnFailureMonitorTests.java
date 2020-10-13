/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.bdd.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import com.google.common.eventbus.EventBus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddRunContext;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.screenshot.Screenshot;
import org.vividus.selenium.screenshot.ScreenshotTaker;

@ExtendWith(MockitoExtension.class)
class GenericScreenshotOnFailureMonitorTests
{
    @Mock private EventBus eventBus;
    @Mock private IBddRunContext bddRunContext;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private ScreenshotTaker screenshotTaker;
    @Mock private Screenshot screenshotMock;
    @InjectMocks private GenericScreenshotOnFailureMonitor monitor;

    @Test
    void shouldTakeAssertionFailureScreenshot()
    {
        String screenshotName = "screenshot-name";
        when(screenshotTaker.takeScreenshot(screenshotName)).thenReturn(Optional.of(screenshotMock));

        Optional<Screenshot> screenshot = monitor.takeAssertionFailureScreenshot(screenshotName);

        assertTrue(screenshot.isPresent());
        assertEquals(screenshotMock, screenshot.get());
        verifyNoMoreInteractions(eventBus, bddRunContext, webDriverProvider, screenshotTaker, screenshotMock);
    }
}
