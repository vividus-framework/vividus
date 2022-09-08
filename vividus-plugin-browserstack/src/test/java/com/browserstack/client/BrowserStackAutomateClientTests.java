/*
 * Copyright 2019-2022 the original author or authors.
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

package com.browserstack.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import com.browserstack.automate.model.Session;
import com.browserstack.client.BrowserStackClient.Method;
import com.browserstack.client.exception.BrowserStackException;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.browserstack.BrowserStackAutomateClient;
import org.vividus.util.json.JsonUtils;

@ExtendWith(MockitoExtension.class)
class BrowserStackAutomateClientTests
{
    private static final String SESSION_ID = "session-id";
    private static final String BROWSER_URL = "https://example.com/path";
    private static final String EMPTY_JSON = "{}";

    @Mock private Session session;
    @Mock private BrowserStackRequest browserStackRequest;
    @Mock private HttpRequest httpRequest;
    @Mock private HttpHeaders httpHeaders;

    private final BrowserStackAutomateClient client = spy(
            new BrowserStackAutomateClient("endpoint", "username", "access-key", new JsonUtils()));

    @ParameterizedTest
    @ValueSource(strings = {
            "404 Not Found",
            "403 Forbidden"
    })
    void shouldGetNetworkLogs(String firstErrorMessage) throws BrowserStackException
    {
        mockCommons();
        var browserStackException = mock(BrowserStackException.class);

        when(browserStackException.getMessage()).thenReturn(firstErrorMessage);
        when(browserStackRequest.asString()).thenThrow(browserStackException).thenReturn(EMPTY_JSON);

        assertEquals(EMPTY_JSON, client.getNetworkLogs(SESSION_ID));
        verifyMocks();
    }

    @Test
    void shouldNotGetNetworkLogsIfErrorOccurred() throws BrowserStackException
    {
        mockCommons();
        var browserStackException = new BrowserStackException(StringUtils.EMPTY);

        doThrow(browserStackException).when(browserStackRequest).asString();

        var exception = assertThrows(BrowserStackException.class,
            () -> client.getNetworkLogs(SESSION_ID));
        assertEquals(browserStackException, exception);
        verifyMocks();
    }

    @Test
    void shouldUpdateSessionStatus() throws BrowserStackException
    {
        doReturn(browserStackRequest).when(client).newRequest(Method.PUT, "/sessions/{sessionId}.json");
        when(browserStackRequest.routeParam("sessionId", SESSION_ID)).thenReturn(browserStackRequest);
        var captor = ArgumentCaptor.forClass(ByteArrayContent.class);
        when(browserStackRequest.body(captor.capture())).thenReturn(browserStackRequest);

        client.updateSessionStatus(SESSION_ID, "passed");

        verify(browserStackRequest).asString();

        var content = captor.getValue();
        assertEquals(19, content.getLength());
    }

    private void mockCommons() throws BrowserStackException
    {
        doReturn(session).when(client).getSession(SESSION_ID);
        when(session.getBrowserUrl()).thenReturn(BROWSER_URL);
        doReturn(browserStackRequest).when(client).newRequest(Method.GET, "/path/networklogs");
        when(browserStackRequest.getHttpRequest()).thenReturn(httpRequest);
        when(httpRequest.getHeaders()).thenReturn(httpHeaders);
    }

    private void verifyMocks()
    {
        verify(httpHeaders).put("Accept", List.of("*/*"));
        verifyNoMoreInteractions(browserStackRequest, session, httpHeaders, httpRequest);
    }
}
