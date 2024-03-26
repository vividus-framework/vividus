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

package org.vividus.steps.html;

import java.util.Optional;
import java.util.Set;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.vividus.context.VariableContext;
import org.vividus.html.HtmlLocatorType;
import org.vividus.html.JsoupUtils;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
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

    private Optional<Element> assertElementByLocatorExists(String html, HtmlLocatorType locatorType, String locator)
    {
        Elements elements = locatorType.findElements(JsoupUtils.getDocument(html), locator);
        if (assertElements(locatorType, locator, ComparisonRule.EQUAL_TO, 1, elements))
        {
            return Optional.of(elements.first());
        }
        return Optional.empty();
    }

    /**
     * Checks if HTML contains elements according to rule by locator
     *
     * @param htmlLocatorType The <b>CSS selector</b> or <b>XPath</b>
     * @param htmlLocator     The locator to locate elements in HTML document
     * @param html            The HTML document
     * @param comparisonRule  The rule to match the quantity of elements. The supported rules:
     *                        <ul>
     *                        <li>less than (&lt;)</li>
     *                        <li>less than or equal to (&lt;=)</li>
     *                        <li>greater than (&gt;)</li>
     *                        <li>greater than or equal to (&gt;=)</li>
     *                        <li>equal to (=)</li>
     *                        <li>not equal to (!=)</li>
     *                        </ul>
     * @param number          The expected number of elements
     * @see <a href="https://www.w3schools.com/cssref/css_selectors.asp"><i>CSS Selector Reference</i></a>
     * @see <a href="https://jsoup.org/apidocs/org/jsoup/select/Selector.html"><i>Jsoup Selector API</i></a>
     * @return true if elements quantity corresponds to rule, false otherwise
     */
    @Then("number of elements found by $htmlLocatorType `$htmlLocator` in HTML `$html` is $comparisonRule `$number`")
    public boolean doesElementByLocatorExist(HtmlLocatorType htmlLocatorType,
            String htmlLocator, String html, ComparisonRule comparisonRule, int number)
    {
        Elements elements = htmlLocatorType.findElements(JsoupUtils.getDocument(html), htmlLocator);
        return assertElements(htmlLocatorType, htmlLocator, comparisonRule, number, elements);
    }

    private boolean assertElements(HtmlLocatorType locatorType, String cssSelector, ComparisonRule comparisonRule,
            int number, Elements elements)
    {
        return softAssert.assertThat(String.format("Number of elements found by %s '%s'", locatorType.getDescription(),
            cssSelector), elements.size(), comparisonRule.getComparisonRule(number));
    }

    /**
     * Checks if HTML has expected data by given locator
     *
     * @param htmlLocatorType The <b>CSS selector</b> or <b>XPath</b>
     * @param htmlLocator     The locator to locate element in HTML document
     * @param html            The HTML document
     * @param expectedText    The expected text of element
     * @see <a href="https://www.w3schools.com/cssref/css_selectors.asp"><i>CSS Selector Reference</i></a>
     * @see <a href="https://jsoup.org/apidocs/org/jsoup/select/Selector.html"><i>Jsoup Selector API</i></a>
     */
    @Then("element found by $htmlLocatorType `$htmlLocator` in HTML `$html` contains text `$expectedText`")
    public void elementContainsDataByLocator(HtmlLocatorType htmlLocatorType, String htmlLocator, String html,
            String expectedText)
    {
        assertElementByLocatorExists(html, htmlLocatorType, htmlLocator).ifPresent(e -> softAssert
            .assertEquals(String.format("Element found by %s contains expected data", htmlLocatorType.getDescription()),
                    expectedText, e.text()));
    }

    /**
     * Sets specified attribute value of the element found by the locator to the variable with name
     *
     * @param attributeName    The name of the element attribute
     * @param html             The HTML document
     * @param htmlLocatorType  The <b>CSS selector</b> or <b>XPath</b>
     * @param htmlLocator      The locator to locate element in HTML document
     * @see <a href="https://www.w3schools.com/cssref/css_selectors.asp"><i>CSS Selector Reference</i></a>
     * @see <a href="https://jsoup.org/apidocs/org/jsoup/select/Selector.html"><i>Jsoup Selector API</i></a>
     * @param scopes scopes The set of variable scopes (comma separated list of scopes e.g.: STORY, NEXT_BATCHES)
     * @param variableName variable name
     */
    @When(value = "I save `$attributeName` attribute value of element found by $htmlLocatorType `$htmlLocator` in HTML "
        + "`$html` to $scopes variable `$variableName`", priority = 1)
    public void saveAttributeValueOfElementByLocator(String attributeName, HtmlLocatorType htmlLocatorType,
            String htmlLocator, String html, Set<VariableScope> scopes, String variableName)
    {
        assertElementByLocatorExists(html, htmlLocatorType, htmlLocator)
                .ifPresent(e -> variableContext.putVariable(scopes, variableName, e.attr(attributeName)));
    }

    /**
     * Sets specified data type (text, data) of the element found by the locator to the variable with name
     *
     * @param dataType        The type of data, either <b>text</b> or <b>data</b> (contents of scripts, comments,
     * CSS styles, etc.)
     * @param htmlLocatorType The <b>CSS selector</b> or <b>XPath</b>
     * @param htmlLocator     The locator to locate element in HTML document
     * @param html            The HTML document
     * @param scopes scopes   The set of variable scopes (comma separated list of scopes e.g.: STORY, NEXT_BATCHES)
     * @param variableName    The variable name
     * @see <a href="https://www.w3schools.com/cssref/css_selectors.asp"><i>CSS Selector Reference</i></a>
     * @see <a href="https://jsoup.org/apidocs/org/jsoup/select/Selector.html"><i>Jsoup Selector API</i></a>
     */
    @When("I save $dataType of element found by $htmlLocatorType `$htmlLocator` in HTML `$html` to $scopes variable"
            + " `$variableName`")
    public void saveData(DataType dataType, HtmlLocatorType htmlLocatorType, String htmlLocator, String html,
            Set<VariableScope> scopes, String variableName)
    {
        assertElementByLocatorExists(html, htmlLocatorType, htmlLocator)
            .ifPresent(e -> variableContext.putVariable(scopes, variableName, dataType.get(e)));
    }

    /**
     * Saves the number of elements found by locator to a variable.
     *
     * @param htmlLocatorType The <b>CSS selector</b> or <b>XPath</b>
     * @param htmlLocator     The locator to locate element in HTML document
     * @param html            The HTML document
     * @param scopes          The set of variable scopes (comma separated list of scopes e.g.: STORY, NEXT_BATCHES)
     * @param variableName    The variable name
     */
    @When("I save number of elements found by $htmlLocatorType `$htmlLocator` in HTML `$html` to $scopes variable"
            + " `$variableName`")
    public void saveNumberOfElements(HtmlLocatorType htmlLocatorType, String htmlLocator, String html,
            Set<VariableScope> scopes, String variableName)
    {
        Elements elements = htmlLocatorType.findElements(JsoupUtils.getDocument(html), htmlLocator);
        variableContext.putVariable(scopes, variableName, elements.size());
    }
}
