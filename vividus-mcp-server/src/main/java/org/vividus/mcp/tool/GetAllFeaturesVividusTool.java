/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.mcp.tool;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.jbehave.core.expressions.DelegatingExpressionProcessor;
import org.jbehave.core.expressions.ExpressionProcessor;
import org.jbehave.core.expressions.MultiArgExpressionProcessor;
import org.vividus.mcp.VividusMcpServer.StepInfo;
import org.vividus.runner.StepsCollector;
import org.vividus.runner.StepsCollector.Step;
import org.vividus.util.ResourceUtils;
import org.vividus.util.json.JsonUtils;
import org.vividus.variable.DynamicVariable;

import io.modelcontextprotocol.spec.McpSchema;

public class GetAllFeaturesVividusTool implements VividusTool
{
    private static final Field EXPRESSION_NAME_FIELD;
    private static final Field DELEGATES_FIELD;

    static
    {
        try
        {
            EXPRESSION_NAME_FIELD = MultiArgExpressionProcessor.class.getDeclaredField("expressionName");
            EXPRESSION_NAME_FIELD.setAccessible(true);
            DELEGATES_FIELD = DelegatingExpressionProcessor.class.getDeclaredField("delegates");
            DELEGATES_FIELD.setAccessible(true);
        }
        catch (NoSuchFieldException e)
        {
            throw new ExceptionInInitializerError(e);
        }
    }

    private final JsonUtils jsonUtils = new JsonUtils();
    private final List<ExpressionProcessor<?>> expressionProcessors;
    private final Map<String, DynamicVariable> dynamicVariables;

    public GetAllFeaturesVividusTool(List<ExpressionProcessor<?>> expressionProcessors,
            Map<String, DynamicVariable> dynamicVariables)
    {
        this.expressionProcessors = expressionProcessors;
        this.dynamicVariables = dynamicVariables;
    }

    @SuppressWarnings("LineLength")
    @Override
    public McpSchema.Tool getMcpTool()
    {
        return McpSchema.Tool.builder()
                .name("vividus_get_all_features")
                .description("""
                Return a list of available VIVIDUS automation tool features. CRITICAL CONSTRAINTS:
                * exact syntax of steps, step parameters and expressions must be preserved.
                * do NOT create, modify, or assume any steps that are not explicitly listed.
                * all the parameter values for steps must be must be extracted from web page to preserve letter case and not from the user input.
                * caseInsensitiveText locator strategy must be used for any web element interactions involving text.
                * cssSelector locator strategy must use standard CSS selector.
                * all steps that accept locator and contain substeps are contextual, meaning that locators in substeps must be build relatively to the parent locator.
                * when a user asks for a list of steps provide exact steps without generalization.
                """)
                .inputSchema(new McpSchema.JsonSchema(
                        "object",
                        null,
                        null,
                        null,
                        null,
                        null
                ))
                .build();
    }

    @Override
    public Object getContent()
    {
        List<StepInfo> steps = StepsCollector.getSteps().stream()
                .filter(Predicate.not(Step::isDeprecated))
                .filter(s -> !s.getPattern().contains("debug"))
                .map(s -> new StepInfo(s.getStartingWord() + " " + s.getPattern(), s.getLocation()))
                .toList();

        List<String> expressions = expressionProcessors.stream()
                .flatMap(p -> extractExpressionNames(p).stream())
                .sorted()
                .distinct()
                .toList();

        List<String> dynamicVariableNames = dynamicVariables.keySet().stream()
                .sorted()
                .map(name -> "${" + name + "}")
                .toList();

        return new GetAllFeaturesResponse(steps, readJsonResourceAsMap("parameters.json"),
                expressions, dynamicVariableNames);
    }

    private static List<String> extractExpressionNames(ExpressionProcessor<?> processor)
    {
        if (processor instanceof DelegatingExpressionProcessor delegating)
        {
            try
            {
                @SuppressWarnings("unchecked")
                Collection<ExpressionProcessor<?>> delegates =
                        (Collection<ExpressionProcessor<?>>) DELEGATES_FIELD.get(delegating);
                return delegates.stream()
                        .flatMap(d -> extractExpressionNames(d).stream())
                        .toList();
            }
            catch (IllegalAccessException e)
            {
                return List.of();
            }
        }
        if (processor instanceof MultiArgExpressionProcessor<?> multiArg)
        {
            try
            {
                return List.of("#{" + EXPRESSION_NAME_FIELD.get(multiArg) + "(...)}");
            }
            catch (IllegalAccessException e)
            {
                return List.of();
            }
        }
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> readJsonResourceAsMap(String resource)
    {
        String json = ResourceUtils.loadResource(resource);
        return jsonUtils.toObject(json, HashMap.class);
    }

    public record GetAllFeaturesResponse(
        List<StepInfo> steps,
        Map<String, String> stepParameters,
        List<String> expressions,
        List<String> dynamicVariables)
    {
    }
}
