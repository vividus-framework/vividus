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

import java.util.Map;

import org.junit.jupiter.api.Test;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;

class DeviceTests
{
    @Test
    void validateHashCodeAndEquals()
    {
        EqualsVerifier.simple().forClass(Device.class)
                .suppress(Warning.NULL_FIELDS)
                .verify();
    }

    @Test
    void testToString()
    {
        var device = new Device();
        device.setDesiredCapabilities(Map.of("platformName", "iOS"));
        assertEquals("{desiredCapabilities={platformName=iOS}}", device.toString());
    }
}
