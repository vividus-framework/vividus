/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.ui.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.RunContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.screenshot.Screenshot;
import org.vividus.ui.screenshot.ScreenshotTaker;

@ExtendWith(MockitoExtension.class)
class PublishingScreenshotOnFailureMonitorTests
{
    @Mock private IAttachmentPublisher attachmentPublisher;
    @Mock private RunContext runContext;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private ScreenshotTaker screenshotTaker;
    @Mock private Screenshot screenshotMock;
    @InjectMocks private PublishingScreenshotOnFailureMonitor monitor;

    @Test
    void shouldTakeAssertionFailureScreenshot()
    {
        String screenshotName = "screenshot-name";
        when(screenshotTaker.takeScreenshot(screenshotName)).thenReturn(Optional.of(screenshotMock));

        Optional<Screenshot> screenshot = monitor.takeScreenshot(screenshotName);

        assertTrue(screenshot.isPresent());
        assertEquals(screenshotMock, screenshot.get());
        verifyNoMoreInteractions(attachmentPublisher, runContext, webDriverProvider, screenshotTaker, screenshotMock);
    }
}
