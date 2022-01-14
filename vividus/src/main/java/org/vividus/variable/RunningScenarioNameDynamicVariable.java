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

package org.vividus.variable;

import java.util.Optional;

import org.vividus.context.RunContext;
import org.vividus.model.RunningScenario;

public class RunningScenarioNameDynamicVariable implements DynamicVariable
{
    private final RunContext runContext;

    public RunningScenarioNameDynamicVariable(RunContext runContext)
    {
        this.runContext = runContext;
    }

    @Override
    public DynamicVariableCalculationResult calculateValue()
    {
        return DynamicVariableCalculationResult.withValueOrError(
                Optional.ofNullable(runContext.getRunningStory().getRunningScenario()).map(RunningScenario::getTitle),
                () -> "no scenario is running"
        );
    }
}
