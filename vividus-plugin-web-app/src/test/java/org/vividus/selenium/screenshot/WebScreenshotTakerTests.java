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
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import javax.imageio.ImageIO;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.google.common.eventbus.EventBus;

import org.jsoup.UncheckedIOException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.util.ResourceUtils;
import org.vividus.util.property.PropertyMappedCollection;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.util.ImageTool;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class WebScreenshotTakerTests
{
    private static final String SCREENSHOT_WAS_TAKEN = "Screenshot was taken: {}";
    private static final AShot ASHOT = mock(AShot.class);
    private static final String STRATEGY = "strategy";
    private static final String SCREENSHOT_NAME = "screenshotName";
    private static final String SCREENSHOT_NAME_GENERATED = SCREENSHOT_NAME + "Generated";
    private static final ru.yandex.qatools.ashot.Screenshot SCREENSHOT =
            new ru.yandex.qatools.ashot.Screenshot(loadImage());

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(AbstractScreenshotTaker.class);

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IScreenshotFileNameGenerator screenshotFileNameGenerator;
    @Mock private IWebElementHighlighter webElementHighlighter;
    @Mock private EventBus eventBus;
    @Mock private IScrollbarHandler scrollbarHandler;
    @Mock private IAshotFactory ashotFactory;
    @Mock private ScreenshotDebugger screenshotDebugger;
    @InjectMocks private WebScreenshotTaker screenshotTaker;

    @Mock(extraInterfaces = JavascriptExecutor.class)
    private WebDriver webDriver;

    @Mock
    private ScreenshotConfiguration screenshotConfiguration;

    @BeforeEach
    void beforeEach()
    {
        screenshotTaker.setHighlighterType(HighlighterType.DEFAULT);
        screenshotTaker.setIndent(1);
        screenshotTaker.setShootingStrategy(STRATEGY);
        screenshotTaker.setAshotConfigurations(
                new PropertyMappedCollection<>(Map.of(STRATEGY, screenshotConfiguration)));
    }

    @SuppressWarnings("unchecked")
    private void mockTakeScreenshotWithHighlights()
    {
        when(webElementHighlighter.takeScreenshotWithHighlights(any(Supplier.class)))
                .thenAnswer(invocation -> ((Supplier<?>) invocation.getArguments()[0]).get());
    }

    private static BufferedImage loadImage()
    {
        try
        {
            return ImageIO.read(ResourceUtils.loadFile(WebScreenshotTakerTests.class, "screenshot.png"));
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    @Test
    void testTakeScreenshotWebElementsDefaultHighlighter() throws IOException
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(screenshotFileNameGenerator.generateScreenshotFileName(SCREENSHOT_NAME))
                .thenReturn(SCREENSHOT_NAME_GENERATED);
        when(ashotFactory.create(false, Optional.of(screenshotConfiguration))).thenReturn(ASHOT);
        mockTakeScreenshotWithHighlights();
        List<WebElement> webElements = List.of(mock(WebElement.class));
        when(ASHOT.takeScreenshot(webDriver, webElements)).thenReturn(SCREENSHOT);
        Optional<Screenshot> screen = screenshotTaker.takeScreenshot(SCREENSHOT_NAME, webElements);
        assertScreenshotWithHighlighter(screen);
    }

    @Test
    void testTakeScreenshotWhenWebDriverIsNotInitialized()
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(Boolean.FALSE);
        Optional<Screenshot> screen = screenshotTaker.takeScreenshot(SCREENSHOT_NAME);
        verifyNoInteractions(webElementHighlighter);
        assertEquals(Optional.empty(), screen);
    }

    @Test
    void testTakeScreenshotWebElementsBlurHighlighter() throws IOException
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(screenshotFileNameGenerator.generateScreenshotFileName(SCREENSHOT_NAME))
                .thenReturn(SCREENSHOT_NAME_GENERATED);
        when(ashotFactory.create(false, Optional.of(screenshotConfiguration))).thenReturn(ASHOT);
        screenshotTaker.setHighlighterType(HighlighterType.BLUR);
        List<WebElement> webElements = List.of(mock(WebElement.class));
        when(ASHOT.takeScreenshot(webDriver, webElements)).thenReturn(SCREENSHOT);
        Optional<Screenshot> screen = screenshotTaker.takeScreenshot(SCREENSHOT_NAME, webElements);
        assertScreenshotWithHighlighter(screen);
    }

    @Test
    void testTakeScreenshotEmptyWebElements() throws IOException
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(screenshotFileNameGenerator.generateScreenshotFileName(SCREENSHOT_NAME))
                .thenReturn(SCREENSHOT_NAME_GENERATED);
        when(ashotFactory.create(false, Optional.of(screenshotConfiguration))).thenReturn(ASHOT);
        mockTakeScreenshotWithHighlights();
        when(ASHOT.takeScreenshot(webDriver)).thenReturn(SCREENSHOT);
        Optional<Screenshot> screen = screenshotTaker.takeScreenshot(SCREENSHOT_NAME, List.of());
        assertScreenshotWithHighlighter(screen);
    }

    @Test
    void testTakeScreenshotWebElementsFullPage() throws IOException
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(screenshotFileNameGenerator.generateScreenshotFileName(SCREENSHOT_NAME))
                .thenReturn(SCREENSHOT_NAME_GENERATED);
        when(ashotFactory.create(false, Optional.of(screenshotConfiguration))).thenReturn(ASHOT);
        screenshotTaker.setFullPageScreenshots(true);
        mockTakeScreenshotWithHighlights();
        List<WebElement> webElements = Collections.singletonList(mock(WebElement.class));
        when(ASHOT.takeScreenshot(webDriver)).thenReturn(SCREENSHOT);
        Optional<Screenshot> screen = screenshotTaker.takeScreenshot(SCREENSHOT_NAME, webElements);
        assertScreenshotWithHighlighter(screen);
    }

    @Test
    void testTakeScreenshot() throws IOException
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(screenshotFileNameGenerator.generateScreenshotFileName(SCREENSHOT_NAME))
                .thenReturn(SCREENSHOT_NAME_GENERATED);
        when(ashotFactory.create(false, Optional.of(screenshotConfiguration))).thenReturn(ASHOT);
        screenshotTaker.setHighlighterType(HighlighterType.MONOCHROME);
        when(ASHOT.takeScreenshot(webDriver)).thenReturn(SCREENSHOT);
        Optional<Screenshot> screen = screenshotTaker.takeScreenshot(SCREENSHOT_NAME);
        assertScreenshotWithHighlighter(screen);
    }

    @Test
    void testTakeViewportScreenshotByPathNotViewport(@TempDir Path tempDir) throws Exception
    {
        mockTakeScreenshotWithHighlights();
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(ashotFactory.create(false, Optional.of(screenshotConfiguration))).thenReturn(ASHOT);
        when(ASHOT.takeScreenshot(webDriver)).thenReturn(SCREENSHOT);
        Path filePath = tempDir.resolve(SCREENSHOT_NAME_GENERATED);
        screenshotTaker.takeScreenshot(filePath);
        verify(eventBus).post(argThat(e -> filePath.equals(((ScreenshotTakeEvent) e).getScreenshotFilePath())));
        assertTrue(Files.exists(filePath));
        assertThat(testLogger.getLoggingEvents(), equalTo(List.of(
                info(SCREENSHOT_WAS_TAKEN, filePath.toAbsolutePath()))));
    }

    @Test
    void shouldTakeScreenshotAsFile(@TempDir Path tempDir) throws IOException
    {
        mockTakeScreenshotWithHighlights();
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(ashotFactory.create(false, Optional.of(screenshotConfiguration))).thenReturn(ASHOT);
        when(ASHOT.takeScreenshot(webDriver)).thenReturn(SCREENSHOT);
        String fileName = "screenshotName.png";
        screenshotTaker.setScreenshotDirectory(tempDir.toFile());
        Path absolutePath = tempDir.resolve(fileName).toAbsolutePath();
        when(screenshotFileNameGenerator.generateScreenshotFileName(fileName))
            .thenReturn(fileName);
        screenshotTaker.takeScreenshotAsFile(fileName);
        verify(eventBus).post(argThat(e -> absolutePath.equals(((ScreenshotTakeEvent) e).getScreenshotFilePath())));
        assertTrue(Files.exists(absolutePath));
        assertThat(testLogger.getLoggingEvents(), equalTo(List.of(
                info(SCREENSHOT_WAS_TAKEN, absolutePath.toAbsolutePath()))));
    }

    @Test
    void shouldTakeAShotScreenshotWithCustomConfiguration() throws IOException
    {
        ScreenshotConfiguration configurationMock = mock(ScreenshotConfiguration.class);
        Optional<ScreenshotConfiguration> screenshotConfiguration = Optional.of(configurationMock);
        WebElement scrollableElement = mock(WebElement.class);
        when(configurationMock.getScrollableElement()).thenReturn(() -> Optional.of(scrollableElement));
        when(ashotFactory.create(false, screenshotConfiguration)).thenReturn(ASHOT);
        when(scrollbarHandler.performActionWithHiddenScrollbars(argThat(s -> {
            s.get();
            return true;
        }), eq(scrollableElement))).thenReturn(SCREENSHOT);
        assertArrayEquals(ImageTool.toByteArray(SCREENSHOT),
                ImageTool.toByteArray(screenshotTaker.takeAshotScreenshot(webDriver,
                        screenshotConfiguration)));
        verify(ASHOT).takeScreenshot(webDriver);
    }

    private void assertScreenshotWithHighlighter(Optional<Screenshot> actualScreenshot) throws IOException
    {
        assertEquals(SCREENSHOT_NAME_GENERATED, actualScreenshot.get().getFileName());
        assertScreenshot(actualScreenshot);
    }

    private void assertScreenshot(Optional<Screenshot> actualScreenshot) throws IOException
    {
        assertNotEquals(Optional.empty(), actualScreenshot);
        assertArrayEquals(ImageTool.toByteArray(SCREENSHOT), actualScreenshot.get().getData());
    }
}
