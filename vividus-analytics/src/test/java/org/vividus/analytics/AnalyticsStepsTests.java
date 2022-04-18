/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.analytics;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import com.google.common.eventbus.EventBus;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.analytics.model.AnalyticsEvent;
import org.vividus.analytics.model.AnalyticsEventBatch;
import org.vividus.reporter.environment.EnvironmentConfigurer;
import org.vividus.reporter.environment.PropertyCategory;
import org.vividus.results.ResultsProvider;
import org.vividus.results.model.ExecutableEntity;
import org.vividus.results.model.Statistic;

@ExtendWith(MockitoExtension.class)
class AnalyticsStepsTests
{
    private static final String CUSTOM_DIMENSION5 = "cd5";

    private static final String START_TESTS = "startTests";

    private static final String START = "start";

    private static final String VIVIDUS = "vividus";

    private static final String EVENT_ACTION = "ea";

    private static final String CUSTOM_DIMENSION3 = "cd3";

    private static final String CUSTOM_DIMENSION2 = "cd2";

    private static final String EVENT_CATEGORY = "ec";

    private static final String SESSION_CONTROL = "sc";

    @Mock private EventBus eventBus;
    @Mock private ResultsProvider resultsProvider;
    @Captor private ArgumentCaptor<AnalyticsEventBatch> analyticsEventBatchCaptor;
    @InjectMocks private AnalyticsSteps reporter;

    @BeforeEach
    void beforeEach()
    {
        EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION.values().forEach(Map::clear);
    }

    @AfterEach
    void afterEach()
    {
        EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION.values().forEach(Map::clear);
    }

    @Test
    void shouldPostTestsStartBeforeTestsWhenNoModulesAvailable()
    {
        configureCommonProperties();
        reporter.postBeforeStoriesAnalytics();
        verify(eventBus).post(analyticsEventBatchCaptor.capture());
        AnalyticsEventBatch batch = analyticsEventBatchCaptor.getValue();
        assertThat(batch.getEvents(), hasSize(1));
        AnalyticsEvent event = batch.getEvents().get(0);
        Map<String, String> payload = event.getPayload();
        assertAll(
            () -> assertEquals(START, payload.get(SESSION_CONTROL)),
            () -> assertEquals(VIVIDUS, payload.get(EVENT_CATEGORY)),
            () -> assertEquals(Runtime.version().toString(), payload.get(CUSTOM_DIMENSION2)),
            () -> assertEquals("not detected", payload.get(CUSTOM_DIMENSION3)),
            () -> assertEquals(START_TESTS, payload.get(EVENT_ACTION)));
    }

    @Test
    void shouldPostVividusVersionAndPluginsInformationAndStatistic()
    {
        configureCommonProperties();
        String vividusVersion = "0.1.1";
        EnvironmentConfigurer.addProperty(PropertyCategory.VIVIDUS, VIVIDUS, vividusVersion);
        String plugin = "vividus-plugin-web-ui";
        String pluginVersion = "0.1.2";
        EnvironmentConfigurer.addProperty(PropertyCategory.VIVIDUS, plugin, pluginVersion);
        reporter.postBeforeStoriesAnalytics();

        Statistic storyStatistics = mock(Statistic.class);
        when(storyStatistics.getTotal()).thenReturn(9L);
        Statistic stepStatistics = mock(Statistic.class);
        when(stepStatistics.getTotal()).thenReturn(40L);
        Statistic scenarioStatistics = mock(Statistic.class);
        when(scenarioStatistics.getTotal()).thenReturn(20L);
        when(resultsProvider.getStatistics()).thenReturn(Map.of(
            ExecutableEntity.STORY, storyStatistics,
            ExecutableEntity.STEP, stepStatistics,
            ExecutableEntity.SCENARIO, scenarioStatistics
        ));
        when(resultsProvider.getDuration()).thenReturn(Duration.ofSeconds(1));

        reporter.postAfterStoriesAnalytics();
        verify(eventBus, times(2)).post(analyticsEventBatchCaptor.capture());
        List<AnalyticsEventBatch> batches = analyticsEventBatchCaptor.getAllValues();
        assertThat(batches, hasSize(2));

        AnalyticsEventBatch beforeBatch = batches.get(0);
        List<AnalyticsEvent> events = beforeBatch.getEvents();
        assertThat(events, hasSize(2));
        AnalyticsEvent analyticsEvent = getAndRemove(events, CUSTOM_DIMENSION2);
        Map<String, String> payload = analyticsEvent.getPayload();
        assertAll(
            () -> assertEquals(START, payload.get(SESSION_CONTROL)),
            () -> assertEquals(VIVIDUS, payload.get(EVENT_CATEGORY)),
            () -> assertEquals(Runtime.version().toString(), payload.get(CUSTOM_DIMENSION2)),
            () -> assertEquals(vividusVersion, payload.get(CUSTOM_DIMENSION3)),
            () -> assertEquals(START_TESTS, payload.get(EVENT_ACTION)));
        AnalyticsEvent analyticsEvent1 = getAndRemove(events, CUSTOM_DIMENSION5);
        Map<String, String> payload1 = analyticsEvent1.getPayload();
        assertAll(
            () -> assertNull(payload1.get(SESSION_CONTROL)),
            () -> assertNull(payload1.get(CUSTOM_DIMENSION2)),
            () -> assertNull(payload1.get(CUSTOM_DIMENSION3)),
            () -> assertEquals(plugin, payload1.get(EVENT_CATEGORY)),
            () -> assertEquals(pluginVersion, payload1.get(CUSTOM_DIMENSION5)),
            () -> assertEquals("use", payload1.get(EVENT_ACTION)));

        AnalyticsEventBatch afterBatch = batches.get(1);
        Map<String, String> payload2 = afterBatch.getEvents().get(0).getPayload();
        assertAll(
            () -> assertEquals("9", payload2.get("cm1")),
            () -> assertEquals("40", payload2.get("cm2")),
            () -> assertEquals("1", payload2.get("cm3")),
            () -> assertEquals("20", payload2.get("cm4")),
            () -> assertEquals("end", payload2.get(SESSION_CONTROL)),
            () -> assertEquals("finishTests", payload2.get(EVENT_ACTION)));
    }

    private AnalyticsEvent getAndRemove(List<AnalyticsEvent> events, String criteria)
    {
        AnalyticsEvent analyticsEvent = events.stream()
                                              .filter(e -> e.getPayload().containsKey(criteria))
                                              .findAny()
                                              .get();
        events.remove(analyticsEvent);
        return analyticsEvent;
    }

    private void configureCommonProperties()
    {
        EnvironmentConfigurer.addProperty(PropertyCategory.CONFIGURATION, "Profiles", "web/desktop/chrome");
        EnvironmentConfigurer.addProperty(PropertyCategory.PROFILE, "Remote Execution", "ON");
    }
}
