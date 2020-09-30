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

package org.vividus.xray.facade;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.jira.JiraClient;
import org.vividus.jira.JiraFacade;
import org.vividus.util.json.JsonPathUtils;
import org.vividus.xray.databind.CucumberTestCaseSerializer;
import org.vividus.xray.databind.ManualTestCaseSerializer;
import org.vividus.xray.model.AbstractTestCase;
import org.vividus.xray.model.CucumberTestCase;
import org.vividus.xray.model.ManualTestCase;
import org.vividus.xray.model.TestExecution;

public class XrayFacade
{
    private static final Logger LOGGER = LoggerFactory.getLogger(XrayFacade.class);

    private final List<String> editableStatuses;
    private final JiraFacade jiraFacade;
    private final JiraClient jiraClient;
    private final ObjectMapper objectMapper;

    public XrayFacade(List<String> editableStatuses, JiraFacade jiraFacade,
            JiraClient jiraClient, ManualTestCaseSerializer manualTestSerializer,
            CucumberTestCaseSerializer cucumberTestSerializer)
    {
        this.editableStatuses = editableStatuses;
        this.jiraFacade = jiraFacade;
        this.jiraClient = jiraClient;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new SimpleModule().addSerializer(ManualTestCase.class, manualTestSerializer)
                                                  .addSerializer(CucumberTestCase.class, cucumberTestSerializer));
    }

    public <T extends AbstractTestCase> String createTestCase(T testCase) throws IOException
    {
        String createTestRequest = objectMapper.writeValueAsString(testCase);
        LOGGER.atInfo().addArgument(testCase::getType).addArgument(createTestRequest).log("Creating {} Test Case: {}");
        String response = jiraFacade.createIssue(createTestRequest);
        String issueKey = JsonPathUtils.getData(response, "$.key");
        LOGGER.atInfo().addArgument(testCase::getType)
                       .addArgument(issueKey)
                       .log("{} Test with key {} has been created");
        return issueKey;
    }

    public <T extends AbstractTestCase> void updateTestCase(String testCaseKey, T testCase)
            throws IOException, NonEditableIssueStatusException
    {
        checkIfIssueEditable(testCaseKey);
        String updateTestRequest = objectMapper.writeValueAsString(testCase);
        LOGGER.atInfo().addArgument(testCase::getType)
                       .addArgument(testCaseKey)
                       .addArgument(updateTestRequest)
                       .log("Updating {} Test Case with ID {}: {}");
        jiraFacade.updateIssue(testCaseKey, updateTestRequest);
        LOGGER.atInfo().addArgument(testCase::getType)
                       .addArgument(testCaseKey)
                       .log("{} Test with key {} has been updated");
    }

    public void updateTestExecution(String testExecutionKey, List<String> testCaseKeys) throws IOException
    {
        TestExecution testExecution = new TestExecution(testCaseKeys);
        String testExecutionRequest = objectMapper.writeValueAsString(testExecution);
        LOGGER.atInfo()
              .addArgument(() -> StringUtils.join(testCaseKeys, ", "))
              .addArgument(testExecutionKey)
              .log("Add {} test cases to {} test execution");
        jiraClient.executePost("/rest/raven/1.0/api/testexec/" + testExecutionKey + "/test", testExecutionRequest);
    }

    private void checkIfIssueEditable(String issueKey) throws IOException, NonEditableIssueStatusException
    {
        String status = jiraFacade.getIssueStatus(issueKey);

        if (editableStatuses.stream().noneMatch(s -> StringUtils.equalsIgnoreCase(s, status)))
        {
            throw new NonEditableIssueStatusException(issueKey, status);
        }
    }

    public void createTestsLink(String testCaseId, String requirementId) throws IOException
    {
        String linkType = "Tests";
        LOGGER.atInfo().addArgument(linkType)
                       .addArgument(testCaseId)
                       .addArgument(requirementId)
                       .log("Create '{}' link from {} to {}");
        jiraFacade.createIssueLink(testCaseId, requirementId, linkType);
    }

    public static final class NonEditableIssueStatusException extends Exception
    {
        private static final long serialVersionUID = -5547086076322794984L;

        public NonEditableIssueStatusException(String testCaseId, String status)
        {
            super("Issue " + testCaseId + " is in non-editable '" + status + "' status");
        }
    }
}
