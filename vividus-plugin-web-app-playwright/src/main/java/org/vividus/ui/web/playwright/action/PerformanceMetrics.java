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

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import org.vividus.testcontext.TestContext;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.model.WebPerformanceMetric;

public class PerformanceMetrics
{
    public static final Class<PerformanceMetrics> KEY = PerformanceMetrics.class;

    private static final String METRICS_SCRIPT = """
            () => {
                const t = window.performance.timing;
                return {
                    'TIME_TO_FIRST_BYTE': t.responseStart - t.navigationStart,
                    'DNS_LOOKUP_TIME': t.domainLookupEnd - t.domainLookupStart,
                    'DOM_CONTENT_LOAD_TIME': t.domComplete - t.domLoading,
                    'PAGE_LOAD_TIME': t.loadEventEnd - t.navigationStart
                };
            }""";

    private final TestContext testContext;
    private final Supplier<Map<WebPerformanceMetric, Long>> metricsInitializer;

    public PerformanceMetrics(TestContext testContext, UiContext uiContext)
    {
        this.testContext = testContext;
        this.metricsInitializer = () ->
        {
            @SuppressWarnings("unchecked")
            Map<String, Number> jsOutput = (Map<String, Number>) uiContext.getCurrentPage().evaluate(METRICS_SCRIPT);
            Map<WebPerformanceMetric, Long> metrics = new EnumMap<>(WebPerformanceMetric.class);
            jsOutput.forEach((k, v) -> metrics.put(WebPerformanceMetric.valueOf(k), v.longValue()));
            return Map.copyOf(metrics);
        };
    }

    public Map<WebPerformanceMetric, Long> getMetrics()
    {
        return testContext.get(KEY, metricsInitializer);
    }
}
