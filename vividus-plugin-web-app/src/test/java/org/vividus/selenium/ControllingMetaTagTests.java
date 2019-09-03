/*
 * Copyright 2019 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.Meta;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.bdd.model.MetaWrapper;

@ExtendWith(MockitoExtension.class)
class ControllingMetaTagTests
{
    private static final String VERSION = "version";
    private static final String VERSION_NUMBER = "10";
    private static final String PROXY = "proxy";
    private static final String APPIUM_VERSION = "appiumVersion";

    @Mock
    private DesiredCapabilities desiredCapabilities;

    @Mock
    private MetaWrapper metaWrapper;

    @Mock
    private Meta meta;

    @Mock
    private Meta metaSecond;

    @Test
    void testGetMetaTagName()
    {
        assertEquals(VERSION, ControllingMetaTag.VERSION.getMetaTagName());
    }

    @Test
    void testSetCapabilityIf()
    {
        when(metaWrapper.getOptionalPropertyValue(VERSION)).thenReturn(Optional.of(VERSION_NUMBER));
        ControllingMetaTag.VERSION.setCapability(desiredCapabilities, metaWrapper);
        verify(metaWrapper).getOptionalPropertyValue(VERSION);
        verify(desiredCapabilities).setCapability(VERSION, VERSION_NUMBER);
    }

    @Test
    void testSetCapabilityIfPropertyNotPresented()
    {
        when(metaWrapper.getOptionalPropertyValue(VERSION)).thenReturn(Optional.empty());
        ControllingMetaTag.VERSION.setCapability(desiredCapabilities, metaWrapper);
        verify(metaWrapper).getOptionalPropertyValue(VERSION);
        verifyZeroInteractions(desiredCapabilities);
    }

    @Test
    void testProxySetCapability()
    {
        ControllingMetaTag.PROXY.setCapability(desiredCapabilities, metaWrapper);
        verifyZeroInteractions(desiredCapabilities, metaWrapper);
    }

    @Test
    void testIsContainedInImpl()
    {
        when(meta.getProperty(VERSION)).thenReturn(VERSION_NUMBER);
        assertTrue(ControllingMetaTag.VERSION.isContainedInImpl(meta));
    }

    @Test
    void testIsContainedInImplPropertyNotPresented()
    {
        when(meta.getProperty(anyString())).thenReturn("");
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
        when(meta.getProperty(VERSION)).thenReturn(VERSION_NUMBER);
        assertTrue(ControllingMetaTag.VERSION.isContainedIn(meta, metaSecond));
    }

    @Test
    void testIsContainedInPropertyNotPresented()
    {
        when(meta.getProperty(VERSION)).thenReturn(StringUtils.EMPTY);
        when(metaSecond.getProperty(VERSION)).thenReturn(StringUtils.EMPTY);
        assertFalse(ControllingMetaTag.VERSION.isContainedIn(meta, metaSecond));
    }

    @Test
    void testIsAnyContainedIn()
    {
        when(meta.getProperty(anyString())).thenReturn(StringUtils.EMPTY);
        when(meta.getProperty(VERSION)).thenReturn(StringUtils.EMPTY);
        when(meta.getProperty(APPIUM_VERSION)).thenReturn("1.8.1");
        assertTrue(ControllingMetaTag.isAnyContainedIn(meta));
    }

    @Test
    void testIsAnyContainedInPropertyNotPresented()
    {
        when(meta.getProperty(anyString())).thenReturn(StringUtils.EMPTY);
        when(meta.getProperty(VERSION)).thenReturn(StringUtils.EMPTY);
        when(meta.getProperty(APPIUM_VERSION)).thenReturn(StringUtils.EMPTY);
        assertFalse(ControllingMetaTag.isAnyContainedIn(meta));
    }

    @Test
    void testSetDesiredCapabilitiesFromMeta()
    {
        when(metaWrapper.getOptionalPropertyValue(anyString())).thenReturn(Optional.empty());
        when(metaWrapper.getOptionalPropertyValue(VERSION)).thenReturn(Optional.of(VERSION_NUMBER));
        ControllingMetaTag.setDesiredCapabilitiesFromMeta(desiredCapabilities, metaWrapper);
        verify(metaWrapper).getOptionalPropertyValue(VERSION);
        verify(desiredCapabilities).setCapability(VERSION, VERSION_NUMBER);
    }

    @Test
    void testSetDesiredCapabilitiesFromMetaPropertyNotPresented()
    {
        when(metaWrapper.getOptionalPropertyValue(anyString())).thenReturn(Optional.empty());
        ControllingMetaTag.setDesiredCapabilitiesFromMeta(desiredCapabilities, metaWrapper);
        verify(metaWrapper).getOptionalPropertyValue(VERSION);
        verifyZeroInteractions(desiredCapabilities);
    }
}
