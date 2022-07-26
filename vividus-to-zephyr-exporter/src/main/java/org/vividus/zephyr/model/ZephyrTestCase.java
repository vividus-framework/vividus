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

package org.vividus.zephyr.model;

import java.util.List;
import java.util.Set;

public class ZephyrTestCase
{
    private String projectKey;
    private Set<String> labels;
    private Set<String> components;
    private List<CucumberTestStep> testSteps;
    private String summary;

    public String getProjectKey()
    {
        return projectKey;
    }

    public void setProjectKey(String projectKey)
    {
        this.projectKey = projectKey;
    }

    public Set<String> getLabels()
    {
        return labels;
    }

    public void setLabels(Set<String> labels)
    {
        this.labels = labels;
    }

    public Set<String> getComponents()
    {
        return components;
    }

    public void setComponents(Set<String> components)
    {
        this.components = components;
    }

    public List<CucumberTestStep> getTestSteps()
    {
        return testSteps;
    }

    public void setTestSteps(List<CucumberTestStep> testSteps)
    {
        this.testSteps = testSteps;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
    }
}
