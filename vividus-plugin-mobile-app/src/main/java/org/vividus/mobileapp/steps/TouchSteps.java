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

package org.vividus.mobileapp.steps;

import static org.apache.commons.lang3.Validate.isTrue;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.Point;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.mobileapp.action.TouchActions;
import org.vividus.mobileapp.model.SwipeDirection;
import org.vividus.mobileapp.model.ZoomType;
import org.vividus.selenium.manager.GenericWebDriverManager;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;

@TakeScreenshotOnFailure
public class TouchSteps
{
    private static final float VISIBILITY_TOP_INDENT_COEFFICIENT = 0.15f;
    private static final float VISIBILITY_BOTTOM_INDENT_COEFFICIENT = 0.25f;

    private final TouchActions touchActions;
    private final IBaseValidations baseValidations;
    private final ISearchActions searchActions;
    private final GenericWebDriverManager genericWebDriverManager;
    private final IUiContext uiContext;

    public TouchSteps(TouchActions touchActions, IBaseValidations baseValidations,
            ISearchActions searchActions, GenericWebDriverManager genericWebDriverManager, IUiContext uiContext)
    {
        this.touchActions = touchActions;
        this.baseValidations = baseValidations;
        this.searchActions = searchActions;
        this.genericWebDriverManager = genericWebDriverManager;
        this.uiContext = uiContext;
    }

    /**
     * Taps on <b>element</b> located by <b>locator</b> with specified <b>duration</b>
     * <br>
     * The atomic actions performed are:
     * <ol>
     * <li>press on the element</li>
     * <li>wait for the duration</li>
     * <li>release</li>
     * </ol>
     * @param locator locator to find an element
     * @param duration between an element is pressed and released in
     * <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> format
     */
    @When(value = "I tap on element located by `$locator` with duration `$duration`", priority = 1)
    public void tapByLocatorWithDuration(Locator locator, Duration duration)
    {
        findElementToTap(locator).ifPresent(e -> touchActions.tap(e, duration));
    }

    /**
     * Taps on <b>element</b> located by <b>locator</b>
     * <br>
     * The atomic actions performed are:
     * <ol>
     * <li>press on the element</li>
     * <li>release</li>
     * </ol>
     * @param locator locator to find an element
     */
    @When("I tap on element located by `$locator`")
    public void tapByLocator(Locator locator)
    {
        findElementToTap(locator).ifPresent(touchActions::tap);
    }

    /**
     * Performs double tap on <b>element</b> located by <b>locator</b>
     * <br>
     * The step is supported for Android and iOS platforms
     * @param locator locator to find an element
     */
    @When("I double tap on element located by `$locator`")
    public void doubleTapByLocator(Locator locator)
    {
        isTrue(genericWebDriverManager.isMobile(), "The step is supported only for Android and iOS platforms");
        baseValidations.assertElementExists("The element to double tap", locator).ifPresent(touchActions::doubleTap);
    }

    /**
     * Swipes to element in <b>direction</b> direction with duration <b>duration</b>
     * The step takes into account current context. If you need to perform swipe on the element,
     * you need to switch the context to this element.
     * @param direction direction to swipe, either <b>UP</b> or <b>DOWN</b> or <b>LEFT</b> or <b>RIGHT</b>
     * @param locator locator to find an element
     * @param swipeDuration swipe duration in <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> format
     */
    @When("I swipe $direction to element located by `$locator` with duration $swipeDuration")
    public void swipeToElement(SwipeDirection direction, Locator locator, Duration swipeDuration)
    {
        locator.getSearchParameters().setWaitForElement(false);
        List<WebElement> elements = new ArrayList<>(searchActions.findElements(locator));
        Rectangle swipeArea = getSearchContextRectangle();
        if (!containsInteractableElement(elements, swipeArea, direction))
        {
            touchActions.swipeUntil(direction, swipeDuration, swipeArea, () ->
            {
                if (elements.isEmpty())
                {
                    elements.addAll(searchActions.findElements(locator));
                }
                return containsInteractableElement(elements, swipeArea, direction);
            });
        }
        if (baseValidations.assertElementsNumber(String.format("The element by locator %s exists", locator), elements,
                ComparisonRule.EQUAL_TO, 1) && (SwipeDirection.UP == direction || SwipeDirection.DOWN == direction))
        {
            adjustVerticalPosition(elements.get(0), direction, swipeArea, swipeDuration);
        }
    }

    /**
     * Performs zoom in/out
     * The step takes into account current context. If you need to perform zoom on the element,
     * you need to switch the context to this element.
     * @param zoomType type of zoom, either <b>IN</b>  or <b>OUT</b>
     */
    @When("I zoom $zoomType context")
    public void zoom(ZoomType zoomType)
    {
        touchActions.performZoom(zoomType, getSearchContextRectangle());
    }

    private Rectangle getSearchContextRectangle()
    {
        SearchContext searchContext = uiContext.getSearchContext();
        if (searchContext instanceof WebElement)
        {
            WebElement contextElement = (WebElement) searchContext;
            return contextElement.getRect();
        }
        return new Rectangle(new Point(0, 0), genericWebDriverManager.getSize());
    }

    private void adjustVerticalPosition(WebElement element, SwipeDirection direction, Rectangle swipeArea,
            Duration swipeDuration)
    {
        int swipeAreaSizeHeight = swipeArea.getHeight();
        int swipeAreaCenterY = swipeAreaSizeHeight / 2;
        int elementTopCoordinateY = element.getLocation().getY();

        int bottomVisibilityIndent = (int) (VISIBILITY_BOTTOM_INDENT_COEFFICIENT * swipeAreaSizeHeight);
        int visibilityY = swipeAreaSizeHeight - bottomVisibilityIndent;
        if (elementTopCoordinateY > visibilityY)
        {
            touchActions.performSwipe(direction, swipeAreaCenterY,
                    swipeAreaCenterY - (elementTopCoordinateY - visibilityY), swipeArea, swipeDuration);
            return;
        }

        int topVisibilityIndent = (int) (VISIBILITY_TOP_INDENT_COEFFICIENT * swipeAreaSizeHeight);
        if (elementTopCoordinateY < topVisibilityIndent)
        {
            touchActions.performSwipe(direction, swipeAreaCenterY,
                    swipeAreaCenterY + topVisibilityIndent - elementTopCoordinateY, swipeArea, swipeDuration);
        }
    }

    private Optional<WebElement> findElementToTap(Locator locator)
    {
        return baseValidations.assertElementExists("The element to tap", locator);
    }

    private boolean containsInteractableElement(List<WebElement> elements, Rectangle swipeAreaRectangle,
            SwipeDirection direction)
    {
        return !elements.isEmpty() && isElementInteractable(elements.get(0).getRect(), swipeAreaRectangle,
                direction);
    }

    private boolean isElementInteractable(Rectangle elementRectangle, Rectangle swipeArea, SwipeDirection direction)
    {
        int elementBoundaryCoordinate = getBoundaryCoordinate(elementRectangle, direction, direction.isBackward());
        int swipeAreaBoundaryCoordinate = getBoundaryCoordinate(swipeArea, direction, !direction.isBackward());
        if (direction.isBackward())
        {
            return elementBoundaryCoordinate < swipeAreaBoundaryCoordinate;
        }
        return elementBoundaryCoordinate > swipeAreaBoundaryCoordinate;
    }

    private int getBoundaryCoordinate(Rectangle elementRectangle, SwipeDirection direction, boolean lowerBoundary)
    {
        int coordinate = direction.isVertical() ? elementRectangle.getY() : elementRectangle.getX();
        return lowerBoundary ? coordinate : coordinate + direction.getAxisLength(elementRectangle);
    }
}
