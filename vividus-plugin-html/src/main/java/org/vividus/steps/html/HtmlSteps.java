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

package org.vividus.steps.html;

import java.util.Optional;
import java.util.Set;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.vividus.context.VariableContext;
import org.vividus.html.LocatorType;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.util.HtmlUtils;
import org.vividus.variable.VariableScope;

public class HtmlSteps
{
    private final ISoftAssert softAssert;
    private final VariableContext variableContext;

    public HtmlSteps(ISoftAssert softAssert, VariableContext variableContext)
    {
        this.softAssert = softAssert;
        this.variableContext = variableContext;
    }

    private Optional<Element> assertElementByLocatorExists(String html, LocatorType locatorType, String locator)
    {
        Elements elements = HtmlUtils.getElements(html, locatorType, locator);
        if (assertElements(locatorType, locator, ComparisonRule.EQUAL_TO, 1, elements))
        {
            return Optional.of(elements.first());
        }
        return Optional.empty();
    }

    /**
     * Checks if HTML contains elements according to rule by locator
     * @param locatorType    <b>CSS selector</b> or <b>XPath</b>
     * @param locator        Locator to locate elements in HTML document
     * @param html           HTML to check
     * @param comparisonRule The rule to match the quantity of elements. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param number         Number of elements
     * @see <a href="https://www.w3schools.com/cssref/css_selectors.asp"><i>CSS Selector Reference</i></a>
     * @see <a href="https://jsoup.org/apidocs/org/jsoup/select/Selector.html"><i>Jsoup Selector API</i></a>
     * @return true if elements quantity corresponds to rule, false otherwise
     */
    @Then("number of elements found by $locatorType `$locator` in HTML `$html` is $comparisonRule `$number`")
    public boolean doesElementByLocatorExist(LocatorType locatorType,
            String locator, String html, ComparisonRule comparisonRule, int number)
    {
        Elements elements = HtmlUtils.getElements(html, locatorType, locator);
        return assertElements(locatorType, locator, comparisonRule, number, elements);
    }

    private boolean assertElements(LocatorType locatorType, String cssSelector, ComparisonRule comparisonRule,
            int number, Elements elements)
    {
        return softAssert.assertThat(String.format("Number of elements found by %s '%s'", locatorType.getDescription(),
            cssSelector), elements.size(), comparisonRule.getComparisonRule(number));
    }

    /**
     * Checks if HTML has expected data by given locator
     * @param html         HTML to check
     * @param expectedText expected value of element
     * @param locatorType  <b>CSS selector</b> or <b>XPath</b>
     * @param locator      Locator to locate element in HTML document
     * @see <a href="https://www.w3schools.com/cssref/css_selectors.asp"><i>CSS Selector Reference</i></a>
     * @see <a href="https://jsoup.org/apidocs/org/jsoup/select/Selector.html"><i>Jsoup Selector API</i></a>
     */
    @Then("element found by $locatorType `$locator` in HTML `$html` contains text `$expectedText`")
    public void elementContainsDataByLocator(LocatorType locatorType, String locator, String html, String expectedText)
    {
        assertElementByLocatorExists(html, locatorType, locator).ifPresent(e -> softAssert
            .assertEquals(String.format("Element found by %s contains expected data", locatorType.getDescription()),
                    expectedText, e.text()));
    }

    /**
     * Sets specified attribute value of the element found by the locator to the variable with name
     * @param attributeName name of the element attribute
     * @param html HTML to check
     * @param locatorType  <b>CSS selector</b> or <b>XPath</b>
     * @param locator CSS selector
     * @see <a href="https://www.w3schools.com/cssref/css_selectors.asp"><i>CSS Selector Reference</i></a>
     * @see <a href="https://jsoup.org/apidocs/org/jsoup/select/Selector.html"><i>Jsoup Selector API</i></a>
     * @param scopes scopes The set of variable scopes (comma separated list of scopes e.g.: STORY, NEXT_BATCHES)
     * @param variableName variable name
     */
    @When(value = "I save `$attributeName` attribute value of element found by $locatorType `$locator` in HTML "
        + "`$html` to $scopes variable `$variableName`", priority = 1)
    public void saveAttributeValueOfElementByLocator(String attributeName, LocatorType locatorType, String locator,
            String html, Set<VariableScope> scopes, String variableName)
    {
        assertElementByLocatorExists(html, locatorType, locator)
                .ifPresent(e -> variableContext.putVariable(scopes, variableName, e.attr(attributeName)));
    }

    /**
     * Sets specified data type (text, data) of the element found by the locator to the variable with name
     * @param dataType to save
     * @param locatorType  <b>CSS selector</b> or <b>XPath</b>
     * @param locator CSS selector
     * @param html HTML to check
     * @param scopes scopes The set of variable scopes (comma separated list of scopes e.g.: STORY, NEXT_BATCHES)
     * @param variableName variable name
     * @see <a href="https://www.w3schools.com/cssref/css_selectors.asp"><i>CSS Selector Reference</i></a>
     * @see <a href="https://jsoup.org/apidocs/org/jsoup/select/Selector.html"><i>Jsoup Selector API</i></a>
     */
    @When("I save $dataType of element found by $locatorType `$locator` in HTML `$html` to $scopes variable"
            + " `$variableName`")
    public void saveData(DataType dataType, LocatorType locatorType, String locator, String html,
            Set<VariableScope> scopes, String variableName)
    {
        assertElementByLocatorExists(html, locatorType, locator)
            .ifPresent(e -> variableContext.putVariable(scopes, variableName, dataType.get(e)));
    }
}
