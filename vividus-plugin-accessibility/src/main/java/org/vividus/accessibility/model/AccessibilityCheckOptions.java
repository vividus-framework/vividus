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

package org.vividus.accessibility.model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;

import org.openqa.selenium.WebElement;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccessibilityCheckOptions
{
    private final String standard;
    private List<String> ignore;
    private List<String> include;
    @JsonIgnore
    private WebElement rootElement;
    @JsonIgnore
    private List<WebElement> elementsToCheck;
    @JsonIgnore
    private List<WebElement> hideElements;
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

    public WebElement getRootElement()
    {
        return rootElement;
    }

    public void setRootElement(WebElement rootElement)
    {
        this.rootElement = rootElement;
    }

    public List<WebElement> getElementsToCheck()
    {
        return elementsToCheck;
    }

    public void setElementsToCheck(List<WebElement> elementsToCheck)
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

    public List<WebElement> getHideElements()
    {
        return hideElements;
    }

    public void setHideElements(List<WebElement> hideElements)
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
