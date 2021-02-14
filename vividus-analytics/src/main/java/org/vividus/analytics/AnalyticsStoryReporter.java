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

package org.vividus.analytics;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import com.google.common.base.Stopwatch;
import com.google.common.eventbus.EventBus;

import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.vividus.analytics.model.AnalyticsEvent;
import org.vividus.analytics.model.CustomDefinitions;
import org.vividus.bdd.SystemStoriesAwareStoryReporter;
import org.vividus.reporter.environment.EnvironmentConfigurer;
import org.vividus.reporter.environment.PropertyCategory;

public class AnalyticsStoryReporter extends SystemStoriesAwareStoryReporter
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
    public void beforeStory(Story story, boolean givenStory)
    {
        if (processSystemStory(story.getPath()))
        {
            return;
        }
        STORIES.incrementAndGet();
    }

    @Override
    public void beforeScenario(Scenario scenario)
    {
        SCENARIOS.incrementAndGet();
    }

    @Override
    public void beforeStep(String step)
    {
        STEPS.incrementAndGet();
    }

    @Override
    protected void beforeStories()
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
        eventBus.post(new AnalyticsEvent("startTests", payload));

        postPluginsAnalytic(modules);
        stopwatch = Stopwatch.createStarted();
    }

    private void postPluginsAnalytic(Map<String, String> modules)
    {
        modules.forEach((k, v) -> {
            if (k.startsWith("vividus-plugin-"))
            {
                Map<String, String> payload = new HashMap<>();
                CustomDefinitions.PLUGIN_VERSION.add(payload, v);
                eventBus.post(new AnalyticsEvent(k, "use", payload));
            }
        });
    }

    @Override
    protected void afterStories()
    {
        long duration = stopwatch.elapsed().toSeconds();
        Map<String, String> payload = new HashMap<>();
        CustomDefinitions.STORIES.add(payload, STORIES.toString());
        CustomDefinitions.SCENARIOS.add(payload, SCENARIOS.toString());
        CustomDefinitions.STEPS.add(payload, STEPS.toString());
        CustomDefinitions.DURATION.add(payload, Long.toString(duration));
        payload.put(SESSION_CONTROL, "end");
        AnalyticsEvent testFinishEvent = new AnalyticsEvent("finishTests", payload);
        eventBus.post(testFinishEvent);
    }

    private Map<String, String> getEnvironmentProperties(PropertyCategory propertyCategory)
    {
        return EnvironmentConfigurer.ENVIRONMENT_CONFIGURATION.get(propertyCategory);
    }
}
