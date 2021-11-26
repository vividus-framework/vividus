/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.steps.json;

import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.flipkart.zjsonpatch.JsonPatch;

import org.jbehave.core.annotations.When;
import org.vividus.context.VariableContext;
import org.vividus.util.json.JsonUtils;
import org.vividus.variable.VariableScope;

public class JsonPatchSteps
{
    private final VariableContext variableContext;
    private final JsonUtils jsonUtils;

    public JsonPatchSteps(VariableContext variableContext, JsonUtils jsonUtils)
    {
        this.variableContext = variableContext;
        this.jsonUtils = jsonUtils;
    }

    /**
     * Performs patching of a json and save it to the variable
     * @param sourceJson json data to be patched
     * @param jsonPatch json data with patch actions
     * @param scopes The set of variable scopes (comma separated list of scopes e.g.: SCENARIO, STORY, NEXT_BATCHES)
     * @param variableName Name of variable
     */
    @When("I patch JSON `$sourceJson` using `$jsonPatch` and save result to $scopes variable `$variableName`")
    public void patchJsonFile(String sourceJson, String jsonPatch, Set<VariableScope> scopes,
            String variableName)
    {
        JsonNode jsonSource = jsonUtils.readTree(sourceJson);
        JsonNode patchJson = jsonUtils.readTree(jsonPatch);
        JsonNode patchedJson = JsonPatch.apply(patchJson, jsonSource);
        variableContext.putVariable(scopes, variableName, patchedJson.toString());
    }
}
