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

package org.vividus.bdd.steps.ui.web;

import static org.hamcrest.Matchers.lessThan;

import java.time.Duration;

import org.jbehave.core.annotations.Then;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.ui.validation.IDescriptiveSoftAssert;
import org.vividus.bdd.steps.ui.web.model.WebPerformanceMetric;
import org.vividus.ui.web.performance.PerformanceMetrics;

public class PerformanceSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PerformanceSteps.class);

    private final PerformanceMetrics performanceMetrics;
    private final IDescriptiveSoftAssert softAssert;

    public PerformanceSteps(PerformanceMetrics performanceMetrics, IDescriptiveSoftAssert softAssert)
    {
        this.performanceMetrics = performanceMetrics;
        this.softAssert = softAssert;
    }

    /**
     * Checks that the <b>page</b> was loaded less than in 'pageLoadTimeThreshold' <b>milliseconds</b>
     * <p>
     * Actions performed at this step:
     * <ul>
     * <li>Gets the <b>page's load time</b>
     * <li>Compares it with a 'pageLoadTimeThreshold'
     * </ul>
     * <p>
     * @deprecated Use the following step with the PAGE_LOAD_TIME metric name:
     * Then $webPerformanceMetric is $comparisonRule `$duration`
     * @param pageLoadTimeThreshold The time in <b>milliseconds</b> bigger than an expected <b>page's load time</b><br>
     */
    @Deprecated(since = "0.3.9", forRemoval = true)
    @Then("the page load time should be less than '$pageLoadTimeThreshold' milliseconds")
    public void thenTheLoadTimeShouldBeLessThan(long pageLoadTimeThreshold)
    {
        LOGGER.warn("This step is deprecated and will be removed in VIVIDUS 0.4.0. The replacement is"
                + " \"Then $webPerformanceMetric is $comparisonRule `$duration`\" with the PAGE_LOAD_TIME metric name");
        softAssert.assertThat("The page load time is less than load time threshold.",
                String.format("The page load time is less than '%s'", pageLoadTimeThreshold),
                performanceMetrics.getMetrics().get(WebPerformanceMetric.PAGE_LOAD_TIME),
                lessThan(pageLoadTimeThreshold));
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
