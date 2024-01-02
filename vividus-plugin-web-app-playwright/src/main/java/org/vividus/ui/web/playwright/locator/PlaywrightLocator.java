/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.playwright.locator;

import java.util.Objects;

public class PlaywrightLocator
{
    private final String locatorType;
    private final String locatorValue;

    public PlaywrightLocator(String locatorType, String locatorValue)
    {
        this.locatorType = locatorType;
        this.locatorValue = locatorValue;
    }

    public String getLocator()
    {
        return locatorType + "=" + locatorValue;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        PlaywrightLocator that = (PlaywrightLocator) o;
        return Objects.equals(locatorType, that.locatorType) && Objects.equals(locatorValue, that.locatorValue);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(locatorType, locatorValue);
    }

    @Override
    public String toString()
    {
        return locatorType + '(' + locatorValue + ')';
    }
}
