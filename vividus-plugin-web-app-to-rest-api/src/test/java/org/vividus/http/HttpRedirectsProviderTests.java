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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.client.CircularRedirectException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.protocol.HttpClientContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.client.HttpClient;
import org.vividus.http.client.HttpResponse;

@ExtendWith(MockitoExtension.class)
class HttpRedirectsProviderTests
{
    private static final URI URI_EXAMPLES = URI.create("http://examples.com");

    @Mock private HttpClientContext httpClientContext;
    @Mock private HttpClient httpClient;
    @InjectMocks private HttpRedirectsProvider redirectsProvider;

    @Test
    void shouldFailWhenStatusCodeIsNotAcceptable() throws IOException
    {
        try (MockedStatic<HttpClientContext> httpClientContextMock = mockStatic(HttpClientContext.class))
        {
            httpClientContextMock.when(HttpClientContext::create).thenReturn(httpClientContext);
            HttpResponse httpResponse = new HttpResponse();
            httpResponse.setStatusCode(HttpStatus.SC_BAD_GATEWAY);
            when(httpClient.doHttpHead(URI_EXAMPLES, httpClientContext)).thenReturn(httpResponse);
            IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
                    () -> redirectsProvider.getRedirects(URI_EXAMPLES));
            assertEquals("Service returned response with unexpected status code: [502]. Expected code from range "
                    + "[200 - 207]", illegalStateException.getMessage());
        }
    }

    @Test
    void shouldUpdateErrorMessageAndRethrowAsIllegalStateExceptionInCaseOfCircularRedirectException() throws IOException
    {
        try (MockedStatic<HttpClientContext> httpClientContextMock = mockStatic(HttpClientContext.class))
        {
            httpClientContextMock.when(HttpClientContext::create).thenReturn(httpClientContext);
            ClientProtocolException clientProtocolException = new ClientProtocolException(
                    new CircularRedirectException("Circular reference"));
            when(httpClient.doHttpHead(URI_EXAMPLES, httpClientContext)).thenThrow(clientProtocolException);
            IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
                    () -> redirectsProvider.getRedirects(URI_EXAMPLES));
            assertEquals("Circular reference Circular redirects are forbidden by default. To allow them, please set "
                            + "property 'http.redirects-provider.circular-redirects-allowed=true'",
                    illegalStateException.getMessage());
        }
    }

    @Test
    void shouldReturnRedirectsList() throws IOException
    {
        try (MockedStatic<HttpClientContext> httpClientContextMock = mockStatic(HttpClientContext.class))
        {
            httpClientContextMock.when(HttpClientContext::create).thenReturn(httpClientContext);
            HttpResponse httpResponse = new HttpResponse();
            httpResponse.setStatusCode(HttpStatus.SC_OK);
            when(httpClient.doHttpHead(URI_EXAMPLES, httpClientContext)).thenReturn(httpResponse);
            List<URI> redirects = List.of(URI_EXAMPLES);
            when(httpClientContext.getRedirectLocations()).thenReturn(redirects);
            assertEquals(redirects, redirectsProvider.getRedirects(URI_EXAMPLES));
        }
    }
}
