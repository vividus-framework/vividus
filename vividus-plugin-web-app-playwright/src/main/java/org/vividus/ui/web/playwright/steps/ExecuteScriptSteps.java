/*
 * Copyright 2019-2025 the original author or authors.
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
import java.util.function.Supplier;

import org.jbehave.core.annotations.When;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.softassert.SoftAssert;
import org.vividus.ui.web.action.JavascriptActions;
import org.vividus.variable.VariableScope;

public class ExecuteScriptSteps
{
    private final JavascriptActions javascriptActions;
    private final ISoftAssert softAssert;
    private final VariableContext variableContext;

    public ExecuteScriptSteps(JavascriptActions javascriptActions, SoftAssert softAssert,
            VariableContext variableContext)
    {
        this.javascriptActions = javascriptActions;
        this.softAssert = softAssert;
        this.variableContext = variableContext;
    }

    /**
     * Executes passed JavaScript code on the opened page
     *
     * @param jsCode       JavaScript code
     *                     (e.g. "document.querySelector('[name="vividus-logo"]').remove()")
     */
    @When("I execute javascript `$jsCode`")
    public void executeJavascript(String jsCode)
    {
        javascriptActions.executeScript(jsCode);
    }

    /**
     * Executes passed JavaScript code on the opened page
     * and saves returned value into the <b>variable</b>
     *
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName A name under which the value should be saved
     * @param jsCode       Code in javascript that returns some value as result
     *                     (e.g. JSON.stringify(window.performance.timing))
     */
    @When(value = "I execute javascript `$jsCode` and save result to $scopes variable `$variableName`", priority = 1)
    public void saveValueFromJS(String jsCode, Set<VariableScope> scopes, String variableName)
    {
        assertAndSaveResult(() -> javascriptActions.executeScript(jsCode), scopes, variableName);
    }

    private void assertAndSaveResult(Supplier<Object> resultProvider, Set<VariableScope> scopes, String variableName)
    {
        Object result = resultProvider.get();
        if (softAssert.assertNotNull("Returned result is not null", result))
        {
            variableContext.putVariable(scopes, variableName, result);
        }
    }
}
