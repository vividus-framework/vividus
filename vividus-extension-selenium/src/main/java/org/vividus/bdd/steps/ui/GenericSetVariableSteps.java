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
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.monitor.TakeScreenshotOnFailure;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.ElementActions;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.context.IUiContext;

@TakeScreenshotOnFailure
public class GenericSetVariableSteps
{
    private final ISoftAssert softAssert;
    private final IBaseValidations baseValidations;
    private final IBddVariableContext bddVariableContext;
    private final ElementActions elementActions;
    private final IUiContext uiContext;
    private final ISearchActions searchActions;

    public GenericSetVariableSteps(ISoftAssert softAssert, IBaseValidations baseValidations,
            IBddVariableContext bddVariableContext, ElementActions elementActions, IUiContext uiContext,
            ISearchActions searchActions)
    {
        this.softAssert = softAssert;
        this.baseValidations = baseValidations;
        this.bddVariableContext = bddVariableContext;
        this.elementActions = elementActions;
        this.uiContext = uiContext;
        this.searchActions = searchActions;
    }

    /**
     * Extracts the <b>text</b> of element found in the context and saves it to the <b>variable</b> with the specified
     * <b>variableName</b>
     * Actions performed at this step:
     * <ul>
     * <li>Saves the text of element into the <i>variable name</i>
     * </ul>
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
    @When("I save text of context element to $scopes variable `$variableName`")
    public void saveContextElementTextToVariable(Set<VariableScope> scopes, String variableName)
    {
        saveVariableIfContextElementPresent(elementActions::getElementText, scopes, variableName);
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
        saveVariableIfContextElementPresent(contextElement -> getAssertedAttributeValue(contextElement, attributeName),
                scopes, variableName);
    }

    /**
     * Extracts the value of <b>attribute</b> of element found by <b>locator</b> and saves it to the <b>variable</b>
     * with the specified <b>variableName</b>
     * Actions performed at this step:
     * <ul>
     * <li>Saves the value of attribute with name <i>attributeName</i> to the <i>variableName</i>
     * </ul>
     * @param attributeName the name of the attribute (for ex. 'name', 'id')
     * @param locator locator to find an element
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
    @When("I save `$attributeName` attribute value of element located `$locator` to $scopes variable `$variableName`")
    public void saveAttributeValueOfElementByLocatorToVariable(String attributeName, Locator locator,
            Set<VariableScope> scopes, String variableName)
    {
        locator.getSearchParameters().setVisibility(Visibility.ALL);
        Optional<Object> value = baseValidations.assertElementExists("The element to extract the attribute", locator)
                                                .map(element -> getAssertedAttributeValue(element, attributeName));
        putVariable(scopes, variableName, value);
    }

    /**
     * Extracts the <b>number</b> of elements found by <b>locator</b> and saves it to the <b>variable</b> with the
     * specified <b>variableName</b>
     * Actions performed at this step:
     * <ul>
     * <li>Finds the elements by <b>locator</b>
     * <li>Saves the number of found elements into the <i>variable</i>
     * </ul>
     * @param locator locator to locate elements
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
    @When("I save number of elements located `$locator` to $scopes variable `$variableName`")
    public void saveNumberOfElementsToVariable(Locator locator, Set<VariableScope> scopes,
            String variableName)
    {
        List<WebElement> elements = searchActions.findElements(locator);
        putVariable(scopes, variableName, Optional.of(elements.size()));
    }

    private void saveVariableIfContextElementPresent(Function<WebElement, Object> contextElementProcessor,
            Set<VariableScope> scopes, String variableName)
    {
        Optional<Object> value = Optional.ofNullable(uiContext.getSearchContext())
                                         .map(context -> uiContext.getSearchContext(WebElement.class))
                                         .map(contextElementProcessor);
        putVariable(scopes, variableName, value);
    }

    private void putVariable(Set<VariableScope> scopes, String variableName, Optional<Object> value)
    {
        bddVariableContext.putVariable(scopes, variableName, value.orElse(null));
    }

    private String getAssertedAttributeValue(WebElement element, String attributeName)
    {
        String value = element.getAttribute(attributeName);
        softAssert.assertNotNull("The '" + attributeName + "' attribute value was found", value);
        return value;
    }
}
