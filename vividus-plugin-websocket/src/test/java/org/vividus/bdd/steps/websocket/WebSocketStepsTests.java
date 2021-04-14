/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.bdd.steps.websocket;

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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.jetty.JettyWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.softassert.SoftAssert;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.util.UriUtils;

@ExtendWith(MockitoExtension.class)
class WebSocketStepsTests
{
    private static final String WEB_SOCKET_KEY = "web-socket-key";
    private static final String WEB_SOCKET_ADDR = "ws://websocket.example.com";
    private static final String MESSAGE = "message";
    private static final String CONNECTION_IS_CLOSED = "WebSocket connection by the key 'web-socket-key' to the "
            + "'ws://websocket.example.com' is either not established or already closed";

    @Spy private SimpleTestContext testContext;
    @Mock private IBddVariableContext bddVariableContext;
    @Mock private SoftAssert softAssert;

    private WebSocketSteps webSocketSteps;

    @Test
    void shouldInit()
    {
        JettyWebSocketClient jettyClient = captureJettyClient();
        webSocketSteps.init();
        verify(jettyClient).start();
    }

    @Test
    void shouldDestroy()
    {
        JettyWebSocketClient jettyClient = captureJettyClient();
        webSocketSteps.destroy();
        verify(jettyClient).stop();
    }

    @Test
    void shouldSendTextMessage() throws InterruptedException, ExecutionException, TimeoutException, IOException
    {
        WebSocketSession session = prepareWebSocketSession(null);
        when(session.isOpen()).thenReturn(true);

        webSocketSteps.connect(WEB_SOCKET_KEY);

        webSocketSteps.sendTextMessage(MESSAGE, WEB_SOCKET_KEY);

        verify(session).sendMessage(argThat(msg ->
        {
            TextMessage textMessage = (TextMessage) msg;
            return MESSAGE.equals(textMessage.getPayload());
        }));
        verifyNoMoreInteractions(session);
    }

    @Test
    void shouldFailOnClosedSocket() throws InterruptedException, ExecutionException, TimeoutException, IOException
    {
        WebSocketSession session = prepareWebSocketSession(null);
        when(session.isOpen()).thenReturn(false);

        webSocketSteps.connect(WEB_SOCKET_KEY);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> webSocketSteps.sendTextMessage(MESSAGE, WEB_SOCKET_KEY));
        assertEquals(CONNECTION_IS_CLOSED, exception.getMessage());
    }

    @Test
    void shouldFailOnUnexsitingKey() throws IOException
    {
        initSteps();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> webSocketSteps.disconnect("unexisting-web-socket-key"));
        assertEquals("WebSocket with the key 'unexisting-web-socket-key' does not exists", exception.getMessage());
    }

    @Test
    void shouldFailOnUnexsitingWebSocket() throws IOException
    {
        initSteps();
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> webSocketSteps.disconnect(WEB_SOCKET_KEY));
        assertEquals(CONNECTION_IS_CLOSED, exception.getMessage());
    }

    @Test
    void shouldDisconnect() throws InterruptedException, ExecutionException, TimeoutException, IOException
    {
        WebSocketSession session = prepareWebSocketSession(null);
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
        WebSocketSession session = prepareWebSocketSession(null);

        webSocketSteps.connect(WEB_SOCKET_KEY);
        webSocketSteps.connect(WEB_SOCKET_KEY);

        verify(session).close();
        verifyNoMoreInteractions(session);
    }

    @Test
    void shouldCleanUp() throws InterruptedException, ExecutionException, TimeoutException, IOException
    {
        WebSocketSession session = prepareWebSocketSession(null);

        webSocketSteps.connect(WEB_SOCKET_KEY);

        webSocketSteps.cleanUp();

        verify(session).close();
        verifyNoMoreInteractions(session);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void shouldDrainTextMessagesToVariable(boolean open) throws Exception
    {
        ArgumentCaptor<TextWebSocketHandler> captor = ArgumentCaptor.forClass(TextWebSocketHandler.class);
        WebSocketSession session = prepareWebSocketSession(captor);
        when(session.isOpen()).thenReturn(open);

        webSocketSteps.connect(WEB_SOCKET_KEY);

        String varableName = "variable-name";
        captor.getValue().handleMessage(session, new TextMessage(MESSAGE));
        webSocketSteps.drainTextMessagesToVariable(WEB_SOCKET_KEY, Set.of(VariableScope.STORY), varableName);

        verify(bddVariableContext).putVariable(Set.of(VariableScope.STORY), varableName, List.of(MESSAGE));
        verifyNoMoreInteractions(session);
    }

    @Test
    void shouldWaitForTextMessages() throws Exception
    {
        ArgumentCaptor<TextWebSocketHandler> captor = ArgumentCaptor.forClass(TextWebSocketHandler.class);
        WebSocketSession session = prepareWebSocketSession(captor);
        when(session.isOpen()).thenReturn(true);

        webSocketSteps.connect(WEB_SOCKET_KEY);

        captor.getValue().handleMessage(session, new TextMessage(MESSAGE));
        webSocketSteps.waitForTextMessages(Duration.ofSeconds(1), WEB_SOCKET_KEY, ComparisonRule.GREATER_THAN, 0);

        verify(softAssert).assertThat(eq("Total count of messages received over WebSocket"), eq(1),
            argThat(arg -> "a value greater than <0>".equals(arg.toString())));
        verifyNoMoreInteractions(session);
    }

    @SuppressWarnings("unchecked")
    private WebSocketSession prepareWebSocketSession(ArgumentCaptor<TextWebSocketHandler> handlerCaptor)
            throws InterruptedException, ExecutionException, TimeoutException
    {
        JettyWebSocketClient jettyClient = captureJettyClient();
        ListenableFuture<WebSocketSession> future = mock(ListenableFuture.class);
        WebSocketSession session = mock(WebSocketSession.class);

        when(jettyClient.doHandshake(handlerCaptor == null ? any() : handlerCaptor.capture(),
                any(WebSocketHttpHeaders.class), eq(UriUtils.createUri(WEB_SOCKET_ADDR)))).thenReturn(future);
        when(future.get(60, TimeUnit.SECONDS)).thenReturn(session);

        return session;
    }

    private void initSteps()
    {
        this.webSocketSteps = new WebSocketSteps(Map.of(WEB_SOCKET_KEY, UriUtils.createUri(WEB_SOCKET_ADDR)),
                testContext, bddVariableContext, softAssert);
    }

    private JettyWebSocketClient captureJettyClient()
    {
        try (MockedConstruction<JettyWebSocketClient> jettyClientConstruction = Mockito
                .mockConstruction(JettyWebSocketClient.class))
        {
            initSteps();

            assertThat(jettyClientConstruction.constructed(), hasSize(1));

            return jettyClientConstruction.constructed().get(0);
        }
    }
}
