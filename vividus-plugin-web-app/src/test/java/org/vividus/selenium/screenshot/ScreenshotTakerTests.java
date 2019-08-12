/*
 * Copyright 2019 the original author or authors.
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
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
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
import com.google.common.eventbus.EventBus;

import org.jsoup.UncheckedIOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.util.ResourceUtils;

import ru.yandex.qatools.ashot.AShot;
import ru.yandex.qatools.ashot.util.ImageTool;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ImageTool.class)
public class ScreenshotTakerTests
{
    private static final String SCREENSHOT_WAS_TAKEN = "Screenshot was taken: {}";
    private static final AShot ASHOT = mock(AShot.class);
    private static final String STRATEGY = "strategy";
    private static final String SCREENSHOT_NAME = "screenshotName";
    private static final String SCREENSHOT_NAME_GENERATED = SCREENSHOT_NAME + "Generated";
    private static final ru.yandex.qatools.ashot.Screenshot SCREENSHOT =
            new ru.yandex.qatools.ashot.Screenshot(loadImage());

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(ScreenshotTaker.class);

    @Rule
    private final TemporaryFolder temp = new TemporaryFolder();

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock(extraInterfaces = {JavascriptExecutor.class})
    private WebDriver webDriver;

    @Mock
    private IWebElementHighlighter webElementHighlighter;

    @Mock
    private IScreenshotFileNameGenerator screenshotFileNameGenerator;

    @Mock
    private IScrollbarHandler scrollbarHandler;

    @Mock
    private IAshotFactory ashotFactory;

    @Mock
    private EventBus eventBus;

    @Mock
    private ScreenshotDebugger screenshotDebugger;

    @Mock
    private ScreenshotConfiguration screenshotConfiguration;

    @InjectMocks
    private ScreenshotTaker screenshotTaker;

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
        screenshotTaker.setHighlighterType(HighlighterType.DEFAULT);
        screenshotTaker.setIndent(1);
        screenshotTaker.setShootingStrategy(STRATEGY);
        screenshotTaker.setAshotConfigurations(Map.of(STRATEGY, screenshotConfiguration));
        when(screenshotConfiguration.getScrollableElement()).thenReturn(Optional::empty);
    }

    @After
    public void after()
    {
        testLogger.clearAll();
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
            return ImageIO.read(ResourceUtils.loadFile(ScreenshotTakerTests.class, "screenshot.png"));
        }
        catch (IOException e)
        {
            new UncheckedIOException(e);
        }
        return null;
    }

    @Test
    public void testTakeScreenshotWebElementsDefaultHighlighter() throws IOException
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(screenshotFileNameGenerator.generateScreenshotFileName(SCREENSHOT_NAME))
                .thenReturn(SCREENSHOT_NAME_GENERATED);
        when(ashotFactory.create(false, Optional.of(screenshotConfiguration))).thenReturn(ASHOT);
        mockTakeScreenshotWithHighlights();
        List<WebElement> webElements = mockWebElementsToHighlight();
        when(ASHOT.takeScreenshot(webDriver, webElements)).thenReturn(SCREENSHOT);
        Optional<Screenshot> screen = screenshotTaker.takeScreenshot(SCREENSHOT_NAME, webElements);
        assertScreenshotWithHighlighter(screen);
    }

    @Test
    public void testTakeScreenshotWhenWebDriverIsNotInitialized()
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(Boolean.FALSE);
        Optional<Screenshot> screen = screenshotTaker.takeScreenshot(SCREENSHOT_NAME);
        verifyZeroInteractions(webElementHighlighter);
        assertEquals(Optional.empty(), screen);
    }

    @Test
    public void testTakeScreenshotWebElementsBlurHighlighter() throws IOException
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(screenshotFileNameGenerator.generateScreenshotFileName(SCREENSHOT_NAME))
                .thenReturn(SCREENSHOT_NAME_GENERATED);
        when(ashotFactory.create(false, Optional.of(screenshotConfiguration))).thenReturn(ASHOT);
        screenshotTaker.setHighlighterType(HighlighterType.BLUR);
        List<WebElement> webElements = mockWebElementsToHighlight();
        when(ASHOT.takeScreenshot(webDriver, webElements)).thenReturn(SCREENSHOT);
        Optional<Screenshot> screen = screenshotTaker.takeScreenshot(SCREENSHOT_NAME, webElements);
        assertScreenshotWithHighlighter(screen);
    }

    @Test
    public void testTakeScreenshotEmptyWebElements() throws IOException
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
    public void testTakeScreenshotWebElementsFullPage() throws IOException
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
        when(scrollbarHandler.performActionWithHiddenScrollbars(argThat(s -> {
            s.get();
            return true;
        }))).thenReturn(SCREENSHOT);
        Optional<Screenshot> screen = screenshotTaker.takeScreenshot(SCREENSHOT_NAME, webElements);
        assertScreenshotWithHighlighter(screen);
    }

    @Test
    public void testTakeScreenshot() throws IOException
    {
        mockTakeScreenshotWithHighlights();
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
    public void testTakeViewportScreenshot()
    {
        ScreenshotTaker spy = Mockito.spy(screenshotTaker);
        doReturn(Optional.of(mock(Screenshot.class))).when(spy).takeScreenshot(SCREENSHOT_NAME, List.of(), true);
        spy.takeScreenshot(SCREENSHOT_NAME, true);
        verify(spy).takeScreenshot(SCREENSHOT_NAME, List.of(), true);
    }

    @Test
    public void testTakeViewportScreenshotByPath() throws Exception
    {
        mockTakeScreenshotWithHighlights();
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(ashotFactory.create(true, Optional.of(screenshotConfiguration))).thenReturn(ASHOT);
        when(ASHOT.takeScreenshot(webDriver)).thenReturn(SCREENSHOT);
        Path filePath = temp.newFolder().toPath().resolve(SCREENSHOT_NAME_GENERATED);
        screenshotTaker.takeScreenshot(filePath, true);
        verify(eventBus).post(argThat(e -> filePath.equals(((ScreenshotTakeEvent) e).getScreenshotFilePath())));
        assertTrue(Files.exists(filePath));
        assertThat(testLogger.getLoggingEvents(), equalTo(List.of(
                info(SCREENSHOT_WAS_TAKEN, filePath.toAbsolutePath()))));
    }

    @Test
    public void testTakeViewportScreenshotByPathNotViewport() throws Exception
    {
        mockTakeScreenshotWithHighlights();
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(ashotFactory.create(false, Optional.of(screenshotConfiguration))).thenReturn(ASHOT);
        when(ASHOT.takeScreenshot(webDriver)).thenReturn(SCREENSHOT);
        Path filePath = temp.newFolder().toPath().resolve(SCREENSHOT_NAME_GENERATED);
        screenshotTaker.takeScreenshot(filePath);
        verify(eventBus).post(argThat(e -> filePath.equals(((ScreenshotTakeEvent) e).getScreenshotFilePath())));
        assertTrue(Files.exists(filePath));
        assertThat(testLogger.getLoggingEvents(), equalTo(List.of(
                info(SCREENSHOT_WAS_TAKEN, filePath.toAbsolutePath()))));
    }

    @Test
    public void shouldTakeScreenshotAsFile() throws Exception
    {
        mockTakeScreenshotWithHighlights();
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(ashotFactory.create(false, Optional.of(screenshotConfiguration))).thenReturn(ASHOT);
        when(ASHOT.takeScreenshot(webDriver)).thenReturn(SCREENSHOT);
        String fileName = "screenshotName.png";
        Path folder = temp.newFolder().toPath();
        screenshotTaker.setScreenshotDirectory(folder.toFile());
        Path absolutePath = folder.resolve(fileName).toAbsolutePath();
        when(screenshotFileNameGenerator.generateScreenshotFileName(fileName))
            .thenReturn(fileName);
        screenshotTaker.takeScreenshotAsFile(fileName);
        verify(eventBus).post(argThat(e -> absolutePath.equals(((ScreenshotTakeEvent) e).getScreenshotFilePath())));
        assertTrue(Files.exists(absolutePath));
        assertThat(testLogger.getLoggingEvents(), equalTo(List.of(
                info(SCREENSHOT_WAS_TAKEN, absolutePath.toAbsolutePath()))));
    }

    @Test
    public void testTakeViewportScreenshotByElements()
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(screenshotFileNameGenerator.generateScreenshotFileName(SCREENSHOT_NAME))
                .thenReturn(SCREENSHOT_NAME_GENERATED);
        when(ashotFactory.create(true, Optional.of(screenshotConfiguration))).thenReturn(ASHOT);
        mockTakeScreenshotWithHighlights();
        when(ASHOT.takeScreenshot(webDriver)).thenReturn(SCREENSHOT);
        screenshotTaker.takeScreenshot(SCREENSHOT_NAME, List.of(), true);
    }

    @Test
    public void testTakeScreenshotAsBytesWebElement() throws Exception
    {
        WebElement context = mock(WebElement.class);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(ashotFactory.create(false, Optional.of(screenshotConfiguration))).thenReturn(ASHOT);
        when(scrollbarHandler.performActionWithHiddenScrollbars(argThat(s -> {
            s.get();
            return true;
        }))).thenReturn(SCREENSHOT);
        assertScreenshot(screenshotTaker.takeScreenshot(SCREENSHOT_NAME, context));
        verify(ASHOT).takeScreenshot(webDriver, context);
    }

    @Test
    public void testTakeScreenshotAsBytesFullpage() throws Exception
    {
        when(ashotFactory.create(false, Optional.of(screenshotConfiguration))).thenReturn(ASHOT);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(scrollbarHandler.performActionWithHiddenScrollbars(argThat(s -> {
            s.get();
            return true;
        }))).thenReturn(SCREENSHOT);
        when(ASHOT.takeScreenshot(webDriver)).thenReturn(SCREENSHOT);
        assertScreenshot(screenshotTaker.takeScreenshot(SCREENSHOT_NAME, webDriver));
    }

    @Test
    public void testTakeScreenshotAsBytesException() throws Exception
    {
        when(ashotFactory.create(false, Optional.of(screenshotConfiguration))).thenReturn(ASHOT);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(scrollbarHandler.performActionWithHiddenScrollbars(argThat(s -> {
            s.get();
            return true;
        }))).thenReturn(SCREENSHOT);
        PowerMockito.mockStatic(ImageTool.class);
        IOException ioException = new IOException();
        PowerMockito.when(ImageTool.toByteArray(SCREENSHOT)).thenThrow(ioException);
        IllegalStateException illegalStateException =
            assertThrows(IllegalStateException.class, () -> screenshotTaker.takeScreenshot(SCREENSHOT_NAME, webDriver));
        assertEquals(ioException, illegalStateException.getCause());
    }

    @Test
    public void shouldTakeAShotScreenshotWithCustomConfiguration() throws IOException
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
        verify(webElementHighlighter).clearAssertingWebElements();
        assertEquals(SCREENSHOT_NAME_GENERATED, actualScreenshot.get().getFileName());
        assertScreenshot(actualScreenshot);
    }

    private void assertScreenshot(Optional<Screenshot> actualScreenshot) throws IOException
    {
        assertNotEquals(Optional.empty(), actualScreenshot);
        assertArrayEquals(ImageTool.toByteArray(SCREENSHOT), actualScreenshot.get().getData());
    }

    private List<WebElement> mockWebElementsToHighlight()
    {
        return List.of(mock(WebElement.class));
    }
}
