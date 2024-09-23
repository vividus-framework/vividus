/*
 * Copyright 2019-2024 the original author or authors.
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
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;

import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.AuthenticationException;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.protocol.RedirectLocations;
import org.apache.hc.core5.http.ClassicHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.io.HttpClientResponseHandler;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

    private static final String USER = "user";
    private static final String PASSWORD = "pass%5E";
    private static final String BASIC_AUTH = USER + ":" + PASSWORD;
    private static final String BASIC_AUTH_DECODED = URLDecoder.decode(BASIC_AUTH, StandardCharsets.UTF_8);
    private static final String SCHEME = "https";
    private static final String HOST = "www.vividus.org";
    private static final HttpHost HTTP_HOST = new HttpHost(SCHEME, HOST, 443);
    private static final URI URI_WITH_BASIC_AUTH = URI.create(SCHEME + "://" + BASIC_AUTH + "@" + HOST + "/");

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
        var httpHost = HTTP_HOST;
        httpClient.setHttpHost(httpHost);
        assertEquals(httpHost, httpClient.getHttpHost());
    }

    @Test
    void shouldDoHttpGetWithPreemptiveAuthEnabled() throws IOException, AuthenticationException
    {
        var httpHost = HTTP_HOST;
        httpClient.setHttpHost(httpHost);
        @SuppressWarnings("PMD.CloseResource") ClassicHttpResponse classicHttpResponse = mock();
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
        var httpContextArgumentCaptor = ArgumentCaptor.forClass(HttpContext.class);
        when(closeableHttpClient.execute(eq(httpHost), isA(HttpGet.class), httpContextArgumentCaptor.capture(),
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
        var httpResponse = httpClient.doHttpGet(URI_WITH_BASIC_AUTH, true);
        assertThat(httpResponse.getResponseTimeInMs(), greaterThan(0L));
        verify(handler).handle(httpResponse);
        var httpContext = httpContextArgumentCaptor.getValue();
        assertInstanceOf(HttpClientContext.class, httpContext);
        var httpClientContext = (HttpClientContext) httpContext;
        var authExchanges = httpClientContext.getAuthExchanges();
        assertEquals(1, authExchanges.size());
        var authExchange = authExchanges.get(HTTP_HOST);
        var authScheme = authExchange.getAuthScheme();
        var authResponse = authScheme.generateAuthResponse(null, null, null);
        var expectedAuthResponse = "Basic " + Base64.getEncoder().encodeToString(
                BASIC_AUTH_DECODED.getBytes(StandardCharsets.UTF_8));
        assertEquals(expectedAuthResponse, authResponse);
        assertNull(httpClientContext.getCredentialsProvider());
    }

    @Test
    void shouldDoHttpGetSkippingResponseBodySaving() throws IOException
    {
        httpClient.setSkipResponseEntity(true);
        var httpHost = HTTP_HOST;
        httpClient.setHttpHost(httpHost);
        var responseHandlerMatcher = responseHandlerMatcher(GET, HttpStatus.SC_OK, mock());
        when(closeableHttpClient.execute(eq(httpHost), isA(HttpGet.class), isA(HttpClientContext.class),
                argThat(responseHandlerMatcher))).thenReturn(new HttpResponse());
        var httpResponse = httpClient.doHttpGet(URI_TO_GO);
        assertThat(httpResponse.getResponseTimeInMs(), greaterThan(0L));
        verify(handler).handle(httpResponse);
    }

    @Test
    void shouldDoHttpHead() throws IOException
    {
        var httpContextArgumentCaptor = ArgumentCaptor.forClass(HttpContext.class);
        var responseHandlerMatcher = responseHandlerMatcher(HEAD, HttpStatus.SC_MOVED_PERMANENTLY, null);
        when(closeableHttpClient.execute(isA(HttpHead.class), httpContextArgumentCaptor.capture(),
                argThat(responseHandlerMatcher))).thenReturn(new HttpResponse());
        var httpResponse = httpClient.doHttpHead(URI_WITH_BASIC_AUTH);
        assertThat(httpResponse.getResponseTimeInMs(), greaterThan(0L));
        verify(handler).handle(httpResponse);
        var httpContext = httpContextArgumentCaptor.getValue();
        assertInstanceOf(HttpClientContext.class, httpContext);
        var httpClientContext = (HttpClientContext) httpContext;
        var authExchanges = httpClientContext.getAuthExchanges();
        assertThat(authExchanges.entrySet(), is(empty()));
        var credentialsProvider = httpClientContext.getCredentialsProvider();
        assertInstanceOf(BasicCredentialsProvider.class, credentialsProvider);
        var credentials = credentialsProvider.getCredentials(new AuthScope(HTTP_HOST), null);
        assertInstanceOf(UsernamePasswordCredentials.class, credentials);
        assertEquals(USER, credentials.getUserPrincipal().getName());
        String decodedPassword = URLDecoder.decode(PASSWORD, StandardCharsets.UTF_8);
        assertArrayEquals(decodedPassword.toCharArray(), ((UsernamePasswordCredentials) credentials).getUserPassword());
    }

    @Test
    void testDoHttpGetThrowingIOExceptionAtExecution() throws IOException
    {
        var message = "execute HTTP GET exception message";
        var request = new HttpGet(URI_TO_GO);
        when(closeableHttpClient.execute(eq(request), isA(HttpClientContext.class), any())).thenThrow(
                new IOException(message));
        var exception = assertThrows(IOException.class, () -> httpClient.execute(request));
        assertEquals(message, exception.getMessage());
    }

    @Test
    void testDoHttpHeadThrowingIOExceptionAtGettingUri() throws URISyntaxException
    {
        var uriSyntaxException = new URISyntaxException("invalid uri", "Unable to parse URI");
        var request = mock(HttpHead.class);
        when(request.getUri()).thenThrow(uriSyntaxException);
        var ioException = assertThrows(IOException.class, () -> httpClient.execute(request));
        assertEquals(uriSyntaxException, ioException.getCause());
    }

    @Test
    void shouldDoHttpGetAndSaveRedirects() throws IOException
    {
        httpClient.setSkipResponseEntity(true);
        var httpHost = HTTP_HOST;
        httpClient.setHttpHost(httpHost);
        var request = new HttpGet(URI_TO_GO);
        var context = HttpClientContext.create();
        RedirectLocations redirectLocations = mock();
        context.setRedirectLocations(redirectLocations);
        var responseHandlerMatcher = responseHandlerMatcher(GET, HttpStatus.SC_OK, mock());
        when(closeableHttpClient.execute(eq(httpHost), eq(request), eq(context),
                argThat(responseHandlerMatcher))).thenReturn(new HttpResponse());
        var httpResponse = httpClient.execute(request, context);
        assertThat(httpResponse.getResponseTimeInMs(), greaterThan(0L));
        assertEquals(redirectLocations, httpResponse.getRedirectLocations());
        verify(handler).handle(httpResponse);
    }

    @Test
    void shouldThrowClientProtocolExceptionOnUnsupportedUrl()
    {
        httpClient.setCloseableHttpClient(HttpClients.createDefault());
        var invalidUrl = URI.create("file:///C:/Users/77805/Downloads/privacy@abc.com");
        var exception = assertThrows(ClientProtocolException.class, () -> httpClient.doHttpGet(invalidUrl));
        assertEquals("Target host is not specified", exception.getMessage());
    }

    private ArgumentMatcher<HttpClientResponseHandler<HttpResponse>> responseHandlerMatcher(String httpMethod,
            int statusCode, HttpEntity httpEntity)
    {
        @SuppressWarnings("PMD.CloseResource")
        ClassicHttpResponse classicHttpResponse = mock();
        var headers = new Header[] { header };
        when(classicHttpResponse.getEntity()).thenAnswer((Answer<HttpEntity>) invocation -> {
            Sleeper.sleep(Duration.ofMillis(THREAD_SLEEP_TIME));
            return httpEntity;
        });
        when(classicHttpResponse.getHeaders()).thenReturn(headers);
        when(classicHttpResponse.getCode()).thenReturn(statusCode);
        return responseHandler -> {
            try
            {
                var httpResponse = responseHandler.handleResponse(classicHttpResponse);
                assertEquals(URI_TO_GO, httpResponse.getFrom());
                assertEquals(httpMethod, httpResponse.getMethod());
                assertNull(httpResponse.getResponseBody());
                assertEquals(statusCode, httpResponse.getStatusCode());
                assertArrayEquals(headers, httpResponse.getResponseHeaders());
                return true;
            }
            catch (HttpException | IOException e)
            {
                throw new IllegalStateException(e);
            }
        };
    }
}
