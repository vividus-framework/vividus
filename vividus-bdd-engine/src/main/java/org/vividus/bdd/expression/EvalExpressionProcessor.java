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

package org.vividus.bdd.expression;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.lang3.StringUtils;
import org.vividus.bdd.context.IBddVariableContext;

@Named
public class EvalExpressionProcessor implements IExpressionProcessor<String>
{
    private static final Pattern EVAL_PATTERN = Pattern.compile("^eval\\((.*)\\)$", Pattern.CASE_INSENSITIVE
            | Pattern.DOTALL);
    private static final int EVAL_GROUP = 1;
    private static final Map<String, Object> NAMESPACES = Map.of("math", Math.class, "stringUtils", StringUtils.class);

    // ThreadLocal is used as workaround for not released fix of issue https://issues.apache.org/jira/browse/JEXL-241
    private final ThreadLocal<JexlEngine> jexlEngine = ThreadLocal
            .withInitial(() -> new JexlBuilder().charset(StandardCharsets.UTF_8).namespaces(NAMESPACES).create());

    private IBddVariableContext bddVariableContext;

    public EvalExpressionProcessor(IBddVariableContext bddVariableContext)
    {
        this.bddVariableContext = bddVariableContext;
    }

    @Override
    public Optional<String> execute(String expression)
    {
        Matcher expressionMatcher = EVAL_PATTERN.matcher(expression);
        if (expressionMatcher.find())
        {
            String expressionToEvaluate = expressionMatcher.group(EVAL_GROUP);
            JexlScript jexlScript = jexlEngine.get().createScript(expressionToEvaluate);
            return Optional.of(String.valueOf(jexlScript.execute(new JexlBddVariableContext(bddVariableContext))));
        }
        return Optional.empty();
    }

    private static final class JexlBddVariableContext extends MapContext
    {
        private final IBddVariableContext bddVariableContext;

        private JexlBddVariableContext(IBddVariableContext bddVariableContext)
        {
            this.bddVariableContext = bddVariableContext;
        }

        @Override
        public Object get(String name)
        {
            Object variable = super.get(name);
            return variable == null ? bddVariableContext.getVariable(name) : variable;
        }

        @Override
        public boolean has(String name)
        {
            return null != get(name);
        }
    }
}
