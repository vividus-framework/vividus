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

import static org.apache.commons.lang3.Validate.isTrue;

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.hamcrest.Matcher;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.When;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.jetty.JettyWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.softassert.SoftAssert;
import org.vividus.testcontext.TestContext;
import org.vividus.util.wait.DurationBasedWaiter;

public class WebSocketSteps
{
    private static final Object KEY = WebSocketContainer.class;
    private static final long DEFAULT_TIMEOUT = 60;

    private final Map<String, URI> webSocketConnections;
    private final TestContext testContext;
    private final IBddVariableContext bddVariableContext;
    private final SoftAssert softAssert;
    private final JettyWebSocketClient webSocketClient;

    public WebSocketSteps(Map<String, URI> webSocketConnections, TestContext testContext,
            IBddVariableContext bddVariableContext, SoftAssert softAssert)
    {
        this.webSocketConnections = webSocketConnections;
        this.testContext = testContext;
        this.bddVariableContext = bddVariableContext;
        this.softAssert = softAssert;
        this.webSocketClient = new JettyWebSocketClient();
    }

    public void init()
    {
        this.webSocketClient.start();
    }

    public void destroy()
    {
        this.webSocketClient.stop();
    }

    /**
     * Creates a new websocket connection.
     *
     * @param webSocketConnectionKey the websocket connection key
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ExecutionException if the computation threw an exception
     * @throws TimeoutException if the wait timed out
     * @throws IOException in case of any error happened at I/O operations
     */
    @When("I connect to `$webSocketConnectionKey` websocket")
    public void connect(String webSocketConnectionKey) throws InterruptedException, ExecutionException,
        TimeoutException, IOException
    {
        URI webSocketEndpoint = getWebSocketEndpoint(webSocketConnectionKey);
        WebSocket webSocket = getContainer().getSocket(webSocketEndpoint);
        if (webSocket != null)
        {
            webSocket.close();
        }

        WebSocketTextMessageCollector collector = new WebSocketTextMessageCollector();
        WebSocketSession session = webSocketClient.doHandshake(collector, new WebSocketHttpHeaders(), webSocketEndpoint)
                .get(DEFAULT_TIMEOUT, TimeUnit.SECONDS);

        getContainer().putSocket(webSocketEndpoint, new WebSocket(session, collector));
    }

    /**
     * Sends a text message over the websocket.
     *
     * @param message                the text message to send
     * @param webSocketConnectionKey the websocket connection key
     * @throws IOException in case of any error happened at I/O operations
     */
    @When("I send text message `$message` over `$webSocketConnectionKey` websocket")
    public void sendTextMessage(String message, String webSocketConnectionKey) throws IOException
    {
        getWebSocket(webSocketConnectionKey, false).sendMessage(message);
    }

    /**
     * Disconnects from an existing websocket. All the received text messages are kept and can be drained into the
     * variable using the drain step.
     *
     * @param webSocketConnectionKey the websocket connection key
     * @throws IOException in case of any error happened at I/O operations
     */
    @When("I disconnect from `$webSocketConnectionKey` websocket")
    public void disconnect(String webSocketConnectionKey) throws IOException
    {
        getWebSocket(webSocketConnectionKey, false).close();
    }

    /**
     * Waits until the count of the text messages received over the specified websocket matches to the rule or until
     * the timeout is exceeded.
     *
     * @param timeout                the maximum time to wait for the messages in ISO-8601 format
     * @param webSocketConnectionKey the websocket connection key
     * @param comparisonRule         The rule to match the variable value. Allowed options:
     *                               <ul>
     *                               <li>less than (&lt;)</li>
     *                               <li>less than or equal to (&lt;=)</li>
     *                               <li>greater than (&gt;)</li>
     *                               <li>greater than or equal to (&gt;=)</li>
     *                               <li>equal to (=)</li>
     *                               <li>not equal to (!=)</li>
     *                               </ul>
     * @param expectedCount          the expected count of the messages to be matched by the rule
     */
    @When("I wait with `$timeout` timeout until count of text messages received over `$webSocketConnectionKey`"
            + " websocket is $comparisonRule `$expectedCount`")
    public void waitForTextMessages(Duration timeout, String webSocketConnectionKey, ComparisonRule comparisonRule,
            int expectedCount)
    {
        WebSocket webSocket = getWebSocket(webSocketConnectionKey, false);
        Matcher<Integer> countMatcher = comparisonRule.getComparisonRule(expectedCount);
        Integer result = new DurationBasedWaiter(timeout, Duration.ofSeconds(1))
                .wait(() -> webSocket.getMessages().size(), countMatcher::matches);
        softAssert.assertThat("Total count of messages received over WebSocket", result, countMatcher);
    }

    /**
     * Drains the text messages received over the specified websocket to the specified variable. If the websocket is
     * not disconnected, the new messages might arrive after the draining. If the websocket is disconnected, all
     * the messages received after the websocket is connected or after the last draining operation are stored to
     * the variable.
     *
     * @param webSocketConnectionKey the websocket connection key
     * @param scopes                 the set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's
     *                               scope<br>
     *                               <i>Available scopes:</i>
     *                               <ul>
     *                               <li><b>STEP</b> - the variable will be available only within the step,
     *                               <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                               <li><b>STORY</b> - the variable will be available within the whole story,
     *                               <li><b>NEXT_BATCHES</b> - the variable will be available starting from next
     *                               batch
     *                               </ul>
     * @param variableName           the variable name to store the messages. The messages are accessible via
     *                               zero-based index, e.g. `${my-var[0]}` will return the first received message.
     */
    @When("I drain text messages received over `$webSocketConnectionKey` websocket to $scopes variable `$variableName`")
    public void drainTextMessagesToVariable(String webSocketConnectionKey, Set<VariableScope> scopes,
            String variableName)
    {
        List<String> messages = new ArrayList<>();
        getWebSocket(webSocketConnectionKey, true).getMessages().drainTo(messages);
        bddVariableContext.putVariable(scopes, variableName, messages);
    }

    @AfterStory
    public void cleanUp() throws IOException
    {
        for (WebSocket socket : getContainer().getSockets())
        {
            socket.close();
        }
    }

    private URI getWebSocketEndpoint(String webSocketConnectionKey)
    {
        URI webSocketEndpoint = webSocketConnections.get(webSocketConnectionKey);
        isTrue(webSocketEndpoint != null, "WebSocket with the key '%s' does not exists", webSocketConnectionKey);
        return webSocketEndpoint;
    }

    private WebSocket getWebSocket(String webSocketConnectionKey, boolean allowClosed)
    {
        URI webSocketEndpoint = getWebSocketEndpoint(webSocketConnectionKey);
        WebSocket webSocket = getContainer().getSocket(webSocketEndpoint);
        isTrue(webSocket != null && (webSocket.isOpen() || allowClosed),
                "WebSocket connection by the key '%s' to the '%s' is either not established or already closed",
                webSocketConnectionKey, webSocketEndpoint);
        return webSocket;
    }

    private WebSocketContainer getContainer()
    {
        return testContext.get(KEY, WebSocketContainer::new);
    }

    private static final class WebSocketContainer
    {
        private final Map<URI, WebSocket> sockets = new HashMap<>();

        private WebSocket getSocket(URI endpoint)
        {
            return sockets.get(endpoint);
        }

        private Collection<WebSocket> getSockets()
        {
            return sockets.values();
        }

        private void putSocket(URI endpoint, WebSocket socket)
        {
            this.sockets.put(endpoint, socket);
        }
    }

    private static final class WebSocket
    {
        private final WebSocketSession session;
        private final WebSocketTextMessageCollector collector;

        private WebSocket(WebSocketSession session, WebSocketTextMessageCollector collector)
        {
            this.session = session;
            this.collector = collector;
        }

        private void close() throws IOException
        {
            session.close();
        }

        private boolean isOpen()
        {
            return session.isOpen();
        }

        private void sendMessage(String message) throws IOException
        {
            TextMessage textMessage = new TextMessage(message);
            session.sendMessage(textMessage);
        }

        private BlockingQueue<String> getMessages()
        {
            return collector.getMessages();
        }
    }

    private static final class WebSocketTextMessageCollector extends TextWebSocketHandler
    {
        private final BlockingQueue<String> messages = new LinkedBlockingDeque<>();

        @Override
        protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception
        {
            messages.add(message.getPayload());
        }

        private BlockingQueue<String> getMessages()
        {
            return messages;
        }
    }
}
