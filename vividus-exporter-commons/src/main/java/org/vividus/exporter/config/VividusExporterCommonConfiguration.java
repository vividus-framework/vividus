/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.exporter.config;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Stream;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.vividus.util.property.PropertyParser;

@ImportResource(locations = { "org/vividus/http/client/spring.xml", "org/vividus/util/spring.xml" })
@PropertySource({
        "org/vividus/http/client/defaults.properties",
        "org/vividus/util/defaults.properties"
})
@Configuration
public class VividusExporterCommonConfiguration
{
    private static final Logger LOGGER = LoggerFactory.getLogger(VividusExporterCommonConfiguration.class);

    @Bean("propertyParser")
    public PropertyParser propertyParser(ConfigurableEnvironment environment,
            @Qualifier("deprecatedProperties") Optional<Map<String, String>> deprecatedProperties)
    {
        Map<String, String> deprecatedPropertiesUnwraped = deprecatedProperties.orElse(Map.of());

        if (!deprecatedPropertiesUnwraped.isEmpty())
        {
            Map<String, Object> propertySource = new HashMap<>();
            deprecatedPropertiesUnwraped.forEach((currentKey, newKey) ->
            {
                String valueByCurrentKey = environment.getProperty(currentKey);
                String valueByNewKey = environment.getProperty(newKey);

                Validate.isTrue(valueByCurrentKey == null || valueByNewKey == null,
                        "The '%s' and '%s' are competing properties, please use '%s' only", currentKey, newKey, newKey);

                if (valueByCurrentKey != null)
                {
                    propertySource.put(newKey, valueByCurrentKey);
                }
            });
            environment.getPropertySources().addFirst(new MapPropertySource("new-props", propertySource));
        }

        return environment.getPropertySources()
                .stream()
                .filter(MapPropertySource.class::isInstance)
                .map(MapPropertySource.class::cast)
                .map(MapPropertySource::getPropertyNames)
                .flatMap(Stream::of)
                .collect(collectingAndThen(toMap(k -> processKey(k, deprecatedPropertiesUnwraped),
                        environment::getProperty, (l, r) -> l, Properties::new), PropertyParser::new));
    }

    private String processKey(String currentKey, Map<String, String> deprecatedProperties)
    {
        if (deprecatedProperties.isEmpty())
        {
            return currentKey;
        }

        String newKey = deprecatedProperties.get(currentKey);
        if (newKey == null)
        {
            return currentKey;
        }

        LOGGER.atWarn().addArgument(currentKey).addArgument(newKey)
                .log("The '{}' property is deprecated, please use '{}' instead.");
        return newKey;
    }
}
