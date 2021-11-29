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

package org.vividus.context;

import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.testcontext.TestContext;
import org.vividus.util.EnumUtils;
import org.vividus.variable.IVariablesFactory;
import org.vividus.variable.VariableScope;
import org.vividus.variable.Variables;

public class VariableTestContext implements VariableContext
{
    private static final Logger LOGGER = LoggerFactory.getLogger(VariableTestContext.class);
    private static final Class<Variables> VARIABLES_KEY = Variables.class;

    private final TestContext testContext;
    private final IVariablesFactory variablesFactory;

    public VariableTestContext(TestContext testContext, IVariablesFactory variablesFactory)
    {
        this.testContext = testContext;
        this.variablesFactory = variablesFactory;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String variableKey)
    {
        return (T) getScopedVariables().getVariable(variableKey);
    }

    @Override
    public void putVariable(Set<VariableScope> variableScopes, String variableKey, Object variableValue)
    {
        variableScopes.forEach(s -> putVariable(s, variableKey, variableValue));
    }

    @Override
    public void putVariable(VariableScope variableScope, String variableKey, Object variableValue)
    {
        LOGGER.atInfo()
                .addArgument(variableValue)
                .addArgument(() -> EnumUtils.toHumanReadableForm(variableScope))
                .addArgument(variableKey)
                .log("Saving a value '{}' into the {} variable '{}'");
        switch (variableScope)
        {
            case NEXT_BATCHES:
                variablesFactory.addNextBatchesVariable(variableKey, variableValue);
                break;
            case STORY:
                getScopedVariables().putStoryVariable(variableKey, variableValue);
                break;
            case SCENARIO:
                getScopedVariables().putScenarioVariable(variableKey, variableValue);
                break;
            case STEP:
                getScopedVariables().putStepVariable(variableKey, variableValue);
                break;
            default:
                throw new IllegalArgumentException("Unsupported variable scope: " + variableScope);
        }
    }

    @Override
    public void initVariables()
    {
        getScopedVariables();
    }

    @Override
    public void initStepVariables()
    {
        getScopedVariables().initStepVariables();
    }

    @Override
    public void clearStepVariables()
    {
        getScopedVariables().clearStepVariables();
    }

    @Override
    public void clearScenarioVariables()
    {
        getScopedVariables().clearScenarioVariables();
    }

    @Override
    public void clearBatchVariables()
    {
        testContext.remove(VARIABLES_KEY);
    }

    @Override
    public Map<String, Object> getVariables()
    {
        return getScopedVariables().getVariables();
    }

    private Variables getScopedVariables()
    {
        return testContext.get(VARIABLES_KEY, variablesFactory::createVariables);
    }
}
