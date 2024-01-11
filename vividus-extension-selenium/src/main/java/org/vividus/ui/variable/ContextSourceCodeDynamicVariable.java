/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.variable;

import static org.vividus.ui.ContextSourceCodeProvider.APPLICATION_SOURCE_CODE;

import org.vividus.ui.ContextSourceCodeProvider;
import org.vividus.variable.DynamicVariable;
import org.vividus.variable.DynamicVariableCalculationResult;

public class ContextSourceCodeDynamicVariable implements DynamicVariable
{
    private final ContextSourceCodeProvider contextSourceCodeProvider;

    public ContextSourceCodeDynamicVariable(ContextSourceCodeProvider contextSourceCodeProvider)
    {
        this.contextSourceCodeProvider = contextSourceCodeProvider;
    }

    @Override
    public DynamicVariableCalculationResult calculateValue()
    {
        String contextSourceCode = contextSourceCodeProvider.getSourceCode().get(APPLICATION_SOURCE_CODE);
        return contextSourceCode != null
                ? DynamicVariableCalculationResult.withValue(contextSourceCode)
                : DynamicVariableCalculationResult.withError("application is not started");
    }
}
