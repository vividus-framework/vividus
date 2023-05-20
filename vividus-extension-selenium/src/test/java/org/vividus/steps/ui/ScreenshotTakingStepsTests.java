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

package org.vividus.steps.ui;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.google.common.eventbus.EventBus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.reporter.event.AttachmentPublishEvent;
import org.vividus.reporter.model.Attachment;
import org.vividus.selenium.screenshot.Screenshot;
import org.vividus.selenium.screenshot.ScreenshotTaker;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class ScreenshotTakingStepsTests
{
    private static final String PATH = "path/image.png";

    @Mock private ScreenshotTaker screenshotTaker;
    @Mock private EventBus eventBus;
    @InjectMocks private ScreenshotTakingSteps screenshotTakingSteps;

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(ScreenshotTakingSteps.class);

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
    void shouldTakeScreenshotAndSaveItTheSpecifiedPath(@TempDir Path path) throws IOException
    {
        byte[] image = new byte[1];
        when(screenshotTaker.takeScreenshotAsByteArray()).thenReturn(image);
        Path screenshotFilePath = path.resolve(PATH);
        screenshotTakingSteps.takeScreenshotToPath(screenshotFilePath);
        assertTrue(Files.exists(screenshotFilePath));
        assertArrayEquals(image, Files.readAllBytes(screenshotFilePath));
        assertThat(testLogger.getLoggingEvents(),
                equalTo(List.of(info("Screenshot is saved at '{}'", screenshotFilePath.toAbsolutePath()))));
    }
}
