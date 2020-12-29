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

package io.appium.java_client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.function.BooleanSupplier;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.remote.RemoteWebElement;
import org.vividus.bdd.mobileapp.configuration.MobileApplicationConfiguration;
import org.vividus.bdd.mobileapp.model.SwipeDirection;
import org.vividus.mobileapp.action.TouchActions;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;
import org.vividus.selenium.screenshot.ScreenshotTaker;
import org.vividus.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class TouchActionsTests
{
    private static final String ACTIONS_OPEN = "{actions=[";
    private static final String ACTIONS_CLOSE = "]}";
    private static final String WAIT = "{action=wait, options={ms=1000}}, ";
    private static final String RELEASE = "{action=release, options={}}";
    private static final Duration DURATION = Duration.ofSeconds(1);
    private static final String SCROLL_UP = ACTIONS_OPEN
            + "{action=press, options={x=1, y=640}}, "
            + WAIT
            + "{action=moveTo, options={x=1, y=160}}, "
            + RELEASE
            + ACTIONS_CLOSE;
    private static final String BLACK_IMAGE = "black.png";
    private static final String WHITE_IMAGE = "white.png";
    private static final String ELEMENT_ID = "elementId";
    private static final Dimension DIMENSION = new Dimension(600, 800);

    @Spy private MobileApplicationConfiguration mobileApplicationConfiguration = new MobileApplicationConfiguration(
            Duration.ZERO, 5);
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private RemoteWebElement element;
    @Mock private PerformsTouchActions performsTouchActions;
    @Mock private GenericWebDriverManager genericWebDriverManager;
    @Mock private ScreenshotTaker screenshotTaker;
    @Mock private BooleanSupplier stopCondition;
    @InjectMocks private TouchActions touchActions;

    @BeforeEach
    void init()
    {
        when(webDriverProvider.getUnwrapped(PerformsTouchActions.class)).thenReturn(performsTouchActions);
    }

    @Test
    void shouldTapOnElement()
    {
        when(element.getId()).thenReturn(ELEMENT_ID);

        touchActions.tap(element, Duration.ofSeconds(1));

        verify(performsTouchActions).performTouchAction(argThat(arg ->
        {
            String parameters = arg.getParameters().toString();
            String actions = ACTIONS_OPEN + "{action=press, options={element=elementId}}, "
                                          + WAIT
                                          + RELEASE
                                          + ACTIONS_CLOSE;
            return actions.equals(parameters);
        }));
    }

    @Test
    void shouldTapOnElementWithoutWaitIfDurationIsZero()
    {
        when(element.getId()).thenReturn(ELEMENT_ID);

        touchActions.tap(element);

        verify(performsTouchActions).performTouchAction(argThat(arg ->
        {
            String parameters = arg.getParameters().toString();
            String actions = ACTIONS_OPEN + "{action=tap, options={element=elementId}}" + ACTIONS_CLOSE;
            return actions.equals(parameters);
        }));
    }

    @Test
    void shouldSwipeUntilConditionIsTrue() throws IOException
    {
        when(genericWebDriverManager.getSize()).thenReturn(DIMENSION);
        when(stopCondition.getAsBoolean()).thenReturn(false)
                                          .thenReturn(false)
                                          .thenReturn(true);
        when(screenshotTaker.takeViewportScreenshot()).thenReturn(getImage(BLACK_IMAGE))
                                                      .thenReturn(getImage(WHITE_IMAGE));

        touchActions.swipeUntil(SwipeDirection.UP, DURATION, stopCondition);

        verifySwipe(3);
        verifyConfiguration();
    }

    @Test
    void shouldNotExceedSwipeLimit() throws IOException
    {
        when(genericWebDriverManager.getSize()).thenReturn(DIMENSION);
        when(stopCondition.getAsBoolean()).thenReturn(false);
        when(screenshotTaker.takeViewportScreenshot()).thenReturn(getImage(BLACK_IMAGE))
                                                      .thenReturn(getImage(WHITE_IMAGE))
                                                      .thenReturn(getImage(BLACK_IMAGE))
                                                      .thenReturn(getImage(WHITE_IMAGE))
                                                      .thenReturn(getImage(BLACK_IMAGE))
                                                      .thenReturn(getImage(WHITE_IMAGE));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> touchActions.swipeUntil(SwipeDirection.UP, DURATION, stopCondition));

        assertEquals("Swiping is stopped due to exceeded swipe limit '5'", exception.getMessage());
        verifySwipe(6);
        verifyConfiguration();
    }

    @Test
    void shouldStopSwipeOnceEndOfPageIsReached() throws IOException
    {
        when(genericWebDriverManager.getSize()).thenReturn(DIMENSION);
        when(stopCondition.getAsBoolean()).thenReturn(false);
        when(screenshotTaker.takeViewportScreenshot()).thenReturn(getImage(BLACK_IMAGE))
                                                      .thenReturn(getImage(WHITE_IMAGE))
                                                      .thenReturn(getImage(BLACK_IMAGE))
                                                      .thenReturn(getImage(BLACK_IMAGE));

        touchActions.swipeUntil(SwipeDirection.UP, DURATION, stopCondition);

        verifySwipe(4);
        verifyConfiguration();
    }

    @Test
    void shouldWrapIOException() throws IOException
    {
        when(genericWebDriverManager.getSize()).thenReturn(DIMENSION);
        IOException exception = mock(IOException.class);

        when(stopCondition.getAsBoolean()).thenReturn(false);
        doThrow(exception).when(screenshotTaker).takeViewportScreenshot();

        UncheckedIOException wrapper = assertThrows(UncheckedIOException.class,
            () -> touchActions.swipeUntil(SwipeDirection.UP, DURATION, stopCondition));

        assertEquals(exception, wrapper.getCause());
        verifySwipe(1);
        verifyConfiguration();
    }

    @Test
    void shouldPerformVerticalSwipe()
    {
        touchActions.performVerticalSwipe(640, 160, DURATION);
        verifySwipe(1);
    }

    private void verifySwipe(int times)
    {
        verify(performsTouchActions, times(times))
                .performTouchAction(argThat(arg -> SCROLL_UP.equals(arg.getParameters().toString())));
        verifyNoMoreInteractions(stopCondition, performsTouchActions, screenshotTaker, genericWebDriverManager,
                webDriverProvider);
    }

    private void verifyConfiguration()
    {
        verify(mobileApplicationConfiguration).getSwipeStabilizationDuration();
        verify(mobileApplicationConfiguration).getSwipeLimit();
        verifyNoMoreInteractions(mobileApplicationConfiguration);
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
