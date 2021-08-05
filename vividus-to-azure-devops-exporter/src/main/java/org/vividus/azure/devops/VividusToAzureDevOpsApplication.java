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

package org.vividus.azure.devops;

import java.io.IOException;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ImportResource;
import org.vividus.azure.devops.configuration.AzureDevOpsExporterOptions;
import org.vividus.azure.devops.exporter.AzureDevOpsExporter;
import org.vividus.util.property.PropertyParser;

@SpringBootApplication
@EnableConfigurationProperties(AzureDevOpsExporterOptions.class)
@ImportResource(locations = { "org/vividus/azure/devops/spring.xml", "org/vividus/http/client/spring.xml" })
public class VividusToAzureDevOpsApplication
{
    public static void main(String [] args) throws BeansException, IOException
    {
        ApplicationContext context = SpringApplication.run(VividusToAzureDevOpsApplication.class, args);
        context.getBean(AzureDevOpsExporter.class).exportResults();
    }

    @Bean("propertyParser")
    public PropertyParser propertyParser(Properties properties)
    {
        return new PropertyParser(properties);
    }
}
