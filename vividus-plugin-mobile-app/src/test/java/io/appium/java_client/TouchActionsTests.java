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

package io.appium.java_client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Optional;
import java.util.function.BooleanSupplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.vividus.mobileapp.action.TouchActions;
import org.vividus.mobileapp.configuration.MobileApplicationConfiguration;
import org.vividus.mobileapp.model.SwipeDirection;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;
import org.vividus.selenium.mobileapp.MobileAppScreenshotTaker;
import org.vividus.ui.context.IUiContext;
import org.vividus.util.ResourceUtils;

import pazone.ashot.Screenshot;
import pazone.ashot.util.ImageTool;

@ExtendWith(MockitoExtension.class)
class TouchActionsTests
{
    private static final String ACTIONS_OPEN = "{actions=[";
    private static final String ACTIONS_CLOSE = "]}";

    private static final String PRESS = "{action=press, options={element=elementId}}, ";
    private static final String WAIT = "{action=wait, options={ms=1000}}, ";
    private static final String RELEASE = "{action=release, options={}}";

    private static final Duration DURATION = Duration.ofSeconds(1);
    private static final String SCROLL_UP = ACTIONS_OPEN
            + "{action=press, options={x=300, y=640}}, "
            + WAIT
            + "{action=moveTo, options={x=300, y=160}}, "
            + RELEASE
            + ACTIONS_CLOSE;
    private static final String BLACK_IMAGE = "black.png";
    private static final String WHITE_IMAGE = "white.png";
    private static final String ELEMENT_ID = "elementId";
    private static final Dimension DIMENSION = new Dimension(600, 800);
    private static final Rectangle SWIPE_AREA = new Rectangle(new Point(0, 0), DIMENSION);

    @Spy private final MobileApplicationConfiguration mobileApplicationConfiguration =
            new MobileApplicationConfiguration(Duration.ZERO, 5, 50, 0);
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private RemoteWebElement element;
    @Mock private PerformsTouchActions performsTouchActions;
    @Mock private GenericWebDriverManager genericWebDriverManager;
    @Mock private IUiContext uiContext;
    @Mock private MobileAppScreenshotTaker screenshotTaker;
    @Mock private BooleanSupplier stopCondition;
    @InjectMocks private TouchActions touchActions;

    private void mockPerformsTouchActions()
    {
        when(webDriverProvider.getUnwrapped(PerformsTouchActions.class)).thenReturn(performsTouchActions);
    }

    @Test
    void shouldTapOnVisibleElement()
    {
        mockPerformsTouchActions();
        when(element.isDisplayed()).thenReturn(true);
        when(element.getId()).thenReturn(ELEMENT_ID);

        touchActions.tap(element, Duration.ofSeconds(1));

        var touchActionCaptor = ArgumentCaptor.forClass(TouchAction.class);
        verify(performsTouchActions).performTouchAction(touchActionCaptor.capture());
        String actions = ACTIONS_OPEN + PRESS
                + WAIT
                + RELEASE
                + ACTIONS_CLOSE;
        assertEquals(actions, touchActionCaptor.getValue().getParameters().toString());
    }

    @Test
    void shouldTapOnInvisibleElement()
    {
        mockPerformsTouchActions();
        when(element.isDisplayed()).thenReturn(false);
        when(element.getLocation()).thenReturn(new Point(1, 3));
        when(element.getSize()).thenReturn(new Dimension(2, 4));
        when(genericWebDriverManager.getSize()).thenReturn(new Dimension(100, 200));

        touchActions.tap(element, Duration.ofSeconds(1));

        var touchActionCaptor = ArgumentCaptor.forClass(TouchAction.class);
        verify(performsTouchActions).performTouchAction(touchActionCaptor.capture());
        String actions = ACTIONS_OPEN + "{action=press, options={x=2, y=5}}, "
                + WAIT
                + RELEASE
                + ACTIONS_CLOSE;
        assertEquals(actions, touchActionCaptor.getValue().getParameters().toString());
    }

    @ParameterizedTest
    @CsvSource({
            "100, 200",
            "1,   -3"
    })
    void shouldTapOnInvisibleElementOutsideViewport(int x, int y)
    {
        mockPerformsTouchActions();
        when(element.isDisplayed()).thenReturn(false);
        when(element.getLocation()).thenReturn(new Point(x, y));
        when(element.getSize()).thenReturn(new Dimension(2, 4));
        when(genericWebDriverManager.getSize()).thenReturn(new Dimension(100, 200));
        when(element.getId()).thenReturn(ELEMENT_ID);

        touchActions.tap(element, Duration.ofSeconds(1));

        var touchActionCaptor = ArgumentCaptor.forClass(TouchAction.class);
        verify(performsTouchActions).performTouchAction(touchActionCaptor.capture());
        String actions = ACTIONS_OPEN + PRESS
                + WAIT
                + RELEASE
                + ACTIONS_CLOSE;
        assertEquals(actions, touchActionCaptor.getValue().getParameters().toString());
    }

    @Test
    void shouldTapOnVisibleElementWithoutWaitIfDurationIsZero()
    {
        mockPerformsTouchActions();
        when(element.getId()).thenReturn(ELEMENT_ID);
        when(element.isDisplayed()).thenReturn(true);

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
        mockPerformsTouchActions();
        var context = mock(AppiumDriver.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(context));
        when(genericWebDriverManager.getSize()).thenReturn(DIMENSION);
        when(stopCondition.getAsBoolean()).thenReturn(false)
                                          .thenReturn(false)
                                          .thenReturn(true);
        when(screenshotTaker.takeViewportScreenshot()).thenReturn(getImage(BLACK_IMAGE))
                                                      .thenReturn(getImage(WHITE_IMAGE));

        touchActions.swipeUntil(SwipeDirection.UP, DURATION, SWIPE_AREA, stopCondition);

        verifySwipe(3);
        verifyConfiguration();
    }

    @Test
    void shouldNotCropElementContextScreenshot()
    {
        mockPerformsTouchActions();
        var context = mock(WebElement.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(context));
        when(stopCondition.getAsBoolean()).thenReturn(false)
                .thenReturn(false)
                .thenReturn(true);
        var blackImage = Mockito.spy(getImage(BLACK_IMAGE));
        var whiteImage = Mockito.spy(getImage(WHITE_IMAGE));
        var blackScreenshot = new Screenshot(blackImage);
        var whiteScreenshot = new Screenshot(whiteImage);

        when(screenshotTaker.takeAshotScreenshot(context, Optional.empty())).thenReturn(blackScreenshot)
            .thenReturn(whiteScreenshot);

        touchActions.swipeUntil(SwipeDirection.UP, DURATION, SWIPE_AREA, stopCondition);

        verifySwipe(3);
        verifyNoMoreInteractions(genericWebDriverManager);
        verify(blackImage, never()).getSubimage(anyInt(), anyInt(), anyInt(), anyInt());
        verify(whiteImage, never()).getSubimage(anyInt(), anyInt(), anyInt(), anyInt());
        verifyConfiguration();
    }

    @Test
    void shouldNotDoAnythingWhenContextIsNotSet()
    {
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.empty());
        touchActions.swipeUntil(SwipeDirection.UP, DURATION, SWIPE_AREA, stopCondition);
        verifyNoInteractions(genericWebDriverManager, screenshotTaker, performsTouchActions);
    }

    @Test
    void shouldWrapIOException() throws IOException
    {
        mockPerformsTouchActions();
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(mock(AppiumDriver.class)));
        when(genericWebDriverManager.getSize()).thenReturn(DIMENSION);
        IOException exception = mock(IOException.class);

        when(stopCondition.getAsBoolean()).thenReturn(false);
        doThrow(exception).when(screenshotTaker).takeViewportScreenshot();

        UncheckedIOException wrapper = assertThrows(UncheckedIOException.class,
                () -> touchActions.swipeUntil(SwipeDirection.UP, DURATION, SWIPE_AREA, stopCondition));

        assertEquals(exception, wrapper.getCause());
        verifySwipe(1);
        verifyConfiguration();
    }

    @Test
    void shouldNotExceedSwipeLimit() throws IOException
    {
        mockPerformsTouchActions();
        var context = mock(AppiumDriver.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(context));
        when(genericWebDriverManager.getSize()).thenReturn(DIMENSION);
        when(stopCondition.getAsBoolean()).thenReturn(false);
        when(screenshotTaker.takeViewportScreenshot()).thenReturn(getImage(BLACK_IMAGE))
                                                      .thenReturn(getImage(WHITE_IMAGE))
                                                      .thenReturn(getImage(BLACK_IMAGE))
                                                      .thenReturn(getImage(WHITE_IMAGE))
                                                      .thenReturn(getImage(BLACK_IMAGE))
                                                      .thenReturn(getImage(WHITE_IMAGE));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> touchActions.swipeUntil(SwipeDirection.UP, DURATION, SWIPE_AREA, stopCondition));

        assertEquals("Swiping is stopped due to exceeded swipe limit '5'", exception.getMessage());
        verifySwipe(6);
        verifyConfiguration();
    }

    @Test
    void shouldStopSwipeOnceEndOfPageIsReached() throws IOException
    {
        mockPerformsTouchActions();
        var context = mock(AppiumDriver.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(context));
        when(genericWebDriverManager.getSize()).thenReturn(DIMENSION);
        when(stopCondition.getAsBoolean()).thenReturn(false);
        when(screenshotTaker.takeViewportScreenshot()).thenReturn(getImage(BLACK_IMAGE))
                                                      .thenReturn(getImage(WHITE_IMAGE))
                                                      .thenReturn(getImage(BLACK_IMAGE))
                                                      .thenReturn(getImage(BLACK_IMAGE));

        touchActions.swipeUntil(SwipeDirection.UP, DURATION, SWIPE_AREA, stopCondition);

        verifySwipe(4);
        verifyConfiguration();
    }

    @Test
    void shouldPerformVerticalSwipe()
    {
        mockPerformsTouchActions();
        touchActions.performSwipe(SwipeDirection.UP, 640, 160, SWIPE_AREA, DURATION);
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
        verify(mobileApplicationConfiguration).getSwipeVerticalXPosition();
        verifyNoMoreInteractions(mobileApplicationConfiguration);
    }

    private BufferedImage getImage(String image)
    {
        try
        {
            byte[] bytes = ResourceUtils.loadResourceAsByteArray(getClass(), image);
            return ImageTool.toBufferedImage(bytes);
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }
}
