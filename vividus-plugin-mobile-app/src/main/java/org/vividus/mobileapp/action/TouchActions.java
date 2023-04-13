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

package org.vividus.mobileapp.action;

import static io.appium.java_client.touch.WaitOptions.waitOptions;
import static io.appium.java_client.touch.offset.ElementOption.element;
import static io.appium.java_client.touch.offset.PointOption.point;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.List;
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
import org.openqa.selenium.interactions.Interactive;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.remote.RemoteWebElement;
import org.vividus.mobileapp.configuration.MobileApplicationConfiguration;
import org.vividus.mobileapp.configuration.SwipeConfiguration;
import org.vividus.mobileapp.configuration.ZoomConfiguration;
import org.vividus.mobileapp.model.MoveCoordinates;
import org.vividus.mobileapp.model.SwipeDirection;
import org.vividus.mobileapp.model.ZoomCoordinates;
import org.vividus.mobileapp.model.ZoomType;
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
    private static final int MOVE_FINGER_DURATION_MS = 200;

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
                     SwipeConfiguration swipeConfiguration = mobileApplicationConfiguration.getSwipeConfiguration();
                     Duration stabilizationDuration = swipeConfiguration.getSwipeStabilizationDuration();
                     int swipeLimit = swipeConfiguration.getSwipeLimit();
                     BufferedImage previousFrame = null;

                     Rectangle adjustedSwipeArea = adjustSwipeArea(swipeArea);

                     MoveCoordinates swipeCoordinates = direction.calculateCoordinates(adjustedSwipeArea,
                             swipeConfiguration);
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
     * Make sure swipe area is not outside screen
     * @param swipeArea Desired swipe area
     * @return Adjusted with screen size swipe area
     */
    private Rectangle adjustSwipeArea(Rectangle swipeArea)
    {
        Dimension screenSize = genericWebDriverManager.getSize();
        int x = Math.max(swipeArea.getX(), 0);
        int y = Math.max(swipeArea.getY(), 0);
        int height = Math.min(swipeArea.getHeight(), screenSize.getHeight());
        int width = Math.min(swipeArea.getWidth(), screenSize.getWidth());
        return new Rectangle(x, y, height, width);
    }

    /**
     * Performs vertical or horizontal swipe from <b>start</b> to <b>end</b> coordinate with <b>swipeDuration</b>
     *
     * @param startCoordinate   start Y coordinate for the vertical direction or
     *                          start X coordinate for the horizontal direction
     * @param endCoordinate     end Y coordinate for the vertical direction or
     *                          end X coordinate for the horizontal direction
     * @param swipeArea         the area to execute the swipe
     * @param direction         direction to swipe, either <b>UP</b> or <b>DOWN</b> or <b>LEFT</b> or <b>RIGHT</b>
     * @param swipeDuration     swipe duration in <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> format
     */
    public void performSwipe(SwipeDirection direction, int startCoordinate, int endCoordinate, Rectangle swipeArea,
            Duration swipeDuration)
    {
        MoveCoordinates coordinates = direction.createCoordinates(startCoordinate, endCoordinate,
                mobileApplicationConfiguration.getSwipeConfiguration(), swipeArea);
        swipe(coordinates, swipeDuration);
    }

    /**
     * Performs zoom in/out
     *
     * @param zoomType type of zoom, either <b>IN</b> or <b>OUT</b>
     * @param contextArea the area to perform zoom
     */
    public void performZoom(ZoomType zoomType, Rectangle contextArea)
    {
        Rectangle zoomArea = calculateZoomArea(contextArea);
        ZoomCoordinates zoomCoordinates = zoomType.calculateCoordinates(zoomArea);
        Sequence moveFinger1Sequence = getFingerMoveSequence("finger1", zoomCoordinates.getFinger1MoveCoordinates());
        Sequence moveFinger2Sequence = getFingerMoveSequence("finger2", zoomCoordinates.getFinger2MoveCoordinates());
        webDriverProvider.getUnwrapped(Interactive.class).perform(List.of(moveFinger1Sequence, moveFinger2Sequence));
    }

    @SuppressWarnings("MagicNumber")
    private Rectangle calculateZoomArea(Rectangle contextArea)
    {
        ZoomConfiguration zoomConfiguration = mobileApplicationConfiguration.getZoomConfiguration();
        int leftIndent = zoomConfiguration.getLeftIndent();
        int topIndent = zoomConfiguration.getTopIndent();

        int widthInPercentage = 100 - (leftIndent + zoomConfiguration.getRightIndent());
        int zoomAreaWidth = contextArea.getWidth() * widthInPercentage / 100;

        int heightInPercentage = 100 - (topIndent + zoomConfiguration.getBottomIndent());
        int zoomAreaHeight = contextArea.getHeight() * heightInPercentage / 100;

        int leftIndentPx = contextArea.getWidth() * leftIndent / 100;
        int zoomAreaX = contextArea.getX() + leftIndentPx;

        int topIndentPx = contextArea.getHeight() * topIndent / 100;
        int zoomAreaY = contextArea.getY() + topIndentPx;
        return new Rectangle(zoomAreaX, zoomAreaY, zoomAreaHeight, zoomAreaWidth);
    }

    private Sequence getFingerMoveSequence(String pointerName, MoveCoordinates moveCoordinates)
    {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, pointerName);
        Sequence fingerMove = new Sequence(finger, 0);
        fingerMove.addAction(finger.createPointerMove(Duration.ofSeconds(0), PointerInput.Origin.viewport(),
                moveCoordinates.getStart()));
        fingerMove.addAction(finger.createPointerDown(0));
        fingerMove.addAction(finger.createPointerMove(Duration.ofMillis(MOVE_FINGER_DURATION_MS),
                PointerInput.Origin.viewport(), moveCoordinates.getEnd()));
        fingerMove.addAction(finger.createPointerUp(0));
        return fingerMove;
    }

    private void swipe(MoveCoordinates coordinates, Duration swipeDuration)
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
