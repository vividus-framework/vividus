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

package org.vividus.azure.functions.steps;

import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.annotations.When;
import org.vividus.azure.functions.service.FunctionService;
import org.vividus.context.VariableContext;
import org.vividus.util.json.JsonUtils;
import org.vividus.variable.VariableScope;

import reactor.core.publisher.Mono;

public class FunctionSteps
{
    private final JsonUtils jsonUtils;
    private final FunctionService functionsService;
    private final VariableContext variableContext;

    public FunctionSteps(JsonUtils jsonUtils, FunctionService functionsService, VariableContext variableContext)
    {
        this.jsonUtils = jsonUtils;
        this.functionsService = functionsService;
        this.variableContext = variableContext;
    }

    /**
     * Triggers Azure function by its name. And saves response from the service into variable with
     *  a provided scope and name
     * @param functionApp   The name of Azure Function App. The value can be retrieved by looking
     *                      at the function in the Azure Portal.
     * @param functionName  The name of the function to execute. The value can be retrieved by looking
     *                      at the function in the Azure Portal.
     * @param resourceGroup Resource group name. Resource group - container that holds related resources
     *                      for an Azure solution. The value can be retrieved by looking
     *                      at the function in the Azure Portal.
     * @param payload       the JSON that to provide to Function App function as input.
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variables scopes<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>scopes
     * @param variableName The variable name to store result. If the variable name is my-var, the following
     *                     variables will be created:
     *                     <ul>
     *                     <li>${my-var.body} - the response body</li>
     *                     <li>${my-var.status-code} - the HTTP status code is in the 200 range for a successful
     *                     request</li>
     *                     <li>${my-var.headers} - the response headers</li>
     *                     <li>${my-var.url} - the request URL</li>
     *                     </ul>
     */
    @SuppressWarnings("unchecked")
    @When("I trigger function `$functionName` from function app `$functionAppName` in resource group `$resourceGroup`"
            + " with payload:$payload and save response into $scopes variable `$variableNames`")
    public void triggerFunction(String functionName, String functionApp, String resourceGroup, String payload,
            Set<VariableScope> scopes, String variableName)
    {
        Map<String, Object> responses = functionsService.triggerFunction(resourceGroup, functionApp, functionName,
                convertPayload(payload));
        responses.compute("body", (k, v) -> ((Mono<String>) v).block());
        variableContext.putVariable(scopes, variableName, responses);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> convertPayload(String payload)
    {
        if (StringUtils.isBlank(payload))
        {
            return Map.of();
        }
        return jsonUtils.toObject(payload, Map.class);
    }
}
