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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.analytics.model.AnalyticsEvent;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTests
{
    @Mock
    private GoogleAnalyticsFacade googleAnalyticsFacade;

    @Mock
    private AnalyticsEvent analyticsEvent;

    @InjectMocks
    private AnalyticsService analyticsService;

    @Test
    void shouldNotPostEventsWhenAnalyticsDisabled()
    {
        analyticsService.onAnalyticEvent(analyticsEvent);
        verifyNoInteractions(googleAnalyticsFacade);
    }

    @Test
    void shouldPostEventsWhenAnalyticsEnabled() throws InterruptedException
    {
        CountDownLatch watcher = new CountDownLatch(1);
        Mockito.lenient().doNothing().when(googleAnalyticsFacade).postEvent(argThat(e -> {
            watcher.countDown();
            assertSame(analyticsEvent, e);
            return true;
        }));
        analyticsService.setEnabled(true);
        analyticsService.onAnalyticEvent(analyticsEvent);
        watcher.await(500, TimeUnit.MILLISECONDS);
        verify(googleAnalyticsFacade).postEvent(analyticsEvent);
    }
}
