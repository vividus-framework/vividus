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

import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.vividus.selenium.type.CapabilitiesValueTypeAdjuster.adjustType;
import static org.vividus.util.property.PropertyParser.putByPath;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import org.jbehave.core.model.Meta;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.bdd.model.MetaWrapper;

import io.appium.java_client.remote.MobileCapabilityType;

public enum ControllingMetaTag
{
    @Deprecated(since = "0.3.10", forRemoval = true)
    BROWSER_NAME(CapabilityType.BROWSER_NAME),
    @Deprecated(since = "0.3.10", forRemoval = true)
    VERSION(CapabilityType.VERSION),
    @Deprecated(since = "0.3.10", forRemoval = true)
    SCREEN_RESOLUTION("screen-resolution"),
    @Deprecated(since = "0.3.10", forRemoval = true)
    APPIUM_VERSION(MobileCapabilityType.APPIUM_VERSION),
    @Deprecated(since = "0.3.10", forRemoval = true)
    DEVICE_NAME(MobileCapabilityType.DEVICE_NAME),
    @Deprecated(since = "0.3.10", forRemoval = true)
    DEVICE_ORIENTATION("device-orientation"),
    @Deprecated(since = "0.3.10", forRemoval = true)
    PLATFORM_VERSION(MobileCapabilityType.PLATFORM_VERSION),
    @Deprecated(since = "0.3.10", forRemoval = true)
    PLATFORM_NAME(CapabilityType.PLATFORM_NAME),
    @Deprecated(since = "0.3.10", forRemoval = true)
    IEDRIVER_VERSION("iedriver-version"),
    @Deprecated(since = "0.3.10", forRemoval = true)
    SELENIUM_VERSION("seleniumVersion"),
    PROXY(CapabilityType.PROXY)
    {
        @Override
        protected boolean isContainedInImpl(Meta meta)
        {
            return meta.hasProperty(getMetaTagName());
        }

        @Override
        protected void setCapability(DesiredCapabilities capabilities, Meta meta)
        {
            // Nothing to do
        }
    },
    CAPABILITY("capability.")
    {
        @Override
        protected void setCapability(DesiredCapabilities capabilities, Meta meta)
        {
            Map<String, Object> capabilitiesContainer = new HashMap<>(capabilities.asMap());
            new MetaWrapper(meta).getPropertiesByKey(k -> k.startsWith(getMetaTagName())).forEach(
                    (k, v) -> putByPath(capabilitiesContainer, substringAfter(k, getMetaTagName()), adjustType(v)));
            capabilitiesContainer.forEach(capabilities::setCapability);
        }
    };

    private static final Logger LOGGER = LoggerFactory.getLogger(ControllingMetaTag.class);

    private final String metaTagName;

    ControllingMetaTag(String metaTagName)
    {
        this.metaTagName = metaTagName;
    }

    public String getMetaTagName()
    {
        return metaTagName;
    }

    protected boolean isContainedInImpl(Meta meta)
    {
        return meta.getOptionalProperty(getMetaTagName()).isPresent();
    }

    public boolean isContainedIn(Meta... metas)
    {
        return Stream.of(metas).anyMatch(this::isContainedInImpl);
    }

    protected void setCapability(DesiredCapabilities capabilities, Meta meta)
    {
        String capabilityName = getMetaTagName();
        LOGGER.warn("Setting of capability via meta tag '{}' is deprecated and will be removed in VIVIDUS 0.4.0, "
                        + "please, use 'capability.{}' meta tag instead", capabilityName, capabilityName);
        meta.getOptionalProperty(capabilityName).ifPresent(value -> capabilities.setCapability(capabilityName, value));
    }

    public static boolean isAnyContainedIn(Meta meta)
    {
        return Stream.of(values()).anyMatch(controllingMetaTag -> controllingMetaTag.isContainedIn(meta));
    }

    public static void setDesiredCapabilitiesFromMeta(DesiredCapabilities capabilities, Meta meta)
    {
        for (ControllingMetaTag controllingMetaTag : values())
        {
            controllingMetaTag.setCapability(capabilities, meta);
        }
    }
}
