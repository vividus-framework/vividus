/*
 * Copyright 2019-2025 the original author or authors.
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
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.openqa.selenium.Platform;
import org.openqa.selenium.remote.DesiredCapabilities;

public final class DeviceSearchParameters
{
    private static final String DEVICE_SEARCH_CAPABILITY_PREFIX = "mobitru-device-search:";
    private static final String MISSING_CAPABILITY_ERROR_FORMAT = "The `%s` capability must be set";
    private static final String VERSION = "version";
    private static final String TYPE = "type";
    private static final String UDIDS = "udids";

    private final String platform;
    private final Map<String, String> parameters;
    private final Set<String> udids;

    public DeviceSearchParameters(DesiredCapabilities desiredCapabilities)
    {
        Map<String, String> parameters = desiredCapabilities.asMap().entrySet().stream()
                .filter(e -> e.getKey().startsWith(DEVICE_SEARCH_CAPABILITY_PREFIX))
                .collect(Collectors.toMap(e -> StringUtils.substringAfter(e.getKey(), DEVICE_SEARCH_CAPABILITY_PREFIX),
                        e -> e.getValue().toString()));
        Optional<Platform> platformOptional = Optional.ofNullable(desiredCapabilities.getPlatformName());
        Function<Platform, String> getPlatformName = p -> p.name().toLowerCase();

        String udidsString = parameters.remove(UDIDS);
        if (udidsString != null)
        {
            this.udids = Set.of(udidsString.split(",\\s*"));
            this.platform = getPlatformName.apply(platformOptional.orElse(Platform.ANY));
        }
        else
        {
            this.udids = Set.of();
            this.platform = getPlatformName.apply(platformOptional
                    .orElseThrow(() -> new IllegalArgumentException(
                            String.format(MISSING_CAPABILITY_ERROR_FORMAT, "platformName"))));
            Optional.ofNullable(desiredCapabilities.getCapability("platformVersion"))
                    .ifPresent(platformVersion -> parameters.put(VERSION, (String) platformVersion));
            Validate.isTrue(parameters.get(TYPE) != null,
                    String.format(MISSING_CAPABILITY_ERROR_FORMAT, DEVICE_SEARCH_CAPABILITY_PREFIX + TYPE));
        }
        Validate.isTrue(udids.isEmpty() != parameters.isEmpty(),
                "Conflicting capabilities are found. Only one device selection method is allowed:"
                        + " either by udids or by search parameters");
        this.parameters = Collections.unmodifiableMap(parameters);
    }

    @Override
    public String toString()
    {
        final String commaSeparator = ", ";
        StringBuilder value = new StringBuilder("platform = " + platform);
        for (Map.Entry<String, String> entry : parameters.entrySet())
        {
            value.append(commaSeparator).append(entry.getKey()).append(" = ").append(entry.getValue());
        }
        if (!udids.isEmpty())
        {
            String udidsString = String.join(commaSeparator, udids);
            value.append(", udids = ").append(udidsString);
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

    public Set<String> getUdids()
    {
        return udids;
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
        return platform.equals(that.platform) && Objects.equals(parameters, that.parameters)
                && Objects.equals(udids, that.udids);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(platform, parameters, udids);
    }
}
