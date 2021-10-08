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

package org.vividus.ui.web.action.search;

import java.util.Arrays;
import java.util.Objects;

import org.vividus.ui.action.search.SearchParameters;

public class ShadowDomSearchParameters extends SearchParameters
{
    private final String upperShadowHost;
    private final String[] innerShadowHosts;

    public ShadowDomSearchParameters(String targetValue, String upperShadowHost, String... innerShadowHosts)
    {
        super(targetValue);
        this.upperShadowHost = upperShadowHost;
        this.innerShadowHosts = Arrays.copyOf(innerShadowHosts, innerShadowHosts.length);
    }

    public String getUpperShadowHost()
    {
        return upperShadowHost;
    }

    public String[] getInnerShadowHosts()
    {
        return Arrays.copyOf(innerShadowHosts, innerShadowHosts.length);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(upperShadowHost, Arrays.hashCode(innerShadowHosts), getValue(),
                getVisibility(), isWaitForElement());
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
        ShadowDomSearchParameters other = (ShadowDomSearchParameters) obj;
        return Objects.equals(getValue(), other.getValue())
                && Objects.equals(getUpperShadowHost(), other.getUpperShadowHost())
                && Arrays.equals(innerShadowHosts, other.innerShadowHosts)
                && getVisibility() == other.getVisibility()
                && isWaitForElement() == other.isWaitForElement();
    }
}
