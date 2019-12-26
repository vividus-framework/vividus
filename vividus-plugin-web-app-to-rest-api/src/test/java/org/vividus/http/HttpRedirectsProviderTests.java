/*
 * Copyright 2019 the original author or authors.
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
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.http.HttpStatus;
import org.apache.http.client.CircularRedirectException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.protocol.HttpClientContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.vividus.http.client.HttpClient;
import org.vividus.http.client.HttpResponse;

@RunWith(PowerMockRunner.class)
@PrepareForTest(HttpClientContext.class)
public class HttpRedirectsProviderTests
{
    private static final URI URI_EXAMPLES = URI.create("http://examples.com");

    @Mock
    private HttpClientContext httpClientContext;

    @Mock
    private HttpClient httpClient;

    @InjectMocks
    private HttpRedirectsProvider redirectsProvider;

    @Before
    public void before()
    {
        MockitoAnnotations.initMocks(this);
        PowerMockito.mockStatic(HttpClientContext.class);
        when(HttpClientContext.create()).thenReturn(httpClientContext);
    }

    @Test
    public void shouldFailWhenStatusCodeIsNotAcceptable() throws IOException
    {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(HttpStatus.SC_BAD_GATEWAY);
        when(httpClient.doHttpHead(URI_EXAMPLES, httpClientContext)).thenReturn(httpResponse);
        IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
            () -> redirectsProvider.getRedirects(URI_EXAMPLES));
        assertEquals("Service returned response with unexpected status code: [502]. Expected code from range "
                + "[200 - 207]", illegalStateException.getMessage());
    }

    @Test
    public void shouldUpdateErrorMessageAndRethrowAsIllegalStateExceptionInCaseOfCircularRedirectException()
            throws IOException
    {
        ClientProtocolException clientProtocolException = new ClientProtocolException(
                new CircularRedirectException("Circular reference"));
        when(httpClient.doHttpHead(URI_EXAMPLES, httpClientContext)).thenThrow(clientProtocolException);
        IllegalStateException illegalStateException = assertThrows(IllegalStateException.class,
            () -> redirectsProvider.getRedirects(URI_EXAMPLES));
        assertEquals("Circular reference Circular redirects are forbidden by default. To allow them, please set "
                        + "property 'http.redirects-provider.circular-redirects-allowed=true'",
                illegalStateException.getMessage());
    }

    @Test
    public void shouldReturnRedirectsList() throws IOException
    {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(HttpStatus.SC_OK);
        when(httpClient.doHttpHead(URI_EXAMPLES, httpClientContext)).thenReturn(httpResponse);
        List<URI> redirects = List.of(URI_EXAMPLES);
        when(httpClientContext.getRedirectLocations()).thenReturn(redirects);
        assertEquals(redirects, redirectsProvider.getRedirects(URI_EXAMPLES));
    }
}
