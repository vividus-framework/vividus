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

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.BoundingBox;

import org.hamcrest.Matcher;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.StringComparisonRule;
import org.vividus.steps.ui.web.ViewportPresence;
import org.vividus.ui.web.action.ResourceFileLoader;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.action.ElementActions;
import org.vividus.ui.web.playwright.assertions.PlaywrightSoftAssert;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;
import org.vividus.ui.web.playwright.locator.Visibility;
import org.vividus.ui.web.validation.ScrollValidations;
import org.vividus.variable.VariableScope;

public class ElementSteps
{
    private final UiContext uiContext;
    private final ISoftAssert softAssert;
    private final VariableContext variableContext;
    private final ElementActions elementActions;
    private final PlaywrightSoftAssert playwrightSoftAssert;
    private final ScrollValidations<Locator> scrollValidations;
    private final ResourceFileLoader resourceFileLoader;

    public ElementSteps(UiContext uiContext, ISoftAssert softAssert, VariableContext variableContext,
            ElementActions elementActions, PlaywrightSoftAssert playwrightSoftAssert,
            ScrollValidations<Locator> scrollValidations, ResourceFileLoader resourceFileLoader)
    {
        this.uiContext = uiContext;
        this.softAssert = softAssert;
        this.variableContext = variableContext;
        this.elementActions = elementActions;
        this.playwrightSoftAssert = playwrightSoftAssert;
        this.scrollValidations = scrollValidations;
        this.resourceFileLoader = resourceFileLoader;
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
     * @return               The locator of found elements.
     */
    @Then("number of elements found by `$locator` is $comparisonRule `$number`")
    public Locator assertElementsNumber(PlaywrightLocator locator, ComparisonRule comparisonRule, int number)
    {
        Locator elements = uiContext.locateElement(locator);
        int actualNumberOfElements = elements.count();
        boolean matches = comparisonRule.getComparisonRule(number).matches(actualNumberOfElements);
        StringBuilder assertionMessage = new StringBuilder("The number of elements found by '")
                .append(locator).append("' is ").append(actualNumberOfElements);
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
        return elements;
    }

    /**
     * Saves the information about the size of an element and its coordinates relative to the viewport.
     *
     * @param locator        The locator to find an element
     * @param scopes         The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     *                       <i>Available scopes:</i>
     *                       <ul>
     *                       <li><b>STEP</b> - the variable will be available only within the step,
     *                       <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                       <li><b>STORY</b> - the variable will be available within the whole story,
     *                       <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                       </ul>
     * @param variableName   The name of the variable to save the coordinates and size of an element which can be
     *                       accessed on the variable name using dot notation:
     *                       <table>
     *                       <caption>A table of size and coordinate properties</caption>
     *                       <thead>
     *                       <tr>
     *                           <th><b>attribute</b></th>
     *                           <th><b>description</b></th>
     *                       </tr>
     *                       </thead>
     *                       <tbody>
     *                       <tr>
     *                           <td>x</td>
     *                           <td>the x coordinate</td>
     *                       </tr>
     *                       <tr>
     *                           <td>y</td>
     *                           <td>the y coordinate</td>
     *                       </tr>
     *                       <tr>
     *                           <td>height</td>
     *                           <td>the height of the element</td>
     *                       </tr>
     *                       <tr>
     *                           <td>width</td>
     *                           <td>the width of the element</td>
     *                       </tr>
     *                       </tbody>
     *                       </table>
     */
    @When("I save coordinates and size of element located by `$locator` to $scopes variable `$variableName`")
    public void saveElementCoordinatesAndSize(PlaywrightLocator locator, Set<VariableScope> scopes, String variableName)
    {
        BoundingBox box = uiContext.locateElement(locator).boundingBox();
        variableContext.putVariable(scopes, variableName, Map.of(
            "x", box.x,
            "y", box.y,
            "height", box.height,
            "width", box.width
        ));
    }

    /**
     * Extracts the <b>number</b> of elements found by <b>locator</b> and saves it to the <b>variable</b> with the
     * specified <b>variableName</b>
     * Actions performed at this step:
     * <ul>
     * <li>Finds the elements by <b>locator</b>
     * <li>Saves the number of found elements into the <i>variable</i>
     * </ul>
     * @param locator The locator to find an element
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A name under which the value should be saved
     */
    @When("I save number of elements located by `$locator` to $scopes variable `$variableName`")
    public void saveNumberOfElementsToVariable(PlaywrightLocator locator, Set<VariableScope> scopes,
            String variableName)
    {
        int elementCount = uiContext.locateElement(locator).count();
        variableContext.putVariable(scopes, variableName, elementCount);
    }

    /**
     * Extracts the value of <b>attribute</b> of element found in the context and saves it to the <b>variable</b> with
     * the specified <b>variableName</b>
     * Actions performed at this step:
     * <ul>
     * <li>Saves the value of attribute with name <i>attributeName</i> to the <i>variableName</i>
     * </ul>
     * @param attributeName  The name of the attribute (for ex. 'name', 'id')
     * @param scopes         The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     *                       <i>Available scopes:</i>
     *                       <ul>
     *                       <li><b>STEP</b> - the variable will be available only within the step,
     *                       <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                       <li><b>STORY</b> - the variable will be available within the whole story,
     *                       <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                       </ul>
     * @param variableName   The name under which the attribute value should be saved
     */
    @When("I save `$attributeName` attribute value of context element to $scopes variable `$variableName`")
    public void saveContextElementAttributeValueToVariable(String attributeName, Set<VariableScope> scopes,
            String variableName)
    {
        saveAttributeValueOfElement(uiContext.getCurrentContexOrPageRoot(), attributeName, scopes, variableName);
    }

    /**
     * Gets the value of <b>attribute</b> from element located by <b>locator</b> and saves it to the <b>variable</b>
     * with the specified <b>variableName</b>
     * Actions performed at this step:
     * <ul>
     * <li>Saves the value of attribute with name <i>attributeName</i> to the <i>variableName</i>
     * </ul>
     * @param attributeName  The name of the attribute (for ex. 'name', 'id')
     * @param locator        The locator to find an element
     * @param scopes         The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     *                       <i>Available scopes:</i>
     *                       <ul>
     *                       <li><b>STEP</b> - the variable will be available only within the step,
     *                       <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                       <li><b>STORY</b> - the variable will be available within the whole story,
     *                       <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                       </ul>
     * @param variableName   The name under which the attribute value should be saved
     */
    @When("I save `$attributeName` attribute value of element located by `$locator` to $scopes variable "
          + "`$variableName`")
    public void saveAttributeValueOfElement(String attributeName, PlaywrightLocator locator, Set<VariableScope> scopes,
            String variableName)
    {
        saveAttributeValueOfElement(uiContext.locateElement(locator), attributeName, scopes, variableName);
    }

    /**
     * Checks that the context <b>element</b> has an expected <b>CSS property</b>
     * @param cssName A name of the <b>CSS property</b>
     * @param comparisonRule is equal to, contains, does not contain
     * @param cssValue An expected value of the <b>CSS property</b>
     */
    @Then("context element has CSS property `$cssName` with value that $comparisonRule `$cssValue`")
    public void assertElementCssProperty(String cssName, StringComparisonRule comparisonRule, String cssValue)
    {
        String actualCssValue = elementActions.getCssValue(uiContext.getCurrentContexOrPageRoot(),
                cssName);
        Matcher<String> matcher = comparisonRule.createMatcher(cssValue);
        softAssert.assertThat("Element css property value is", actualCssValue, matcher);
    }

    /**
     * This step uploads a file with a given relative path to an element located by the provided locator.
     * <p>
     * A <b>relative path</b> is a path that starts from a given working directory, eliminating the need to provide the
     * full absolute path(i.e. <i>'about.jpeg'</i> is in the root directory or <i>'/story/uploadfiles/about.png'</i>)
     * </p>
     *
     * @param locator The locator for the upload element.
     * @param filePath The relative path to the file to be uploaded.
     * @see <a href="https://en.wikipedia.org/wiki/Path_(computing)#Absolute_and_relative_paths">Absolute and
     * relative paths</a>
     * @throws IOException If an I/O error occurs when loading the file.
     */
    @When("I select element located by `$locator` and upload `$resourceNameOrFilePath`")
    public void uploadFile(PlaywrightLocator locator, String filePath) throws IOException
    {
        File fileForUpload = resourceFileLoader.loadFile(filePath);
        Locator fileInputLocator = uiContext.locateElement(locator);
        playwrightSoftAssert.runAssertion("A file input element is not found", () -> {
            PlaywrightAssertions.assertThat(fileInputLocator).hasCount(1);
            fileInputLocator.setInputFiles(fileForUpload.toPath());
        });
    }

    /**
     * Verifies elements' located by locator state.
     * Where state one of: ENABLED/DISABLED, SELECTED/NOT_SELECTED, VISIBLE/NOT_VISIBLE
     * Step intended to verify strictly either number of elements and their state
     * <p><i>In case when locator's visibility and checked state are equal (For an example ':v' and 'VISIBLE')
     * exception will be thrown. In such cases please use step:
     * 'Then number of elements found by `$locator` is $comparisonRule `$number`'.
     * Contradictory visibility parameters (locator - ':v' and checked state - 'NOT_VISIBLE') are also not allowed.
     * </i></p>
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
    public void assertElementsNumberInState(
            ElementState state, PlaywrightLocator locator, ComparisonRule comparisonRule, int number)
    {
        Visibility visibility = locator.getVisibility();
        if (visibility == Visibility.VISIBLE && (state == ElementState.VISIBLE || state == ElementState.NOT_VISIBLE))
        {
            String errorMessage = String.format(state == ElementState.VISIBLE
                    ? "Locator visibility: %s and the state: %s to validate are the same. This makes no sense. "
                      + "Please consider validation of elements size instead."
                    : "Contradictory input parameters. Locator visibility: '%s', the state: '%s'.",
                    visibility, state);
            throw new IllegalArgumentException(errorMessage);
        }
        String description = "Element state is not " + state;
        assertElementsNumber(locator, comparisonRule, number).all().forEach(
                element -> playwrightSoftAssert.runAssertion(description, () -> state.assertElementState(element)));
    }

    /**
     * Checks if the element located by the specified locater is or is not presented in the browser viewport
     *
     * @param locator The locator of the element to check presence in viewport
     * @param presence The presence state of the element, either <b>is</b> or <b>is not</b>
     */
    @Then("element located by `$locator` $presence visible in viewport")
    public void checkElementViewportPresence(PlaywrightLocator locator, ViewportPresence presence)
    {
        Locator element = uiContext.locateElement(locator);
        scrollValidations.assertElementPositionAgainstViewport(element, presence);
    }

    private void saveAttributeValueOfElement(Locator element, String attributeName, Set<VariableScope> scopes,
            String variableName)
    {
        Optional.ofNullable(element.getAttribute(attributeName))
                .ifPresentOrElse(value -> variableContext.putVariable(scopes, variableName, value),
                        () -> softAssert.recordFailedAssertion(
                                String.format("The '%s' attribute does not exist", attributeName)));
    }
}
