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

package org.vividus.http;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ConnectionClosedException;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.http.exception.HttpRequestBuildException;
import org.vividus.softassert.ISoftAssert;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class HttpRequestExecutorTests
{
    private static final String URL = "http://www.example.com/";
    private static final long RESPONSE_TIME_IN_MS = 100;

    @Mock private IHttpClient httpClient;
    @Mock private HttpTestContext httpTestContext;
    @Mock private ISoftAssert softAssert;
    @InjectMocks private HttpRequestExecutor httpRequestExecutor;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(HttpRequestExecutor.class);

    @Test
    void testExecuteHttpRequestFirstTime() throws IOException
    {
        when(httpTestContext.getCookieStore()).thenReturn(Optional.empty());
        when(httpTestContext.getRequestConfig()).thenReturn(Optional.empty());
        HttpResponse httpResponse = mockHttpResponse();
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
        @SuppressWarnings("PMD.CloseResource")
        HttpEntity requestEntity = new StringEntity("content", StandardCharsets.UTF_8);
        when(httpTestContext.getRequestEntity()).thenReturn(Optional.of(requestEntity));
        httpRequestExecutor.executeHttpRequest(HttpMethod.GET, URL, Optional.empty());
        verify(softAssert).recordFailedAssertion(
                (Exception) argThat(arg -> arg instanceof HttpRequestBuildException
                        && "HTTP GET request can't include body".equals(((Exception) arg).getMessage()))
        );
        verify(httpTestContext).releaseRequestData();
    }

    @Test
    void testExecuteShouldUseCookieStoreFromContext() throws IOException
    {
        CookieStore cookieStore = mock();
        when(httpTestContext.getCookieStore()).thenReturn(Optional.of(cookieStore));
        HttpResponse httpResponse = mockHttpResponse();
        httpRequestExecutor.executeHttpRequest(HttpMethod.GET, URL, Optional.empty());

        verify(httpClient).execute(argThat(ClassicHttpRequest.class::isInstance),
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
                argThat((ArgumentMatcher<Exception>) arg -> arg instanceof HttpRequestBuildException && arg.getMessage()
                        .equals("java.lang.IllegalArgumentException: Scheme is missing in URL: " + url)));
        verify(httpTestContext).releaseRequestData();
    }

    @Test
    void testExecuteHttpRequestWithoutContent() throws IOException
    {
        httpRequestExecutor.executeHttpRequest(HttpMethod.PATCH, URL, Optional.empty());
        verify(softAssert).recordFailedAssertion(
                (Exception) argThat(arg -> arg instanceof HttpRequestBuildException
                        && "HTTP PATCH request must include body".equals(((Exception) arg).getMessage()))
        );
        verify(httpTestContext).releaseRequestData();
    }

    @Test
    void testExecuteHttpRequestConnectionClosedException() throws IOException
    {
        when(httpClient.execute(requestWithMatchingUrl(URL), nullable(HttpContext.class))).thenThrow(
                new ConnectionClosedException());
        httpRequestExecutor.executeHttpRequest(HttpMethod.GET, URL, Optional.empty());
        verify(softAssert).recordFailedAssertion(
                (Exception) argThat(arg -> arg instanceof ConnectionClosedException
                        && "Connection is closed".equals(((Exception) arg).getMessage())));
        verify(httpTestContext).releaseRequestData();
    }

    @Test
    void testExecuteHttpRequestIOException() throws IOException
    {
        when(httpClient.execute(requestWithMatchingUrl(URL), nullable(HttpContext.class))).thenThrow(new IOException());
        assertThrows(IOException.class,
            () -> httpRequestExecutor.executeHttpRequest(HttpMethod.GET, URL, Optional.empty()));
        verify(httpTestContext).releaseRequestData();
    }

    private HttpResponse mockHttpResponse() throws IOException
    {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setResponseTimeInMs(RESPONSE_TIME_IN_MS);
        when(httpClient.execute(requestWithMatchingUrl(URL), nullable(HttpContext.class))).thenReturn(httpResponse);
        return httpResponse;
    }

    private static ClassicHttpRequest requestWithMatchingUrl(String url)
    {
        return argThat(request -> {
            try
            {
                return request != null && url.equals(request.getUri().toString());
            }
            catch (URISyntaxException e)
            {
                throw new IllegalArgumentException(e);
            }
        });
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
