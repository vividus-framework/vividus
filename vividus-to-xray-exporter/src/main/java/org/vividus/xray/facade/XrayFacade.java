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
import org.vividus.jira.JiraFacade;
import org.vividus.util.json.JsonPathUtils;
import org.vividus.xray.databind.ManualTestCaseSerializer;
import org.vividus.xray.model.ManualTestCase;

public class XrayFacade
{
    private static final Logger LOGGER = LoggerFactory.getLogger(XrayFacade.class);

    private final String projectKey;
    private final List<String> editableStatuses;
    private final JiraFacade jiraFacade;
    private final ObjectMapper objectMapper;

    public XrayFacade(String projectKey, List<String> editableStatuses, JiraFacade jiraFacade,
            ManualTestCaseSerializer manualTestSerializer)
    {
        this.jiraFacade = jiraFacade;
        this.projectKey = projectKey;
        this.editableStatuses = editableStatuses;
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .registerModule(new SimpleModule().addSerializer(ManualTestCase.class, manualTestSerializer));
    }

    public String createTestCase(TestCaseParameters testCaseParameters) throws IOException
    {
        String createTestRequest = objectMapper.writeValueAsString(createManualTest(testCaseParameters));
        LOGGER.atInfo().addArgument(createTestRequest).log("Creating Test Case: {}");
        String response = jiraFacade.createIssue(createTestRequest);
        String issueKey = JsonPathUtils.getData(response, "$.key");
        LOGGER.atInfo().addArgument(issueKey).log("Test with key {} has been created");
        return issueKey;
    }

    public void updateTestCase(String testCaseKey, TestCaseParameters testCaseParameters)
            throws IOException, NonEditableIssueStatusException
    {
        checkIfIssueEditable(testCaseKey);
        String updateTestRequest = objectMapper.writeValueAsString(createManualTest(testCaseParameters));
        LOGGER.atInfo().addArgument(testCaseKey)
                       .addArgument(updateTestRequest)
                       .log("Updating Test Case with ID {}: {}");
        jiraFacade.updateIssue(testCaseKey, updateTestRequest);
        LOGGER.atInfo().addArgument(testCaseKey).log("Test with key {} has been updated");
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

    private ManualTestCase createManualTest(TestCaseParameters testCaseParameters)
    {
        ManualTestCase manualTest = new ManualTestCase();
        manualTest.setProjectKey(projectKey);
        manualTest.setSummary(testCaseParameters.getSummary());
        manualTest.setLabels(testCaseParameters.getLabels());
        manualTest.setComponents(testCaseParameters.getComponents());
        manualTest.setManualTestSteps(testCaseParameters.getSteps());
        return manualTest;
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
