/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.bdd.steps.ui.web.listener;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.testcontext.TestContext;
import org.vividus.ui.web.event.PageLoadEndEvent;
import org.vividus.ui.web.listener.PerformanceMetricsListener;
import org.vividus.ui.web.performance.PerformanceMetrics;

@ExtendWith(MockitoExtension.class)
class PerformanceMetricsListenerTests
{
    @Mock private TestContext testContext;
    @InjectMocks private PerformanceMetricsListener listener;

    @Test
    void shouldResetMetricsAfterNavigateBack()
    {
        listener.afterNavigateBack(null);
        verify(testContext).remove(PerformanceMetrics.class);
    }

    @Test
    void shouldResetMetricsAfterNavigateForward()
    {
        listener.afterNavigateForward(null);
        verify(testContext).remove(PerformanceMetrics.class);
    }

    @Test
    void shouldResetMetricsAfterNavigateTo()
    {
        listener.afterNavigateTo(null, null);
        verify(testContext).remove(PerformanceMetrics.class);
    }

    @Test
    void shouldResetMetricsAfterSwitchToWindow()
    {
        listener.afterSwitchToWindow(null, null);
        verify(testContext).remove(PerformanceMetrics.class);
    }

    @ParameterizedTest
    @CsvSource({
        "true, 1",
        "false, 0"
    })
    void shouldResetMetricsOnNewPage(boolean newPage, int times)
    {
        PageLoadEndEvent event = new PageLoadEndEvent(newPage, null);
        listener.handle(event);
        verify(testContext, times(times)).remove(PerformanceMetrics.class);
    }
}
