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

package org.vividus.yaml.steps;

import java.util.List;
import java.util.Set;

import org.jbehave.core.annotations.When;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.variable.VariableScope;

import io.github.yamlpath.YamlPath;

public class YamlSteps
{
    private final VariableContext variableContext;
    private final ISoftAssert softAssert;

    public YamlSteps(VariableContext variableContext, ISoftAssert softAssert)
    {
        this.variableContext = variableContext;
        this.softAssert = softAssert;
    }

    /**
     * Saves a value of YAML element found in the given YAML into the variable with the specified name and scope
     *
     * @param yaml         The YAML used to find YAML element value.
     * @param yamlPath     The YAML path used to find YAML element value.
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName The name of the variable to save the found YAML element value.
     */
    @When("I save YAML element value from `$yaml` by YAML path `$yamlPath` to $scopes variable `$variableName`")
    public void saveYamlValueToVariable(String yaml, String yamlPath, Set<VariableScope> scopes, String variableName)
    {
        Set<Object> result = YamlPath.from(yaml).read(yamlPath);
        if (result.isEmpty())
        {
            softAssert.recordFailedAssertion(String.format("No YAML element is found by YAML path '%s'", yamlPath));
            return;
        }

        Object value = result.iterator().next();
        if (value instanceof String || value instanceof Boolean || value instanceof Number)
        {
            variableContext.putVariable(scopes, variableName, String.valueOf(value));
        }
        else
        {
            String actualType = value instanceof List ? "array" : "object";
            softAssert.recordFailedAssertion(String.format(
                    "Value of YAML element found by YAML path '%s' must be either null, or boolean, or string, or"
                            + " integer, or float, but found %s", yamlPath, actualType));
        }
    }
}
