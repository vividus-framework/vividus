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

package org.vividus.steps.ui;

import java.util.List;
import java.util.Set;

import org.jbehave.core.annotations.When;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ui.web.model.JsArgument;
import org.vividus.steps.ui.web.model.JsArgumentType;
import org.vividus.ui.action.JavascriptActions;
import org.vividus.variable.VariableScope;

public class ExecuteScriptSteps extends AbstractExecuteScriptSteps
{
    private final JavascriptActions javascriptActions;

    public ExecuteScriptSteps(JavascriptActions javascriptActions, VariableContext variableContext,
            ISoftAssert softAssert)
    {
        super(softAssert, variableContext);
        this.javascriptActions = javascriptActions;
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
     *                     (e.g. var a=1; return a;)
     */
    @When("I execute javascript `$jsCode` and save result to $scopes variable `$variableName`")
    public void saveValueFromJS(String jsCode, Set<VariableScope> scopes, String variableName)
    {
        assertAndSaveResult(() -> javascriptActions.executeScript(jsCode), scopes, variableName);
    }

    /**
     * Executes JavaScript code with specified arguments
     *
     * @param jsCode JavaScript code
     * @param args   A table containing JS command argument value and type (one of <i>STRING</i> or <i>OBJECT</i>).
     *               The parameter is optional and could be omitted.<br>
     */
    @When("I execute javascript `$jsCode` with arguments:$args")
    public void executeJavascriptWithArguments(String jsCode, List<JsArgument> args)
    {
        javascriptActions.executeScript(jsCode, args.stream().map(this::convertRowToArgument).toArray());
    }

    private Object convertRowToArgument(JsArgument arg)
    {
        JsArgumentType type = arg.getType();
        String value = arg.getValue();
        if (type == null || value == null)
        {
            throw new IllegalArgumentException("Please, specify command argument values and types");
        }
        return type.convert(value);
    }
}
