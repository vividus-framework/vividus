/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.batch;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;

public class BatchConfiguration
{
    private String resourceLocation;
    private List<String> resourceIncludePatterns = List.of();
    private List<String> resourceExcludePatterns = List.of();
    private String name;
    private Integer threads;
    private List<String> metaFilters;
    private Boolean failFast;
    private ScenarioExecutionConfiguration scenario = new ScenarioExecutionConfiguration();
    private StoryExecutionConfiguration story = new StoryExecutionConfiguration();
    private Map<String, String> variables = Map.of();

    public String getResourceLocation()
    {
        return resourceLocation;
    }

    public void setResourceLocation(String resourceLocation)
    {
        this.resourceLocation = resourceLocation;
    }

    public List<String> getResourceIncludePatterns()
    {
        return Collections.unmodifiableList(resourceIncludePatterns);
    }

    public void setResourceIncludePatterns(String resourceIncludePatterns)
    {
        this.resourceIncludePatterns = convertToList(resourceIncludePatterns);
    }

    public List<String> getResourceExcludePatterns()
    {
        return Collections.unmodifiableList(resourceExcludePatterns);
    }

    public void setResourceExcludePatterns(String resourceExcludePatterns)
    {
        this.resourceExcludePatterns = convertToList(resourceExcludePatterns);
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Integer getThreads()
    {
        return threads;
    }

    public void setThreads(Integer threads)
    {
        this.threads = threads;
    }

    public List<String> getMetaFilters()
    {
        return metaFilters;
    }

    public void setMetaFilters(List<String> metaFilters)
    {
        this.metaFilters = metaFilters;
    }

    public void setMetaFilters(String metaFilters)
    {
        setMetaFilters(metaFilters != null ? List.of(StringUtils.split(metaFilters, ',')) : null);
    }

    public Duration getStoryExecutionTimeout()
    {
        return story.executionTimeout;
    }

    public void overrideStoryExecutionTimeout(Duration storyExecutionTimeout)
    {
        story.executionTimeout = storyExecutionTimeout;
    }

    public Boolean isFailFast()
    {
        return failFast;
    }

    public void setFailFast(Boolean failFast)
    {
        this.failFast = failFast;
    }

    public Boolean isFailScenarioFast()
    {
        return scenario.failFast;
    }

    public void setScenario(ScenarioExecutionConfiguration scenario)
    {
        this.scenario = scenario;
    }

    public Boolean isFailStoryFast()
    {
        return story.failFast;
    }

    public void setStory(StoryExecutionConfiguration story)
    {
        this.story = story;
    }

    public void setVariables(Map<String, String> variables)
    {
        this.variables = variables;
    }

    public Map<String, String> getVariables()
    {
        return variables;
    }

    private List<String> convertToList(String list)
    {
        if (list == null)
        {
            return List.of();
        }
        return Stream.of(StringUtils.split(list, ','))
                     .map(String::strip)
                     .collect(Collectors.toList());
    }

    public static final class StoryExecutionConfiguration
    {
        private Boolean failFast;
        private Duration executionTimeout;

        public void setFailFast(Boolean failFast)
        {
            this.failFast = failFast;
        }

        public void setExecutionTimeout(Duration executionTimeout)
        {
            this.executionTimeout = executionTimeout;
        }
    }

    public static final class ScenarioExecutionConfiguration
    {
        private Boolean failFast;

        public void setFailFast(Boolean failFast)
        {
            this.failFast = failFast;
        }
    }
}
