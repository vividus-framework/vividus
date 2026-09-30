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

package org.vividus.ui.web.playwright.steps;

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
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.ui.web.playwright.action.PerformanceMetrics;
import org.vividus.ui.web.playwright.model.WebPerformanceMetric;

@ExtendWith(MockitoExtension.class)
class PerformanceStepsTests
{
    @Mock private PerformanceMetrics performanceMetrics;
    @Mock private ISoftAssert softAssert;
    @InjectMocks private PerformanceSteps steps;

    @Test
    void shouldCheckWebPerformanceMetric()
    {
        when(performanceMetrics.getMetrics()).thenReturn(Map.of(WebPerformanceMetric.PAGE_LOAD_TIME, 500L));
        steps.checkWebPerformanceMetric(WebPerformanceMetric.PAGE_LOAD_TIME, ComparisonRule.LESS_THAN,
                Duration.ofSeconds(1));
        verify(softAssert).assertThat(eq("Page load time"), eq(500L), argThat(matcher -> matcher.matches(500L)));
    }
}
