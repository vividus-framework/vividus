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

package org.vividus.http;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpEntity;
import org.apache.http.client.CookieStore;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.http.exception.HttpRequestBuildException;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.wait.WaitMode;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class HttpRequestExecutorTests
{
    private static final String URL = "http://www.example.com/";
    private static final long RESPONSE_TIME_IN_MS = 100;

    @Mock
    private IHttpClient httpClient;

    @Mock
    private HttpTestContext httpTestContext;

    @Mock
    private ISoftAssert softAssert;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(HttpRequestExecutor.class);

    @InjectMocks
    private HttpRequestExecutor httpRequestExecutor;

    @Test
    void testExecuteHttpRequestFirstTime() throws IOException
    {
        when(httpTestContext.getCookieStore()).thenReturn(Optional.empty());
        when(httpTestContext.getRequestConfig()).thenReturn(Optional.empty());
        HttpResponse httpResponse = mockHttpResponse(URL);
        httpRequestExecutor.executeHttpRequest(HttpMethod.GET, URL, Optional.empty());
        InOrder orderedHttpTestContext = inOrder(httpTestContext);
        verifyHttpTestContext(orderedHttpTestContext, httpResponse);
        orderedHttpTestContext.verify(httpTestContext).releaseRequestData();
        orderedHttpTestContext.verifyNoMoreInteractions();
        assertThat(logger.getLoggingEvents(), equalTo(List.of(createResponseTimeLogEvent(httpResponse))));
    }

    @Test
    void testExecuteHttpRequestWithContent() throws IOException
    {
        HttpEntity requestEntity = new StringEntity("content", StandardCharsets.UTF_8);
        when(httpTestContext.getRequestEntity()).thenReturn(Optional.of(requestEntity));
        httpRequestExecutor.executeHttpRequest(HttpMethod.GET, URL, Optional.empty());
        verify(softAssert).recordFailedAssertion(
                (Exception) argThat(arg -> arg instanceof HttpRequestBuildException
                        && "java.lang.IllegalStateException: HTTP GET request can't include body"
                        .equals(((Exception) arg).getMessage())));
        verify(httpTestContext).releaseRequestData();
    }

    @Test
    void testExecuteShouldUseCookieStoreFromContext() throws IOException
    {
        CookieStore cookieStore = mock(CookieStore.class);
        when(httpTestContext.getCookieStore()).thenReturn(Optional.of(cookieStore));
        HttpResponse httpResponse = mockHttpResponse(URL);
        httpRequestExecutor.executeHttpRequest(HttpMethod.GET, URL, Optional.empty());

        verify(httpClient).execute(argThat(HttpUriRequest.class::isInstance),
                argThat(e -> e != null && e.getAttribute("http.cookie-store") != null));
        InOrder orderedHttpTestContext = inOrder(httpTestContext);
        verifyHttpTestContext(orderedHttpTestContext, httpResponse);
        orderedHttpTestContext.verify(httpTestContext).releaseRequestData();
        orderedHttpTestContext.verifyNoMoreInteractions();
        assertThat(logger.getLoggingEvents(), equalTo(List.of(createResponseTimeLogEvent(httpResponse))));
    }

    @Test
    void testExecuteHttpRequestWithIllegalUrl() throws IOException
    {
        String url = "malformed.url";
        httpRequestExecutor.executeHttpRequest(HttpMethod.GET, url, Optional.empty());
        verify(softAssert).recordFailedAssertion(
                (Exception) argThat(arg -> arg instanceof HttpRequestBuildException
                        && ("java.lang.IllegalArgumentException: Scheme is missing in URL: " + url)
                        .equals(((Exception) arg).getMessage())));
        verify(httpTestContext).releaseRequestData();
    }

    @Test
    void testExecuteHttpRequestWithoutContent() throws IOException
    {
        httpRequestExecutor.executeHttpRequest(HttpMethod.POST, URL, Optional.empty());
        verify(softAssert).recordFailedAssertion(
                (Exception) argThat(arg -> arg instanceof HttpRequestBuildException
                        && "java.lang.IllegalStateException: HTTP POST request must include body"
                        .equals(((Exception) arg).getMessage())));
        verify(httpTestContext).releaseRequestData();
    }

    @Test
    void testExecuteHttpRequestConnectionClosedException() throws IOException
    {
        when(httpClient.execute(argThat(e -> e instanceof HttpRequestBase && URL.equals(e.getURI().toString())),
                nullable(HttpContext.class))).thenThrow(new ConnectionClosedException());
        httpRequestExecutor.executeHttpRequest(HttpMethod.GET, URL, Optional.empty());
        verify(softAssert).recordFailedAssertion(
                (Exception) argThat(arg -> arg instanceof ConnectionClosedException
                        && "Connection is closed".equals(((Exception) arg).getMessage())));
        verify(httpTestContext).releaseRequestData();
    }

    @Test
    void testExecuteHttpRequestIOException() throws IOException
    {
        when(httpClient.execute(argThat(e -> e instanceof HttpRequestBase && URL.equals(e.getURI().toString())),
                nullable(HttpContext.class))).thenThrow(new IOException());
        assertThrows(IOException.class,
            () -> httpRequestExecutor.executeHttpRequest(HttpMethod.GET, URL, Optional.empty()));
        verify(httpTestContext).releaseRequestData();
    }

    @Test
    void testExecuteHttpRequestPredicateFunction() throws IOException
    {
        HttpResponse httpResponse1 = new HttpResponse();
        httpResponse1.setResponseTimeInMs(0);
        HttpResponse httpResponse2 = new HttpResponse();
        httpResponse2.setResponseTimeInMs(RESPONSE_TIME_IN_MS);
        when(httpClient.execute(argThat(e -> e instanceof HttpRequestBase && URL.equals(e.getURI().toString())),
                nullable(HttpContext.class))).thenReturn(httpResponse1).thenReturn(httpResponse2);
        httpRequestExecutor.executeHttpRequest(HttpMethod.GET, URL, Optional.empty(),
            response -> response.getResponseTimeInMs() >= RESPONSE_TIME_IN_MS, new WaitMode(Duration.ofSeconds(1), 2));
        InOrder orderedHttpTestContext = inOrder(httpTestContext);
        verifyHttpTestContext(orderedHttpTestContext, httpResponse1);
        verifyHttpTestContext(orderedHttpTestContext, httpResponse2);
        orderedHttpTestContext.verify(httpTestContext).releaseRequestData();
        orderedHttpTestContext.verifyNoMoreInteractions();
        assertThat(logger.getLoggingEvents(),
                equalTo(List.of(createResponseTimeLogEvent(httpResponse1), createResponseTimeLogEvent(httpResponse2))));
    }

    private HttpResponse mockHttpResponse(String url) throws IOException
    {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setResponseTimeInMs(RESPONSE_TIME_IN_MS);
        when(httpClient.execute(argThat(e -> e instanceof HttpRequestBase && URI.create(url).equals(e.getURI())),
                nullable(HttpContext.class))).thenReturn(httpResponse);
        return httpResponse;
    }

    private void verifyHttpTestContext(InOrder orderedHttpTestContext, HttpResponse httpResponse)
    {
        orderedHttpTestContext.verify(httpTestContext).getRequestHeaders();
        orderedHttpTestContext.verify(httpTestContext).getRequestEntity();
        orderedHttpTestContext.verify(httpTestContext).putResponse(httpResponse);
    }

    private LoggingEvent createResponseTimeLogEvent(HttpResponse httpResponse)
    {
        return info("Response time: {} ms", httpResponse.getResponseTimeInMs());
    }
}
