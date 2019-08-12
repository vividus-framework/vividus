/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd.steps.html;

import java.util.Optional;
import java.util.Set;

import javax.inject.Inject;

import org.hamcrest.Matchers;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.softassert.ISoftAssert;

public class HtmlSteps
{
    @Inject private ISoftAssert softAssert;
    @Inject private IBddVariableContext bddVariableContext;

    /**
     * Checks if HTML contains element by CSS selector
     * @param html HTML to check
     * @param cssSelector CSS selector
     * @see <a href="https://www.w3schools.com/cssref/css_selectors.asp"><i>CSS Selector Reference</i></a>
     * @see <a href="https://jsoup.org/apidocs/org/jsoup/select/Selector.html"><i>Jsoup Selector API</i></a>
     * @return element by CSS selector
     */
    @Then("HTML `$html` contains element by CSS selector `$cssSelector`")
    public Optional<Element> doesElementByCssSelectorExist(String html, String cssSelector)
    {
        Document document = Jsoup.parse(html);
        Elements elements = document.select(cssSelector);
        if (softAssert.assertThat(String.format("Number of elements found by css selector '%s'", cssSelector),
                elements.size(), Matchers.equalTo(1)))
        {
            return Optional.of(elements.first());
        }
        return Optional.empty();
    }

    /**
     * Checks if HTML has expected data by given CSS selector
     * @param html HTML to check
     * @param expectedData expected value of element by CSSs selector
     * @param cssSelector CSS selector
     * @see <a href="https://www.w3schools.com/cssref/css_selectors.asp"><i>CSS Selector Reference</i></a>
     * @see <a href="https://jsoup.org/apidocs/org/jsoup/select/Selector.html"><i>Jsoup Selector API</i></a>
     */
    @Then("HTML `$html` contains data `$expectedData` by CSS selector `$cssSelector`")
    public void elementContainsDataByCssSelector(String html, String expectedData, String cssSelector)
    {
        doesElementByCssSelectorExist(html, cssSelector).ifPresent(e -> softAssert
                .assertEquals("Element found by css selector contains expected data", expectedData, e.text()));
    }

    /**
     * Sets specified attribute value of the element found by the CSS selector to the variable with name
     * @param attributeName name of the element attribute
     * @param html HTML to check
     * @param cssSelector CSS selector
     * @see <a href="https://www.w3schools.com/cssref/css_selectors.asp"><i>CSS Selector Reference</i></a>
     * @see <a href="https://jsoup.org/apidocs/org/jsoup/select/Selector.html"><i>Jsoup Selector API</i></a>
     * @param scopes scopes The set of variable scopes (comma separated list of scopes e.g.: STORY, NEXT_BATCHES)
     * @param variableName variable name
     */
    @When(value = "I save `$attributeName` attribute value of element from HTML `$html` by CSS selector"
            + " `$cssSelector` to $scopes variable `$variableName`", priority = 1)
    public void saveAttributeValueOfElementByCssSelector(String attributeName, String html, String cssSelector,
            Set<VariableScope> scopes, String variableName)
    {
        doesElementByCssSelectorExist(html, cssSelector)
                .ifPresent(e -> bddVariableContext.putVariable(scopes, variableName, e.attr(attributeName)));
    }

    /**
     * Sets specified data type (text, data) of the element found by the CSS selector to the variable with name
     * @param dataType to save
     * @param html HTML to check
     * @param cssSelector CSS selector
     * @see <a href="https://www.w3schools.com/cssref/css_selectors.asp"><i>CSS Selector Reference</i></a>
     * @see <a href="https://jsoup.org/apidocs/org/jsoup/select/Selector.html"><i>Jsoup Selector API</i></a>
     * @param scopes scopes The set of variable scopes (comma separated list of scopes e.g.: STORY, NEXT_BATCHES)
     * @param variableName variable name
     */
    @When("I save $dataType of element from HTML `$html` by CSS selector `$cssSelector` to "
            + "$scopes variable `$variableName`")
    public void saveData(DataType dataType, String html, String cssSelector, Set<VariableScope> scopes,
            String variableName)
    {
        doesElementByCssSelectorExist(html, cssSelector)
                .ifPresent(e -> bddVariableContext.putVariable(scopes, variableName, dataType.get(e)));
    }
}
