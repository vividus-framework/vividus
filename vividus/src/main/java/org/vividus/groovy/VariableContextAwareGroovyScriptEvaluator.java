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

package org.vividus.groovy;

import org.springframework.scripting.ScriptSource;
import org.springframework.scripting.groovy.GroovyScriptEvaluator;
import org.vividus.context.VariableContext;

public class VariableContextAwareGroovyScriptEvaluator extends GroovyScriptEvaluator
{
    private final VariableContext variableContext;

    public VariableContextAwareGroovyScriptEvaluator(VariableContext variableContext)
    {
        this.variableContext = variableContext;
    }

    @Override
    public Object evaluate(ScriptSource script)
    {
        return evaluate(script, variableContext.getVariables());
    }
}
