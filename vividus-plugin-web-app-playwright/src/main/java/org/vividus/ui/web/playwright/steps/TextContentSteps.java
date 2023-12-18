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

package org.vividus.ui.web.playwright.steps;

import java.util.Set;
import java.util.regex.Pattern;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.PlaywrightAssertions;

import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.vividus.context.VariableContext;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.assertions.PlaywrightSoftAssert;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;
import org.vividus.variable.VariableScope;

public class TextContentSteps
{
    private final UiContext uiContext;
    private final VariableContext variableContext;
    private final PlaywrightSoftAssert playwrightSoftAssert;

    public TextContentSteps(UiContext uiContext, VariableContext variableContext,
            PlaywrightSoftAssert playwrightSoftAssert)
    {
        this.uiContext = uiContext;
        this.variableContext = variableContext;
        this.playwrightSoftAssert = playwrightSoftAssert;
    }

    /**
     * Saves the text of the context into a variable.
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
    @When("I save text of context to $scopes variable `$variableName`")
    public void saveTextOfContext(Set<VariableScope> scopes, String variableName)
    {
        saveTextOfElement(uiContext.getCurrentContexOrPageRoot(), scopes, variableName);
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
    public void saveTextOfElement(PlaywrightLocator locator, Set<VariableScope> scopes, String variableName)
    {
        saveTextOfElement(uiContext.locateElement(locator), scopes, variableName);
    }

    private void saveTextOfElement(Locator element, Set<VariableScope> scopes, String variableName)
    {
        variableContext.putVariable(scopes, variableName, element.textContent());
    }

    /**
     * Validates the text from current context matches the specified regular expression.
     *
     * @param regex The regular expression used to validate the context text.
     */
    @Then("text matches `$regex`")
    public void assertTextMatchesRegex(Pattern regex)
    {
        playwrightSoftAssert.runAssertion("The text matching regular expression is not found in the context",
                () -> PlaywrightAssertions.assertThat(uiContext.getCurrentContexOrPageRoot()).containsText(regex)
        );
    }

    /**
     * Validates the text is present in the current context. The expected text is case-sensitive.
     *
     * @param text The expected text to be found in the context text.
     */
    @Then("text `$text` exists")
    public void assertTextExists(String text)
    {
        playwrightSoftAssert.runAssertion("The expected text is not found in the context",
                () -> PlaywrightAssertions.assertThat(uiContext.getCurrentContexOrPageRoot()).containsText(text)
        );
    }

    /**
     * Validates the text is not present in the current context.
     *
     * @param text The text that should not be present in the context.
     */
    @Then("text `$text` does not exist")
    public void assertTextDoesNotExist(String text)
    {
        playwrightSoftAssert.runAssertion("The unexpected text is found in the context",
                () -> PlaywrightAssertions.assertThat(uiContext.getCurrentContexOrPageRoot()).not().containsText(text)
        );
    }
}
