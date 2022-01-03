/*
 * Copyright 2019-2022 the original author or authors.
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

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toMap;

import java.util.Properties;
import java.util.stream.Stream;

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
    @Bean("propertyParser")
    public PropertyParser propertyParser(ConfigurableEnvironment environment)
    {
        return environment.getPropertySources()
                .stream()
                .filter(MapPropertySource.class::isInstance)
                .map(MapPropertySource.class::cast)
                .map(MapPropertySource::getPropertyNames)
                .flatMap(Stream::of)
                .collect(collectingAndThen(toMap(identity(), environment::getProperty, (l, r) -> l,
                        Properties::new), PropertyParser::new));
    }
}
