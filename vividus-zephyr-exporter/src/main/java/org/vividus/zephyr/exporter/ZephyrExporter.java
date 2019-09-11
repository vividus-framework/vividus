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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.vividus.facade.jira.IJiraFacade;
import org.vividus.zephyr.exporter.databind.ZephyrTestSerializer;
import org.vividus.zephyr.exporter.model.jbehave.Scenario;
import org.vividus.zephyr.exporter.model.jbehave.Story;
import org.vividus.zephyr.exporter.model.zephyr.ZephyrTest;

@Service
public class ZephyrExporter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ZephyrExporter.class);

    @Autowired
    private IJiraFacade jiraFacade;

    @Autowired
    private ZephyrExporterProperties zephyrExporterProperties;

    public void exportResults(ZonedDateTime now) throws IOException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new SimpleModule().addSerializer(ZephyrTest.class, new ZephyrTestSerializer()));

        List<File> jsonFiles = Files.walk(zephyrExporterProperties.getSourceDirectory())
                .map(Path::toFile)
                .filter(File::isFile)
                .filter(f -> f.getName().endsWith(".json"))
                .collect(Collectors.toList());

        if (parseJsonResultsFile(jsonFiles, objectMapper, now).isEmpty())
        {
            LOGGER.info("No test execution results to export are found");
        }
    }

    private List<String> parseJsonResultsFile(List<File> jsonFiles, ObjectMapper objectMapper, ZonedDateTime now)
            throws IOException
    {
        List<String> jiraKeys = new ArrayList<>();
        for (File jsonFile : jsonFiles)
        {
            LOGGER.info("Parsing {}", jsonFile);
            Story story = objectMapper.readValue(jsonFile, Story.class);
            List<Scenario> scenarios = story.getScenarios();
            if (scenarios != null)
            {
                LOGGER.info("Number of scenarios {}", scenarios.size());
                for (Scenario scenario : scenarios)
                {
                    jiraKeys.add(getTestCaseId(scenario, objectMapper));
                }
            }
            else
            {
                LOGGER.info("Scenarios are not found");
            }
        }
        return jiraKeys;
    }

    private String getTestCaseId(Scenario scenario, ObjectMapper objectMapper) throws IOException
    {
        Optional<String> testCaseId = scenario.findTestCaseId();
        return testCaseId.isPresent() ? testCaseId.get() : createNewTestCase(scenario, objectMapper);
    }

    private String createNewTestCase(Scenario scenario, ObjectMapper objectMapper) throws IOException
    {
        ZephyrTest zephyrTest = new ZephyrTest();
        zephyrTest.setProjectKey(zephyrExporterProperties.getJiraProjectKey());
        zephyrTest.setSummary(scenario.getTitle());
        zephyrTest.setDescription(scenario.getTitle());

        String createTestRequest = objectMapper.writeValueAsString(zephyrTest);

        LOGGER.info("Creating Zephyr Test: {}", createTestRequest);

        return jiraFacade.createIssue(zephyrExporterProperties.getJiraProjectKey(), createTestRequest).getKey();
    }
}
