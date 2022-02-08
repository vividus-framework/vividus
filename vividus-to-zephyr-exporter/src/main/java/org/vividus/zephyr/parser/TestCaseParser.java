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

package org.vividus.zephyr.parser;

import static org.apache.commons.lang3.Validate.notEmpty;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.zephyr.configuration.ZephyrExporterProperties;
import org.vividus.zephyr.configuration.ZephyrFileVisitor;
import org.vividus.zephyr.model.TestCaseExecution;
import org.vividus.zephyr.model.TestCaseStatus;

public class TestCaseParser
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseParser.class);

    private ZephyrExporterProperties zephyrExporterProperties;

    public TestCaseParser(ZephyrExporterProperties zephyrExporterProperties)
    {
        this.zephyrExporterProperties = zephyrExporterProperties;
    }

    public List<TestCaseExecution> createTestExecutions(ObjectMapper objectMapper) throws IOException
    {
        List<TestCaseExecution> testCaseExecutions = parseJsonResultsFile(getJsonResultsFiles(), objectMapper);
        notEmpty(testCaseExecutions, "There are not any test cases for exporting",
                zephyrExporterProperties.getSourceDirectory());
        LOGGER.info("Test cases: {}", testCaseExecutions);
        Map<String, TreeSet<TestCaseStatus>> testCasesMap = testCaseExecutions.stream()
                .collect(Collectors.groupingBy(TestCaseExecution::getKey,
                        Collectors.mapping(TestCaseExecution::getStatus, Collectors.toCollection(TreeSet::new))));

        List<TestCaseExecution> testCasesForExportingExecution = new ArrayList<>();
        for (Map.Entry<String, TreeSet<TestCaseStatus>> entry : testCasesMap.entrySet())
        {
            TestCaseStatus testCaseStatus = entry.getValue().first();
            if (zephyrExporterProperties.getStatusesOfTestCasesToAddToExecution().contains(testCaseStatus))
            {
                testCasesForExportingExecution.add(new TestCaseExecution(entry.getKey(), testCaseStatus));
            }
        }
        LOGGER.info("Test cases for exporting to JIRA: {}", testCasesForExportingExecution);
        return testCasesForExportingExecution;
    }

    private List<File> getJsonResultsFiles() throws IOException
    {
        ZephyrFileVisitor crawler = new ZephyrFileVisitor();
        Files.walkFileTree(zephyrExporterProperties.getSourceDirectory(), crawler);
        List<File> jsonFiles = crawler.getFiles();
        notEmpty(jsonFiles, "Folder '%s' does not contain needed json files",
                zephyrExporterProperties.getSourceDirectory());
        LOGGER.info("Json files: {}", jsonFiles);
        return jsonFiles;
    }

    private List<TestCaseExecution> parseJsonResultsFile(List<File> jsonFiles, ObjectMapper objectMapper)
    {
        List<TestCaseExecution> testCaseExecutions = new ArrayList<>();
        for (File jsonFile : jsonFiles)
        {
            try
            {
                TestCaseExecution testCaseExecution = objectMapper.readValue(jsonFile, TestCaseExecution.class);
                List<String> testCaseKeys = testCaseExecution.getKeys();
                if (testCaseKeys.isEmpty())
                {
                    continue;
                }
                if (testCaseKeys.size() > 1)
                {
                    for (String key : testCaseKeys)
                    {
                        TestCaseExecution additionalTestCaseExecution = new TestCaseExecution(key,
                                testCaseExecution.getStatus());
                        testCaseExecutions.add(additionalTestCaseExecution);
                    }
                }
                else
                {
                    testCaseExecutions.add(testCaseExecution);
                }
            }
            catch (IOException e)
            {
                throw new IllegalArgumentException("Problem with reading values from json file " + jsonFile, e);
            }
        }
        return testCaseExecutions;
    }
}
