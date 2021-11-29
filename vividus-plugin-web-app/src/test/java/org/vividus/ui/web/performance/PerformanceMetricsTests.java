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

package org.vividus.ui.web.performance;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.steps.ui.web.model.WebPerformanceMetric;
import org.vividus.testcontext.TestContext;
import org.vividus.ui.web.action.WebJavascriptActions;

@ExtendWith(MockitoExtension.class)
class PerformanceMetricsTests
{
    @Mock private TestContext testContext;
    @Mock private WebJavascriptActions javascriptActions;
    @InjectMocks private PerformanceMetrics performanceMetrics;

    @SuppressWarnings("unchecked")
    @Test
    void shouldReturnMetrics()
    {
        when(javascriptActions.executeScriptFromResource(PerformanceMetrics.class, "metrics.js"))
                .thenReturn(Map.of(WebPerformanceMetric.TIME_TO_FIRST_BYTE.name(), 1L));
        doAnswer(a ->
        {
            Supplier<Map<WebPerformanceMetric, Long>> metricsSupplier = a.getArgument(1);
            return metricsSupplier.get();
        }).when(testContext).get(eq(PerformanceMetrics.class), any(Supplier.class));

        assertEquals(Map.of(WebPerformanceMetric.TIME_TO_FIRST_BYTE, 1L), performanceMetrics.getMetrics());
    }
}
