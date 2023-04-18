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

import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jbehave.core.expressions.DelegatingExpressionProcessor;
import org.jbehave.core.expressions.RelaxedMultiArgExpressionProcessor;

public class ReplaceByRegExpProcessors extends DelegatingExpressionProcessor
{
    private static final int ARGS_LIMIT = 3;

    public ReplaceByRegExpProcessors()
    {
        super(List.of(
                createReplaceExpression("replaceFirstByRegExp", Matcher::replaceFirst),
                createReplaceExpression("replaceAllByRegExp", Matcher::replaceAll)
        ));
    }

    private static RelaxedMultiArgExpressionProcessor<String> createReplaceExpression(String expressionName,
            BiFunction<Matcher, String, String> replacer)
    {
        return new RelaxedMultiArgExpressionProcessor<>(expressionName, ARGS_LIMIT, args -> {
            String regex = args.get(0);
            String replacement = args.get(1);
            String input = args.get(2);
            return replacer.apply(Pattern.compile(regex, Pattern.DOTALL).matcher(input), replacement);
        });
    }
}
