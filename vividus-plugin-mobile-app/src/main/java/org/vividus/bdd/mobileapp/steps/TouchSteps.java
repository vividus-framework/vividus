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

package org.vividus.bdd.mobileapp.steps;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.mobileapp.model.SwipeDirection;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.mobileapp.action.KeyboardActions;
import org.vividus.mobileapp.action.TouchActions;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;

@TakeScreenshotOnFailure
public class TouchSteps
{
    private final TouchActions touchActions;
    private final KeyboardActions keyboardActions;
    private final IBaseValidations baseValidations;
    private final ISearchActions searchActions;

    public TouchSteps(TouchActions touchActions, KeyboardActions keyboardActions, IBaseValidations baseValidations,
            ISearchActions searchActions)
    {
        this.touchActions = touchActions;
        this.keyboardActions = keyboardActions;
        this.baseValidations = baseValidations;
        this.searchActions = searchActions;
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
    @When(value = "I tap on element located `$locator` with duration `$duration`", priority = 1)
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
    @When("I tap on element located `$locator`")
    public void tapByLocator(Locator locator)
    {
        findElementToTap(locator).ifPresent(touchActions::tap);
    }

    /**
     * Type <b>text</b> into the <b>element</b>
     * <br>
     * The atomic actions performed are:
     * <ol>
     * <li>type text into the element</li>
     * <li>hide keyboard</li>
     * </ol>
     * @param text text to type into the element
     * @param locator locator to find an element
     */
    @When("I type `$text` in field located `$locator`")
    public void typeTextInField(String text, Locator locator)
    {
        baseValidations.assertElementExists("The element to type text", locator)
                .ifPresent(e -> keyboardActions.typeText(e, text));
    }

    /**
     * Clear a field located by the <b>locator</b>
     * <br>
     * The atomic actions performed are:
     * <ol>
     * <li>clear the field</li>
     * <li>hide keyboard</li>
     * </ol>
     * @param locator locator to find a field
     */
    @When("I clear field located `$locator`")
    public void clearTextInField(Locator locator)
    {
        baseValidations.assertElementExists("The element to clear", locator).ifPresent(keyboardActions::clearText);
    }

    /**
     * Swipes to element in <b>direction</b> direction with duration <b>duration</b>
     * @param direction direction to swipe, either <b>UP</b> or <b>DOWN</b>
     * @param locator locator to find an element
     * @param duration swipe duration in <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> format
     */
    @When("I swipe $direction to element located `$locator` with duration $swipeDuration")
    public void swipeToElement(SwipeDirection direction, Locator locator, Duration duration)
    {
        locator.getSearchParameters().setWaitForElement(false);

        List<WebElement> elements = searchActions.findElements(locator);
        if (elements.isEmpty())
        {
            touchActions.swipeUntil(direction, duration, () ->
            {
                elements.addAll(searchActions.findElements(locator));
                return !elements.isEmpty();
            });
        }

        baseValidations.assertElementsNumber(String.format("The element by locator %s exists", locator), elements,
                ComparisonRule.EQUAL_TO, 1);
    }

    private Optional<WebElement> findElementToTap(Locator locator)
    {
        return baseValidations.assertElementExists("The element to tap", locator);
    }
}
