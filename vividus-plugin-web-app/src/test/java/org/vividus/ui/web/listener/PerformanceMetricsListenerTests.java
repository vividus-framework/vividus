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

package org.vividus.ui.web.listener;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.common.eventbus.EventBus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.testcontext.TestContext;
import org.vividus.ui.web.event.PageLoadEndEvent;
import org.vividus.ui.web.listener.PerformanceMetricsListener.Factory;
import org.vividus.ui.web.performance.PerformanceMetrics;

@ExtendWith(MockitoExtension.class)
class PerformanceMetricsListenerTests
{
    @Mock private TestContext testContext;
    @Mock private EventBus eventBus;

    private PerformanceMetricsListener createListener()
    {
        var listener = new Factory(testContext, eventBus).createListener(null);
        verify(eventBus).register(listener);
        return listener;
    }

    @Test
    void shouldUnregisterListenerBeforeWebDriverQuit()
    {
        var listener = createListener();
        listener.beforeQuit(null);
        verify(eventBus).unregister(listener);
    }

    @Test
    void shouldResetPerformanceMetricsAfterAnyNavigation()
    {
        createListener().afterAnyNavigationCall(null, null, null, null);
        verify(testContext).remove(PerformanceMetrics.KEY);
    }

    @Test
    void shouldResetPerformanceMetricsAfterSwitchToWindow()
    {
        createListener().afterWindow(null, null, null);
        verify(testContext).remove(PerformanceMetrics.KEY);
    }

    @ParameterizedTest
    @CsvSource({
        "true, 1",
        "false, 0"
    })
    void shouldResetPerformanceMetricsOnNewPageLoad(boolean newPage, int times)
    {
        PageLoadEndEvent event = new PageLoadEndEvent(newPage, null);
        createListener().onPageLoadFinish(event);
        verify(testContext, times(times)).remove(PerformanceMetrics.KEY);
    }
}
