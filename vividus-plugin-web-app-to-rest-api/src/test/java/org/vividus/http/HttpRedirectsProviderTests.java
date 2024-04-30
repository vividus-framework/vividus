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

package org.vividus.http;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.hc.client5.http.CircularRedirectException;
import org.apache.hc.client5.http.ClientProtocolException;
import org.apache.hc.client5.http.HttpResponseException;
import org.apache.hc.client5.http.protocol.RedirectLocations;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ProtocolException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.client.HttpClient;
import org.vividus.http.client.HttpResponse;

@ExtendWith(MockitoExtension.class)
class HttpRedirectsProviderTests
{
    private static final URI TEST_URI = URI.create("https://examples.com");

    @Mock private HttpClient httpClient;
    @InjectMocks private HttpRedirectsProvider redirectsProvider;

    @ParameterizedTest
    @ValueSource(ints = {
            HttpStatus.SC_EARLY_HINTS,
            HttpStatus.SC_ALREADY_REPORTED
    })
    void shouldFailWhenStatusCodeIsNotAcceptable(int statusCode) throws IOException
    {
        var httpResponse = new HttpResponse();
        httpResponse.setStatusCode(statusCode);
        when(httpClient.doHttpHead(TEST_URI)).thenReturn(httpResponse);
        var httpResponseException = assertThrows(HttpResponseException.class,
                () -> redirectsProvider.getRedirects(TEST_URI));
        var expectedError = ("status code: %1$d, reason phrase: HTTP response status code is expected to be in "
                + "range [200 - 207], but was %1$d").formatted(statusCode);
        assertEquals(expectedError, httpResponseException.getMessage());
    }

    @Test
    void shouldUpdateErrorMessageAndRethrowAsIoExceptionInCaseOfCircularRedirectException() throws IOException
    {
        var clientProtocolException = new ClientProtocolException(new CircularRedirectException("Circular reference"));
        when(httpClient.doHttpHead(TEST_URI)).thenThrow(clientProtocolException);
        var ioException = assertThrows(IOException.class, () -> redirectsProvider.getRedirects(TEST_URI));
        assertEquals("Circular reference Circular redirects are forbidden by default. To allow them, please set "
                + "property 'http.redirects-provider.circular-redirects-allowed=true'", ioException.getMessage());
    }

    @Test
    void shouldUpdateErrorMessageAndRethrowAsIoExceptionInCaseOfNotCircularRedirectException() throws IOException
    {
        var protocolExceptionMessage = "Redirect URI does not specify a valid host name";
        var protocolException = new ProtocolException(protocolExceptionMessage);
        var clientProtocolException = new ClientProtocolException(protocolException.getMessage(), protocolException);
        when(httpClient.doHttpHead(TEST_URI)).thenThrow(clientProtocolException);
        var ioException = assertThrows(IOException.class, () -> redirectsProvider.getRedirects(TEST_URI));
        assertEquals(protocolExceptionMessage, ioException.getMessage());
    }

    @Test
    void shouldReturnRedirectsList() throws IOException
    {
        var httpResponse = new HttpResponse();
        httpResponse.setStatusCode(HttpStatus.SC_OK);
        var redirect = URI.create("https://redirected.from.examples.com");
        RedirectLocations redirectLocations = new RedirectLocations();
        redirectLocations.add(redirect);
        httpResponse.setRedirectLocations(redirectLocations);
        when(httpClient.doHttpHead(TEST_URI)).thenReturn(httpResponse);
        assertEquals(List.of(redirect), redirectsProvider.getRedirects(TEST_URI));
    }
}
