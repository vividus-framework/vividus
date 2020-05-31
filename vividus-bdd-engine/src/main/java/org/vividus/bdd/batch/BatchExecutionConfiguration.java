/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.bdd.batch;

import java.time.Duration;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

public class BatchExecutionConfiguration
{
    private String name;
    private Integer threads;
    private List<String> metaFilters;
    private Duration storyExecutionTimeout;
    private Boolean ignoreFailure;

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

    public void setMetaFilters(String metaFilters)
    {
        setMetaFilters(metaFilters != null ? List.of(StringUtils.split(metaFilters, ',')) : null);
    }

    public void setMetaFilters(List<String> metaFilters)
    {
        this.metaFilters = metaFilters;
    }

    public Duration getStoryExecutionTimeout()
    {
        return storyExecutionTimeout;
    }

    public void setStoryExecutionTimeout(Duration storyExecutionTimeout)
    {
        this.storyExecutionTimeout = storyExecutionTimeout;
    }

    public Boolean isIgnoreFailure()
    {
        return ignoreFailure;
    }

    public void setIgnoreFailure(Boolean ignoreFailure)
    {
        this.ignoreFailure = ignoreFailure;
    }
}
