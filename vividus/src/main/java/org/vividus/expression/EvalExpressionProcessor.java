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

package org.vividus.expression;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlScript;
import org.apache.commons.jexl3.MapContext;
import org.apache.commons.jexl3.introspection.JexlPermissions;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.jbehave.core.expressions.SingleArgExpressionProcessor;
import org.vividus.context.VariableContext;

public class EvalExpressionProcessor extends SingleArgExpressionProcessor<String>
{
    @SuppressWarnings({ "checkstyle:TodoComment", "checkstyle:TrailingComment" })
    private static final JexlEngine JEXL_ENGINE = new JexlBuilder()
            .charset(StandardCharsets.UTF_8)
            .permissions(JexlPermissions.UNRESTRICTED) // TODO: Change to RESTRICTED in VIVIDUS 0.6.0
            .namespaces(Map.of(
                    "math", Math.class,
                    "stringUtils", StringUtils.class,
                    "wordUtils", WordUtils.class)
            )
            .create();

    public EvalExpressionProcessor(VariableContext variableContext)
    {
        super("eval", expressionToEvaluate -> {
            JexlScript jexlScript = JEXL_ENGINE.createScript(expressionToEvaluate);
            return String.valueOf(jexlScript.execute(new JexlVariableContext(variableContext)));
        });
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
            return Optional.ofNullable(super.get(name)).orElseGet(() -> variableContext.getVariable(name));
        }

        @Override
        public boolean has(String name)
        {
            return null != get(name);
        }
    }
}
