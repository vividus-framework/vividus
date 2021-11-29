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

import java.util.Optional;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.hamcrest.Matcher;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.When;
import org.vividus.monitor.TakeScreenshotOnFailure;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.SubSteps;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.context.SearchContextSetter;

@SuppressWarnings("MagicNumber")
@TakeScreenshotOnFailure
public class GenericNestedSteps
{
    private final IUiContext uiContext;
    private final ISearchActions searchActions;
    private final ISoftAssert softAssert;

    public GenericNestedSteps(IUiContext uiContext, ISearchActions searchActions, ISoftAssert softAssert)
    {
        this.uiContext = uiContext;
        this.searchActions = searchActions;
        this.softAssert = softAssert;
    }

    /**
     * Steps designed to perform steps while specified condition persists
     * <b>if</b> condition changed cycle ends.
     * Actions performed by step:
     * <ul>
     * <li>1. Searches for elements using locator</li>
     * <li>2. Checks that elements quantity matches comparison rule and elements number</li>
     * <li>3. Performs steps</li>
     * <li>4. Restores previously set context</li>
     * <li>Repeat 1-4 until iteration limit reached or elements quantity changed</li>
     * </ul>
     * To avoid infinite loops used iterationLimit. If iteration's limit reached step will fail.
     * <br> Usage example:
     * <code>
     * <br>When I find = 1 elements By.xpath(.//*[contains(@class,'fancybox-wrap')]) and while elements
     * number persists do up to 5 iteration of
     * <br>|step|
     * <br>|When I compare against baseline with name 'test_composit1_step'|
     * <br>|When I click on all elements by xpath './/a[@title='Close']'|
     * </code>
     * @param comparisonRule The rule to match the quantity of elements. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param number         Number of elements to find
     * @param locator        Locator to locate element
     * @param iterationLimit Max iterations to perform
     * @param stepsToExecute Examples table with steps to execute for each found elements
     */
    @When(value = "I find $comparisonRule `$number` elements `$locator` and while they exist do up "
            + "to $iterationLimit iteration of$stepsToExecute", priority = 5)
    @Alias("I find $comparisonRule '$number' elements $locator and while they exist do up "
            + "to $iterationLimit iteration of$stepsToExecute")
    public void performAllStepsWhileElementsExist(ComparisonRule comparisonRule, int number, Locator locator,
            int iterationLimit, SubSteps stepsToExecute)
    {
        int iterationsCounter = iterationLimit;
        Matcher<Integer> elementNumberMatcher = comparisonRule.getComparisonRule(number);
        MutableBoolean firstIteration = new MutableBoolean(true);
        while (iterationsCounter > 0 && isExpectedElementsQuantity(locator, elementNumberMatcher, firstIteration))
        {
            SearchContextSetter contextSetter = uiContext.getSearchContextSetter();
            try
            {
                stepsToExecute.execute(Optional.empty());
            }
            finally
            {
                contextSetter.setSearchContext();
            }
            iterationsCounter--;
        }
        if (iterationsCounter == 0)
        {
            softAssert.recordFailedAssertion(
                    String.format("Elements number %s was not changed after %d iteration(s)",
                            elementNumberMatcher.toString(), iterationLimit));
        }
    }

    private boolean isExpectedElementsQuantity(Locator locator, Matcher<Integer> elementsMatcher,
            MutableBoolean firstIteration)
    {
        if (firstIteration.isTrue())
        {
            firstIteration.setValue(false);
            return softAssert.assertThat("Elements number", getElementsNumber(locator), elementsMatcher);
        }
        return elementsMatcher.matches(getElementsNumber(locator));
    }

    private int getElementsNumber(Locator locator)
    {
        return searchActions.findElements(uiContext.getSearchContext(), locator).size();
    }
}
