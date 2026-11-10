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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.vividus.mcp.VividusMcpServer.StepInfo;
import org.vividus.runner.StepsCollector;
import org.vividus.runner.StepsCollector.Step;
import org.vividus.util.ResourceUtils;
import org.vividus.util.json.JsonUtils;

import io.modelcontextprotocol.spec.McpSchema;

public class GetAllFeaturesVividusTool implements VividusTool
{
    private final JsonUtils jsonUtils = new JsonUtils();

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

        return new GetAllFeaturesResponse(steps, readJsonResourceAsMap("parameters.json"),
                readJsonResourceAsMap("expressions.json"));
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
        Map<String, String> expressions)
    {
    }
}
