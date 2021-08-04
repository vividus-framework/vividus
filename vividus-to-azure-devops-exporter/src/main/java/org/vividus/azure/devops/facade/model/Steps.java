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

package org.vividus.azure.devops.facade.model;

import java.util.List;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

@JacksonXmlRootElement(localName = "steps")
public final class Steps
{
    @JacksonXmlProperty(isAttribute = true)
    private final String id;
    @JacksonXmlProperty(isAttribute = true)
    private final String last;
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "step")
    private final List<Step> steps;

    public Steps(List<Step> steps)
    {
        this.id = "0";
        this.last = String.valueOf(steps.size() + 1);
        this.steps = steps;
    }

    public String getId()
    {
        return id;
    }

    public String getLast()
    {
        return last;
    }

    public List<Step> getSteps()
    {
        return steps;
    }
}
