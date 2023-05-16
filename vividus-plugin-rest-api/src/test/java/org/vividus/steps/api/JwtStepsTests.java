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

package org.vividus.steps.api;

import static org.mockito.Mockito.verify;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class JwtStepsTests
{
    private static final String VARIABLE_NAME = "name";
    private static final Set<VariableScope> SCENARIO_SCOPE = Set.of(VariableScope.SCENARIO);

    @Mock private VariableContext variableContext;
    @InjectMocks private JwtSteps jwtSteps;

    @Test
    void shouldGenerateJwtAndSaveResult() throws NoSuchAlgorithmException, InvalidKeyException
    {
        jwtSteps.generateJwtAndSaveResult("header", "payload", "secretKey", SCENARIO_SCOPE, VARIABLE_NAME);
        verify(variableContext).putVariable(SCENARIO_SCOPE, VARIABLE_NAME,
                "aGVhZGVy.cGF5bG9hZA.7ohq4qZ0mhuYy1ot9g6V0G4f_u-mW95j0e6j4-QXDvk");
    }
}
