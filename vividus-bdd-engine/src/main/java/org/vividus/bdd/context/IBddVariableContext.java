/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.bdd.context;

import java.util.Set;

import org.vividus.bdd.variable.VariableScope;

public interface IBddVariableContext
{
    <T> T getVariable(String variableKey);

    void putVariable(VariableScope variableScope, String variableKey, Object variableValue);

    void putVariable(Set<VariableScope> variableScopes, String variableKey, Object variableValue);

    void initVariables();

    void clearVariables(VariableScope variableScope);
}
