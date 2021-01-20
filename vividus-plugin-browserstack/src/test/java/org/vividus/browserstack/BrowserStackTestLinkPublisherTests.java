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

package org.vividus.browserstack;

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import com.browserstack.automate.model.Session;
import com.browserstack.client.exception.BrowserStackException;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class BrowserStackTestLinkPublisherTests
{
    private static final String SESSION_ID = "session-id";
    private static final String URL = "https://example.com";

    @Mock private Session session;
    @Mock private BrowserStackAutomateClient appAutomateClient;
    @InjectMocks private BrowserStackTestLinkPublisher linkPublisher;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(BrowserStackTestLinkPublisher.class);

    @Test
    void shouldReturnSessionUrl() throws BrowserStackException
    {
        when(appAutomateClient.getSession(SESSION_ID)).thenReturn(session);
        when(session.getPublicUrl()).thenReturn(URL);

        assertEquals(Optional.of(URL), linkPublisher.getCloudTestUrl(SESSION_ID));
        assertThat(logger.getLoggingEvents(), is(empty()));
    }

    @Test
    void shouldLogExceptionsOccuredWhileGettingSessionLink() throws BrowserStackException
    {
        BrowserStackException exception = mock(BrowserStackException.class);
        doThrow(exception).when(appAutomateClient).getSession(SESSION_ID);

        assertEquals(Optional.empty(), linkPublisher.getCloudTestUrl(SESSION_ID));

        assertThat(logger.getLoggingEvents(),
            is(List.of(error(exception, "Unable to get an URL for BrowserStack session with the ID {}", SESSION_ID))));
    }
}
