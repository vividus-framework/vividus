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

package org.vividus.mobileapp.action;

import static io.appium.java_client.touch.WaitOptions.waitOptions;
import static io.appium.java_client.touch.offset.ElementOption.element;
import static io.appium.java_client.touch.offset.PointOption.point;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.function.BooleanSupplier;
import java.util.function.UnaryOperator;

import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebElement;
import org.vividus.bdd.mobileapp.configuration.MobileApplicationConfiguration;
import org.vividus.bdd.mobileapp.model.SwipeCoordinates;
import org.vividus.bdd.mobileapp.model.SwipeDirection;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.WebDriverUtil;
import org.vividus.selenium.manager.GenericWebDriverManager;
import org.vividus.selenium.screenshot.ScreenshotTaker;
import org.vividus.util.Sleeper;

import io.appium.java_client.PerformsTouchActions;
import io.appium.java_client.TouchAction;
import io.appium.java_client.touch.offset.ElementOption;
import ru.yandex.qatools.ashot.util.ImageTool;

@TakeScreenshotOnFailure
public class TouchActions
{
    private static final int HEIGHT_DIVIDER = 3;

    private final IWebDriverProvider webDriverProvider;
    private final GenericWebDriverManager genericWebDriverManager;
    private final ScreenshotTaker screenshotTaker;
    private final MobileApplicationConfiguration mobileApplicationConfiguration;

    public TouchActions(IWebDriverProvider webDriverProvider, GenericWebDriverManager genericWebDriverManager,
            ScreenshotTaker screenshotTaker, MobileApplicationConfiguration mobileApplicationConfiguration)
    {
        this.webDriverProvider = webDriverProvider;
        this.genericWebDriverManager = genericWebDriverManager;
        this.screenshotTaker = screenshotTaker;
        this.mobileApplicationConfiguration = mobileApplicationConfiguration;
    }

    /**
     * Taps on the <b>element</b>
     * <br>
     * The atomic actions performed are:
     * <ol>
     * <li>press on the element</li>
     * <li>release</li>
     * </ol>
     * @param element element to tap, must not be {@code null}
     */
    public void tap(WebElement element)
    {
        performAction(b -> b.tap(elementOption(element)));
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
     * @param element element to tap, must not be {@code null}
     * @param duration between an element is pressed and released in
     * <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> format, must not be {@code null}
     */
    public void tap(WebElement element, Duration duration)
    {
        performAction(b -> b.press(elementOption(element))
                            .waitAction(waitOptions(duration))
                            .release());
    }

    /**
     * Swipes in <b>swipeDuration</b> direction until one of the following conditions happen:
     * <ul>
     * <li>the <b>stopCondition</b> return true result</li>
     * <li>the end of mobile scroll view is reached</li>
     * <li>the swipe limit is exceeded</li>
     * </ul>
     * @param direction direction to swipe, either <b>UP</b> or <b>DOWN</b>
     * @param swipeDuration duration between a pointer moves from the start to the end of the swipe coordinates
     * @param stopCondition condition to stop swiping
     */
    public void swipeUntil(SwipeDirection direction, Duration swipeDuration, BooleanSupplier stopCondition)
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

        int screenshotHeightToCompare = genericWebDriverManager.getSize().getHeight() / HEIGHT_DIVIDER;
        int heightOffset = screenshotHeightToCompare * 2;

        Duration stabilizationDuration = mobileApplicationConfiguration.getSwipeStabilizationDuration();
        int swipeLimit = mobileApplicationConfiguration.getSwipeLimit();
        BufferedImage previousFrame = null;
        for (int count = 0; count <= swipeLimit; count++)
        {
            swipe(direction.calculateCoordinates(genericWebDriverManager.getSize()), swipeDuration);
            Sleeper.sleep(stabilizationDuration);
            if (stopCondition.getAsBoolean())
            {
                break;
            }
            BufferedImage area = takeScreenshot();
            BufferedImage currentFrame = area.getSubimage(0, area.getHeight() - heightOffset, area.getWidth(),
                    screenshotHeightToCompare);
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
    }

    /**
     * Performs vertical swipe from <b>startY</b> to <b>endY</b> with <b>swipeDuration</b>
     *
     * @param startY start Y coordinate
     * @param endY end Y coordinate
     * @param swipeDuration swipe duration in <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> format
     */
    public void performVerticalSwipe(int startY, int endY, Duration swipeDuration)
    {
        swipe(new SwipeCoordinates(1, startY, 1, endY), swipeDuration);
    }

    private BufferedImage takeScreenshot()
    {
        try
        {
            return screenshotTaker.takeViewportScreenshot();
        }
        catch (IOException e)
        {
            throw new UncheckedIOException(e);
        }
    }

    private void swipe(SwipeCoordinates coordinates, Duration swipeDuration)
    {
        performAction(b -> b.press(point(coordinates.getStart()))
                            .waitAction(waitOptions(swipeDuration))
                            .moveTo(point(coordinates.getEnd()))
                            .release());
    }

    private static ElementOption elementOption(WebElement webElement)
    {
        return element(WebDriverUtil.unwrap(webElement, RemoteWebElement.class));
    }

    private void performAction(UnaryOperator<TouchAction<?>> actionBuilder)
    {
        PerformsTouchActions performsTouchActions = webDriverProvider.getUnwrapped(PerformsTouchActions.class);
        TouchAction<?> touchAction = new TouchAction<>(performsTouchActions);
        actionBuilder.apply(touchAction).perform();
    }
}
