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

package org.vividus.mcp;

import java.io.IOException;
import java.util.List;
import java.util.function.Predicate;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.vividus.runner.StepsCollector;
import org.vividus.runner.StepsCollector.Step;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerTransportProvider;

public class VividusMcpServer
{
    private final ObjectMapper objectMapper = new ObjectMapper();

    public void startSyncServer()
    {
        McpServerFeatures.SyncToolSpecification steps = new McpServerFeatures.SyncToolSpecification(
                new McpSchema.Tool("vividus_get_all_steps",
                    "Get a list of available VIVIDUS automation tool steps including their syntax and parameters. "
                    + "When a user asks for a list of steps provide exact steps without generalization.", """
                {
                  "type" : "object",
                  "id" : "urn:jsonschema:Operation",
                  "properties" : {},
                  "required" : [],
                  "additionalProperties" : false
                }
                """),
                (exchange, args) ->
                {
                    try
                    {
                        List<StepInfo> infos = StepsCollector.getSteps().stream()
                                .filter(Predicate.not(Step::isDeprecated))
                                .map(s -> new StepInfo(s.getStartingWord() + " " + s.getPattern(), s.getLocation()))
                                .toList();

                        String infosAsString = objectMapper.writeValueAsString(infos);
                        return new McpSchema.CallToolResult(infosAsString, false);
                    }
                    catch (IOException e)
                    {
                        return new McpSchema.CallToolResult(e.getMessage(), true);
                    }
                });

        McpServerTransportProvider transportProvider = new StdioServerTransportProvider(objectMapper);
        McpServer.sync(transportProvider)
                 .serverInfo("vividus-mcp-server", "0.6.16")
                 .capabilities(McpSchema.ServerCapabilities.builder()
                     .tools(true)
                     .build())
                 .tools(steps)
                 .build();
    }

    public static class StepInfo
    {
        private final String name;
        private final String module;

        public StepInfo(String name, String module)
        {
            this.name = name;
            this.module = module;
        }

        public String getName()
        {
            return name;
        }

        public String getModule()
        {
            return module;
        }
    }
}
