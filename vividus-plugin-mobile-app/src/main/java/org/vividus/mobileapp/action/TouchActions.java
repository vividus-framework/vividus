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

package org.vividus.mobileapp.action;

import static io.appium.java_client.touch.WaitOptions.waitOptions;
import static io.appium.java_client.touch.offset.ElementOption.element;
import static io.appium.java_client.touch.offset.PointOption.point;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.vividus.mobileapp.configuration.MobileApplicationConfiguration;
import org.vividus.mobileapp.model.SwipeCoordinates;
import org.vividus.mobileapp.model.SwipeDirection;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverUtils;
import org.vividus.selenium.manager.GenericWebDriverManager;
import org.vividus.selenium.mobileapp.MobileAppScreenshotTaker;
import org.vividus.ui.context.IUiContext;
import org.vividus.util.Sleeper;

import io.appium.java_client.PerformsTouchActions;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.offset.ElementOption;
import io.appium.java_client.touch.offset.PointOption;
import pazone.ashot.util.ImageTool;

public class TouchActions
{
    private static final int HEIGHT_DIVIDER = 3;

    private final IWebDriverProvider webDriverProvider;
    private final GenericWebDriverManager genericWebDriverManager;
    private final MobileAppScreenshotTaker screenshotTaker;
    private final MobileApplicationConfiguration mobileApplicationConfiguration;
    private final IUiContext uiContext;

    public TouchActions(IWebDriverProvider webDriverProvider, GenericWebDriverManager genericWebDriverManager,
            MobileAppScreenshotTaker screenshotTaker,
            MobileApplicationConfiguration mobileApplicationConfiguration, IUiContext uiContext)
    {
        this.webDriverProvider = webDriverProvider;
        this.genericWebDriverManager = genericWebDriverManager;
        this.screenshotTaker = screenshotTaker;
        this.mobileApplicationConfiguration = mobileApplicationConfiguration;
        this.uiContext = uiContext;
    }

    /**
     * Taps on the <b>element</b>
     * <br>
     * The atomic actions performed are:
     * <ol>
     * <li>press on the element</li>
     * <li>release</li>
     * </ol>
     * If the <b>element</b> is not visible, the tap is performed using coordinates
     * @param element element to tap, must not be {@code null}
     */
    public void tap(WebElement element)
    {
        buildTapAction(element, TouchAction::tap, TouchAction::tap).perform();
    }

    /**
     * Taps on the <b>element</b> with specified <b>duration</b>
     * <br>
     * The atomic actions performed are:
     * <ol>
     * <li>press on the element</li>
     * <li>wait for the duration</li>
     * <li>release</li>
     * </ol>
     * If the <b>element</b> is not visible, the tap is performed using coordinates
     * @param element element to tap, must not be {@code null}
     * @param duration between an element is pressed and released in
     * <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> format, must not be {@code null}
     */
    public void tap(WebElement element, Duration duration)
    {
        buildTapAction(element, TouchAction::press, TouchAction::press)
                .waitAction(waitOptions(duration))
                .release()
                .perform();
    }

    //CHECKSTYLE:OFF
    /**
     * Performs double tap on the <b>element</b>
     * See details for
     * <a href="https://github.com/appium/appium-xcuitest-driver#mobile-doubletap">iOS</a>,
     * <a href="https://github.com/appium/appium-uiautomator2-driver/blob/master/docs/android-mobile-gestures.md#mobile-doubleclickgesture">Android</a>
     * @param element element to double tap, must not be {@code null}
     */
    //CHECKSTYLE:ON
    public void doubleTap(WebElement element)
    {
        String script;
        if (genericWebDriverManager.isAndroid())
        {
            script = "mobile: doubleClickGesture";
        }
        else if (genericWebDriverManager.isIOS())
        {
            script = "mobile: doubleTap";
        }
        else
        {
            throw new IllegalArgumentException("Double tap action is available only for Android and iOS platforms");
        }
        Map<String, Object> args = Map.of("elementId", WebDriverUtils.unwrap(element, RemoteWebElement.class).getId());
        webDriverProvider.getUnwrapped(JavascriptExecutor.class).executeScript(script, args);
    }

    private TouchAction<?> buildTapAction(WebElement element, BiConsumer<TouchAction<?>, ElementOption> tapByElement,
            BiConsumer<TouchAction<?>, PointOption<?>> tapByCoordinates)
    {
        TouchAction<?> touchActions = newTouchActions();
        // Workaround for known Appium/iOS/XCUITest issue:
        // https://github.com/appium/appium/issues/4131#issuecomment-64504187
        // https://discuss.appium.io/t/ios-visible-false-event-elements-are-visible-on-screen/27630/6
        if (!element.isDisplayed())
        {
            Point elementCenter = getCenter(element);
            if (isInViewport(elementCenter))
            {
                tapByCoordinates.accept(touchActions, point(elementCenter));
                return touchActions;
            }
        }
        tapByElement.accept(touchActions, element(WebDriverUtils.unwrap(element, RemoteWebElement.class)));
        return touchActions;
    }

    /**
     * Swipes in <b>swipeDuration</b> direction until one of the following conditions happen:
     * <ul>
     * <li>the <b>stopCondition</b> return true result</li>
     * <li>the end of mobile scroll view is reached</li>
     * <li>the swipe limit is exceeded</li>
     * </ul>
     * @param direction     direction to swipe, either <b>UP</b> or <b>DOWN</b>
     * @param swipeDuration duration between a pointer moves from the start to the end of the swipe coordinates
     * @param swipeArea     the area to execute the swipe
     * @param stopCondition condition to stop swiping
     */
    public void swipeUntil(SwipeDirection direction, Duration swipeDuration, Rectangle swipeArea,
            BooleanSupplier stopCondition)
    {
        /*
         * mobile:scroll
         * iOS
         * - scroll to an element works only if the element resides in UP scroll direction
         * - if the element in DOWN scroll direction, the command still tries to find the element in UP scroll direction
         * - payload is {"element" : "hash", "toVisible" : true}
         * - see https://appium.io/docs/en/commands/mobile-command/#ios
         * Android
         * - supports only accessibility id, uiautomator strategies
         * - payload is {"strategy": "accessibility id", "selector": "selector"}
         * - see https://appium.io/docs/en/commands/mobile-command/#android-uiautomator2-only
         * */
        uiContext.getOptionalSearchContext()
                 .map(this::createScreenShooter)
                 .ifPresent(screenShooter -> {
                     Duration stabilizationDuration = mobileApplicationConfiguration.getSwipeStabilizationDuration();
                     int swipeLimit = mobileApplicationConfiguration.getSwipeLimit();
                     BufferedImage previousFrame = null;
                     SwipeCoordinates swipeCoordinates = direction.calculateCoordinates(swipeArea,
                             mobileApplicationConfiguration);
                     for (int count = 0; count <= swipeLimit; count++)
                     {
                         swipe(swipeCoordinates, swipeDuration);
                         Sleeper.sleep(stabilizationDuration);
                         if (stopCondition.getAsBoolean())
                         {
                             break;
                         }
                         BufferedImage currentFrame = screenShooter.get();
                         if (previousFrame != null && ImageTool.equalImage(currentFrame).matches(previousFrame))
                         {
                             break;
                         }
                         previousFrame = currentFrame;
                         if (count == swipeLimit)
                         {
                             throw new IllegalStateException(
                                     String.format("Swiping is stopped due to exceeded swipe limit '%d'", swipeLimit));
                         }
                     }
                 });
    }

    private Supplier<BufferedImage> createScreenShooter(SearchContext searchContext)
    {
        if (searchContext instanceof WebDriver)
        {
            int screenshotHeightToCompare = genericWebDriverManager.getSize().getHeight() / HEIGHT_DIVIDER;
            int heightOffset = screenshotHeightToCompare * 2;
            return () -> {
                try
                {
                    BufferedImage area = screenshotTaker.takeViewportScreenshot();
                    return area.getSubimage(0, area.getHeight() - heightOffset, area.getWidth(),
                            screenshotHeightToCompare);
                }
                catch (IOException e)
                {
                    throw new UncheckedIOException(e);
                }
            };
        }
        else
        {
            return () -> screenshotTaker.takeAshotScreenshot(searchContext, Optional.empty()).getImage();
        }
    }

    /**
     * Performs vertical swipe from <b>startY</b> to <b>endY</b> with <b>swipeDuration</b>
     *
     * @param startY        start Y coordinate
     * @param endY          end Y coordinate
     * @param swipeArea     the area to execute the swipe
     * @param swipeDuration swipe duration in <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> format
     */
    public void performVerticalSwipe(int startY, int endY, Rectangle swipeArea, Duration swipeDuration)
    {
        swipe(SwipeDirection.createCoordinates(startY, endY, swipeArea.getWidth(),
                mobileApplicationConfiguration.getSwipeVerticalXPosition(), swipeArea.getPoint()), swipeDuration);
    }

    private void swipe(SwipeCoordinates coordinates, Duration swipeDuration)
    {
        newTouchActions()
                .press(point(coordinates.getStart()))
                .waitAction(waitOptions(swipeDuration))
                .moveTo(point(coordinates.getEnd()))
                .release()
                .perform();
    }

    private static Point getCenter(WebElement element)
    {
        Point upperLeft = element.getLocation();
        Dimension size = element.getSize();
        return new Point(upperLeft.x + size.getWidth() / 2, upperLeft.y + size.getHeight() / 2);
    }

    private boolean isInViewport(Point point)
    {
        Dimension viewport = genericWebDriverManager.getSize();
        return isValueInInterval(point.x, viewport.getWidth()) && isValueInInterval(point.y, viewport.getHeight());
    }

    private static boolean isValueInInterval(int value, int rightEnd)
    {
        return 0 <= value && value < rightEnd;
    }

    private TouchAction<?> newTouchActions()
    {
        return new TouchAction<>(webDriverProvider.getUnwrapped(PerformsTouchActions.class));
    }
}
