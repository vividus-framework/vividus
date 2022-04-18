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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.google.common.eventbus.EventBus;

import org.jbehave.core.annotations.AfterStories;
import org.jbehave.core.annotations.BeforeStories;
import org.vividus.analytics.model.AnalyticsEvent;
import org.vividus.analytics.model.AnalyticsEventBatch;
import org.vividus.analytics.model.CustomDefinitions;
import org.vividus.reporter.environment.EnvironmentConfigurer;
import org.vividus.reporter.environment.PropertyCategory;
import org.vividus.results.ResultsProvider;
import org.vividus.results.model.ExecutableEntity;
import org.vividus.results.model.Statistic;

public class AnalyticsSteps
{
    private static final String SESSION_CONTROL = "sc";

    private final EventBus eventBus;
    private final ResultsProvider resultsProvider;

    public AnalyticsSteps(EventBus eventBus, ResultsProvider resultsProvider)
    {
        this.eventBus = eventBus;
        this.resultsProvider = resultsProvider;
    }

    @BeforeStories
    public void postBeforeStoriesAnalytics()
    {
        Map<String, String> payload = new HashMap<>();
        Map<String, String> configuration = getEnvironmentProperties(PropertyCategory.CONFIGURATION);
        Map<String, String> modules = getEnvironmentProperties(PropertyCategory.VIVIDUS);

        CustomDefinitions.PROFILES.add(payload, configuration.get("Profiles"));
        CustomDefinitions.JAVA.add(payload, Runtime.version().toString());
        CustomDefinitions.VIVIDUS.add(payload, modules.getOrDefault("vividus", "not detected"));
        CustomDefinitions.REMOTE.add(payload,
                getEnvironmentProperties(PropertyCategory.PROFILE).get("Remote Execution"));
        payload.put(SESSION_CONTROL, "start");

        List<AnalyticsEvent> events = new ArrayList<>();
        events.add(new AnalyticsEvent("startTests", payload));
        events.addAll(collectPluginsAnalytic(modules));

        eventBus.post(new AnalyticsEventBatch(events));
    }

    @AfterStories
    public void postAfterStoriesAnalytics()
    {
        Map<String, String> payload = new HashMap<>();
        Map<ExecutableEntity, Statistic> statistics = resultsProvider.getStatistics();
        CustomDefinitions.STORIES.add(payload, getTotal(statistics, ExecutableEntity.STORY));
        CustomDefinitions.SCENARIOS.add(payload, getTotal(statistics, ExecutableEntity.SCENARIO));
        CustomDefinitions.STEPS.add(payload, getTotal(statistics, ExecutableEntity.STEP));
        CustomDefinitions.DURATION.add(payload, Long.toString(resultsProvider.getDuration().toSeconds()));
        payload.put(SESSION_CONTROL, "end");

        AnalyticsEvent testFinishEvent = new AnalyticsEvent("finishTests", payload);
        eventBus.post(new AnalyticsEventBatch(List.of(testFinishEvent)));
    }

    private static String getTotal(Map<ExecutableEntity, Statistic> statistics, ExecutableEntity nodeType)
    {
        long total = statistics.get(nodeType).getTotal();
        return Long.toString(total);
    }

    private List<AnalyticsEvent> collectPluginsAnalytic(Map<String, String> modules)
    {
        return modules.entrySet().stream().filter(module -> module.getKey().startsWith("vividus-plugin-")).map(module ->
        {
            Map<String, String> payload = new HashMap<>();
            CustomDefinitions.PLUGIN_VERSION.add(payload, module.getValue());
            return new AnalyticsEvent(module.getKey(), "use", payload);
        }).collect(Collectors.toList());
    }

    private Map<String, String> getEnvironmentProperties(PropertyCategory propertyCategory)
    {
        return EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION.get(propertyCategory);
    }
}
