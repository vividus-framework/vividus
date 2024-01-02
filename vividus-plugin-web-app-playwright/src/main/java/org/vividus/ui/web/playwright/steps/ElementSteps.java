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

import org.jbehave.core.annotations.Then;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

public class ElementSteps
{
    private final UiContext uiContext;
    private final ISoftAssert softAssert;

    public ElementSteps(UiContext uiContext, ISoftAssert softAssert)
    {
        this.uiContext = uiContext;
        this.softAssert = softAssert;
    }

    /**
     * Validates the context contains the number of elements matching the specified comparison rule.
     *
     * @param locator        The locator used to find elements.
     * @param comparisonRule The rule to match the number of elements. The supported rules:
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
    @Then("number of elements found by `$locator` is $comparisonRule `$number`")
    public void assertElementsNumber(PlaywrightLocator locator, ComparisonRule comparisonRule, int number)
    {
        int actualNumberOfElements = uiContext.locateElement(locator).count();
        boolean matches = comparisonRule.getComparisonRule(number).matches(actualNumberOfElements);
        StringBuilder assertionMessage = new StringBuilder("The number of elements found by ")
                .append('\'').append(locator).append('\'')
                .append(" is ").append(actualNumberOfElements);
        if (comparisonRule == ComparisonRule.EQUAL_TO)
        {
            if (!matches)
            {
                assertionMessage.append(", but ").append(number).append(' ')
                        .append(number == 1 ? "was" : "were")
                        .append(" expected");
            }
        }
        else
        {
            assertionMessage.append(", ")
                    .append(matches ? "it is" : "but it is not").append(' ')
                    .append(comparisonRule).append(' ').append(number);
        }
        softAssert.recordAssertion(matches, assertionMessage.toString());
    }
}
