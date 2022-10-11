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

package org.vividus.steps.ui;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matcher;
import org.jbehave.core.annotations.Then;
import org.openqa.selenium.WebElement;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.ui.model.StringSortingOrder;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.State;
import org.vividus.ui.action.ElementActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;

@TakeScreenshotOnFailure
public class GenericElementSteps
{
    private final IBaseValidations baseValidations;
    private final ElementActions elementActions;
    private final ISoftAssert softAssert;

    public GenericElementSteps(IBaseValidations baseValidations, ElementActions elementActions, ISoftAssert softAssert)
    {
        this.baseValidations = baseValidations;
        this.elementActions = elementActions;
        this.softAssert = softAssert;
    }

    /**
     * Checks whether the context contains exact number of elements by locator
     *
     * @param locator        Locator to locate element
     * @param comparisonRule The rule to match the number of elements. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param number         The expected number of elements
     * @return list of Web elements
     */
    @Then("number of elements found by `$locator` is $comparisonRule `$number`")
    public List<WebElement> assertElementsNumber(Locator locator, ComparisonRule comparisonRule, int number)
    {
        return baseValidations.assertIfNumberOfElementsFound("The number of found elements", locator, number,
                comparisonRule);
    }

    /**
     * Verifies elements' located by locator state.
     * Where state one of: ENABLED/DISABLED, SELECTED/NOT_SELECTED, VISIBLE/NOT_VISIBLE
     * Step intended to verify strictly either number of elements and their state
     * <p><i>In case when locator's visibility and checked state are equal (For an example ':i' and 'NOT_VISIBLE')
     * exception will be thrown. In such cases please use step:
     * 'Then number of elements found by `$locator` is $comparisonRule `$number`'.
     * Contradictory visibility parameters (locator - ':i' and checked state - 'VISIBLE') are also not allowed.</i></p>
     *
     * @param state          Desired state of an element
     * @param locator        Locator to locate element
     * @param comparisonRule The rule to match the number of elements. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param number         The expected number of elements
     */
    @Then("number of $state elements found by `$locator` is $comparisonRule `$number`")
    public void assertElementsNumberInState(State state, Locator locator, ComparisonRule comparisonRule, int number)
    {
        Visibility visibility = locator.getSearchParameters().getVisibility();
        if (visibility != Visibility.ALL && (state == State.VISIBLE || state == State.NOT_VISIBLE))
        {
            String errorMessage = String.format(
                    state == visibility.getState()
                            ? "Locator visibility: %s and the state: %s to validate are the same. This makes no sense. "
                                    + "Please consider validation of elements size instead."
                            : "Contradictory input parameters. Locator visibility: '%s', the state: '%s'.",
                    visibility, state);
            throw new IllegalArgumentException(errorMessage);
        }

        String description = "Element is " + state;
        assertElementsNumber(locator, comparisonRule, number)
                .forEach(e -> baseValidations.assertElementState(description, state, e));
    }

    /**
     * Verifies if the elements located by the <b>locator</b> are sorted in <b>sortingOrder</b> order by their text
     * @param locator locator to locate elements
     * @param sortingOrder sorting order, can be either <b>ASCENDING</b> or <b>DESCENDING</b> or
     * <b>CASE_INSENSITIVE_ASCENDING</b> or <b>CASE_INSENSITIVE_DESCENDING</b>
     */
    @Then("elements located by `$locator` are sorted by text in $sortingOrder order")
    public void areElementSorted(Locator locator, StringSortingOrder sortingOrder)
    {
        ComparisonRule rule = ComparisonRule.GREATER_THAN;
        int requiredNumber = 1;

        List<WebElement> elements = baseValidations.assertNumberOfElementsFound("The elements to check the sorting",
                locator, requiredNumber, rule);

        Matcher<Integer> requiredSizeMatcher = rule.getComparisonRule(requiredNumber);
        if (requiredSizeMatcher.matches(elements.size()))
        {
            List<String> texts = new ArrayList<>();
            for (int index = 0; index < elements.size(); index++)
            {
                String text = elementActions.getElementText(elements.get(index));
                if (softAssert.assertTrue(String.format("The element with index %d contains not empty text", index + 1),
                        isNotEmpty(text)))
                {
                    texts.add(text);
                }
            }

            if (requiredSizeMatcher.matches(texts.size()))
            {
                softAssert.assertEquals(
                        String.format("The elements are sorted in %s order", sortingOrder.name().toLowerCase()),
                        texts.stream().sorted(sortingOrder.getSortingType()).collect(toList()), texts);
            }
            else
            {
                softAssert.recordFailedAssertion("There are not enough elements with text to check sorting: " + texts);
            }
        }
    }
}
