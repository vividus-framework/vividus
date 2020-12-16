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

package org.vividus.bdd.steps.ui;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.hamcrest.Matcher;
import org.jbehave.core.annotations.Then;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.ui.model.StringSortingOrder;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.State;
import org.vividus.ui.action.ElementActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.Visibility;

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
     * Checks whether the context contains exact amount of elements by locator
     * @param locator to locate element
     * @param comparisonRule The rule to compare values
     * (<i>Possible values:<b> LESS_THAN, LESS_THAN_OR_EQUAL_TO, GREATER_THAN, GREATER_THAN_OR_EQUAL_TO,
     * EQUAL_TO</b></i>)
     * @param quantity desired amount of elements
     * @return list of Web elements
     */
    @Then("number of elements found by `$locator` is $comparisonRule `$quantity`")
    public List<WebElement> assertElementsNumber(Locator locator, ComparisonRule comparisonRule,
            int quantity)
    {
        return baseValidations.assertIfNumberOfElementsFound("The number of found elements", locator, quantity,
                comparisonRule);
    }

    /**
     * Verifies elements' located by locator state.
     * Where state one of: ENABLED/DISABLED, SELECTED/NOT_SELECTED, VISIBLE/NOT_VISIBLE
     * Step intended to verify strictly either number of elements and their state
     * @param state Desired state of an element
     * @param locator Locator to locate element
     * @param comparisonRule The rule to compare values
     * (<i>Possible values:<b> LESS_THAN, LESS_THAN_OR_EQUAL_TO, GREATER_THAN, GREATER_THAN_OR_EQUAL_TO,
     * EQUAL_TO</b></i>)
     * @param quantity Desired amount of elements
     */
    @Then("number of $state elements found by `$locator` is $comparisonRule `$quantity`")
    public void assertElementsNumberInState(State state, Locator locator, ComparisonRule comparisonRule, int quantity)
    {
        Visibility visibility = locator.getSearchParameters().getVisibility();
        Validate.isTrue(state != visibility.getState(),
            "Locator visibility: %s and the state: %s to validate are the same."
            + " This makes no sense. Please consider validation of elements size instead.", visibility, state);
        String description = "Element is " + state;
        assertElementsNumber(locator, comparisonRule, quantity)
                .forEach(e -> baseValidations.assertElementState(description, state, e));
    }

    /**
     * Verifies if the elements located by the <b>locator</b> are sorted in <b>sortingOrder</b> order by their text
     * @param locator locator to locate elements
     * @param sortingOrder sorting order, can be either <b>ASCENDING</b> or <b>DESCENDING</b> or
     * <b>CASE_INSENSITIVE_ASCENDING</b> or <b>CASE_INSENSITIVE_DESCENDING</b>
     */
    @Then("elements located `$locator` are sorted by text in $sortingOrder order")
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
                if (softAssert.assertTrue(format("The element with index %d contains not empty text", index + 1),
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
