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

import java.util.List;

import org.jbehave.core.annotations.Then;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.ui.State;
import org.vividus.ui.action.search.Locator;

public class GenericElementSteps
{
    private final IBaseValidations baseValidations;

    public GenericElementSteps(IBaseValidations baseValidations)
    {
        this.baseValidations = baseValidations;
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
        String description = "Element is " + state;
        assertElementsNumber(locator, comparisonRule, quantity)
                .forEach(e -> baseValidations.assertElementState(description, state, e));
    }
}
