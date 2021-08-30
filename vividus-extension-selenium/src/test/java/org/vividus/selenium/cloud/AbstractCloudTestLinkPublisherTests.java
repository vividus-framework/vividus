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

package org.vividus.selenium.cloud;

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.google.common.eventbus.EventBus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.SessionId;
import org.vividus.reporter.event.LinkPublishEvent;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.cloud.AbstractCloudTestLinkPublisher.GetCloudTestUrlException;
import org.vividus.selenium.event.AfterWebDriverQuitEvent;
import org.vividus.testcontext.TestContext;
import org.vividus.testcontext.ThreadedTestContext;

@ExtendWith(MockitoExtension.class)
class AbstractCloudTestLinkPublisherTests
{
    private static final String SESSION_ID = "session-id";
    private static final String URL = "https://example.com";
    private static final String TEST_CLOUD_NAME = "Test";

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private EventBus eventBus;
    @Mock private AfterWebDriverQuitEvent webDriverQuitEvent;
    @Spy private ThreadedTestContext testContext;
    @InjectMocks private TestCloudTestLinkPublisher linkPublisher;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AbstractCloudTestLinkPublisher.class);

    @Test
    void shouldPublishSessionLinkAfterScenario()
    {
        mockDriverSession();

        linkPublisher.resetState();
        linkPublisher.publishCloudTestLinkAfterScenario();
        linkPublisher.publishCloudTestLinkOnWebDriverQuit(webDriverQuitEvent);

        verifyLinkPublishEvent(1);
        verifyNoInteractions(webDriverQuitEvent);
    }

    @Test
    void shouldNotPublishSessionLinkAfterScenarioIfWebDriverWasClosed()
    {
        when(webDriverQuitEvent.getSessionId()).thenReturn(SESSION_ID);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(false);

        linkPublisher.resetState();
        linkPublisher.publishCloudTestLinkOnWebDriverQuit(webDriverQuitEvent);
        linkPublisher.publishCloudTestLinkAfterScenario();

        verifyLinkPublishEvent(1);
        verifyNoMoreInteractions(webDriverProvider);
    }

    @Test
    void shouldPublishSessionLinkAfterScenarioIfMultipleWebDriverSessionReCreationOccurred()
    {
        when(webDriverQuitEvent.getSessionId()).thenReturn(SESSION_ID);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        mockDriverSession();

        linkPublisher.resetState();
        linkPublisher.publishCloudTestLinkOnWebDriverQuit(webDriverQuitEvent);
        linkPublisher.publishCloudTestLinkOnWebDriverQuit(webDriverQuitEvent);
        linkPublisher.publishCloudTestLinkAfterScenario();

        verifyLinkPublishEvent(3);
        verifyNoMoreInteractions(webDriverProvider);
    }

    @Test
    void shouldLogException() throws GetCloudTestUrlException
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        mockDriverSession();

        GetCloudTestUrlException thrown = mock(GetCloudTestUrlException.class);
        TestCloudTestLinkPublisher spy = spy(linkPublisher);
        doThrow(thrown).when(spy).getCloudTestUrl(SESSION_ID);

        spy.resetState();
        spy.publishCloudTestLinkAfterScenario();

        assertThat(logger.getLoggingEvents(), is(List
            .of(error(thrown, "Unable to get an URL for {} session with the ID {}", TEST_CLOUD_NAME, SESSION_ID))));
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
            return URL.equals(event.getUrl()) && "Test Test URL".equals(event.getName());
        }));
    }

    private static final class TestCloudTestLinkPublisher extends AbstractCloudTestLinkPublisher
    {
        TestCloudTestLinkPublisher(IWebDriverProvider webDriverProvider, EventBus eventBus, TestContext testContext)
        {
            super(TEST_CLOUD_NAME, webDriverProvider, eventBus, testContext);
        }

        @Override
        protected String getCloudTestUrl(String sessionId) throws GetCloudTestUrlException
        {
            return URL;
        }
    }
}
