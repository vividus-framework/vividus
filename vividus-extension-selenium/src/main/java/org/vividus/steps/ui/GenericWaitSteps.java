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

package org.vividus.steps.ui;

import java.time.Duration;
import java.util.List;
import java.util.function.Function;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.hamcrest.Matcher;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.Point;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.State;
import org.vividus.ui.action.IExpectedConditions;
import org.vividus.ui.action.IExpectedSearchContextCondition;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.IWaitActions;
import org.vividus.ui.action.WaitResult;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;

@TakeScreenshotOnFailure
public class GenericWaitSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(GenericWaitSteps.class);

    private final IWaitActions waitActions;
    private final IUiContext uiContext;
    private final IExpectedConditions<Locator> expectedSearchActionsConditions;
    private final ISearchActions searchActions;
    private final ISoftAssert softAssert;
    private final IBaseValidations baseValidations;

    public GenericWaitSteps(IWaitActions waitActions, IUiContext uiContext,
            IExpectedConditions<Locator> expectedSearchActionsConditions, ISearchActions searchActions,
            ISoftAssert softAssert, IBaseValidations baseValidations)
    {
        this.waitActions = waitActions;
        this.uiContext = uiContext;
        this.expectedSearchActionsConditions = expectedSearchActionsConditions;
        this.searchActions = searchActions;
        this.softAssert = softAssert;
        this.baseValidations = baseValidations;
    }

    /**
     * Waits for appearance of an <b><i>element</i></b> with the specified <b>locator</b>
     * Step supports only <b>VISIBLE</b> elements waiting. If locator will be configured to <b>ALL</b>
     * or <b>INVISIBLE</b> exception will be thrown.
     * @param locator locator to locate element
     */
    @When("I wait until element located by `$locator` appears")
    public void waitForElementAppearance(Locator locator)
    {
        waitForConditionValidatingVisibility(locator, expectedSearchActionsConditions::visibilityOfElement);
    }

    /**
     * Waits for disappearance of an <b><i>element</i></b> with the specified <b>locator</b>
     * Step supports only <b>VISIBLE</b> elements waiting. If locator will be configured to <b>ALL</b>
     * or <b>INVISIBLE</b> exception will be thrown.
     * @param locator locator to locate element
     */
    @When("I wait until element located by `$locator` disappears")
    public void waitForElementDisappearance(Locator locator)
    {
        waitForConditionValidatingVisibility(locator, expectedSearchActionsConditions::invisibilityOfElement);
    }

    private <T> void waitForConditionValidatingVisibility(Locator locator,
            Function<Locator, IExpectedSearchContextCondition<T>> conditionFactory)
    {
        Validate.isTrue(Visibility.VISIBLE == locator.getSearchParameters().getVisibility(),
            "The step supports locators with VISIBLE visibility settings only, but the locator is `%s`",
                locator.toHumanReadableString());
        waitForCondition(conditionFactory.apply(locator));
    }

    /**
     * Waits <b>duration</b> with <b>pollingDuration</b> until <b>an element</b> by the specified <b>locator</b>
     * becomes a <b>state</b> in the specified search context
     * <p>
     * Actions performed at this step:
     * </p>
     * <ul>
     * <li><i>Finds</i> an element by the specified <b>locator</b> within search context
     * <li><i>Waits</i> <b>duration</b> with <b>pollingDuration</b> until this element becomes <b>state</b>
     * </ul>
     * <br>Example:<br>
     * <code>When I wait 'PT30S' with 'PT10S' polling until element located `By.id(text)` becomes NOT_VISIBLE</code>
     * - wait until all elements with id=text becomes not visible for 30 seconds, polling every 10 seconds
     *
     * @param duration        Total waiting time according to
     *                        <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> standard
     * @param pollingDuration Defines the timeout between attempts according to
     *                        <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a> standard
     * @param locator         Locator to search for elements
     * @param state           State value of the element
     * (<i>Possible values:</i> <b>ENABLED, DISABLED, SELECTED, NOT_SELECTED, VISIBLE, NOT_VISIBLE</b>)
     * @return True if element becomes a <b>state</b>, otherwise false
     */
    @When("I wait `$duration` with `$pollingDuration` polling until element located by `$locator` becomes $state")
    public boolean waitDurationWithPollingDurationTillElementState(Duration duration, Duration pollingDuration,
                                                                   Locator locator, State state)
    {
        return waitActions.wait(uiContext.getSearchContext(), duration, pollingDuration,
                state.getExpectedCondition(expectedSearchActionsConditions, locator)).isWaitPassed();
    }

    /**
     * Waits for expected number of elements.
     * @param locator        The locator to find elements.
     * @param comparisonRule The rule to match the variable value. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param number         The expected number of elements.
     */
    @When("I wait until number of elements located by `$locator` is $comparisonRule $number")
    public void waitForElementNumber(Locator locator, ComparisonRule comparisonRule, int number)
    {
        uiContext.getOptionalSearchContext().ifPresent(context -> {
            locator.getSearchParameters().setWaitForElement(false);
            Matcher<Integer> rule = comparisonRule.getComparisonRule(number);
            IExpectedSearchContextCondition<Boolean> condition = new IExpectedSearchContextCondition<>()
            {
                @Override
                public Boolean apply(SearchContext searchContext)
                {
                    return rule.matches(searchActions.findElements(context, locator).size());
                }

                @Override
                public String toString()
                {
                    return "number of elements located by \"" + locator.toHumanReadableString() + "\" is "
                            + comparisonRule + " " + number;
                }
            };
            waitActions.wait(context, condition);
        });
    }

    /**
     * Waits for an <b><i>element</i></b> with the specified <b>locator</b> to stop moving
     * @param locator locator to locate element
     */
    @When("I wait until element located by `$locator` stops moving")
    public void waitForElementStopsMoving(Locator locator)
    {
        uiContext.getOptionalSearchContext().ifPresent(context ->
            baseValidations.assertElementExists("The element to validate stop of its movement",
                    context, locator).ifPresent(webElement -> {
                        locator.getSearchParameters().setWaitForElement(false);
                        IExpectedSearchContextCondition<Boolean> condition = new IExpectedSearchContextCondition<>()
                        {
                            private Point lastPosition;
                            private boolean retryOnStaleAttempt;
                            private WebElement movingWebElement = webElement;

                            @Override
                            public Boolean apply(SearchContext context)
                            {
                                try
                                {
                                    Point currentPosition = movingWebElement.getLocation();
                                    boolean elementStoppedMoving = currentPosition.equals(lastPosition);
                                    if (!elementStoppedMoving)
                                    {
                                        LOGGER.info("The element is moved at '{}'", currentPosition);
                                    }
                                    lastPosition = currentPosition;
                                    return elementStoppedMoving;
                                }
                                catch (StaleElementReferenceException e)
                                {
                                    if (!retryOnStaleAttempt)
                                    {
                                        List<WebElement> movingWebElements = searchActions
                                                .findElements(context, locator);
                                        if (movingWebElements.size() == 1)
                                        {
                                            movingWebElement = movingWebElements.get(0);
                                            retryOnStaleAttempt = true;
                                            return false;
                                        }
                                    }
                                    throw e;
                                }
                            }

                            @Override
                            public String toString()
                            {
                                return String.format("element located by \"%s\" stopped moving",
                                        locator.toHumanReadableString());
                            }
                        };
                        waitActions.wait(context, condition);
                    })
        );
    }

    /**
     * Validates the element located by the locator has existed for the period specified by the duration.
     * The actions performed by the step:
     * <ul>
     * <li>check the search context is set,</li>
     * <li>search for the element to validate existence (this search may include wait for element appearance if it's
     * configured),</li>
     * <li>validate the element has presented for the period specified by the duration.</li>
     * </ul>
     *
     * @param locator  The locator to find an element.
     * @param duration The period of time the element should have existed in
     *                 <a href="https://en.wikipedia.org/wiki/ISO_8601">ISO 8601</a>
     *                 format.
     */
    @Then("element located by `$locator` exists for `$duration` duration")
    public void doesElementByLocatorExistsForDuration(Locator locator, Duration duration)
    {
        String prettyPrintedDuration = formatDuration(duration);
        uiContext.getOptionalSearchContext().ifPresent(searchContext ->
        {
            if (baseValidations.assertElementExists("The element to validate existence", searchContext, locator)
                    .isPresent())
            {
                locator.getSearchParameters().setWaitForElement(false);

                IExpectedSearchContextCondition<Boolean> condition = context -> searchActions.findElements(context,
                        locator).isEmpty();
                WaitResult<Boolean> result = waitActions.wait(searchContext, duration, condition, false);

                String assertionMessage = String.format("Element located by locator %s has existed for %s",
                        locator.toHumanReadableString(), prettyPrintedDuration);
                softAssert.assertTrue(assertionMessage, !result.isWaitPassed());
            }
        });
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
