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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.List;
import java.util.Map;

import org.jbehave.core.model.Meta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.remote.DesiredCapabilities;

class ControllingMetaTagTests
{
    private static final String PROXY = "proxy";

    @Test
    void shouldNotSetCapabilityForProxy()
    {
        var meta = mock(Meta.class);
        var desiredCapabilities = mock(DesiredCapabilities.class);
        ControllingMetaTag.PROXY.setCapability(desiredCapabilities, meta);
        verifyNoInteractions(desiredCapabilities, meta);
    }

    @Test
    void shouldContainProxyMetaTag()
    {
        var meta = new Meta(List.of(PROXY));
        assertTrue(ControllingMetaTag.PROXY.isContainedIn(meta));
    }

    @ParameterizedTest
    @CsvSource({
            "capability.version 96, true",
            "version 96, false"
    })
    void testIsAnyContainedIn(String metaProperty, boolean expectedResult)
    {
        var meta = new Meta(List.of(metaProperty));
        assertEquals(expectedResult, ControllingMetaTag.isAnyContainedIn(meta));
    }

    @Test
    void shouldNotSetDesiredCapabilitiesFromMetaForUnknownTags()
    {
        var meta = new Meta(List.of(PROXY, "browserName Chrome"));
        var desiredCapabilities = mock(DesiredCapabilities.class);
        ControllingMetaTag.setDesiredCapabilitiesFromMeta(desiredCapabilities, meta);
        verify(desiredCapabilities).asMap();
        verifyNoMoreInteractions(desiredCapabilities);
    }

    @Test
    void shouldSetDesiredCapabilitiesFromCapabilityMeta()
    {
        var meta = new Meta(List.of(
                "capability.appiumVersion 1.20.1",
                "capability.version 95",
                "capability.acceptInsecureCerts true",
                "capability.group.appiumVersion 1.22.0",
                "capability.group.version 96",
                "capability.group.acceptInsecureCerts false"
        ));
        DesiredCapabilities capabilities = new DesiredCapabilities();
        ControllingMetaTag.CAPABILITY.setCapability(capabilities, meta);
        var acceptInsecureCerts = "acceptInsecureCerts";
        var version = "version";
        var appiumVersion = "appiumVersion";
        assertEquals(Map.of(
            acceptInsecureCerts, true,
            appiumVersion, "1.20.1",
            version, "95",
            "group", Map.of(
                        acceptInsecureCerts, false,
                        appiumVersion, "1.22.0",
                        version, "96"
                    )
        ), capabilities.asMap());
    }
}
