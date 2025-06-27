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

public class GetAllStepsVividusTool implements VividusTool
{
    private final JsonUtils jsonUtils = new JsonUtils();

    @Override
    public VividusToolParameters getParameters()
    {
        return new VividusToolParameters(
            "vividus_get_all_steps",
            "Get a list of available VIVIDUS automation tool steps including their syntax and parameters. "
                + "When a user asks for a list of steps provide exact steps without generalization.",
            """
            {
              "type" : "object",
              "id" : "urn:jsonschema:Operation",
              "properties" : {},
              "required" : [],
              "additionalProperties" : false
            }
            """
        );
    }

    @Override
    public Object getContent()
    {
        List<StepInfo> steps = StepsCollector.getSteps().stream()
                .filter(Predicate.not(Step::isDeprecated))
                .filter(s -> !s.getPattern().contains("debug"))
                .map(s -> new StepInfo(s.getStartingWord() + " " + s.getPattern(), s.getLocation()))
                .toList();

        return new GetAllStepsResponse(steps, readJsonResourceAsMap("parameters.json"),
                readJsonResourceAsMap("expressions.json"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> readJsonResourceAsMap(String resource)
    {
        String json = ResourceUtils.loadResource(resource);
        return jsonUtils.toObject(json, HashMap.class);
    }

    public record GetAllStepsResponse(
        List<StepInfo> steps,
        Map<String, String> stepParameters,
        Map<String, String> expressions)
    {
    }
}
