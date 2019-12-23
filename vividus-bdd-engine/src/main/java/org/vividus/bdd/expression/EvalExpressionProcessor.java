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

package org.vividus.bdd.expression;

import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Named;

import org.apache.commons.jexl3.JexlBuilder;
import org.apache.commons.jexl3.JexlEngine;
import org.apache.commons.jexl3.JexlScript;

@Named
public class EvalExpressionProcessor implements IExpressionProcessor
{
    private static final Pattern EVAL_PATTERN = Pattern.compile("^eval\\((.*)\\)$", Pattern.CASE_INSENSITIVE
            | Pattern.DOTALL);
    private static final int EVAL_GROUP = 1;

    // ThreadLocal is used as workaround for not released fix of issue https://issues.apache.org/jira/browse/JEXL-241
    private final ThreadLocal<JexlEngine> jexlEngine = ThreadLocal
            .withInitial(() -> new JexlBuilder().charset(StandardCharsets.UTF_8).create());

    @Override
    public Optional<String> execute(String expression)
    {
        Matcher expressionMatcher = EVAL_PATTERN.matcher(expression);
        if (expressionMatcher.find())
        {
            String expressionToEvaluate = expressionMatcher.group(EVAL_GROUP);
            JexlScript jexlScript = jexlEngine.get().createScript(expressionToEvaluate);
            return Optional.of(jexlScript.execute(JexlEngine.EMPTY_CONTEXT).toString());
        }
        return Optional.empty();
    }
}
