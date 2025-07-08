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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.vividus.mcp.tool.VividusTool;
import org.vividus.mcp.tool.VividusToolParameters;

import io.modelcontextprotocol.server.McpServer.SyncSpecification;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.Tool;

class VividusMcpServerTests
{
    @SuppressWarnings("unchecked")
    @Test
    void shouldStartSyncServer()
    {
        try (MockedStatic<io.modelcontextprotocol.server.McpServer> mcpServerMock = Mockito
                .mockStatic(io.modelcontextprotocol.server.McpServer.class))
        {
            SyncSpecification spec = mock();
            mcpServerMock.when(() -> io.modelcontextprotocol.server.McpServer.sync(any())).thenReturn(spec);
            when(spec.serverInfo("vividus-mcp-server", "0.6.16")).thenReturn(spec);
            when(spec.capabilities(any())).thenReturn(spec);
            ArgumentCaptor<List<SyncToolSpecification>> toolsSpecsCaptor = ArgumentCaptor.forClass(List.class);
            when(spec.tools(toolsSpecsCaptor.capture())).thenReturn(spec);

            VividusTool testTool = new TestVividusTool();
            VividusTool errorTool = spy(testTool);
            String error = "error";
            when(errorTool.getContent()).thenAnswer(inv ->
            {
                throw new IOException(error);
            });
            VividusMcpServer server = new VividusMcpServer(List.of(testTool, errorTool));
            server.startSyncServer();

            verify(spec).build();

            List<SyncToolSpecification> toolSpecs = toolsSpecsCaptor.getValue();
            assertThat(toolSpecs, hasSize(2));
            verifyTool(testTool, toolSpecs.get(0), false, "\"%s\"".formatted(testTool.getContent()));
            verifyTool(errorTool, toolSpecs.get(1), true, error);
        }
    }

    private void verifyTool(VividusTool testTool, SyncToolSpecification toolSpec, boolean error, String content)
    {
        VividusToolParameters params = testTool.getParameters();
        Tool tool = toolSpec.tool();
        assertEquals(params.name(), tool.name());
        assertEquals(params.description(), tool.description());
        assertNull(tool.inputSchema().properties());
        CallToolResult call = toolSpec.call().apply(null, null);
        assertEquals(error, call.isError());
        TextContent textContent = (TextContent) call.content().get(0);
        assertEquals(content, textContent.text());
    }

    private static final class TestVividusTool implements VividusTool
    {
        @Override
        public VividusToolParameters getParameters()
        {
            return new VividusToolParameters("name", "description", "{}");
        }

        @Override
        public Object getContent()
        {
            return "content";
        }
    }
}
