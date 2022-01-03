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

package org.vividus.zephyr;

import java.io.IOException;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.ImportResource;
import org.vividus.exporter.config.VividusExporterCommonConfiguration;
import org.vividus.jira.JiraConfigurationException;
import org.vividus.zephyr.configuration.ZephyrExporterConfiguration;
import org.vividus.zephyr.configuration.ZephyrExporterProperties;
import org.vividus.zephyr.exporter.ZephyrExporter;

@SpringBootApplication
@Import(VividusExporterCommonConfiguration.class)
@ImportResource(locations = { "org/vividus/zephyr/spring.xml", "org/vividus/jira/spring.xml" })
@EnableConfigurationProperties({ ZephyrExporterConfiguration.class, ZephyrExporterProperties.class })
@SuppressWarnings("checkstyle:hideutilityclassconstructor")
public class VividusToZephyrExporterApplication
{
    public static void main(String[] args) throws IOException, JiraConfigurationException
    {
        ApplicationContext ctx = SpringApplication.run(VividusToZephyrExporterApplication.class, args);
        ZephyrExporter exporter = ctx.getBean(ZephyrExporter.class);
        exporter.exportResults();
    }
}
