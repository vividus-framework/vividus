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

import com.fasterxml.jackson.databind.ObjectMapper;

import org.vividus.mcp.tool.VividusTool;
import org.vividus.mcp.tool.VividusToolParameters;

import io.modelcontextprotocol.server.McpServer;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.transport.StdioServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpServerTransportProvider;

public class VividusMcpServer
{
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final List<VividusTool> tools;

    public VividusMcpServer(List<VividusTool> tools)
    {
        this.tools = tools;
    }

    public void startSyncServer()
    {
        List<SyncToolSpecification> toolSpecs = tools.stream().map(this::asToolSpecification).toList();

        McpServerTransportProvider transportProvider = new StdioServerTransportProvider(objectMapper);
        McpServer.sync(transportProvider)
                 .serverInfo("vividus-mcp-server", "0.6.16")
                 .capabilities(McpSchema.ServerCapabilities.builder()
                     .tools(true)
                     .build())
                 .tools(toolSpecs)
                 .build();
    }

    private McpServerFeatures.SyncToolSpecification asToolSpecification(VividusTool tool)
    {
        VividusToolParameters params = tool.getParameters();
        return McpServerFeatures.SyncToolSpecification.builder()
                .tool(McpSchema.Tool.builder()
                        .name(params.name())
                        .description(params.description())
                        .inputSchema(params.schema())
                        .build())
                .callHandler((exchange, request) -> {
                    try
                    {
                        String content = objectMapper.writeValueAsString(tool.getContent());
                        return new McpSchema.CallToolResult(content, false);
                    }
                    catch (IOException e)
                    {
                        return new McpSchema.CallToolResult(e.getMessage(), true);
                    }
                })
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
