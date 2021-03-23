/*
 * Copyright 2021 the original author or authors.
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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.functions.service.FunctionService;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.util.json.JsonUtils;

import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)
class FunctionStepsTests
{
    private static final String DATA = "data";
    private static final String BODY = "body";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.STORY);
    private static final String VARIABLE_NAME = "variableName";
    private static final String FUNCTION_NAME = "functionName";
    private static final String APP_NAME = "appName";
    private static final String RESOURCE_GROUP = "resourceGroup";
    private static final String PAYLOAD = "{\"key\":\"value\"}";

    @Mock private JsonUtils jsonUtils;
    @Mock private FunctionService functionService;
    @Mock private IBddVariableContext bddVariableContext;

    @InjectMocks private FunctionSteps functionSteps;

    @Test
    void shouldTriggerFunctionAndSaveResponsesToAScopedVariale()
    {
        Map<String, String> payloadMap = Map.of("key", "value");
        when(jsonUtils.toObject(PAYLOAD, Map.class)).thenReturn(payloadMap);
        mockTrigger(payloadMap);
        functionSteps.triggerFunction(FUNCTION_NAME, APP_NAME, RESOURCE_GROUP, PAYLOAD,
                SCOPES, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(SCOPES, VARIABLE_NAME, Map.of(BODY, DATA));
    }

    private void mockTrigger(Map<String, String> payloadMap)
    {
        Map<String, Object> result = new HashMap<>();
        result.put(BODY, Mono.just(DATA));
        when(functionService.triggerFunction(RESOURCE_GROUP, APP_NAME, FUNCTION_NAME, payloadMap)).thenReturn(result);
    }

    @Test
    void shouldTriggerFunctionWithEmptyPayload()
    {
        Map<String, String> payloadMap = Map.of();
        mockTrigger(payloadMap);
        functionSteps.triggerFunction(FUNCTION_NAME, APP_NAME, RESOURCE_GROUP, "", SCOPES, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(SCOPES, VARIABLE_NAME, Map.of(BODY, DATA));
        verifyNoInteractions(jsonUtils);
    }
}
