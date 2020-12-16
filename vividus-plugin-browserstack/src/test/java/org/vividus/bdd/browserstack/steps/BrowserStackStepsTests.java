/*
 * Copyright 2019-2020 the original author or authors.
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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.browserstack.automate.model.Session;
import com.browserstack.client.exception.BrowserStackException;
import com.google.common.eventbus.EventBus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.vividus.browserstack.BrowserStackAutomateClient;
import org.vividus.reporter.event.LinkPublishEvent;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.event.WebDriverQuitEvent;
import org.vividus.testcontext.ThreadedTestContext;

@ExtendWith(MockitoExtension.class)
class BrowserStackStepsTests
{
    private static final String SESSION_ID = "session-id";
    private static final String URL = "https://example.com";

    @Mock private Session session;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private EventBus eventBus;
    @Mock private BrowserStackAutomateClient appAutomateClient;
    @Mock private WebDriverQuitEvent webDriverQuitEvent;
    @Spy private ThreadedTestContext testContext;
    @InjectMocks private BrowserStackSteps browserStackSteps;

    @Test
    void shouldPublishSessionLinkAfterScenario() throws BrowserStackException
    {
        mockDriverSession();
        mockSession();

        browserStackSteps.resetState();
        browserStackSteps.publishSessionLinkAfterScenario();
        browserStackSteps.publishSessionLinkOnWebDriverQuit(webDriverQuitEvent);

        verifyLinkPublishEvent(1);
        verifyNoInteractions(webDriverQuitEvent);
    }

    @Test
    void shouldNotPublishSessionLinkAfterScenarioIfWebDriverWasClosed() throws BrowserStackException
    {
        when(webDriverQuitEvent.getSessionId()).thenReturn(SESSION_ID);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(false);
        mockSession();

        browserStackSteps.resetState();
        browserStackSteps.publishSessionLinkOnWebDriverQuit(webDriverQuitEvent);
        browserStackSteps.publishSessionLinkAfterScenario();

        verifyLinkPublishEvent(1);
        verifyNoMoreInteractions(webDriverProvider);
    }

    @Test
    void shouldPublishSessionLinkAfterScenarioIfMultipleWebDriverSessionReCreationOccured() throws BrowserStackException
    {
        when(webDriverQuitEvent.getSessionId()).thenReturn(SESSION_ID);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        mockDriverSession();
        mockSession();

        browserStackSteps.resetState();
        browserStackSteps.publishSessionLinkOnWebDriverQuit(webDriverQuitEvent);
        browserStackSteps.publishSessionLinkOnWebDriverQuit(webDriverQuitEvent);
        browserStackSteps.publishSessionLinkAfterScenario();

        verifyLinkPublishEvent(3);
        verifyNoMoreInteractions(webDriverProvider);
    }

    private void mockSession() throws BrowserStackException
    {
        when(appAutomateClient.getSession(SESSION_ID)).thenReturn(session);
        when(session.getPublicUrl()).thenReturn(URL);
    }

    private void mockDriverSession()
    {
        RemoteWebDriver remoteWebDriver = mock(RemoteWebDriver.class);
        SessionId sessionId = mock(SessionId.class);

        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(webDriverProvider.getUnwrapped(RemoteWebDriver.class)).thenReturn(remoteWebDriver);
        when(remoteWebDriver.getSessionId()).thenReturn(sessionId);
        when(sessionId.toString()).thenReturn(SESSION_ID);
    }

    private void verifyLinkPublishEvent(int times)
    {
        verify(eventBus, times(times)).post(argThat(arg ->
        {
            LinkPublishEvent event = (LinkPublishEvent) arg;
            return URL.equals(event.getUrl()) && "BrowserStack Session URL".equals(event.getName());
        }));
    }
}
