/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.steps.ui;

import java.time.Duration;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.vividus.monitor.TakeScreenshotOnFailure;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.IExpectedConditions;
import org.vividus.ui.action.IExpectedSearchContextCondition;
import org.vividus.ui.action.IWaitActions;
import org.vividus.ui.action.WaitResult;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;

@TakeScreenshotOnFailure
public class GenericWaitSteps
{
    private final IWaitActions waitActions;
    private final IUiContext uiContext;
    private final IExpectedConditions<Locator> expectedSearchActionsConditions;
    private final ISoftAssert softAssert;

    public GenericWaitSteps(IWaitActions waitActions, IUiContext uiContext,
            IExpectedConditions<Locator> expectedSearchActionsConditions, ISoftAssert softAssert)
    {
        this.waitActions = waitActions;
        this.uiContext = uiContext;
        this.expectedSearchActionsConditions = expectedSearchActionsConditions;
        this.softAssert = softAssert;
    }

    /**
     * Waits for appearance of an <b><i>element</i></b> with the specified <b>locator</b>
     * @param locator locator to locate element
     */
    @When("I wait until element located `$locator` appears")
    public void waitForElementAppearance(Locator locator)
    {
        waitForCondition(expectedSearchActionsConditions.visibilityOfElement(locator));
    }

    /**
     * Waits for disappearance of an <b><i>element</i></b> with the specified <b>locator</b>
     * @param locator locator to locate element
     */
    @When("I wait until element located `$locator` disappears")
    public void waitForElementDisappearance(Locator locator)
    {
        waitForCondition(expectedSearchActionsConditions.invisibilityOfElement(locator));
    }

    /**
     * Checks that element located by <b>locator</b> exists during the <b>duration</b>
     * @param locator locator to find an element
     * @param duration total waiting time according to <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a>
     * standard
     */
    @Then("element located `$locator` exists for `$duration` duration")
    public void doesElementByLocatorExistsForDuration(Locator locator, Duration duration)
    {
        String prettyDuration = formatDuration(duration);

        WaitResult<Boolean> result = waitActions.wait(uiContext.getSearchContext(), duration,
                expectedSearchActionsConditions
                        .not(expectedSearchActionsConditions.presenceOfAllElementsLocatedBy(locator)), false);

        String assertionMessage = String.format("Element located by locator %s has existed for %s",
                locator.toHumanReadableString(), prettyDuration);
        softAssert.assertFalse(assertionMessage, result.isWaitPassed());
    }

    private String formatDuration(Duration duration)
    {
        StringBuilder durationBuilder = new StringBuilder();
        appendIf(duration.toMinutesPart() != 0, "m' minutes' ", durationBuilder);
        appendIf(duration.toSecondsPart() != 0, "s' seconds' ", durationBuilder);
        appendIf(duration.toMillisPart() != 0, "S' millis'", durationBuilder);
        Validate.isTrue(durationBuilder.length() > 0, "Unable to convert duration %s", duration);
        return DurationFormatUtils.formatDuration(duration.toMillis(), durationBuilder.toString().strip());
    }

    private static void appendIf(boolean outcome, String value, StringBuilder builder)
    {
        if (outcome)
        {
            builder.append(value);
        }
    }

    private void waitForCondition(IExpectedSearchContextCondition<?> condition)
    {
        waitActions.wait(uiContext.getSearchContext(), condition);
    }
}
