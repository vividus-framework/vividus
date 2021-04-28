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

package org.vividus.accessibility.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessibilityCheckOptions
{
    private final String standard;
    private List<String> ignore;
    private List<String> include;
    private String rootElement;
    private String elementsToCheck;
    private String hideElements;
    @JsonIgnore
    private ViolationLevel level;

    public AccessibilityCheckOptions(AccessibilityStandard standard)
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

    public String getRootElement()
    {
        return rootElement;
    }

    public void setRootElement(String rootElement)
    {
        this.rootElement = rootElement;
    }

    public String getElementsToCheck()
    {
        return elementsToCheck;
    }

    public void setElementsToCheck(String elementsToCheck)
    {
        this.elementsToCheck = elementsToCheck;
    }

    public void setInclude(List<String> include)
    {
        this.include = include;
    }

    public List<String> getInclude()
    {
        return include;
    }

    public String getHideElements()
    {
        return hideElements;
    }

    public void setHideElements(String hideElements)
    {
        this.hideElements = hideElements;
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
