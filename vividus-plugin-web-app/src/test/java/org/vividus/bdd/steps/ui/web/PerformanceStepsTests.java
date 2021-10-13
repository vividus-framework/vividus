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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.ui.validation.IDescriptiveSoftAssert;
import org.vividus.bdd.steps.ui.web.model.WebPerformanceMetric;
import org.vividus.ui.web.performance.PerformanceMetrics;

@ExtendWith(MockitoExtension.class)
class PerformanceStepsTests
{
    private static final long PAGE_LOAD_TIME_THRESHOLD = 1000L;

    @Mock private PerformanceMetrics performanceMetrics;
    @Mock private IDescriptiveSoftAssert softAssert;
    @InjectMocks private PerformanceSteps performanceSteps;

    @Test
    void shouldVerifyThatTheLoadTimeShouldBeLessThanThresholdValue()
    {
        when(performanceMetrics.getMetrics())
                .thenReturn(Map.of(WebPerformanceMetric.PAGE_LOAD_TIME, PAGE_LOAD_TIME_THRESHOLD));
        performanceSteps.thenTheLoadTimeShouldBeLessThan(PAGE_LOAD_TIME_THRESHOLD);
        verify(softAssert).assertThat(eq("The page load time is less than load time threshold."),
                eq("The page load time is less than '1000'"), eq(PAGE_LOAD_TIME_THRESHOLD), any());
    }

    @Test
    void shouldCheckWebPerformanceMetric()
    {
        when(performanceMetrics.getMetrics())
                .thenReturn(Map.of(WebPerformanceMetric.PAGE_LOAD_TIME, PAGE_LOAD_TIME_THRESHOLD));
        performanceSteps.checkWebPerformanceMetric(WebPerformanceMetric.PAGE_LOAD_TIME, ComparisonRule.EQUAL_TO,
            Duration.ofMillis(PAGE_LOAD_TIME_THRESHOLD));
        verify(softAssert).assertThat(eq(WebPerformanceMetric.PAGE_LOAD_TIME.toString()), eq(PAGE_LOAD_TIME_THRESHOLD),
            argThat(arg -> "a value equal to <1000L>".equals(arg.toString())));
    }
}
