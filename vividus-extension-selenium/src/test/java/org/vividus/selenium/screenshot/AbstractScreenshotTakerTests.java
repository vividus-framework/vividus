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

package org.vividus.selenium.screenshot;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.google.common.eventbus.EventBus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.util.ResourceUtils;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class AbstractScreenshotTakerTests
{
    private static final String IMAGE_PNG = "image.png";

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(AbstractScreenshotTaker.class);

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private TakesScreenshot takesScreenshot;
    @Mock private EventBus eventBus;
    @Mock private IScreenshotFileNameGenerator screenshotFileNameGenerator;
    @InjectMocks private TestScreenshotTaker testScreenshotTaker;

    @AfterEach
    void afterEach()
    {
        verifyNoMoreInteractions(webDriverProvider, takesScreenshot);
    }

    @Test
    void shouldTakeViewportScreenshot() throws IOException
    {
        byte[] bytes = ResourceUtils.loadResourceAsByteArray(getClass(), IMAGE_PNG);

        when(webDriverProvider.getUnwrapped(TakesScreenshot.class)).thenReturn(takesScreenshot);
        when(takesScreenshot.getScreenshotAs(OutputType.BYTES)).thenReturn(bytes);

        BufferedImage image = testScreenshotTaker.takeViewportScreenshot();

        assertEquals(400, image.getWidth());
        assertEquals(600, image.getHeight());
    }

    @Test
    void shouldGenerateScreenshotFileName()
    {
        String screenshotName = "fileName";
        testScreenshotTaker.generateScreenshotFileName(screenshotName);
        verify(screenshotFileNameGenerator).generateScreenshotFileName(screenshotName);
    }

    @Test
    void shouldGeneratePathStartingWithScrerenshotDirectoryToAScreenshotWithFileName(@TempDir File tempDir)
    {
        String screenshotName = "image";
        testScreenshotTaker.setScreenshotDirectory(tempDir);
        when(screenshotFileNameGenerator.generateScreenshotFileName(screenshotName)).thenReturn(IMAGE_PNG);
        assertEquals(tempDir.toPath().resolve(IMAGE_PNG), testScreenshotTaker.generateScreenshotPath(screenshotName));
    }

    @Test
    void shouldNotPublishEmptyScreenshotData(@TempDir Path path) throws IOException
    {
        assertNull(testScreenshotTaker.takeScreenshotAsFile(path.resolve(IMAGE_PNG), () ->  new byte[0]));
        verifyNoInteractions(eventBus);
        assertThat(testLogger.getLoggingEvents(), empty());
    }

    @Test
    void shouldPublishScreenshot(@TempDir Path path) throws IOException
    {
        testScreenshotTaker.setScreenshotDirectory(path.toFile());
        byte[] screenshot = new byte[1];
        Path screenshotPath = testScreenshotTaker
                .takeScreenshotAsFile(path.resolve(IMAGE_PNG), () ->  screenshot);
        verify(eventBus).post(argThat(s -> screenshotPath.equals(((ScreenshotTakeEvent) s).getScreenshotFilePath())));
        assertTrue(Files.exists(screenshotPath));
        assertArrayEquals(screenshot, Files.readAllBytes(screenshotPath));
        assertThat(testLogger.getLoggingEvents(), equalTo(List.of(
                info("Screenshot was taken: {}", screenshotPath.toAbsolutePath()))));
    }

    @Test
    void shouldSupplyWebDriverProvider()
    {
        assertSame(webDriverProvider, testScreenshotTaker.getWebDriverProvider());
    }

    private static class TestScreenshotTaker extends AbstractScreenshotTaker
    {
        TestScreenshotTaker(IWebDriverProvider webDriverProvider, EventBus eventBus,
                IScreenshotFileNameGenerator screenshotFileNameGenerator)
        {
            super(webDriverProvider, eventBus, screenshotFileNameGenerator);
        }

        @Override
        public Optional<Screenshot> takeScreenshot(String screenshotName)
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Path takeScreenshotAsFile(String screenshotName) throws IOException
        {
            throw new UnsupportedOperationException();
        }

        @Override
        public Path takeScreenshot(Path screenshotFilePath) throws IOException
        {
            throw new UnsupportedOperationException();
        }
    }
}
