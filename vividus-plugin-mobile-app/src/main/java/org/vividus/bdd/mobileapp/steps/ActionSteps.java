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
import java.util.Optional;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.mobileapp.action.TapActions;
import org.vividus.ui.action.search.Locator;

public class ActionSteps
{
    private final TapActions tapActions;
    private final IBaseValidations baseValidations;

    public ActionSteps(TapActions tapActions, IBaseValidations baseValidations)
    {
        this.tapActions = tapActions;
        this.baseValidations = baseValidations;
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
    @When("I tap on element located `$locator` with duration `$duration`")
    public void tapByLocatorWithDuration(Locator locator, Duration duration)
    {
        findElementToTap(locator).ifPresent(e -> tapActions.tap(e, duration));
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
        findElementToTap(locator).ifPresent(tapActions::tap);
    }

    private Optional<WebElement> findElementToTap(Locator locator)
    {
        return baseValidations.assertElementExists("The element to tap", locator);
    }
}
