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

package org.vividus.bdd.browserstack.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.util.Set;

import com.browserstack.client.exception.BrowserStackException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.browserstack.BrowserStackAutomateClient;
import org.vividus.json.JsonContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.event.AfterWebDriverQuitEvent;
import org.vividus.testcontext.TestContext;

@ExtendWith(MockitoExtension.class)
class DebugStepsTests
{
    private static final String VARIABLE_NAME = "variable-name";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.STORY);
    private static final String PREVIOUS_SESSION_ID = "previousSessionId";
    private static final String SESSION_ID = "session-id";
    private static final String NETWORK_LOGS = "network-logs";

    @Mock private BrowserStackAutomateClient automateClient;
    @Mock private IBddVariableContext bddVariableContext;
    @Mock private TestContext textContext;
    @Mock private JsonContext jsonContext;
    @Mock private IAttachmentPublisher attachmentPublisher;
    @InjectMocks private DebugSteps debugSteps;

    @Test
    void shouldSaveNetworkLogs() throws BrowserStackException
    {
        when(textContext.get(PREVIOUS_SESSION_ID)).thenReturn(SESSION_ID);
        when(automateClient.getNetworkLogs(SESSION_ID)).thenReturn(NETWORK_LOGS);

        debugSteps.saveNetworkLogs(SCOPES, VARIABLE_NAME);

        verify(bddVariableContext).putVariable(SCOPES, VARIABLE_NAME, NETWORK_LOGS);
        verifyNoMoreInteractions(automateClient, bddVariableContext, textContext);
    }

    @Test
    void shouldFailIfNoSessionAvailable()
    {
        when(textContext.get(PREVIOUS_SESSION_ID)).thenReturn(null);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> debugSteps.saveNetworkLogs(SCOPES, VARIABLE_NAME));
        assertEquals("Unable to find a previous session", exception.getMessage());
        verifyNoMoreInteractions(textContext);
        verifyNoInteractions(automateClient, bddVariableContext);
    }

    @Test
    void shouldSavePreviousSessionId()
    {
        debugSteps.onWebDriverQuit(new AfterWebDriverQuitEvent(SESSION_ID));
        verify(textContext).put(PREVIOUS_SESSION_ID, SESSION_ID);
        verifyNoInteractions(automateClient, bddVariableContext);
    }

    @Test
    void shouldSaveNetworkLogsIntoJsonContext() throws BrowserStackException
    {
        when(textContext.get(PREVIOUS_SESSION_ID)).thenReturn(SESSION_ID);
        when(automateClient.getNetworkLogs(SESSION_ID)).thenReturn(NETWORK_LOGS);

        debugSteps.saveNetworkLogsToJsonContext();

        verify(jsonContext).putJsonContext(NETWORK_LOGS);
        verify(attachmentPublisher).publishAttachment(NETWORK_LOGS.getBytes(StandardCharsets.UTF_8),
                "BrowserStack network logs.har");
    }
}
