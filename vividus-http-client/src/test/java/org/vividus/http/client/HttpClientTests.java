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

package org.vividus.http.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.vividus.http.handler.HttpResponseHandler;
import org.vividus.util.Sleeper;

@ExtendWith(MockitoExtension.class)
class HttpClientTests
{
    private static final String GET = "GET";
    private static final String HEAD = "HEAD";

    private static final int THREAD_SLEEP_TIME = 5;

    private static final String VIVIDUS_ORG = "https://www.vividus.org/";
    private static final URI URI_TO_GO = URI.create(VIVIDUS_ORG);

    @Mock private HttpResponseHandler handler;
    @Mock private Header header;
    @Mock private CloseableHttpClient closeableHttpClient;
    @InjectMocks private HttpClient httpClient;

    @BeforeEach
    void init()
    {
        httpClient.setHttpResponseHandlers(List.of(handler));
    }

    @Test
    void testClose() throws Exception
    {
        httpClient.close();
        verify(closeableHttpClient).close();
    }

    @Test
    void testGetHttpHost()
    {
        HttpHost httpHost = HttpHost.create(VIVIDUS_ORG);
        httpClient.setHttpHost(httpHost);
        assertEquals(httpHost, httpClient.getHttpHost());
    }

    @Test
    void testDoHttpGet() throws Exception
    {
        HttpHost httpHost = HttpHost.create(VIVIDUS_ORG);
        httpClient.setHttpHost(httpHost);
        CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
        HttpContext context = null;
        HttpEntity httpEntity = mock(HttpEntity.class);
        byte[] body = { 0, 1, 2 };
        Header[] headers = { header };
        StatusLine statusLine = mock(StatusLine.class);
        int statusCode = HttpStatus.SC_OK;
        when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(body));
        when(closeableHttpResponse.getEntity()).thenReturn(httpEntity);
        when(closeableHttpResponse.getAllHeaders()).thenReturn(headers);
        when(statusLine.getStatusCode()).thenReturn(statusCode);
        when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
        when(closeableHttpClient.execute(eq(httpHost), isA(HttpGet.class), eq(context)))
                .thenAnswer(getAnswerWithSleep(closeableHttpResponse));
        HttpResponse httpResponse = httpClient.doHttpGet(URI_TO_GO);
        assertEquals(VIVIDUS_ORG, httpResponse.getFrom().toString());
        assertEquals(GET, httpResponse.getMethod());
        assertArrayEquals(body, httpResponse.getResponseBody());
        assertArrayEquals(headers, httpResponse.getResponseHeaders());
        assertEquals(statusCode, httpResponse.getStatusCode());
        assertThat(httpResponse.getResponseTimeInMs(), greaterThan(0L));
        verify(handler).handle(httpResponse);
    }

    @Test
    void testDoHttpGetThrowingIOExceptionAtExecution() throws Exception
    {
        HttpContext context = null;
        String message = "execute HTTP GET exception message";
        when(closeableHttpClient.execute(isA(HttpGet.class), eq(context))).thenThrow(new IOException(message));
        IOException exception = assertThrows(IOException.class, () -> httpClient.doHttpGet(URI_TO_GO));
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testDoHttpGetThrowingIOExceptionAtResponseParsing() throws Exception
    {
        CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
        HttpContext context = null;
        String message = "read HTTP GET response exception message";
        HttpEntity httpEntity = mock(HttpEntity.class);
        when(httpEntity.getContent()).thenThrow(new IOException(message));
        when(closeableHttpResponse.getEntity()).thenReturn(httpEntity);
        when(closeableHttpClient.execute(isA(HttpGet.class), eq(context))).thenReturn(closeableHttpResponse);
        assertThrows(IOException.class, () -> httpClient.doHttpGet(URI_TO_GO), message);
    }

    @Test
    void testDoHttpHead() throws Exception
    {
        CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
        HttpContext context = null;
        when(closeableHttpResponse.getEntity()).thenReturn(null);
        StatusLine statusLine = mock(StatusLine.class);
        int statusCode = HttpStatus.SC_MOVED_PERMANENTLY;
        Header[] headers = { header };
        when(closeableHttpResponse.getAllHeaders()).thenReturn(headers);
        when(statusLine.getStatusCode()).thenReturn(statusCode);
        when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
        when(closeableHttpClient.execute(isA(HttpHead.class), eq(context)))
                .thenAnswer(getAnswerWithSleep(closeableHttpResponse));
        HttpResponse httpResponse = httpClient.doHttpHead(URI_TO_GO);
        assertEquals(HEAD, httpResponse.getMethod());
        assertEquals(URI_TO_GO, httpResponse.getFrom());
        assertEquals(statusCode, httpResponse.getStatusCode());
        assertThat(httpResponse.getResponseTimeInMs(), greaterThan(0L));
        assertThat(httpResponse.getResponseHeaders(), is(equalTo(headers)));
        verify(handler).handle(httpResponse);
    }

    @Test
    void testDoHttpHeadContext() throws Exception
    {
        CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
        HttpContext context = mock(HttpContext.class);
        when(closeableHttpResponse.getEntity()).thenReturn(null);
        StatusLine statusLine = mock(StatusLine.class);
        int statusCode = HttpStatus.SC_MOVED_PERMANENTLY;
        Header[] headers = { header };
        when(closeableHttpResponse.getAllHeaders()).thenReturn(headers);
        when(statusLine.getStatusCode()).thenReturn(statusCode);
        when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
        when(closeableHttpClient.execute(isA(HttpHead.class), eq(context)))
                .thenAnswer(getAnswerWithSleep(closeableHttpResponse));
        HttpResponse httpResponse = httpClient.doHttpHead(URI_TO_GO, context);
        assertEquals(HEAD, httpResponse.getMethod());
        assertEquals(URI_TO_GO, httpResponse.getFrom());
        assertEquals(statusCode, httpResponse.getStatusCode());
        assertThat(httpResponse.getResponseTimeInMs(), greaterThan(0L));
        assertThat(httpResponse.getResponseHeaders(), is(equalTo(headers)));
        verify(handler).handle(httpResponse);
    }

    @Test
    void testDoHttpGetContext() throws Exception
    {
        httpClient.setSkipResponseEntity(true);
        CloseableHttpResponse closeableHttpResponse = mock(CloseableHttpResponse.class);
        HttpContext context = mock(HttpContext.class);
        HttpEntity httpEntity = mock(HttpEntity.class);
        when(closeableHttpResponse.getEntity()).thenReturn(httpEntity);
        StatusLine statusLine = mock(StatusLine.class);
        int statusCode = HttpStatus.SC_OK;
        Header[] headers = { header };
        when(closeableHttpResponse.getAllHeaders()).thenReturn(headers);
        when(statusLine.getStatusCode()).thenReturn(statusCode);
        when(closeableHttpResponse.getStatusLine()).thenReturn(statusLine);
        when(closeableHttpClient.execute(isA(HttpGet.class), eq(context)))
                .thenAnswer(getAnswerWithSleep(closeableHttpResponse));
        HttpResponse httpResponse = httpClient.doHttpGet(URI_TO_GO, context);
        assertEquals(GET, httpResponse.getMethod());
        assertEquals(URI_TO_GO, httpResponse.getFrom());
        assertNull(httpResponse.getResponseBody());
        assertEquals(statusCode, httpResponse.getStatusCode());
        assertThat(httpResponse.getResponseTimeInMs(), greaterThan(0L));
        verify(handler).handle(httpResponse);
    }

    private static Answer<CloseableHttpResponse> getAnswerWithSleep(CloseableHttpResponse closeableHttpResponse)
    {
        return invocation ->
        {
            Sleeper.sleep(Duration.ofMillis(THREAD_SLEEP_TIME));
            return closeableHttpResponse;
        };
    }
}
