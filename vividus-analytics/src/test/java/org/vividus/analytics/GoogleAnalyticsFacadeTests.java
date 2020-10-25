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

package org.vividus.analytics;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.analytics.model.AnalyticsEvent;
import org.vividus.http.client.IHttpClient;

@ExtendWith({MockitoExtension.class, TestLoggerFactoryExtension.class})
class GoogleAnalyticsFacadeTests
{
    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(GoogleAnalyticsFacade.class);

    private static final String VALUE = "v%";

    private static final String KEY = "k";

    private static final String TRACKING_ID = "UA-123456789-1";

    private static final URI ANALYTICS_URI = URI.create("https://analytics.google.com/collect");

    private static final String USER_DIR = "user.dir";

    @Mock
    private IHttpClient httpClient;

    @InjectMocks
    private GoogleAnalyticsFacade googleAnalyticsFacade;

    private String userDir;

    @BeforeEach
    void beforeEach()
    {
        userDir = System.getProperty(USER_DIR);
        googleAnalyticsFacade.setAnalyticsUri(ANALYTICS_URI);
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
    @CsvSource({ "/root/some/folders/here-we-go-tests",
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
        googleAnalyticsFacade.postEvent(analyticsEvent);
        verify(httpClient).execute(argThat(r -> {
            HttpPost request = (HttpPost) r;
            assertAll(
                () -> assertEquals("User-Agent: ", r.getFirstHeader("User-Agent").toString()),
                () -> assertEquals("POST https://analytics.google.com/collect HTTP/1.1", r.getRequestLine().toString()),
                () -> assertEquals("v=1&t=event&tid=UA-123456789-1&cid=b1a66498-4c8e-3fe7-86c8-a55d68007da6&k=v%25",
                        EntityUtils.toString(request.getEntity(), StandardCharsets.UTF_8))
            );
            return true;
        }));
    }

    @Test
    void shouldLogMessageInCaseOfException() throws IOException
    {
        googleAnalyticsFacade.init();
        AnalyticsEvent analyticsEvent = mock(AnalyticsEvent.class);
        when(analyticsEvent.getPayload()).thenReturn(Map.of(KEY, VALUE));
        IOException ioException = new IOException();
        when(httpClient.execute(any(HttpPost.class))).thenThrow(ioException);
        googleAnalyticsFacade.postEvent(analyticsEvent);
        assertEquals(List.of(info(ioException, "Unable to send analytics")), LOGGER.getAllLoggingEvents());
    }
}
