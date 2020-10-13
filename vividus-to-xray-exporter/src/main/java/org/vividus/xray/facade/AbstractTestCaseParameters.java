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

package org.vividus.xray.facade;

import java.util.Set;

import org.vividus.xray.model.TestCaseType;

public abstract class AbstractTestCaseParameters
{
    private TestCaseType type;
    private String summary;
    private Set<String> labels;
    private Set<String> components;

    public TestCaseType getType()
    {
        return type;
    }

    public void setType(TestCaseType type)
    {
        this.type = type;
    }

    public String getSummary()
    {
        return summary;
    }

    public void setSummary(String summary)
    {
        this.summary = summary;
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
}
