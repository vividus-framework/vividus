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

package org.vividus.zephyr.exporter;

import java.io.IOException;
import java.util.List;
import java.util.OptionalInt;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.jira.JiraFacade;
import org.vividus.jira.model.JiraIssue;
import org.vividus.zephyr.configuration.ZephyrConfiguration;
import org.vividus.zephyr.configuration.ZephyrExporterProperties;
import org.vividus.zephyr.databind.TestCaseDeserializer;
import org.vividus.zephyr.facade.IZephyrFacade;
import org.vividus.zephyr.facade.ZephyrFacade;
import org.vividus.zephyr.model.ExecutionStatus;
import org.vividus.zephyr.model.TestCase;
import org.vividus.zephyr.model.ZephyrExecution;
import org.vividus.zephyr.parser.TestCaseParser;

public class ZephyrExporter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ZephyrExporter.class);

    private final JiraFacade jiraFacade;
    private IZephyrFacade zephyrFacade;
    private TestCaseParser testCaseParser;
    private ZephyrExporterProperties zephyrExporterProperties;
    private final ObjectMapper objectMapper;

    public ZephyrExporter(JiraFacade jiraFacade, ZephyrFacade zephyrFacade, TestCaseParser testCaseParser,
            ZephyrExporterProperties zephyrExporterProperties) throws IOException
    {
        this.jiraFacade = jiraFacade;
        this.zephyrFacade = zephyrFacade;
        this.testCaseParser = testCaseParser;
        this.zephyrExporterProperties = zephyrExporterProperties;
        this.objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY, true)
            .registerModule(new SimpleModule().addDeserializer(TestCase.class, new TestCaseDeserializer()));
    }

    public void exportResults() throws IOException
    {
        List<TestCase> testCasesForImporting = testCaseParser.createTestCases(objectMapper);
        ZephyrConfiguration configuration = zephyrFacade.prepareConfiguration();
        for (TestCase testCase : testCasesForImporting)
        {
            exportTestExecution(testCase, configuration);
        }
    }

    private void exportTestExecution(TestCase testCase, ZephyrConfiguration configuration) throws IOException
    {
        JiraIssue issue = jiraFacade.getIssue(testCase.getKey());
        ZephyrExecution execution = new ZephyrExecution(configuration, issue.getId(), testCase.getStatus());
        OptionalInt executionId;

        if (zephyrExporterProperties.getUpdateExecutionStatusesOnly())
        {
            executionId = zephyrFacade.findExecutionId(issue.getId());
        }
        else
        {
            String createExecution = objectMapper.writeValueAsString(execution);
            executionId = OptionalInt.of(zephyrFacade.createExecution(createExecution));
        }
        if (executionId.isPresent())
        {
            String executionBody = objectMapper.writeValueAsString(new ExecutionStatus(
                String.valueOf(configuration.getTestStatusPerZephyrIdMapping().get(execution.getTestCaseStatus()))));
            zephyrFacade.updateExecutionStatus(executionId.getAsInt(), executionBody);
        }
        else
        {
            LOGGER.atInfo().addArgument(testCase::getKey).log("Test case result for {} was not exported, "
                    + "because execution does not exist");
        }
    }
}
