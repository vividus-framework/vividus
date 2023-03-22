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

package org.vividus.http.client;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.List;
import java.util.stream.Stream;

import org.apache.commons.lang3.function.FailableSupplier;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
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
    void testGetHttpHost() throws URISyntaxException
    {
        var httpHost = createHttpHost();
        httpClient.setHttpHost(httpHost);
        assertEquals(httpHost, httpClient.getHttpHost());
    }

    @TestFactory
    Stream<DynamicTest> httpGetTests()
    {
        return Stream.of(
                dynamicTest("shouldDoHttpGetWithHttpContext", () -> {
                    HttpContext context = mock();
                    testDoHttpGet(context, () -> httpClient.doHttpGet(URI_TO_GO, context));
                }),
                dynamicTest("shouldDoHttpGetWithoutHttpContext", () -> {
                    testDoHttpGet(null, () -> httpClient.doHttpGet(URI_TO_GO));
                })
        );
    }

    private void testDoHttpGet(HttpContext context, FailableSupplier<HttpResponse, IOException> test)
            throws IOException, URISyntaxException
    {
        var httpHost = createHttpHost();
        httpClient.setHttpHost(httpHost);
        @SuppressWarnings("PMD.CloseResource")
        ClassicHttpResponse classicHttpResponse = mock();
        HttpEntity httpEntity = mock();
        byte[] body = { 0, 1, 2 };
        var headers = new Header[] { header };
        var statusCode = HttpStatus.SC_OK;
        when(httpEntity.getContent()).thenReturn(new ByteArrayInputStream(body));
        when(classicHttpResponse.getEntity()).thenAnswer((Answer<HttpEntity>) invocation -> {
            Sleeper.sleep(Duration.ofMillis(THREAD_SLEEP_TIME));
            return httpEntity;
        });
        when(classicHttpResponse.getHeaders()).thenReturn(headers);
        when(classicHttpResponse.getCode()).thenReturn(statusCode);
        when(closeableHttpClient.execute(eq(httpHost), isA(HttpGet.class), eq(context),
                argThat((ArgumentMatcher<HttpClientResponseHandler<HttpResponse>>) responseHandler -> {
                    try
                    {
                        var httpResponse = responseHandler.handleResponse(classicHttpResponse);
                        assertEquals(VIVIDUS_ORG, httpResponse.getFrom().toString());
                        assertEquals(GET, httpResponse.getMethod());
                        assertArrayEquals(body, httpResponse.getResponseBody());
                        assertArrayEquals(headers, httpResponse.getResponseHeaders());
                        assertEquals(statusCode, httpResponse.getStatusCode());
                        return true;
                    }
                    catch (HttpException | IOException e)
                    {
                        throw new IllegalStateException(e);
                    }
                }))).thenReturn(new HttpResponse());
        var httpResponse = test.get();
        assertThat(httpResponse.getResponseTimeInMs(), greaterThan(0L));
        verify(handler).handle(httpResponse);
    }

    @Test
    void shouldDoHttpGetSkippingResponseBodySaving() throws URISyntaxException, IOException
    {
        httpClient.setSkipResponseEntity(true);
        var httpHost = createHttpHost();
        httpClient.setHttpHost(httpHost);
        @SuppressWarnings("PMD.CloseResource")
        ClassicHttpResponse classicHttpResponse = mock();
        HttpEntity httpEntity = mock();
        var headers = new Header[] { header };
        var statusCode = HttpStatus.SC_OK;
        when(classicHttpResponse.getEntity()).thenAnswer((Answer<HttpEntity>) invocation -> {
            Sleeper.sleep(Duration.ofMillis(THREAD_SLEEP_TIME));
            return httpEntity;
        });
        when(classicHttpResponse.getHeaders()).thenReturn(headers);
        when(classicHttpResponse.getCode()).thenReturn(statusCode);
        when(closeableHttpClient.execute(eq(httpHost), isA(HttpGet.class), nullable(HttpContext.class),
                argThat((ArgumentMatcher<HttpClientResponseHandler<HttpResponse>>) responseHandler -> {
                    try
                    {
                        var httpResponse = responseHandler.handleResponse(classicHttpResponse);
                        assertEquals(VIVIDUS_ORG, httpResponse.getFrom().toString());
                        assertEquals(GET, httpResponse.getMethod());
                        assertNull(httpResponse.getResponseBody());
                        assertArrayEquals(headers, httpResponse.getResponseHeaders());
                        assertEquals(statusCode, httpResponse.getStatusCode());
                        return true;
                    }
                    catch (HttpException | IOException e)
                    {
                        throw new IllegalStateException(e);
                    }
                }))).thenReturn(new HttpResponse());
        var httpResponse = httpClient.doHttpGet(URI_TO_GO);
        assertThat(httpResponse.getResponseTimeInMs(), greaterThan(0L));
        verify(handler).handle(httpResponse);
    }

    @TestFactory
    Stream<DynamicTest> httpHeadTests()
    {
        return Stream.of(
                dynamicTest("shouldDoHttpHeadWithHttpContext", () -> {
                    HttpContext context = mock();
                    testDoHttpHead(context, () -> httpClient.doHttpHead(URI_TO_GO, context));
                }),
                dynamicTest("shouldDoHttpHeadWithoutHttpContext", () -> {
                    testDoHttpHead(null, () -> httpClient.doHttpHead(URI_TO_GO));
                })
        );
    }

    private void testDoHttpHead(HttpContext context, FailableSupplier<HttpResponse, IOException> test)
            throws IOException
    {
        @SuppressWarnings("PMD.CloseResource")
        ClassicHttpResponse classicHttpResponse = mock();
        when(classicHttpResponse.getEntity()).thenAnswer((Answer<HttpEntity>) invocation -> {
            Sleeper.sleep(Duration.ofMillis(THREAD_SLEEP_TIME));
            return null;
        });
        var statusCode = HttpStatus.SC_MOVED_PERMANENTLY;
        var headers = new Header[] { header };
        when(classicHttpResponse.getHeaders()).thenReturn(headers);
        when(classicHttpResponse.getCode()).thenReturn(statusCode);
        when(closeableHttpClient.execute(isA(HttpHead.class), eq(context),
                argThat((ArgumentMatcher<HttpClientResponseHandler<HttpResponse>>) responseHandler -> {
                    try
                    {
                        var httpResponse = responseHandler.handleResponse(classicHttpResponse);
                        assertEquals(HEAD, httpResponse.getMethod());
                        assertEquals(URI_TO_GO, httpResponse.getFrom());
                        assertEquals(statusCode, httpResponse.getStatusCode());
                        assertArrayEquals(headers, httpResponse.getResponseHeaders());
                        return true;
                    }
                    catch (HttpException | IOException e)
                    {
                        throw new IllegalStateException(e);
                    }
                }))).thenReturn(new HttpResponse());
        var httpResponse = test.get();
        assertThat(httpResponse.getResponseTimeInMs(), greaterThan(0L));
        verify(handler).handle(httpResponse);
    }

    @Test
    void testDoHttpGetThrowingIOExceptionAtExecution() throws IOException
    {
        var message = "execute HTTP GET exception message";
        when(closeableHttpClient.execute(isA(HttpGet.class), nullable(HttpContext.class), any())).thenThrow(
                new IOException(message));
        var exception = assertThrows(IOException.class, () -> httpClient.doHttpGet(URI_TO_GO));
        assertEquals(message, exception.getMessage());
    }

    private static HttpHost createHttpHost() throws URISyntaxException
    {
        return HttpHost.create(VIVIDUS_ORG.substring(0, VIVIDUS_ORG.length() - 1));
    }
}
