/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.analytics;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.analytics.model.AnalyticsEvent;
import org.vividus.analytics.model.AnalyticsEventBatch;
import org.vividus.http.client.IHttpClient;

@ExtendWith({MockitoExtension.class, TestLoggerFactoryExtension.class})
class GoogleAnalyticsFacadeTests
{
    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(GoogleAnalyticsFacade.class);

    private static final String VALUE = "v%";

    private static final String KEY = "k";

    private static final String TRACKING_ID = "UA-123456789-1";

    private static final String USER_DIR = "user.dir";

    private static final int MAX_EVENTS_PER_BATCH = 20;

    private static final String ENCODED_EVENT = "v=1&t=event&tid=UA-123456789-1&"
            + "cid=b1a66498-4c8e-3fe7-86c8-a55d68007da6&k=v%25";

    private static final String DEFAULT_DIR = "/root/some/folders/here-we-go-tests";

    @Mock private IHttpClient httpClient;
    @Captor private ArgumentCaptor<HttpPost> httpPostCaptor;
    @InjectMocks private GoogleAnalyticsFacade googleAnalyticsFacade;

    private String userDir;

    @BeforeEach
    void beforeEach()
    {
        userDir = System.getProperty(USER_DIR);
        googleAnalyticsFacade.setTrackingId(TRACKING_ID);
    }

    @AfterEach
    void afterEach()
    {
        useUserDir(userDir);
    }

    private static void useUserDir(String userDir)
    {
        System.setProperty(USER_DIR, userDir);
    }

    @ParameterizedTest
    @CsvSource({ DEFAULT_DIR,
                 "/root/some/folders/here-we-go-tests/",
                 "/root/some/folders/here-we-go-tests/scripts",
                 "/root/some/folders/here-we-go-tests/scripts/",
                 "c:\\windows\\is\\my\\love\\here-we-go-tests\\",
                 "c:\\windows\\is\\my\\love\\here-we-go-tests",
                 "c:\\windows\\is\\my\\love\\here-we-go-tests\\scripts\\",
                 "c:\\windows\\is\\my\\love\\here-we-go-tests\\scripts"})
    void shouldPostEvent(String userDir) throws IOException
    {
        useUserDir(userDir);
        googleAnalyticsFacade.init();
        AnalyticsEvent analyticsEvent = mock(AnalyticsEvent.class);
        when(analyticsEvent.getPayload()).thenReturn(Map.of(KEY, VALUE));
        googleAnalyticsFacade.postEvent(new AnalyticsEventBatch(List.of(analyticsEvent, analyticsEvent)));
        verify(httpClient).execute(httpPostCaptor.capture());
        HttpPost request = httpPostCaptor.getValue();
        assertAll(
            () -> assertEquals("User-Agent: ", request.getFirstHeader("User-Agent").toString()),
            () -> assertEquals("POST https://www.google-analytics.com/batch HTTP/1.1",
                    request.getRequestLine().toString()),
            () -> assertEquals(ENCODED_EVENT + System.lineSeparator() + ENCODED_EVENT,
                    EntityUtils.toString(request.getEntity(), StandardCharsets.UTF_8))
        );
    }

    @Test
    void shouldSendEventsByPartsIfTheirQuantityExceedsGALimit() throws IOException
    {
        useUserDir(DEFAULT_DIR);
        googleAnalyticsFacade.init();

        AnalyticsEvent analyticsEvent = mock(AnalyticsEvent.class);
        when(analyticsEvent.getPayload()).thenReturn(Map.of(KEY, VALUE));

        List<AnalyticsEvent> analyticsEvents = new ArrayList<>();
        IntStream.range(0, MAX_EVENTS_PER_BATCH).forEach(unused -> analyticsEvents.add(analyticsEvent));
        AnalyticsEvent secondRequestEvent = mock(AnalyticsEvent.class);
        String eventKey = "second-request-key";
        String eventValue = "second-request-value";
        when(secondRequestEvent.getPayload()).thenReturn(Map.of(eventKey, eventValue));
        analyticsEvents.add(secondRequestEvent);

        googleAnalyticsFacade.postEvent(new AnalyticsEventBatch(analyticsEvents));
        verify(httpClient, times(2)).execute(httpPostCaptor.capture());
        List<HttpPost> request = httpPostCaptor.getAllValues();
        assertThat(request, hasSize(2));

        HttpPost firstRequest = request.get(0);
        assertEquals(StringUtils.repeat(ENCODED_EVENT, System.lineSeparator(), MAX_EVENTS_PER_BATCH),
                EntityUtils.toString(firstRequest.getEntity(), StandardCharsets.UTF_8));

        HttpPost secondRequest = request.get(1);
        assertEquals("v=1&t=event&tid=UA-123456789-1&cid=b1a66498-4c8e-3fe7-86c8-a55d68007da6&"
                + "second-request-key=second-request-value",
                EntityUtils.toString(secondRequest.getEntity(), StandardCharsets.UTF_8));
    }

    @Test
    void shouldLogMessageInCaseOfException() throws IOException
    {
        googleAnalyticsFacade.init();
        AnalyticsEvent analyticsEvent = mock(AnalyticsEvent.class);
        when(analyticsEvent.getPayload()).thenReturn(Map.of(KEY, VALUE));
        IOException ioException = new IOException();
        when(httpClient.execute(httpPostCaptor.capture())).thenThrow(ioException);
        googleAnalyticsFacade.postEvent(new AnalyticsEventBatch(List.of(analyticsEvent)));
        assertEquals(List.of(info(ioException, "Unable to send analytics")), LOGGER.getLoggingEvents());
    }
}
