/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.playwright.steps;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.hamcrest.Matcher;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.When;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.SubSteps;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.assertions.PlaywrightSoftAssert;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

public class NestedSteps
{
    private final UiContext uiContext;
    private final ISoftAssert softAssert;
    private final PlaywrightSoftAssert playwrightSoftAssert;

    public NestedSteps(UiContext uiContext, ISoftAssert softAssert, PlaywrightSoftAssert playwrightSoftAssert)
    {
        this.uiContext = uiContext;
        this.softAssert = softAssert;
        this.playwrightSoftAssert = playwrightSoftAssert;
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
     * <br>|When I click on element located by `xpath(.//a[@title='Close'])`|
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
    @When("I find $comparisonRule `$number` elements `$locator` and while they exist do up "
                  + "to $iterationLimit iteration of$stepsToExecute")
    @Alias("I find $comparisonRule '$number' elements $locator and while they exist do up "
           + "to $iterationLimit iteration of$stepsToExecute")
    public void performAllStepsWhileElementsExist(ComparisonRule comparisonRule, int number, PlaywrightLocator locator,
            int iterationLimit, SubSteps stepsToExecute)
    {
        int iterationsCounter = iterationLimit;
        Matcher<Integer> elementNumberMatcher = comparisonRule.getComparisonRule(number);
        MutableBoolean firstIteration = new MutableBoolean(true);
        while (iterationsCounter > 0 && isExpectedElementsQuantity(locator, elementNumberMatcher, firstIteration))
        {
            runStepsWithContextReset(() -> stepsToExecute.execute(Optional.empty()));
            iterationsCounter--;
        }
        if (iterationsCounter == 0)
        {
            softAssert.recordFailedAssertion(
                    String.format("Elements number %s was not changed after %d iteration(s)",
                            elementNumberMatcher.toString(), iterationLimit));
        }
    }

    /**
     * Steps designed to perform steps against all elements found by locator
     * <b>if</b> they are matching comparison rule.
     * Actions performed by step:
     * <ul>
     * <li>Searches for elements using locator</li>
     * <li>Checks that elements quantity matches comparison rule and elements number</li>
     * <li>For each element switches context and performs all steps. No steps will be performed
     * in case of comparison rule mismatch</li>
     * <li>Restores previously set context</li>
     * </ul>
     * <br> Usage example:
     * <code>
     * <br>When I find equal to 1 elements by By.xpath(.//*[contains(@class,'fancybox-wrap')]) and for each element do
     * <br>|step|
     * <br>|When I click on element located by `xpath(.//a[@title='Close'])`|
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
     * @param stepsToExecute Examples table with steps to execute for each found elements
     */
    @When("I find $comparisonRule `$number` elements by `$locator` and for each element do$stepsToExecute")
    @Alias("I find $comparisonRule '$number' elements by $locator and for each element do$stepsToExecute")
    public void executeStepsForAllLocatedElements(ComparisonRule comparisonRule, int number, PlaywrightLocator locator,
            SubSteps stepsToExecute)
    {
        Matcher<Integer> elementNumberMatcher = comparisonRule.getComparisonRule(number);
        List<Locator> elements = uiContext.locateElement(locator).all();
        softAssert.assertThat("Elements to iterate with steps", elements.size(), elementNumberMatcher);
        IntStream.range(0, elements.size()).forEach(i -> {
            Locator element = elements.get(i);
            playwrightSoftAssert.runAssertion(String.format("An element for iteration %d is not found", i + 1),
                    () -> PlaywrightAssertions.assertThat(element).hasCount(1));
            runStepsWithContextReset(() -> {
                uiContext.setContext(element);
                stepsToExecute.execute(Optional.empty());
            });
        });
    }

    private boolean isExpectedElementsQuantity(PlaywrightLocator locator, Matcher<Integer> elementsMatcher,
            MutableBoolean firstIteration)
    {
        int elementsNumber = uiContext.locateElement(locator).count();
        if (firstIteration.isTrue())
        {
            firstIteration.setValue(false);
            return softAssert.assertThat("Elements number", elementsNumber, elementsMatcher);
        }
        return elementsMatcher.matches(elementsNumber);
    }

    private void runStepsWithContextReset(Runnable subStepExecutor)
    {
        Locator originalContext = uiContext.getContext();
        try
        {
            subStepExecutor.run();
        }
        finally
        {
            uiContext.setContext(originalContext);
        }
    }
}
