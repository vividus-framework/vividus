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

package org.vividus.accessibility.model.htmlcs;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.vividus.accessibility.model.AbstractAccessibilityCheckOptions;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HtmlCsCheckOptions extends AbstractAccessibilityCheckOptions
{
    private final String standard;
    private List<String> ignore;
    private List<String> include;
    @JsonIgnore
    private ViolationLevel level;

    public HtmlCsCheckOptions(AccessibilityStandard standard)
    {
        this.standard = standard.getStandardName();
    }

    public String getStandard()
    {
        return standard;
    }

    public List<String> getIgnore()
    {
        return ignore;
    }

    public void setIgnore(List<String> ignore)
    {
        this.ignore = ignore;
    }

    public void setInclude(List<String> include)
    {
        this.include = include;
    }

    public List<String> getInclude()
    {
        return include;
    }

    public ViolationLevel getLevel()
    {
        return level;
    }

    public void setLevel(ViolationLevel level)
    {
        this.level = level;
    }
}
