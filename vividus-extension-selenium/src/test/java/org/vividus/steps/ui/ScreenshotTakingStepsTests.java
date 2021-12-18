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

package org.vividus.steps.ui;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import com.google.common.eventbus.EventBus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.reporter.event.AttachmentPublishEvent;
import org.vividus.reporter.model.Attachment;
import org.vividus.selenium.screenshot.Screenshot;
import org.vividus.selenium.screenshot.ScreenshotTaker;

@ExtendWith(MockitoExtension.class)
class ScreenshotTakingStepsTests
{
    private static final String PATH = "path";

    @Mock private ScreenshotTaker screenshotTaker;
    @Mock private EventBus eventBus;
    @InjectMocks private ScreenshotTakingSteps screenshotTakingSteps;

    @Test
    void testTakeScreenshot()
    {
        String screenshotName = "Step_Screenshot";
        byte[] screenshotData = new byte[1];
        when(screenshotTaker.takeScreenshot(screenshotName))
                .thenReturn(Optional.of(new Screenshot(screenshotName, screenshotData)));
        var eventCaptor = ArgumentCaptor.forClass(AttachmentPublishEvent.class);
        screenshotTakingSteps.takeScreenshot();
        verify(screenshotTaker).takeScreenshot(screenshotName);
        verify(eventBus).post(eventCaptor.capture());
        AttachmentPublishEvent attachmentPublishEvent = eventCaptor.getValue();
        Attachment attachment = attachmentPublishEvent.getAttachment();
        assertEquals(screenshotName, attachment.getTitle());
        assertArrayEquals(screenshotData, attachment.getContent());
        assertEquals("image/png", attachment.getContentType());
    }

    @Test
    void testTakeScreenshotToPath() throws IOException
    {
        Path screenshotFilePath = Paths.get(PATH);
        screenshotTakingSteps.takeScreenshotToPath(screenshotFilePath);
        verify(screenshotTaker).takeScreenshot(screenshotFilePath);
    }
}
