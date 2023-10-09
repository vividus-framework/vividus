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

package org.vividus.ssllabs;

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
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

import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.util.json.JsonUtils;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class SslLabsClientTests
{
    private static final String SSL_LABS_HOST = "https://api.ssllabs.com";
    private static final String ANALYZED_HOST = "www.example.com";
    private static final int TOO_MANY_REQUESTS = 429;
    private static final int SERVICE_IS_OVERLOADED = 529;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(SslLabsClient.class);
    private final LoggingEvent errorLogEntry = error(
            "SSL scan has not been performed successfully during specified waiting period");

    @Mock private IHttpClient httpClient;

    private SslLabsClient client;

    @BeforeEach
    public void beforeEach()
    {
        try (var duration = mockStatic(Duration.class))
        {
            duration.when(() -> Duration.ofMinutes(10)).thenReturn(Duration.ZERO);
            duration.when(() -> Duration.ofSeconds(30)).thenReturn(Duration.ZERO);
            client = new SslLabsClient(httpClient, new JsonUtils(), SSL_LABS_HOST);
        }
    }

    @Test
    void shouldPerformSslScan() throws IOException
    {
        String response = "{\"status\":\"READY\",\"endpoints\":[{\"ipAddress\":\"216.24.57.253\","
                + "\"statusMessage\":\"Ready\",\"grade\":\"A+\"},{\"ipAddress\":\"216.24.57.3\",\"grade\":\"T\"}]}";
        mockResponse(response);
        assertEquals(Grade.T, client.performSslScan(ANALYZED_HOST).get());
        assertThat(logger.getLoggingEvents(), is(List.of()));
    }

    @Test
    void shouldPerformSslScanWithIOException() throws IOException
    {
        try (var grade = mockStatic(Grade.class))
        {
            var ioException = mock(IOException.class);
            when(httpClient.doHttpGet(any(URI.class))).thenThrow(ioException);
            assertEquals(Optional.empty(), client.performSslScan(ANALYZED_HOST));
            grade.verify(() -> Grade.fromString(anyString()), never());
            assertThat(logger.getLoggingEvents(),
                    is(List.of(error(ioException, "Unable to process request"), errorLogEntry)));
        }
    }

    @Test
    void shouldPerformSslScanWhenTooManyRequests() throws IOException
    {
        testPerformSslScanWithUnexpectedStatus(TOO_MANY_REQUESTS);
    }

    @Test
    void shouldPerformSslScanWhenServiceIsOverloaded() throws IOException
    {
        testPerformSslScanWithUnexpectedStatus(SERVICE_IS_OVERLOADED);
    }

    @Test
    void shouldPerformSslScanWhenWhenServiceUnavailable() throws IOException
    {
        testPerformSslScanWithUnexpectedStatus(HttpStatus.SC_SERVICE_UNAVAILABLE);
    }

    @Test
    void shouldPerformSslScanWithUnsupportedStatusCode() throws IOException
    {
        testPerformSslScanWithUnexpectedStatus(HttpStatus.SC_BAD_REQUEST);
    }

    @Test
    void shouldPerformSslScanWithErrorStatus() throws IOException
    {
        try (var grade = mockStatic(Grade.class))
        {
            String response =
                    "{\"host\":\"https://example.com\",\"status\":\"ERROR\",\"statusMessage\":\"Unexpected error\"}";
            mockResponse(response);
            assertEquals(Optional.empty(), client.performSslScan(ANALYZED_HOST));
            grade.verify(() -> Grade.fromString(anyString()), never());
            assertThat(logger.getLoggingEvents(), is(List.of(
                    error("Status message '{}' received for host {}", "Unexpected error", "https://example.com"),
                    errorLogEntry)));
        }
    }

    @Test
    void shouldPerformSslScanWhenReadyStatusDidNotReceived() throws IOException
    {
        try (var grade = mockStatic(Grade.class))
        {
            String response = "{\"status\":\"\"}";
            mockResponse(response);
            assertEquals(Optional.empty(), client.performSslScan(ANALYZED_HOST));
            grade.verify(() -> Grade.fromString(anyString()), never());
            assertThat(logger.getLoggingEvents(), hasItems(errorLogEntry));
        }
    }

    @Test
    void shouldPerformSslScanEmptyEndpoints() throws IOException
    {
        try (var grade = mockStatic(Grade.class))
        {
            String response = "{\"status\":\"READY\",\"endpoints\":[]}";
            mockResponse(response);
            assertEquals(Optional.empty(), client.performSslScan(ANALYZED_HOST));
            grade.verify(() -> Grade.fromString(anyString()), never());
            assertThat(logger.getLoggingEvents(), is(List.of()));
        }
    }

    private void testPerformSslScanWithUnexpectedStatus(int statusCode) throws IOException
    {
        try (var grade = mockStatic(Grade.class))
        {
            String response = "{\"status\":\"ERROR\"}";
            HttpResponse httpResponse = new HttpResponse();
            httpResponse.setStatusCode(statusCode);
            httpResponse.setResponseBody(response.getBytes(StandardCharsets.UTF_8));
            when(httpClient.doHttpGet(any(URI.class))).thenReturn(httpResponse);
            assertEquals(Optional.empty(), client.performSslScan(ANALYZED_HOST));
            grade.verify(() -> Grade.fromString(anyString()), never());
            assertThat(logger.getLoggingEvents(), hasItems(
                    warn("Unexpected status code received: {}\nResponse body: {}", statusCode, response),
                    errorLogEntry));
        }
    }

    private void mockResponse(String response) throws IOException
    {
        HttpResponse httpResponse = new HttpResponse();
        httpResponse.setStatusCode(HttpStatus.SC_OK);
        httpResponse.setResponseBody(response.getBytes(StandardCharsets.UTF_8));
        when(httpClient.doHttpGet(any(URI.class))).thenReturn(httpResponse);
    }
}
