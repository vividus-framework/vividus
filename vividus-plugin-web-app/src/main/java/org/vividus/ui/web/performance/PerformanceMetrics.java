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

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Supplier;

import org.vividus.bdd.steps.ui.web.model.WebPerformanceMetric;
import org.vividus.testcontext.TestContext;
import org.vividus.ui.web.action.WebJavascriptActions;

public class PerformanceMetrics
{
    private final TestContext testContext;
    private final Supplier<Map<WebPerformanceMetric, Long>> metricsInitializer;

    public PerformanceMetrics(TestContext testContext, WebJavascriptActions javascriptActions)
    {
        this.testContext = testContext;
        this.metricsInitializer = () ->
        {
            Map<String, Long> jsOutput = javascriptActions.executeScriptFromResource(getClass(), "metrics.js");
            Map<WebPerformanceMetric, Long> metrics = new EnumMap<>(WebPerformanceMetric.class);
            jsOutput.forEach((k, v) -> metrics.put(WebPerformanceMetric.valueOf(k), v));
            return Map.copyOf(metrics);
        };
    }

    public Map<WebPerformanceMetric, Long> getMetrics()
    {
        return testContext.get(PerformanceMetrics.class, metricsInitializer);
    }
}
