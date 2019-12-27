/*
 * Copyright 2019 the original author or authors.
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

package org.vividus.ui.web.action.search;

import java.util.Objects;

public class SearchParameters
{
    private String value;
    private Visibility visibility = Visibility.VISIBLE;
    private boolean waitForElement = true;

    public SearchParameters()
    {
        // Default constructor
    }

    public SearchParameters(String value)
    {
        this.value = value;
    }

    public SearchParameters(SearchParameters searchParameters)
    {
        this.value = searchParameters.value;
        this.visibility = searchParameters.visibility;
        this.waitForElement = searchParameters.waitForElement;
    }

    public String getValue()
    {
        return value;
    }

    public Visibility getVisibility()
    {
        return visibility;
    }

    public SearchParameters setVisibility(Visibility visibility)
    {
        this.visibility = visibility;
        return this;
    }

    public boolean isWaitForElement()
    {
        return waitForElement;
    }

    public SearchParameters setWaitForElement(boolean waitForElement)
    {
        this.waitForElement = waitForElement;
        return this;
    }

    @Override
    public String toString()
    {
        return value;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(value, visibility, waitForElement);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        SearchParameters other = (SearchParameters) obj;
        return Objects.equals(value, other.value) && visibility == other.visibility
                && waitForElement == other.waitForElement;
    }
}
