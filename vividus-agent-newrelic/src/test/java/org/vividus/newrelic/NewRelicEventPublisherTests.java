/*
 * Copyright 2019-2026 the original author or authors.
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

package org.vividus.newrelic;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.newrelic.model.RunPhase;
import org.vividus.newrelic.model.VividusRunEvent;
import org.vividus.util.json.JsonUtils;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class NewRelicEventPublisherTests
{
    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(NewRelicEventPublisher.class);

    private static final String UNSUCCESSFUL_STATUS =
            "Unable to send New Relic event, response status code is {}";
    private static final String API_KEY = "insert-key";
    private static final String RUN_ID = "run-1";
    private static final String ENDPOINT = "https://insights-collector.newrelic.com/v1/accounts/12345/events";
    private static final URI EVENT_URI = URI.create(ENDPOINT);

    private final JsonUtils jsonUtils = new JsonUtils();

    @Mock private IHttpClient httpClient;
    @Mock private HttpResponse httpResponse;
    @Captor private ArgumentCaptor<HttpPost> requestCaptor;

    private NewRelicEventPublisher publisher;

    @BeforeEach
    void setUp()
    {
        publisher = new NewRelicEventPublisher(httpClient, API_KEY, ENDPOINT);
    }

    @Test
    void shouldPostEvent() throws IOException, ParseException
    {
        when(httpClient.execute(requestCaptor.capture())).thenReturn(httpResponse);
        when(httpResponse.getStatusCode()).thenReturn(HttpStatus.SC_OK);

        publisher.postEvent(startedRun());

        HttpPost request = requestCaptor.getValue();
        JsonNode body = jsonUtils.readTree(EntityUtils.toString(request.getEntity(), StandardCharsets.UTF_8));
        assertAll(
                () -> assertEquals("POST", request.getMethod()),
                () -> assertEquals(EVENT_URI, request.getUri()),
                () -> assertEquals(API_KEY, request.getFirstHeader("Api-Key").getValue()),
                () -> assertEquals(ContentType.APPLICATION_JSON.toString(), request.getEntity().getContentType()),
                () -> assertEquals(jsonUtils.readTree(
                        "[{\"eventType\":\"VividusRun\",\"runId\":\"" + RUN_ID + "\",\"phase\":\"started\"}]"), body),
                () -> assertTrue(LOGGER.getLoggingEvents().isEmpty())
        );
    }

    @Test
    void shouldLogUnsuccessfulResponseStatus() throws IOException
    {
        when(httpClient.execute(requestCaptor.capture())).thenReturn(httpResponse);
        when(httpResponse.getStatusCode()).thenReturn(HttpStatus.SC_FORBIDDEN);

        publisher.postEvent(startedRun());

        verify(httpClient).execute(requestCaptor.getValue());
        assertEquals(info(UNSUCCESSFUL_STATUS, HttpStatus.SC_FORBIDDEN), LOGGER.getLoggingEvents().get(0));
    }

    @Test
    void shouldLogResponseStatusBelowSuccessRange() throws IOException
    {
        when(httpClient.execute(requestCaptor.capture())).thenReturn(httpResponse);
        when(httpResponse.getStatusCode()).thenReturn(HttpStatus.SC_CONTINUE);

        publisher.postEvent(startedRun());

        assertEquals(info(UNSUCCESSFUL_STATUS, HttpStatus.SC_CONTINUE), LOGGER.getLoggingEvents().get(0));
    }

    @Test
    void shouldLogIoException() throws IOException
    {
        IOException ioException = new IOException();
        when(httpClient.execute(requestCaptor.capture())).thenThrow(ioException);

        publisher.postEvent(startedRun());

        assertEquals(info(ioException, "Unable to send New Relic event"), LOGGER.getLoggingEvents().get(0));
    }

    private static VividusRunEvent startedRun()
    {
        return new VividusRunEvent(RUN_ID, RunPhase.STARTED, null, Map.of());
    }
}
