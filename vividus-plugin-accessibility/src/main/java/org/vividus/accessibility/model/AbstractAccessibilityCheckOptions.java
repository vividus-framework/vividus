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
import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.openqa.selenium.WebElement;

public abstract class AbstractAccessibilityCheckOptions
{
    @JsonIgnore
    private Optional<WebElement> rootElement = Optional.empty();
    @JsonIgnore
    private List<WebElement> elementsToCheck;
    @JsonIgnore
    private List<WebElement> hideElements;

    public Optional<WebElement> getRootElement()
    {
        return rootElement;
    }

    public void setRootElement(Optional<WebElement> rootElement)
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

    public List<WebElement> getHideElements()
    {
        return hideElements;
    }

    public void setHideElements(List<WebElement> hideElements)
    {
        this.hideElements = hideElements;
    }
}
