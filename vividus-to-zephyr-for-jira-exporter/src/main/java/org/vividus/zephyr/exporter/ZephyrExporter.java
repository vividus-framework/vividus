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

package org.vividus.zephyr.exporter;

import static org.apache.commons.lang3.Validate.notBlank;
import static org.apache.commons.lang3.Validate.notEmpty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.jira.IJiraFacade;
import org.vividus.zephyr.IZephyrFacade;
import org.vividus.zephyr.ZephyrConfiguration;
import org.vividus.zephyr.databind.TestCaseDeserializer;
import org.vividus.zephyr.model.ExecutionStatus;
import org.vividus.zephyr.model.TestCase;
import org.vividus.zephyr.model.TestCaseStatus;
import org.vividus.zephyr.model.ZephyrExecution;

public class ZephyrExporter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ZephyrExporter.class);

    private IJiraFacade jiraFacade;
    private IZephyrFacade zephyrFacade;
    private ZephyrExporterProperties zephyrExporterProperties;

    public void exportResults() throws IOException
    {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true);
        objectMapper.registerModule(new SimpleModule().addDeserializer(TestCase.class, new TestCaseDeserializer()));

        List<TestCase> testCases = parseJsonResultsFile(getJsonResultsFiles(), objectMapper);
        LOGGER.info("Test cases: {}", testCases);
        Map<String, Set<TestCaseStatus>> testCasesMap = testCases.stream()
                .collect(Collectors.groupingBy(TestCase::getKey,
                        Collectors.mapping(TestCase::getStatus, Collectors.toSet())));

        List<TestCase> testCasesForImporting = new ArrayList<>();
        for (Map.Entry<String, Set<TestCaseStatus>> entry : testCasesMap.entrySet())
        {
            TreeSet<TestCaseStatus> statuses = new TreeSet<>(entry.getValue());
            testCasesForImporting.add(new TestCase(entry.getKey(), statuses.first()));
        }
        LOGGER.info("Test cases for exporting to JIRA: {}", testCasesForImporting);
        ZephyrConfiguration configuration = zephyrFacade.prepareConfiguration();
        for (TestCase testCase : testCasesForImporting)
        {
            createNewTestExecution(testCase, configuration, objectMapper);
        }
    }

    private List<File> getJsonResultsFiles() throws IOException
    {
        List<File> jsonFiles = Files.walk(zephyrExporterProperties.getSourceDirectory())
                .map(Path::toFile)
                .filter(File::isFile)
                .filter(f -> f.getPath().contains("test-cases"))
                .collect(Collectors.toList());

        notEmpty(jsonFiles, "Folder '%s' does not contain needed json files",
                zephyrExporterProperties.getSourceDirectory());
        LOGGER.info("Json files: {}", jsonFiles);
        return jsonFiles;
    }

    private List<TestCase> parseJsonResultsFile(List<File> jsonFiles, ObjectMapper objectMapper)
    {
        List<TestCase> testCases = new ArrayList<>();
        for (File jsonFile : jsonFiles)
        {
            try
            {
                TestCase testCase = objectMapper.readValue(jsonFile, TestCase.class);
                notBlank(testCase.getKey(), "Test case does not have issue key");
                if (testCase.getKey().contains(" "))
                {
                    String[] keys = testCase.getKey().split("\\s+");
                    for (String key : keys)
                    {
                        TestCase additionalTestCase = new TestCase(key, testCase.getStatus());
                        testCases.add(additionalTestCase);
                    }
                }
                else
                {
                    testCases.add(testCase);
                }
            }
            catch (IOException e)
            {
                throw new IllegalArgumentException("Problem with reading values from json file " + jsonFile, e);
            }
        }
        return testCases;
    }

    private void createNewTestExecution(TestCase testCase, ZephyrConfiguration configuration, ObjectMapper objectMapper)
            throws IOException
    {
        ZephyrExecution execution = new ZephyrExecution(configuration, jiraFacade.getIssueId(testCase.getKey()),
                testCase.getStatus());
        try
        {
            String createExecution = objectMapper.writeValueAsString(execution);
            int executionId = zephyrFacade.createExecution(createExecution);

            String executionBody = objectMapper.writeValueAsString(new ExecutionStatus(
                String.valueOf(execution.getTestCaseStatus().getStatusId())));
            zephyrFacade.updateExecutionStatus(executionId, executionBody);
        }
        catch (JsonProcessingException e)
        {
            throw new IllegalArgumentException("Problem with serializing execution to json file", e);
        }
    }

    public void setJiraFacade(IJiraFacade jiraFacade)
    {
        this.jiraFacade = jiraFacade;
    }

    public void setZephyrExporterProperties(ZephyrExporterProperties zephyrExporterProperties)
    {
        this.zephyrExporterProperties = zephyrExporterProperties;
    }

    public void setZephyrFacade(IZephyrFacade zephyrFacade)
    {
        this.zephyrFacade = zephyrFacade;
    }
}
