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

package org.vividus.expression;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.vividus.context.VariableContext;

@Named
public class EvalExpressionProcessor extends AbstractExpressionProcessor<String>
{
    private static final Pattern EVAL_PATTERN = Pattern.compile("^eval\\((.*)\\)$", Pattern.CASE_INSENSITIVE
            | Pattern.DOTALL);
    private static final int EVAL_GROUP = 1;
    private static final Map<String, Object> NAMESPACES = Map.of("math", Math.class, "stringUtils", StringUtils.class,
            "wordUtils", WordUtils.class);

    private final JexlEngine jexlEngine = new JexlBuilder().charset(StandardCharsets.UTF_8).namespaces(NAMESPACES)
            .create();

    private final VariableContext variableContext;

    public EvalExpressionProcessor(VariableContext variableContext)
    {
        super(EVAL_PATTERN);
        this.variableContext = variableContext;
    }

    @Override
    protected String evaluateExpression(Matcher expressionMatcher)
    {
        String expressionToEvaluate = expressionMatcher.group(EVAL_GROUP);
        JexlScript jexlScript = jexlEngine.createScript(expressionToEvaluate);
        return String.valueOf(jexlScript.execute(new JexlVariableContext(variableContext)));
    }

    private static final class JexlVariableContext extends MapContext
    {
        private final VariableContext variableContext;

        private JexlVariableContext(VariableContext variableContext)
        {
            this.variableContext = variableContext;
        }

        @Override
        public Object get(String name)
        {
            Object variable = super.get(name);
            return variable == null ? variableContext.getVariable(name) : variable;
        }

        @Override
        public boolean has(String name)
        {
            return null != get(name);
        }
    }
}
