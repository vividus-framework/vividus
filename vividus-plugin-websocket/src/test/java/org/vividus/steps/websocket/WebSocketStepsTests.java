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

package org.vividus.steps.websocket;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class WebSocketStepsTests
{
    private static final String WEB_SOCKET_KEY = "web-socket-key";
    private static final String WEB_SOCKET_ENDPOINT = "ws://websocket.example.com";
    private static final String MESSAGE = "message";
    private static final String CONNECTION_IS_CLOSED = "WebSocket connection by the key 'web-socket-key' to the "
            + "'ws://websocket.example.com' is either not established or already closed";

    @Spy private SimpleTestContext testContext;
    @Mock private VariableContext variableContext;
    @Mock private ISoftAssert softAssert;

    private WebSocketSteps webSocketSteps;

    @Test
    void shouldSendTextMessage() throws InterruptedException, ExecutionException, TimeoutException, IOException
    {
        var session = prepareWebSocketSession(null);
        when(session.isOpen()).thenReturn(true);

        webSocketSteps.connect(WEB_SOCKET_KEY);

        webSocketSteps.sendTextMessage(MESSAGE, WEB_SOCKET_KEY);

        verify(session).sendMessage(argThat(msg ->
        {
            var textMessage = (TextMessage) msg;
            return MESSAGE.equals(textMessage.getPayload());
        }));
        verifyNoMoreInteractions(session);
    }

    @Test
    void shouldFailOnClosedSocket() throws InterruptedException, ExecutionException, TimeoutException, IOException
    {
        var session = prepareWebSocketSession(null);
        when(session.isOpen()).thenReturn(false);

        webSocketSteps.connect(WEB_SOCKET_KEY);

        var exception = assertThrows(IllegalArgumentException.class,
            () -> webSocketSteps.sendTextMessage(MESSAGE, WEB_SOCKET_KEY));
        assertEquals(CONNECTION_IS_CLOSED, exception.getMessage());
    }

    @Test
    void shouldFailOnUnexsitingKey()
    {
        initSteps();
        var exception = assertThrows(IllegalArgumentException.class,
            () -> webSocketSteps.disconnect("unexisting-web-socket-key"));
        assertEquals("WebSocket with the key 'unexisting-web-socket-key' does not exists", exception.getMessage());
    }

    @Test
    void shouldFailOnUnexsitingWebSocket()
    {
        initSteps();
        var exception = assertThrows(IllegalArgumentException.class,
            () -> webSocketSteps.disconnect(WEB_SOCKET_KEY));
        assertEquals(CONNECTION_IS_CLOSED, exception.getMessage());
    }

    @Test
    void shouldDisconnect() throws InterruptedException, ExecutionException, TimeoutException, IOException
    {
        var session = prepareWebSocketSession(null);
        when(session.isOpen()).thenReturn(true);

        webSocketSteps.connect(WEB_SOCKET_KEY);

        webSocketSteps.disconnect(WEB_SOCKET_KEY);

        verify(session).close();
        verifyNoMoreInteractions(session);
    }

    @Test
    void shouldCloseExistingWebSocketIfNewIsStarted() throws InterruptedException, ExecutionException, TimeoutException,
        IOException
    {
        var session = prepareWebSocketSession(null);

        webSocketSteps.connect(WEB_SOCKET_KEY);
        webSocketSteps.connect(WEB_SOCKET_KEY);

        verify(session).close();
        verifyNoMoreInteractions(session);
    }

    @Test
    void shouldCleanUp() throws InterruptedException, ExecutionException, TimeoutException, IOException
    {
        var session = prepareWebSocketSession(null);

        webSocketSteps.connect(WEB_SOCKET_KEY);

        webSocketSteps.cleanUp();

        verify(session).close();
        verifyNoMoreInteractions(session);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void shouldDrainTextMessagesToVariable(boolean open) throws Exception
    {
        var captor = ArgumentCaptor.forClass(TextWebSocketHandler.class);
        var session = prepareWebSocketSession(captor);
        when(session.isOpen()).thenReturn(open);

        webSocketSteps.connect(WEB_SOCKET_KEY);

        var varableName = "variable-name";
        captor.getValue().handleMessage(session, new TextMessage(MESSAGE));
        webSocketSteps.drainTextMessagesToVariable(WEB_SOCKET_KEY, Set.of(VariableScope.STORY), varableName);

        verify(variableContext).putVariable(Set.of(VariableScope.STORY), varableName, List.of(MESSAGE));
        verifyNoMoreInteractions(session);
    }

    @Test
    void shouldWaitForTextMessages() throws Exception
    {
        var captor = ArgumentCaptor.forClass(TextWebSocketHandler.class);
        var session = prepareWebSocketSession(captor);
        when(session.isOpen()).thenReturn(true);

        webSocketSteps.connect(WEB_SOCKET_KEY);

        captor.getValue().handleMessage(session, new TextMessage(MESSAGE));
        webSocketSteps.waitForTextMessages(Duration.ofSeconds(1), WEB_SOCKET_KEY, ComparisonRule.GREATER_THAN, 0);

        verify(softAssert).assertThat(eq("Total count of messages received over WebSocket"), eq(1),
            argThat(arg -> "a value greater than <0>".equals(arg.toString())));
        verifyNoMoreInteractions(session);
    }

    private WebSocketSession prepareWebSocketSession(ArgumentCaptor<TextWebSocketHandler> handlerCaptor)
            throws InterruptedException, ExecutionException, TimeoutException
    {
        var webSocketClient = captureWebSocketClient();
        CompletableFuture<WebSocketSession> future = mock();
        var session = mock(WebSocketSession.class);

        when(webSocketClient.execute(handlerCaptor == null ? any() : handlerCaptor.capture(),
                eq(WEB_SOCKET_ENDPOINT))).thenReturn(future);
        when(future.get(60, TimeUnit.SECONDS)).thenReturn(session);

        return session;
    }

    private void initSteps()
    {
        this.webSocketSteps = new WebSocketSteps(Map.of(WEB_SOCKET_KEY, WEB_SOCKET_ENDPOINT), testContext,
                variableContext, softAssert);
    }

    private StandardWebSocketClient captureWebSocketClient()
    {
        try (var webSocketClientConstruction = Mockito.mockConstruction(StandardWebSocketClient.class))
        {
            initSteps();

            assertThat(webSocketClientConstruction.constructed(), hasSize(1));

            return webSocketClientConstruction.constructed().get(0);
        }
    }
}
