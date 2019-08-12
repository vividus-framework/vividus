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

import org.jbehave.core.model.Meta;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.bdd.model.MetaWrapper;

public enum ControllingMetaTag
{
    BROWSER_NAME(CapabilityType.BROWSER_NAME),
    VERSION(CapabilityType.VERSION),
    SCREEN_RESOLUTION(SauceLabsCapabilityType.SCREEN_RESOLUTION),
    APPIUM_VERSION(SauceLabsCapabilityType.APPIUM_VERSION),
    DEVICE_NAME(SauceLabsCapabilityType.DEVICE_NAME),
    DEVICE_ORIENTATION(SauceLabsCapabilityType.DEVICE_ORIENTATION),
    PLATFORM_VERSION(SauceLabsCapabilityType.PLATFORM_VERSION),
    PLATFORM_NAME(CapabilityType.PLATFORM_NAME),
    IEDRIVER_VERSION(SauceLabsCapabilityType.IEDRIVER_VERSION),
    SELENIUM_VERSION(SauceLabsCapabilityType.SELENIUM_VERSION),
    PROXY(CapabilityType.PROXY)
    {
        @Override
        protected boolean isContainedInImpl(MetaWrapper metaWrapper)
        {
            return metaWrapper.hasProperty(getMetaTagName());
        }

        @Override
        protected void setCapability(DesiredCapabilities capabilities, MetaWrapper meta)
        {
            // Nothing to do
        }
    };

    private final String metaTagName;

    ControllingMetaTag(String metaTagName)
    {
        this.metaTagName = metaTagName;
    }

    public String getMetaTagName()
    {
        return metaTagName;
    }

    protected boolean isContainedInImpl(MetaWrapper metaWrapper)
    {
        return metaWrapper.getOptionalPropertyValue(getMetaTagName()).isPresent();
    }

    public boolean isContainedIn(Meta... metas)
    {
        for (Meta meta : metas)
        {
            if (isContainedInImpl(new MetaWrapper(meta)))
            {
                return true;
            }
        }
        return false;
    }

    protected void setCapability(DesiredCapabilities capabilities, MetaWrapper meta)
    {
        String capabilityName = getMetaTagName();
        meta.getOptionalPropertyValue(capabilityName)
                .ifPresent(value -> capabilities.setCapability(capabilityName, value));
    }

    public static boolean isAnyContainedIn(Meta meta)
    {
        for (ControllingMetaTag controllingMetaTag : values())
        {
            if (controllingMetaTag.isContainedIn(meta))
            {
                return true;
            }
        }
        return false;
    }

    public static void setDesiredCapabilitiesFromMeta(DesiredCapabilities capabilities, MetaWrapper meta)
    {
        for (ControllingMetaTag controllingMetaTag : values())
        {
            controllingMetaTag.setCapability(capabilities, meta);
        }
    }
}
