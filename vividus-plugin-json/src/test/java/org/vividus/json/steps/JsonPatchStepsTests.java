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

package org.vividus.json.steps;

import static org.mockito.Mockito.verify;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.util.json.JsonJackson3Utils;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class JsonPatchStepsTests
{
    private static final String VARIABLE_NAME = "json";
    private static final String SOURCE_JSON = "{\"a\":\"b\"}";
    private static final String PATCH_JSON = "[{ \"op\": \"replace\", \"path\": \"/a\", \"value\": \"c\" }]";
    private static final String EXPECTED_RESULT = "{\"a\":\"c\"}";

    @Mock
    private VariableContext variableContext;

    @Test
    void patchJsonFile()
    {
        var variableScope = Set.of(VariableScope.SCENARIO);
        var jsonPatchSteps = new JsonPatchSteps(variableContext, new JsonJackson3Utils());
        jsonPatchSteps.patchJsonFile(SOURCE_JSON, PATCH_JSON, variableScope, VARIABLE_NAME);
        verify(variableContext).putVariable(variableScope, VARIABLE_NAME, EXPECTED_RESULT);
    }
}
