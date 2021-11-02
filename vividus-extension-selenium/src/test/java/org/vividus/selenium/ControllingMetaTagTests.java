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

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.jbehave.core.model.Meta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.remote.DesiredCapabilities;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class ControllingMetaTagTests
{
    private static final String VERSION = "version";
    private static final String VERSION_NUMBER = "10";
    private static final String PROXY = "proxy";
    private static final String APPIUM_VERSION = "appiumVersion";

    @Mock private DesiredCapabilities desiredCapabilities;
    @Mock private Meta meta;
    @Mock private Meta metaSecond;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(ControllingMetaTag.class);

    @Test
    void testGetMetaTagName()
    {
        assertEquals(VERSION, ControllingMetaTag.VERSION.getMetaTagName());
    }

    @Test
    void testSetCapabilityIf()
    {
        when(meta.getOptionalProperty(VERSION)).thenReturn(Optional.of(VERSION_NUMBER));
        ControllingMetaTag.VERSION.setCapability(desiredCapabilities, meta);
        verify(meta).getOptionalProperty(VERSION);
        verify(desiredCapabilities).setCapability(VERSION, VERSION_NUMBER);
        assertThat(logger.getLoggingEvents(), is(List.of(
                warn("Setting of capability via meta tag '{}' is deprecated and will be removed in VIVIDUS 0.4.0, "
                        + "please, use 'capability.{}' meta tag instead", VERSION, VERSION))));
    }

    @Test
    void testSetCapabilityIfPropertyNotPresented()
    {
        when(meta.getOptionalProperty(VERSION)).thenReturn(Optional.empty());
        ControllingMetaTag.VERSION.setCapability(desiredCapabilities, meta);
        verify(meta).getOptionalProperty(VERSION);
        verifyNoInteractions(desiredCapabilities);
    }

    @Test
    void testProxySetCapability()
    {
        ControllingMetaTag.PROXY.setCapability(desiredCapabilities, meta);
        verifyNoInteractions(desiredCapabilities, meta);
    }

    @Test
    void testIsContainedInImpl()
    {
        when(meta.getOptionalProperty(VERSION)).thenReturn(Optional.of(VERSION_NUMBER));
        assertTrue(ControllingMetaTag.VERSION.isContainedInImpl(meta));
    }

    @Test
    void testIsContainedInImplPropertyNotPresented()
    {
        when(meta.getOptionalProperty(anyString())).thenReturn(Optional.empty());
        assertFalse(ControllingMetaTag.VERSION.isContainedInImpl(meta));
    }

    @Test
    void testProxyIsContainedInImpl()
    {
        when(meta.hasProperty(PROXY)).thenReturn(true);
        assertTrue(ControllingMetaTag.PROXY.isContainedInImpl(meta));
    }

    @Test
    void testProxyIsContainedInImplPropertyNotPresented()
    {
        when(meta.hasProperty(PROXY)).thenReturn(false);
        assertFalse(ControllingMetaTag.PROXY.isContainedInImpl(meta));
    }

    @Test
    void testIsContainedIn()
    {
        when(meta.getOptionalProperty(VERSION)).thenReturn(Optional.of(VERSION_NUMBER));
        assertTrue(ControllingMetaTag.VERSION.isContainedIn(meta, metaSecond));
    }

    @Test
    void testIsContainedInPropertyNotPresented()
    {
        when(meta.getOptionalProperty(VERSION)).thenReturn(Optional.empty());
        when(metaSecond.getOptionalProperty(VERSION)).thenReturn(Optional.empty());
        assertFalse(ControllingMetaTag.VERSION.isContainedIn(meta, metaSecond));
    }

    @Test
    void testIsAnyContainedIn()
    {
        when(meta.getOptionalProperty(anyString())).thenReturn(Optional.empty());
        when(meta.getOptionalProperty(VERSION)).thenReturn(Optional.empty());
        when(meta.getOptionalProperty(APPIUM_VERSION)).thenReturn(Optional.of("1.8.1"));
        assertTrue(ControllingMetaTag.isAnyContainedIn(meta));
    }

    @Test
    void testIsAnyContainedInPropertyNotPresented()
    {
        when(meta.getOptionalProperty(anyString())).thenReturn(Optional.empty());
        when(meta.getOptionalProperty(VERSION)).thenReturn(Optional.empty());
        when(meta.getOptionalProperty(APPIUM_VERSION)).thenReturn(Optional.empty());
        assertFalse(ControllingMetaTag.isAnyContainedIn(meta));
    }

    @Test
    void testSetDesiredCapabilitiesFromMeta()
    {
        when(meta.getOptionalProperty(anyString())).thenReturn(Optional.empty());
        when(meta.getOptionalProperty(VERSION)).thenReturn(Optional.of(VERSION_NUMBER));
        ControllingMetaTag.setDesiredCapabilitiesFromMeta(desiredCapabilities, meta);
        verify(meta).getOptionalProperty(VERSION);
        verify(desiredCapabilities).setCapability(VERSION, VERSION_NUMBER);
    }

    @Test
    void testSetDesiredCapabilitiesFromMetaPropertyNotPresented()
    {
        when(meta.getOptionalProperty(anyString())).thenReturn(Optional.empty());
        ControllingMetaTag.setDesiredCapabilitiesFromMeta(desiredCapabilities, meta);
        verify(meta).getOptionalProperty(VERSION);
        verify(desiredCapabilities).asMap();
        verifyNoMoreInteractions(desiredCapabilities);
    }

    @Test
    void testSetDesiredCapabilitiesFromCapabilityMeta()
    {
        String prefix = ControllingMetaTag.CAPABILITY.getMetaTagName();
        String groupPrefix = "group.";
        String acceptInsecureCerts = "acceptInsecureCerts";
        Map<String, String> metaValues = Map.of(
                prefix + APPIUM_VERSION, VERSION_NUMBER,
                prefix + VERSION, VERSION_NUMBER,
                prefix + acceptInsecureCerts, Boolean.TRUE.toString(),
                prefix + groupPrefix + APPIUM_VERSION, VERSION_NUMBER,
                prefix + groupPrefix + VERSION, VERSION_NUMBER,
                prefix + groupPrefix + acceptInsecureCerts, Boolean.TRUE.toString()
        );
        when(meta.getPropertyNames()).thenReturn(metaValues.keySet());
        metaValues.forEach((name, value) -> when(meta.getProperty(name)).thenReturn(value));
        DesiredCapabilities capabilities = new DesiredCapabilities();
        ControllingMetaTag.CAPABILITY.setCapability(capabilities, meta);
        assertEquals(Map.of(
            acceptInsecureCerts, true,
            APPIUM_VERSION, VERSION_NUMBER,
            VERSION, VERSION_NUMBER,
            "group", Map.of(
                        acceptInsecureCerts, true,
                        APPIUM_VERSION, VERSION_NUMBER,
                        VERSION, VERSION_NUMBER
                    )
        ), capabilities.asMap());
        verifyNoMoreInteractions(meta);
    }
}
