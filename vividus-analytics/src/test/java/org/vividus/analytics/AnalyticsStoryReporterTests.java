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
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.google.common.eventbus.EventBus;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.steps.StepCollector.Stage;
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

@ExtendWith(MockitoExtension.class)
class AnalyticsStoryReporterTests
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
    @Captor private ArgumentCaptor<AnalyticsEventBatch> analyticsEventBatchCaptor;
    @InjectMocks private AnalyticsStoryReporter reporter;

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
        reporter.beforeStoriesSteps(Stage.BEFORE);
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
    void shouldDoNothingOnBeforeStoriesAtAfterStage()
    {
        reporter.beforeStoriesSteps(Stage.AFTER);
        verifyNoInteractions(eventBus);
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
        reporter.beforeStoriesSteps(Stage.BEFORE);
        Stream.generate(() -> "/tony_stark/projects/vividus")
                .limit(9)
                .map(Story::new)
                .forEach(s -> reporter.beforeStory(s, false));
        Stream.generate(() -> 0).limit(40).forEach(v -> reporter.beforeStep(null));
        Stream.generate(() -> 0).limit(20).forEach(v -> reporter.beforeScenario(mock(Scenario.class)));
        reporter.afterStoriesSteps(Stage.AFTER);
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
            () -> assertThat(payload2.get("cm3"), matchesRegex("\\d+")),
            () -> assertEquals("20", payload2.get("cm4")),
            () -> assertEquals("end", payload2.get(SESSION_CONTROL)),
            () -> assertEquals("finishTests", payload2.get(EVENT_ACTION)));
    }

    @Test
    void shouldDoNothingOnAfterStoriesAtBeforeStage()
    {
        reporter.afterStoriesSteps(Stage.BEFORE);
        verifyNoInteractions(eventBus);
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
