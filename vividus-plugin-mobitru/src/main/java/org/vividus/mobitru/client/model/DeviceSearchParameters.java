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

package org.vividus.mobitru.client.model;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.openqa.selenium.remote.DesiredCapabilities;

public final class DeviceSearchParameters
{
    private static final String DEVICE_SEARCH_CAPABILITY_PREFIX = "mobitru-device-search:";
    private static final String MISSING_CAPABILITY_ERROR_FORMAT = "The `%s` capability must be set";
    private static final String VERSION = "version";
    private static final String TYPE = "type";

    private final String platform;
    private final Map<String, String> parameters;

    public DeviceSearchParameters(DesiredCapabilities desiredCapabilities)
    {
        this.platform = Optional.ofNullable(desiredCapabilities.getPlatformName()).orElseThrow(
                () -> new IllegalArgumentException(String.format(MISSING_CAPABILITY_ERROR_FORMAT, "platformName")))
                .name().toLowerCase();
        Map<String, String> parameters = desiredCapabilities.asMap().entrySet().stream()
                .filter(e -> e.getKey().startsWith(DEVICE_SEARCH_CAPABILITY_PREFIX))
                .collect(Collectors.toMap(e -> StringUtils.substringAfter(e.getKey(), DEVICE_SEARCH_CAPABILITY_PREFIX),
                        e -> e.getValue().toString()));
        Optional.ofNullable(desiredCapabilities.getCapability("platformVersion"))
                .ifPresent(platformVersion -> parameters.put(VERSION, (String) platformVersion));
        Validate.isTrue(parameters.get(TYPE) != null,
                String.format(MISSING_CAPABILITY_ERROR_FORMAT, DEVICE_SEARCH_CAPABILITY_PREFIX + TYPE));
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    @Override
    public String toString()
    {
        StringBuilder value = new StringBuilder("platform = " + platform);
        for (Map.Entry<String, String> entry : parameters.entrySet())
        {
            value.append(", ").append(entry.getKey()).append(" = ").append(entry.getValue());
        }
        return value.toString();
    }

    public String getPlatform()
    {
        return platform;
    }

    public Map<String, String> getParameters()
    {
        return parameters;
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
        DeviceSearchParameters that = (DeviceSearchParameters) o;
        return platform.equals(that.platform) && Objects.equals(parameters, that.parameters);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(platform, parameters);
    }
}
