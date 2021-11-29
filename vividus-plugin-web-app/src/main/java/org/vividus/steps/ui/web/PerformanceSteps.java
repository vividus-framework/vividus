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

package org.vividus.steps.ui.web;

import java.time.Duration;

import org.jbehave.core.annotations.Then;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.ui.web.model.WebPerformanceMetric;
import org.vividus.ui.web.performance.PerformanceMetrics;

public class PerformanceSteps
{
    private final PerformanceMetrics performanceMetrics;
    private final ISoftAssert softAssert;

    public PerformanceSteps(PerformanceMetrics performanceMetrics, ISoftAssert softAssert)
    {
        this.performanceMetrics = performanceMetrics;
        this.softAssert = softAssert;
    }

    /**
     * Checks web performance metrics.
     *
     * @param metric         Then web performance metric. Allowed options:
     *                       <ul>
     *                       <li>TIME_TO_FIRST_BYTE</li>
     *                       <li>DNS_LOOKUP_TIME</li>
     *                       <li>DOM_CONTENT_LOAD_TIME</li>
     *                       <li>PAGE_LOAD_TIME</li>
     *                       </ul>
     * @param comparisonRule The duration comparison rule. Allowed options:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than(&gt;)</li>
     *                       <li>greater than or equal to(&gt;=)</li>
     *                       <li>equal to(=))</li>
     *                       </ul>
     * @param duration The duration to compare against in ISO 8601 format.
     */
    @Then("metric $webPerformanceMetric is $comparisonRule `$duration`")
    public void checkWebPerformanceMetric(WebPerformanceMetric metric, ComparisonRule comparisonRule, Duration duration)
    {
        long metricValue = performanceMetrics.getMetrics().get(metric);
        softAssert.assertThat(metric.toString(), metricValue, comparisonRule.getComparisonRule(duration.toMillis()));
    }
}
