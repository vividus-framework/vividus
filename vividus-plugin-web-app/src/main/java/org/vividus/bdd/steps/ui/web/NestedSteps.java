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

package org.vividus.bdd.steps.ui.web;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.inject.Inject;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.hamcrest.Matcher;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.SubSteps;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.context.SearchContextSetter;
import org.vividus.ui.web.action.ICssSelectorFactory;
import org.vividus.ui.web.action.search.WebLocatorType;

@SuppressWarnings("MagicNumber")
@TakeScreenshotOnFailure
public class NestedSteps
{
    @Inject private IUiContext uiContext;
    @Inject private IBaseValidations baseValidations;
    @Inject private ISearchActions searchActions;
    @Inject private ISoftAssert softAssert;
    @Inject private ICssSelectorFactory cssSelectorFactory;

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
     * @param stepsToExecute Examples table with steps to execute for each found elements
     */
    @When(value = "I find $comparisonRule `$number` elements by `$locator` and for each element do$stepsToExecute",
            priority = 5)
    @Alias("I find $comparisonRule '$number' elements by $locator and for each element do$stepsToExecute")
    public void performAllStepsForElementIfFound(ComparisonRule comparisonRule, int number, Locator locator,
            SubSteps stepsToExecute)
    {
        List<WebElement> elements = baseValidations
                .assertIfNumberOfElementsFound("Elements to iterate with steps", locator, number, comparisonRule);
        if (!elements.isEmpty())
        {
            List<String> cssSelectors = cssSelectorFactory.getCssSelectors(elements).collect(Collectors.toList());
            runStepsWithContextReset(() ->
            {
                uiContext.putSearchContext(elements.get(0), () -> { });
                stepsToExecute.execute(Optional.empty());
            });
            IntStream.range(1, cssSelectors.size()).forEach(i -> {
                WebElement element = baseValidations
                        .assertIfElementExists("An element for iteration " + (i + 1),
                                new Locator(WebLocatorType.CSS_SELECTOR, cssSelectors.get(i)));
                runStepsWithContextReset(() ->
                {
                    uiContext.putSearchContext(element, () -> { });
                    stepsToExecute.execute(Optional.empty());
                });
            });
        }
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

    private void runStepsWithContextReset(Runnable subStepExecutor)
    {
        SearchContextSetter contextSetter = uiContext.getSearchContextSetter();
        try
        {
            subStepExecutor.run();
        }
        finally
        {
            contextSetter.setSearchContext();
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
