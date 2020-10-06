/*
 * Copyright 2019-2020 the original author or authors.
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.bdd.variable.IVariablesFactory;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.bdd.variable.Variables;
import org.vividus.testcontext.TestContext;

public class BddVariableContext implements IBddVariableContext
{
    private static final Logger LOGGER = LoggerFactory.getLogger(BddVariableContext.class);
    private static final Class<Variables> VARIABLES_KEY = Variables.class;

    private TestContext testContext;
    private IVariablesFactory variablesFactory;

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getVariable(String variableKey)
    {
        return (T) getVariables().getVariable(variableKey);
    }

    @Override
    public void putVariable(Set<VariableScope> variableScopes, String variableKey, Object variableValue)
    {
        variableScopes.forEach(s -> putVariable(s, variableKey, variableValue));
    }

    @Override
    public void putVariable(VariableScope variableScope, String variableKey, Object variableValue)
    {
        LOGGER.info("Saving a value '{}' into the '{}' variable '{}'", variableValue, variableScope, variableKey);
        switch (variableScope)
        {
            case NEXT_BATCHES:
                variablesFactory.addNextBatchesVariable(variableKey, variableValue);
                break;
            case STORY:
                getVariables().putStoryVariable(variableKey, variableValue);
                break;
            case SCENARIO:
                getVariables().putScenarioVariable(variableKey, variableValue);
                break;
            case STEP:
                getVariables().putStepVariable(variableKey, variableValue);
                break;
            default:
                throw new IllegalArgumentException("Unsupported variable scope: " + variableScope);
        }
    }

    @Override
    public void initVariables()
    {
        getVariables();
    }

    @Override
    public void initStepVariables()
    {
        getVariables().initStepVariables();
    }

    @Override
    public void clearStepVariables()
    {
        getVariables().clearStepVariables();
    }

    @Override
    public void clearScenarioVariables()
    {
        getVariables().clearScenarioVariables();
    }

    @Override
    public void clearStoryVariables()
    {
        getVariables().clearStoryVariables();
    }

    @Override
    public void clearBatchVariables()
    {
        testContext.remove(VARIABLES_KEY);
    }

    private Variables getVariables()
    {
        return testContext.get(VARIABLES_KEY, variablesFactory::createVariables);
    }

    public void setTestContext(TestContext testContext)
    {
        this.testContext = testContext;
    }

    public void setVariablesFactory(IVariablesFactory variablesFactory)
    {
        this.variablesFactory = variablesFactory;
    }
}
