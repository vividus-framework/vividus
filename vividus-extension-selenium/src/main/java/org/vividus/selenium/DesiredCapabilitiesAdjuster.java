/*
 * Copyright 2019-2022 the original author or authors.
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

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Validate;
import org.openqa.selenium.remote.DesiredCapabilities;

public abstract class DesiredCapabilitiesAdjuster
{
    public final void adjust(DesiredCapabilities desiredCapabilities)
    {
        Map<String, Object> input = desiredCapabilities.asMap();
        Map<String, Object> capabilitiesToAdd = getExtraCapabilities(new DesiredCapabilities(desiredCapabilities));
        Set<Entry<String, Object>> conflictingCapabilities = capabilitiesToAdd.entrySet()
                                                                              .stream()
                                                                              .filter(e -> input.containsKey(
                                                                                      e.getKey()))
                                                                              .collect(Collectors.toSet());
        Validate.isTrue(conflictingCapabilities.isEmpty(),
            "Capabilities adjuster tried to add conflicting capabilities: %s", conflictingCapabilities);
        capabilitiesToAdd.forEach(desiredCapabilities::setCapability);
    }

    protected abstract Map<String, Object> getExtraCapabilities(DesiredCapabilities desiredCapabilities);
}
