/*
 * Copyright 2019-2023 the original author or authors.
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebElement;
import org.vividus.annotation.Replacement;
import org.vividus.context.VariableContext;
import org.vividus.selenium.locator.Locator;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.ElementActions;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.monitor.TakeScreenshotOnFailure;
import org.vividus.variable.VariableScope;

@TakeScreenshotOnFailure
public class GenericSetVariableSteps
{
    private final ISoftAssert softAssert;
    private final IBaseValidations baseValidations;
    private final VariableContext variableContext;
    private final ElementActions elementActions;
    private final IUiContext uiContext;
    private final ISearchActions searchActions;

    public GenericSetVariableSteps(ISoftAssert softAssert, IBaseValidations baseValidations,
            VariableContext variableContext, ElementActions elementActions, IUiContext uiContext,
            ISearchActions searchActions)
    {
        this.softAssert = softAssert;
        this.baseValidations = baseValidations;
        this.variableContext = variableContext;
        this.elementActions = elementActions;
        this.uiContext = uiContext;
        this.searchActions = searchActions;
    }

    /**
     * Saves the text of the context element into a variable.
     *
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable scopes.
     *                     <br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName The name of the variable to save the text content.
     */
    @When("I save text of context element to $scopes variable `$variableName`")
    public void saveContextElementTextToVariable(Set<VariableScope> scopes, String variableName)
    {
        saveTextOfElement(uiContext.getSearchContext(WebElement.class), scopes, variableName);
    }

    /**
     * Finds the element by the given locator and saves its text into a variable.
     *
     * @param locator      The locator used to find the element whose text content will be saved.
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable scopes.
     *                     <br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName The name of the variable to save the text content.
     */
    @When("I save text of element located by `$locator` to $scopes variable `$variableName`")
    public void saveTextOfElement(Locator locator, Set<VariableScope> scopes, String variableName)
    {
        saveTextOfElement(baseValidations.assertElementExists("The element to get text", locator), scopes,
                variableName);
    }

    private void saveTextOfElement(Optional<WebElement> element, Set<VariableScope> scopes, String variableName)
    {
        element.map(elementActions::getElementText)
               .ifPresent(text -> variableContext.putVariable(scopes, variableName, text));
    }

    /**
     * Extracts the value of <b>attribute</b> of element found in the context and saves it to the <b>variable</b> with
     * the specified <b>variableName</b>
     * Actions performed at this step:
     * <ul>
     * <li>Saves the value of attribute with name <i>attributeName</i> to the <i>variableName</i>
     * </ul>
     * @param attributeName the name of the attribute (for ex. 'name', 'id')
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario
     * <li><b>STORY</b> - the variable will be available within the whole story
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A name under which the value should be saved
     */
    @When("I save `$attributeName` attribute value of context element to $scopes variable `$variableName`")
    public void saveContextElementAttributeValueToVariable(String attributeName, Set<VariableScope> scopes,
            String variableName)
    {
        Optional<WebElement> webElement = uiContext.getSearchContext(WebElement.class);
        saveAttributeValueOfElement(webElement, attributeName, scopes, variableName);
    }

    /**
     * Extracts the value of <b>attribute</b> of element found by <b>locator</b> and saves it to the <b>variable</b>
     * with the specified <b>variableName</b>
     * Actions performed at this step:
     * <ul>
     * <li>Saves the value of attribute with name <i>attributeName</i> to the <i>variableName</i>
     * </ul>
     * @param attributeName the name of the attribute (for ex. 'name', 'id')
     * @param locator The locator to find an element
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario
     * <li><b>STORY</b> - the variable will be available within the whole story
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A name under which the value should be saved
     * @deprecated Use step: When I save `$attributeName` attribute value of element located by `$locator` to $scopes
     * variable `$variableName`
     */
    @Deprecated(since = "0.6.0", forRemoval = true)
    @Replacement(versionToRemoveStep = "0.7.0",
            replacementFormatPattern = "When I save `%1$s` attribute value of element located by `%2$s` to %3$s "
                    + "variable `%4$s`")
    @When("I save `$attributeName` attribute value of element located `$locator` to $scopes variable `$variableName`")
    public void saveAttributeValueOfElementByLocatorToVariable(String attributeName, Locator locator,
            Set<VariableScope> scopes, String variableName)
    {
        Optional<WebElement> webElement = baseValidations.assertElementExists("The element to extract the attribute",
                locator);
        saveAttributeValueOfElement(webElement, attributeName, scopes, variableName);
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
    public void saveAttributeValueOfElement(String attributeName, Locator locator, Set<VariableScope> scopes,
            String variableName)
    {
        baseValidations.assertElementExists("The element to get the attribute value", locator)
                       .map(e -> e.getAttribute(attributeName))
                       .ifPresentOrElse(value -> variableContext.putVariable(scopes, variableName, value),
                           () -> softAssert.recordFailedAssertion(
                               String.format("The '%s' attribute does not exist", attributeName)));
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
    public void saveNumberOfElementsToVariable(Locator locator, Set<VariableScope> scopes, String variableName)
    {
        List<WebElement> elements = searchActions.findElements(locator);
        variableContext.putVariable(scopes, variableName, elements.size());
    }

    @Deprecated(since = "0.6.0", forRemoval = true)
    private void saveAttributeValueOfElement(Optional<WebElement> element, String attributeName,
            Set<VariableScope> scopes, String variableName)
    {
        element.map(el -> {
            String value = el.getAttribute(attributeName);
            softAssert.assertNotNull(String.format("The '%s' attribute value was found", attributeName), value);
            return value;
        }).ifPresent(value -> variableContext.putVariable(scopes, variableName, value));
    }
}
