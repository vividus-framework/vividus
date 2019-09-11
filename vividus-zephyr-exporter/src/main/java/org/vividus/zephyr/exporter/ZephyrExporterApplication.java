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

package org.vividus.zephyr.exporter;

import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.google.common.collect.Lists;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.core.env.PropertySource;
import org.vividus.util.property.IPropertyMapper;
import org.vividus.util.property.PropertyMapper;
import org.vividus.util.property.PropertyParser;

@SpringBootApplication
@ImportResource(
        locations = { "org/vividus/facade/jira/spring.xml", "org/vividus/http/client/spring.xml" })
@EnableConfigurationProperties(ZephyrExporterProperties.class)
public class ZephyrExporterApplication implements CommandLineRunner
{
    @Autowired
    private ZephyrExporter zephyrExporter;

    public static void main(String[] args)
    {
        SpringApplication.run(ZephyrExporterApplication.class, args);
    }

    @Override
    public void run(String... args) throws IOException
    {
        zephyrExporter.exportResults(ZonedDateTime.now());
    }

    @Bean("propertyMapper")
    public IPropertyMapper propertyMapper(ConfigurableEnvironment environment)
    {
        Properties properties = new Properties();
        List<PropertySource<?>> propertySources = Lists.newArrayList(environment.getPropertySources().iterator());
        for (PropertySource<?> propertySource : Lists.reverse(propertySources))
        {
            if (propertySource instanceof EnumerablePropertySource)
            {
                properties.putAll(Stream.of(((EnumerablePropertySource<?>) propertySource).getPropertyNames())
                        .collect(Collectors.toMap(Function.identity(), propertySource::getProperty)));
            }
        }
        PropertyMapper propertyMapper = new PropertyMapper();
        propertyMapper.setPropertyParser(propertyParser(properties));
        return propertyMapper;
    }

    @Bean("propertyParser")
    public PropertyParser propertyParser(Properties properties)
    {
        PropertyParser propertyParser = new PropertyParser();
        propertyParser.setProperties(properties);
        return propertyParser;
    }

    @Bean("deserializers")
    public Set<JsonDeserializer<?>> deserializers()
    {
        return Set.of();
    }
}
