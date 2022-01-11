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

public enum ControllingMetaTag
{
    PROXY
    {
        @Override
        public boolean isContainedIn(Meta meta)
        {
            return meta.hasProperty(CapabilityType.PROXY);
        }

        @Override
        protected void setCapability(DesiredCapabilities capabilities, Meta meta)
        {
            // Nothing to do
        }
    },
    CAPABILITY
    {
        private static final String META_PROPERTY_PREFIX = "capability.";

        @Override
        public boolean isContainedIn(Meta meta)
        {
            return meta.getPropertyNames().stream().anyMatch(s -> s.startsWith(META_PROPERTY_PREFIX));
        }

        @Override
        protected void setCapability(DesiredCapabilities capabilities, Meta meta)
        {
            Map<String, Object> capabilitiesContainer = new HashMap<>(capabilities.asMap());
            meta.getPropertyNames()
                    .stream()
                    .filter(name -> name.startsWith(META_PROPERTY_PREFIX))
                    .forEach(k -> putByPath(capabilitiesContainer, substringAfter(k, META_PROPERTY_PREFIX),
                            adjustType(meta.getProperty(k))));
            capabilitiesContainer.forEach(capabilities::setCapability);
        }
    };

    public abstract boolean isContainedIn(Meta meta);

    protected abstract void setCapability(DesiredCapabilities capabilities, Meta meta);

    public static boolean isAnyContainedIn(Meta meta)
    {
        return Stream.of(values()).anyMatch(controllingMetaTag -> controllingMetaTag.isContainedIn(meta));
    }

    public static void setDesiredCapabilitiesFromMeta(DesiredCapabilities capabilities, Meta meta)
    {
        Stream.of(values()).forEach(controllingMetaTag -> controllingMetaTag.setCapability(capabilities, meta));
    }
}
