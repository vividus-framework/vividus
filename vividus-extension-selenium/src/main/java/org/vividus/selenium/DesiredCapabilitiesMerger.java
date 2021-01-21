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

package org.vividus.selenium;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.remote.DesiredCapabilities;

public final class DesiredCapabilitiesMerger
{
    private DesiredCapabilitiesMerger()
    {
    }

    /**
     * Merge the source capabilities into the base capabilities, the values from the source
     * capabilities have precedence over the values in the base capabilities
     *
     * @param base The base capabilities to merge the source capabilities into
     * @param source The source capabilities to merge into the base capabilities
     * @return The merging result of the base and source desired capabilities
     */
    @SuppressWarnings("unchecked")
    public static DesiredCapabilities merge(DesiredCapabilities base, DesiredCapabilities source)
    {
        DesiredCapabilities baseCapabilities = new DesiredCapabilities(base);
        source.asMap().forEach((capabilityName, capabilityValue) ->
        {
            Object baseCapabilityValue = baseCapabilities.getCapability(capabilityName);
            if (baseCapabilityValue instanceof Map && capabilityValue instanceof Map)
            {
                Map<String, Object> capabilities = new HashMap<>((Map<String, Object>) baseCapabilityValue);
                capabilities.putAll((Map<String, Object>) capabilityValue);
                baseCapabilities.setCapability(capabilityName, capabilities);
                return;
            }
            baseCapabilities.setCapability(capabilityName, capabilityValue);
        });
        return baseCapabilities;
    }
}
