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
import org.vividus.zephyr.model.TestCase;
import org.vividus.zephyr.model.TestCaseStatus;

public class TestCaseParser
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TestCaseParser.class);

    private ZephyrExporterProperties zephyrExporterProperties;

    public TestCaseParser(ZephyrExporterProperties zephyrExporterProperties)
    {
        this.zephyrExporterProperties = zephyrExporterProperties;
    }

    public List<TestCase> createTestCases(ObjectMapper objectMapper) throws IOException
    {
        List<TestCase> testCases = parseJsonResultsFile(getJsonResultsFiles(), objectMapper);
        notEmpty(testCases, "There are not any test cases for exporting",
                zephyrExporterProperties.getSourceDirectory());
        LOGGER.info("Test cases: {}", testCases);
        Map<String, TreeSet<TestCaseStatus>> testCasesMap = testCases.stream()
                .collect(Collectors.groupingBy(TestCase::getKey,
                        Collectors.mapping(TestCase::getStatus, Collectors.toCollection(TreeSet::new))));

        List<TestCase> testCasesForImporting = new ArrayList<>();
        for (Map.Entry<String, TreeSet<TestCaseStatus>> entry : testCasesMap.entrySet())
        {
            testCasesForImporting.add(new TestCase(entry.getKey(), entry.getValue().first()));
        }
        LOGGER.info("Test cases for exporting to JIRA: {}", testCasesForImporting);
        return testCasesForImporting;
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

    private List<TestCase> parseJsonResultsFile(List<File> jsonFiles, ObjectMapper objectMapper)
    {
        List<TestCase> testCases = new ArrayList<>();
        for (File jsonFile : jsonFiles)
        {
            try
            {
                TestCase testCase = objectMapper.readValue(jsonFile, TestCase.class);
                List<String> testCaseKeys = testCase.getKeys();
                if (testCaseKeys.isEmpty())
                {
                    continue;
                }
                if (testCaseKeys.size() > 1)
                {
                    for (String key : testCaseKeys)
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
}
