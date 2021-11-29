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

package org.vividus.steps.ui.web;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebElement;
import org.vividus.monitor.TakeScreenshotOnFailure;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.SubSteps;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.context.SearchContextSetter;
import org.vividus.ui.web.action.ICssSelectorFactory;
import org.vividus.ui.web.action.search.WebLocatorType;

@SuppressWarnings("MagicNumber")
@TakeScreenshotOnFailure
public class NestedSteps
{
    private final IUiContext uiContext;
    private final IBaseValidations baseValidations;
    private final ICssSelectorFactory cssSelectorFactory;

    public NestedSteps(IUiContext uiContext, IBaseValidations baseValidations, ICssSelectorFactory cssSelectorFactory)
    {
        this.uiContext = uiContext;
        this.baseValidations = baseValidations;
        this.cssSelectorFactory = cssSelectorFactory;
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
}
