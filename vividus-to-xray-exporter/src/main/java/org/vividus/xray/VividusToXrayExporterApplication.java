/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.xray;

import java.io.IOException;
import java.util.Properties;
import java.util.stream.Stream;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.vividus.util.property.PropertyParser;
import org.vividus.xray.configuration.JiraFieldsMapping;
import org.vividus.xray.configuration.XrayExporterOptions;
import org.vividus.xray.exporter.XrayExporter;

@SpringBootApplication
@ImportResource(locations = { "org/vividus/jira/spring.xml", "org/vividus/http/client/spring.xml",
        "org/vividus/xray/spring.xml" })
@PropertySource("classpath:/properties/defaults/default.properties")
@EnableConfigurationProperties({ XrayExporterOptions.class, JiraFieldsMapping.class })
public class VividusToXrayExporterApplication
{
    public static void main(String[] args) throws IOException
    {
        ApplicationContext context = SpringApplication.run(VividusToXrayExporterApplication.class, args);
        context.getBean(XrayExporter.class).exportResults();
    }

    @Bean("propertyParser")
    public PropertyParser propertyParser(ConfigurableEnvironment environment)
    {
        Properties properties = new Properties();
        environment.getPropertySources().stream()
                                        .filter(MapPropertySource.class::isInstance)
                                        .map(MapPropertySource.class::cast)
                                        .map(MapPropertySource::getPropertyNames)
                                        .flatMap(Stream::of)
                                        .forEach(prop -> properties.put(prop, environment.getProperty(prop)));
        PropertyParser propertyParser = new PropertyParser();
        propertyParser.setProperties(properties);
        return propertyParser;
    }
}
