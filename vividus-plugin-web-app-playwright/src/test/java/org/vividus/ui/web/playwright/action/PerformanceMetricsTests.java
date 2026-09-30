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

package org.vividus.ui.web.playwright.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.EnumMap;
import java.util.Map;

import com.microsoft.playwright.Page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.model.WebPerformanceMetric;

@ExtendWith(MockitoExtension.class)
class PerformanceMetricsTests
{
    @Mock private UiContext uiContext;

    @Test
    void shouldCollectAndCachePerformanceMetrics()
    {
        Page page = mock(Page.class);
        when(uiContext.getCurrentPage()).thenReturn(page);
        when(page.evaluate(anyString())).thenReturn(Map.of(
                "TIME_TO_FIRST_BYTE", 1,
                "DNS_LOOKUP_TIME", 2,
                "DOM_CONTENT_LOAD_TIME", 3,
                "PAGE_LOAD_TIME", 4
        ));
        PerformanceMetrics performanceMetrics = new PerformanceMetrics(new SimpleTestContext(), uiContext);

        Map<WebPerformanceMetric, Long> metrics = performanceMetrics.getMetrics();

        Map<WebPerformanceMetric, Long> expected = new EnumMap<>(WebPerformanceMetric.class);
        expected.put(WebPerformanceMetric.TIME_TO_FIRST_BYTE, 1L);
        expected.put(WebPerformanceMetric.DNS_LOOKUP_TIME, 2L);
        expected.put(WebPerformanceMetric.DOM_CONTENT_LOAD_TIME, 3L);
        expected.put(WebPerformanceMetric.PAGE_LOAD_TIME, 4L);
        assertEquals(expected, metrics);

        performanceMetrics.getMetrics();
        verify(page).evaluate(anyString());
    }
}
