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

package org.vividus.selenium.mobileapp.screenshot.strategies;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Set;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.vividus.mobileapp.action.TouchActions;
import org.vividus.mobileapp.configuration.MobileApplicationConfiguration;
import org.vividus.reporter.event.AttachmentPublisher;
import org.vividus.selenium.mobileapp.MobileAppWebDriverManager;
import org.vividus.selenium.mobileapp.screenshot.strategies.MobileAppFullShootingStrategy.TemplateMissmatchException;
import org.vividus.selenium.screenshot.ScreenshotUtils;
import org.vividus.util.ResourceUtils;

import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;
import ru.yandex.qatools.ashot.util.ImageTool;

@ExtendWith(MockitoExtension.class)
class MobileAppFullShootingStrategyTests
{
    private static final String UNSUPPORTED_MESSAGE = "Element screenshot is not supported";
    private static final String TOP_IMAGE = "top-image.png";
    private static final String FIRST_IMAGE = "1st-scroll-image.png";
    private static final String SECOND_IMAGE = "2nd-scroll-image.png";

    @Mock private TouchActions touchActions;
    @Mock private MobileAppWebDriverManager genericWebDriverManager;
    @Mock private AttachmentPublisher attachmentPublisher;
    @Mock private WebDriver webDriver;

    private MobileAppFullShootingStrategy strategy;

    @Test
    void shouldReturnFullScreenshot()
    {
        defaultInit();

        Dimension dimension = new Dimension(375, 812);
        when(genericWebDriverManager.getSize()).thenReturn(dimension);
        when(genericWebDriverManager.getDpr()).thenReturn(3D);

        try (MockedStatic<ScreenshotUtils> utils = mockStatic(ScreenshotUtils.class))
        {
            utils.when(() -> ScreenshotUtils.takeViewportScreenshot(webDriver))
                 .thenReturn(getImage(TOP_IMAGE))
                 .thenReturn(getImage(FIRST_IMAGE))
                 .thenReturn(getImage(SECOND_IMAGE))
                 .thenReturn(getImage("3rd-scroll-image.png"));

            BufferedImage fullImage = strategy.getScreenshot(webDriver);

            assertThat(getImage("full-image.png"), ImageTool.equalImage(fullImage));
        }
    }

    @Test
    void shouldReturnScreenshotLimitedBySwipeLimit()
    {
        init(1);

        Dimension dimension = new Dimension(375, 812);
        when(genericWebDriverManager.getSize()).thenReturn(dimension);
        when(genericWebDriverManager.getDpr()).thenReturn(3D);

        try (MockedStatic<ScreenshotUtils> utils = mockStatic(ScreenshotUtils.class))
        {
            utils.when(() -> ScreenshotUtils.takeViewportScreenshot(webDriver))
                 .thenReturn(getImage(TOP_IMAGE))
                 .thenReturn(getImage(FIRST_IMAGE))
                 .thenReturn(getImage(SECOND_IMAGE));

            BufferedImage fullImage = strategy.getScreenshot(webDriver);

            assertThat(getImage("swipe-limit-image.png"), ImageTool.equalImage(fullImage));
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "full-missmatch.png", "threshold-missmatch.png" })
    void shouldFailWithTemplateMissmatch(String image)
    {
        defaultInit();

        Dimension dimension = new Dimension(375, 812);
        when(genericWebDriverManager.getSize()).thenReturn(dimension);
        when(genericWebDriverManager.getDpr()).thenReturn(3D);

        try (MockedStatic<ScreenshotUtils> utils = mockStatic(ScreenshotUtils.class))
        {
            utils.when(() -> ScreenshotUtils.takeViewportScreenshot(webDriver))
                 .thenReturn(getImage(TOP_IMAGE))
                 .thenReturn(getImage(FIRST_IMAGE))
                 .thenReturn(getImage(image));

            IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> strategy.getScreenshot(webDriver));
            assertThat(thrown.getCause(), instanceOf(TemplateMissmatchException.class));
            TemplateMissmatchException cause = (TemplateMissmatchException) thrown.getCause();
            assertEquals("Unable to match the template in the target image", cause.getMessage());
        }
    }

    @Test
    void shouldReturnStrategy()
    {
        defaultInit();
        ShootingStrategy shootingStrategy = mock(ShootingStrategy.class);

        assertEquals(strategy, strategy.getDecoratedShootingStrategy(shootingStrategy));

        verifyNoInteractions(shootingStrategy);
    }

    @Test
    void shouldFailOnElementScreenshot()
    {
        defaultInit();
        Set<Coords> coords = Set.of();

        UnsupportedOperationException thrown = assertThrows(UnsupportedOperationException.class,
            () -> strategy.getScreenshot(webDriver, coords));
        assertEquals(UNSUPPORTED_MESSAGE, thrown.getMessage());
    }

    @Test
    void shouldFailOnPrepareCoords()
    {
        defaultInit();
        Set<Coords> coords = Set.of();

        UnsupportedOperationException thrown = assertThrows(UnsupportedOperationException.class,
            () -> strategy.prepareCoords(coords));
        assertEquals(UNSUPPORTED_MESSAGE, thrown.getMessage());
    }

    @Test
    void shouldWrapIOExceptionIntoRuntimeException()
    {
        defaultInit();

        Dimension dimension = new Dimension(375, 812);
        when(genericWebDriverManager.getSize()).thenReturn(dimension);
        when(genericWebDriverManager.getDpr()).thenReturn(3D);

        try (MockedStatic<ScreenshotUtils> utils = mockStatic(ScreenshotUtils.class))
        {
            IOException thrownMock = mock(IOException.class);
            utils.when(() -> ScreenshotUtils.takeViewportScreenshot(webDriver)).thenThrow(thrownMock);
            UncheckedIOException thrown = assertThrows(UncheckedIOException.class,
                () -> strategy.getScreenshot(webDriver));
            assertEquals(thrownMock, thrown.getCause());
        }
    }

    private void defaultInit()
    {
        init(10);
    }

    private void init(int swipeLimit)
    {
        MobileApplicationConfiguration mobileAppConfig = new MobileApplicationConfiguration(Duration.ZERO,
                swipeLimit, 0, 0);
        strategy = new MobileAppFullShootingStrategy(touchActions, genericWebDriverManager, mobileAppConfig,
                attachmentPublisher);
    }

    private BufferedImage getImage(String image)
    {
        byte[] bytes = ResourceUtils.loadResourceAsByteArray(getClass(), image);
        try (InputStream inputStream = new ByteArrayInputStream(bytes))
        {
            return ImageIO.read(inputStream);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
