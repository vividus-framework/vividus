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

package org.vividus.configuration;

import java.util.List;
import java.util.Properties;

public class DelegatingPropertiesProcessor implements PropertiesProcessor
{
    private final List<PropertiesProcessor> propertiesProcessors;

    public DelegatingPropertiesProcessor(List<PropertiesProcessor> propertiesProcessors)
    {
        this.propertiesProcessors = propertiesProcessors;
    }

    @Override
    public Properties processProperties(Properties properties)
    {
        return propertiesProcessors.stream().reduce(properties,
                (result, processor) -> processor.processProperties(result), (p1, p2) -> p1);
    }

    @Override
    public String processProperty(String propertyName, String propertyValue)
    {
        return propertiesProcessors.stream().reduce(propertyValue,
                (result, processor) -> processor.processProperty(propertyName, result), (p1, p2) -> p1);
    }
}
