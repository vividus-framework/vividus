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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.remote.DesiredCapabilities;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

class DeviceSearchParametersTests
{
    private static final String DEVICE_TYPE_CAPABILITY_NAME = "mobitru-device-search:type";
    private static final String TYPE = "type";
    private static final String PLATFORM_NAME = "platformName";
    private static final String IOS = "ios";
    private static final String TWELVE = "12";
    private static final String PHONE = "phone";

    static Stream<Arguments> getValidCapabilities()
    {
        return Stream.of(
                Arguments.of(Map.of("platformVersion", TWELVE, PLATFORM_NAME, IOS, DEVICE_TYPE_CAPABILITY_NAME, PHONE),
                        IOS, Map.of(TYPE, PHONE, "version", TWELVE)),
                Arguments.of(Map.of(PLATFORM_NAME, IOS, DEVICE_TYPE_CAPABILITY_NAME, PHONE), IOS, Map.of(TYPE, PHONE)));
    }

    static Stream<Arguments> getInsufficientCapabilities()
    {
        // CHECKSTYLE:OFF
        return Stream.of(
                Arguments.of(Map.of(PLATFORM_NAME, IOS), DEVICE_TYPE_CAPABILITY_NAME),
                Arguments.of(Map.of(),                   PLATFORM_NAME)
        );
        // CHECKSTYLE:ON
    }

    @ParameterizedTest
    @MethodSource("getValidCapabilities")
    void shouldInitialize(Map<String, String> capabilities, String expectedPlatform,
            Map<String, String> expectedParameters)
    {
        var searchParameters = new DeviceSearchParameters(new DesiredCapabilities(capabilities));
        assertEquals(expectedPlatform, searchParameters.getPlatform());
        assertEquals(expectedParameters, searchParameters.getParameters());
    }

    @ParameterizedTest
    @MethodSource("getInsufficientCapabilities")
    void shouldFailIfMissingCapability(Map<String, String> capabilities, String capabilityName)
    {
        var thrown = assertThrows(IllegalArgumentException.class,
                () -> new DeviceSearchParameters(new DesiredCapabilities(capabilities)));
        assertEquals(String.format("The `%s` capability must be set", capabilityName), thrown.getMessage());
    }

    @Test
    void validateHashCodeAndEquals()
    {
        EqualsVerifier.forClass(DeviceSearchParameters.class)
                .suppress(Warning.NULL_FIELDS)
                .verify();
    }

    @Test
    void testToString()
    {
        var searchConfiguration = new DeviceSearchParameters(
                new DesiredCapabilities(Map.of(PLATFORM_NAME, IOS, DEVICE_TYPE_CAPABILITY_NAME, PHONE)));
        assertEquals("platform = ios, type = phone", searchConfiguration.toString());
    }
}
