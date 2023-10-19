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

package io.appium.java_client;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import org.apache.commons.lang3.function.FailableRunnable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.RemoteWebElement;
import org.vividus.mobileapp.action.TouchActions;
import org.vividus.mobileapp.configuration.MobileApplicationConfiguration;
import org.vividus.mobileapp.configuration.SwipeConfiguration;
import org.vividus.mobileapp.configuration.ZoomConfiguration;
import org.vividus.mobileapp.model.SwipeDirection;
import org.vividus.mobileapp.model.ZoomType;
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
    private static final String BLACK_IMAGE = "black.png";
    private static final String WHITE_IMAGE = "white.png";
    private static final String ELEMENT_ID = "elementId";
    private static final Dimension DIMENSION = new Dimension(600, 800);
    private static final Rectangle ACTION_AREA = new Rectangle(new Point(0, 0), DIMENSION);
    private static final String FINGER_1 = "finger1";
    private static final String FINGER_2 = "finger2";

    private final SwipeConfiguration swipeConfiguration =
            new SwipeConfiguration(Duration.ZERO, 5, 50, 0);
    private final ZoomConfiguration zoomConfiguration = new ZoomConfiguration(10, 20, 15, 25);
    @Spy private final MobileApplicationConfiguration mobileApplicationConfiguration =
            new MobileApplicationConfiguration(swipeConfiguration, zoomConfiguration);
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
        var actions = ACTIONS_OPEN + PRESS
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
        var actions = ACTIONS_OPEN + "{action=press, options={x=2, y=5}}, "
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
        var actions = ACTIONS_OPEN + PRESS
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
            var parameters = arg.getParameters().toString();
            var actions = ACTIONS_OPEN + "{action=tap, options={element=elementId}}" + ACTIONS_CLOSE;
            return actions.equals(parameters);
        }));
    }

    @Test
    void shouldSwipeUntilConditionIsTrue() throws IOException
    {
        validateSwipe(() -> {
            var context = mock(AppiumDriver.class);
            when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(context));
            when(genericWebDriverManager.getSize()).thenReturn(DIMENSION);
            when(stopCondition.getAsBoolean()).thenReturn(false, false, true);
            when(screenshotTaker.takeViewportScreenshot()).thenReturn(getImage(BLACK_IMAGE)).thenReturn(
                    getImage(WHITE_IMAGE));

            var swipeArea = new Rectangle(new Point(-5, -5), new Dimension(700, 900));
            touchActions.swipeUntil(SwipeDirection.UP, DURATION, swipeArea, stopCondition);
        }, 3);
    }

    @Test
    void shouldNotCropElementContextScreenshot() throws IOException
    {
        validateSwipe(() -> {
            var context = mock(WebElement.class);
            when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(context));
            when(genericWebDriverManager.getSize()).thenReturn(DIMENSION);
            when(stopCondition.getAsBoolean()).thenReturn(false).thenReturn(false).thenReturn(true);
            var blackImage = spy(getImage(BLACK_IMAGE));
            var whiteImage = spy(getImage(WHITE_IMAGE));
            var blackScreenshot = new Screenshot(blackImage);
            var whiteScreenshot = new Screenshot(whiteImage);

            when(screenshotTaker.takeAshotScreenshot(context, Optional.empty())).thenReturn(blackScreenshot).thenReturn(
                    whiteScreenshot);

            touchActions.swipeUntil(SwipeDirection.UP, DURATION, ACTION_AREA, stopCondition);

            verify(blackImage, never()).getSubimage(anyInt(), anyInt(), anyInt(), anyInt());
            verify(whiteImage, never()).getSubimage(anyInt(), anyInt(), anyInt(), anyInt());
        }, 3);
    }

    @Test
    void shouldNotDoAnythingWhenContextIsNotSet()
    {
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.empty());
        touchActions.swipeUntil(SwipeDirection.UP, DURATION, ACTION_AREA, stopCondition);
        verifyNoInteractions(genericWebDriverManager, screenshotTaker, performsTouchActions);
    }

    @Test
    void shouldWrapIOException() throws IOException
    {
        validateSwipe(() -> {
            when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(mock(AppiumDriver.class)));
            when(genericWebDriverManager.getSize()).thenReturn(DIMENSION);
            var exception = mock(IOException.class);

            when(stopCondition.getAsBoolean()).thenReturn(false);
            doThrow(exception).when(screenshotTaker).takeViewportScreenshot();

            var wrapper = assertThrows(UncheckedIOException.class,
                    () -> touchActions.swipeUntil(SwipeDirection.UP, DURATION, ACTION_AREA, stopCondition));

            assertEquals(exception, wrapper.getCause());
        }, 1);
    }

    @Test
    void shouldNotExceedSwipeLimit() throws IOException
    {
        validateSwipe(() -> {
            var context = mock(AppiumDriver.class);
            when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(context));
            when(genericWebDriverManager.getSize()).thenReturn(DIMENSION);
            when(stopCondition.getAsBoolean()).thenReturn(false);
            when(screenshotTaker.takeViewportScreenshot()).thenReturn(getImage(BLACK_IMAGE)).thenReturn(
                            getImage(WHITE_IMAGE)).thenReturn(getImage(BLACK_IMAGE)).thenReturn(getImage(WHITE_IMAGE))
                    .thenReturn(getImage(BLACK_IMAGE)).thenReturn(getImage(WHITE_IMAGE));

            var exception = assertThrows(IllegalStateException.class,
                    () -> touchActions.swipeUntil(SwipeDirection.UP, DURATION, ACTION_AREA, stopCondition));

            assertEquals("Swiping is stopped due to exceeded swipe limit '5'", exception.getMessage());
        }, 6);
    }

    @Test
    void shouldStopSwipeOnceEndOfPageIsReached() throws IOException
    {
        validateSwipe(() -> {
            var context = mock(AppiumDriver.class);
            when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(context));
            when(genericWebDriverManager.getSize()).thenReturn(DIMENSION);
            when(stopCondition.getAsBoolean()).thenReturn(false);
            when(screenshotTaker.takeViewportScreenshot()).thenReturn(getImage(BLACK_IMAGE)).thenReturn(
                    getImage(WHITE_IMAGE)).thenReturn(getImage(BLACK_IMAGE)).thenReturn(getImage(BLACK_IMAGE));

            touchActions.swipeUntil(SwipeDirection.UP, DURATION, ACTION_AREA, stopCondition);
        }, 4);
    }

    @Test
    void shouldPerformVerticalSwipe() throws IOException
    {
        validateSwipe(() -> touchActions.performSwipe(SwipeDirection.UP, 640, 160, ACTION_AREA, DURATION), 1);
    }

    @Test
    void shouldPerformDoubleTapIos()
    {
        var javascriptExecutor = mock(JavascriptExecutor.class);
        when(genericWebDriverManager.isAndroid()).thenReturn(false);
        when(genericWebDriverManager.isIOS()).thenReturn(true);
        when(element.getId()).thenReturn(ELEMENT_ID);
        when(webDriverProvider.getUnwrapped(JavascriptExecutor.class)).thenReturn(javascriptExecutor);

        touchActions.doubleTap(element);
        verify(javascriptExecutor).executeScript("mobile: doubleTap", Map.of(ELEMENT_ID, ELEMENT_ID));
        verifyNoMoreInteractions(webDriverProvider);
    }

    @Test
    void shouldPerformDoubleTapAndroid()
    {
        var javascriptExecutor = mock(JavascriptExecutor.class);
        when(element.getId()).thenReturn(ELEMENT_ID);
        when(genericWebDriverManager.isAndroid()).thenReturn(true);
        when(webDriverProvider.getUnwrapped(JavascriptExecutor.class)).thenReturn(javascriptExecutor);

        touchActions.doubleTap(element);
        verify(javascriptExecutor).executeScript("mobile: doubleClickGesture", Map.of(ELEMENT_ID, ELEMENT_ID));
        verifyNoMoreInteractions(webDriverProvider);
    }

    @Test
    void shouldFailDoubleTapElementIfNotSupportedPlatform()
    {
        when(genericWebDriverManager.isAndroid()).thenReturn(false);
        when(genericWebDriverManager.isIOS()).thenReturn(false);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> touchActions.doubleTap(element));
        assertEquals("Double tap action is available only for Android and iOS platforms", exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    @CsvSource({
            "OUT,  150, 640, 293, 416, 510, 80, 366, 303",
            "IN,   293, 416, 150, 640, 366, 303, 510, 80"
    })
    @ParameterizedTest
    void shouldPerformZoom(ZoomType zoomType, int point1StartX, int point1StartY, int point1EndX, int point1EndY,
            int point2StartX, int point2StartY, int point2EndX, int point2EndY)
    {
        var webDriver = mock(Interactive.class);
        when(webDriverProvider.getUnwrapped(Interactive.class)).thenReturn(webDriver);
        var zoomSequence = buildSwipeSequence(FINGER_1, 200, point1StartX, point1StartY, point1EndX, point1EndY)
                + buildSwipeSequence(FINGER_2, 200, point2StartX, point2StartY, point2EndX, point2EndY);
        touchActions.performZoom(zoomType, ACTION_AREA);
        ArgumentCaptor<Collection<Sequence>> actionsCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(webDriver).perform(actionsCaptor.capture());
        assertEquals(zoomSequence, asString(actionsCaptor.getValue()));
    }

    private static String buildSwipeSequence(String pointerName, int swipeDurationMs, int point1StartX,
            int point1StartY, int point1EndX, int point1EndY)
    {
        var pointerMoveTemplate = "{duration=%d, x=%%s, y=%%s, type=pointerMove, origin=viewport}";
        var swipeSequenceTemplate = "{id=%s, type=pointer, parameters={pointerType=touch}, actions=["
                + format(pointerMoveTemplate, 0) + ", " + "{button=0, type=pointerDown}, "
                + format(pointerMoveTemplate, swipeDurationMs) + ", {button=0, type=pointerUp}]}";
        return format(swipeSequenceTemplate, pointerName, point1StartX, point1StartY, point1EndX, point1EndY);
    }

    @SuppressWarnings("unchecked")
    private void validateSwipe(FailableRunnable<IOException> actionRunner, int times) throws IOException
    {
        Interactive webDriver = mock();
        when(webDriverProvider.getUnwrapped(Interactive.class)).thenReturn(webDriver);

        actionRunner.run();

        ArgumentCaptor<Collection<Sequence>> actionsCaptor = ArgumentCaptor.forClass(Collection.class);
        verify(webDriver, times(times)).perform(actionsCaptor.capture());
        var swipeSequence = buildSwipeSequence(FINGER_1, 1000, 300, 640, 300, 160);
        assertEquals(swipeSequence, asString(actionsCaptor.getValue()));

        verifyNoMoreInteractions(stopCondition, screenshotTaker, genericWebDriverManager, webDriverProvider);
    }

    private BufferedImage getImage(String image) throws IOException
    {
        var bytes = ResourceUtils.loadResourceAsByteArray(getClass(), image);
        return ImageTool.toBufferedImage(bytes);
    }

    private static String asString(Collection<Sequence> sequences)
    {
        return sequences.stream()
                .map(Sequence::encode)
                .map(Map::toString)
                .sorted()
                .collect(Collectors.joining());
    }
}
