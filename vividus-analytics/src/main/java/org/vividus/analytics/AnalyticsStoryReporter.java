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
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.google.common.base.Stopwatch;
import com.google.common.eventbus.EventBus;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Step;
import org.jbehave.core.model.Story;
import org.jbehave.core.reporters.NullStoryReporter;
import org.jbehave.core.steps.StepCollector;
import org.jbehave.core.steps.StepCollector.Stage;
import org.vividus.analytics.model.AnalyticsEvent;
import org.vividus.analytics.model.AnalyticsEventBatch;
import org.vividus.analytics.model.CustomDefinitions;
import org.vividus.reporter.environment.EnvironmentConfigurer;
import org.vividus.reporter.environment.PropertyCategory;

public class AnalyticsStoryReporter extends NullStoryReporter
{
    private static final AtomicLong STORIES = new AtomicLong();
    private static final AtomicLong SCENARIOS = new AtomicLong();
    private static final AtomicLong STEPS = new AtomicLong();

    private static final String SESSION_CONTROL = "sc";

    private Stopwatch stopwatch;

    private final EventBus eventBus;

    public AnalyticsStoryReporter(EventBus eventBus)
    {
        this.eventBus = eventBus;
    }

    @Override
    public void beforeStoriesSteps(StepCollector.Stage stage)
    {
        if (stage == StepCollector.Stage.BEFORE)
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

            stopwatch = Stopwatch.createStarted();
        }
    }

    @Override
    public void afterStoriesSteps(Stage stage)
    {
        if (stage == Stage.AFTER)
        {
            long duration = stopwatch.elapsed().toSeconds();
            Map<String, String> payload = new HashMap<>();
            CustomDefinitions.STORIES.add(payload, STORIES.toString());
            CustomDefinitions.SCENARIOS.add(payload, SCENARIOS.toString());
            CustomDefinitions.STEPS.add(payload, STEPS.toString());
            CustomDefinitions.DURATION.add(payload, Long.toString(duration));
            payload.put(SESSION_CONTROL, "end");

            AnalyticsEvent testFinishEvent = new AnalyticsEvent("finishTests", payload);
            eventBus.post(new AnalyticsEventBatch(List.of(testFinishEvent)));
        }
    }

    @Override
    public void beforeStory(Story story, boolean givenStory)
    {
        STORIES.incrementAndGet();
    }

    @Override
    public void beforeScenario(Scenario scenario)
    {
        SCENARIOS.incrementAndGet();
    }

    @Override
    public void beforeStep(Step step)
    {
        STEPS.incrementAndGet();
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
