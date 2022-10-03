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

package org.vividus.json.variable;

import java.util.Optional;

import org.vividus.json.JsonContext;
import org.vividus.variable.DynamicVariable;
import org.vividus.variable.DynamicVariableCalculationResult;

public class JsonContextVariable implements DynamicVariable
{
    private final JsonContext jsonContext;

    public JsonContextVariable(JsonContext jsonContext)
    {
        this.jsonContext = jsonContext;
    }

    @Override
    public DynamicVariableCalculationResult calculateValue()
    {
        return DynamicVariableCalculationResult.withValueOrError(
                Optional.ofNullable(jsonContext.getJsonContext()),
                () -> "JSON context is not set: neither HTTP request was executed, nor outer step set it"
        );
    }
}
