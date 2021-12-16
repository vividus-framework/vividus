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

package org.vividus.configuration;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class SystemPropertiesProcessor
{
    private static final String SYSTEM_PROPERTIES_PREFIX = "system.";

    private final PropertiesDecryptor propertiesDecryptor;

    SystemPropertiesProcessor(PropertiesDecryptor propertiesDecryptor)
    {
        this.propertiesDecryptor = propertiesDecryptor;
    }

    public Properties process(Properties properties)
    {
        Iterator<Map.Entry<Object, Object>> iterator = properties.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<Object, Object> entry = iterator.next();
            String key = (String) entry.getKey();
            if (key.startsWith(SYSTEM_PROPERTIES_PREFIX))
            {
                String value = (String) entry.getValue();
                value = propertiesDecryptor.decrypt(value);
                System.setProperty(StringUtils.removeStart(key, SYSTEM_PROPERTIES_PREFIX), value);
                iterator.remove();
            }
        }
        return properties;
    }
}
