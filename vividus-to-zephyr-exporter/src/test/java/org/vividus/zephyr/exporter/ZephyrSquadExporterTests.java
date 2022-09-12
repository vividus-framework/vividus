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

package org.vividus.zephyr.exporter;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.jira.JiraConfigurationException;
import org.vividus.jira.JiraFacade;
import org.vividus.jira.model.JiraEntity;
import org.vividus.zephyr.configuration.ZephyrConfiguration;
import org.vividus.zephyr.configuration.ZephyrExporterProperties;
import org.vividus.zephyr.facade.ZephyrSquadFacade;
import org.vividus.zephyr.model.TestCase;
import org.vividus.zephyr.model.TestCaseStatus;
import org.vividus.zephyr.parser.TestCaseParser;

@ExtendWith({MockitoExtension.class, TestLoggerFactoryExtension.class})
class ZephyrSquadExporterTests
{
    private static final String TEST_CASE_KEY1 = "TEST-1";
    private static final String TEST_CASE_KEY2 = "TEST-2";
    private static final String ISSUE_ID1 = "1";
    private static final String ISSUE_ID2 = "2";
    private static final String STATUS_UPDATE_JSON = "{\"status\":\"-1\"}";
    private static final String PASSED_STATUS_ID = "101";
    private static final String STATUS_UPDATE_EXECUTION_ID = "111";

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(ZephyrSquadExporter.class);

    @Mock private JiraFacade jiraFacade;
    @Mock private ZephyrSquadFacade zephyrSquadFacade;
    @Mock private TestCaseParser testCaseParser;
    @Mock private ZephyrExporterProperties zephyrExporterProperties;
    @InjectMocks private ZephyrSquadExporter zephyrSquadExporter;

    @Test
    void testExportResults() throws IOException, URISyntaxException, JiraConfigurationException
    {
        when(testCaseParser.createTestCases(any())).thenReturn(List.of(
                new TestCase(TEST_CASE_KEY1, TestCaseStatus.SKIPPED),
                new TestCase(TEST_CASE_KEY2, TestCaseStatus.PASSED)));
        when(zephyrSquadFacade.prepareConfiguration()).thenReturn(prepareTestConfiguration());
        mockJiraIssueRetrieve(TEST_CASE_KEY1, ISSUE_ID1);
        mockJiraIssueRetrieve(TEST_CASE_KEY2, ISSUE_ID2);
        String executionBody = "{\"cycleId\":\"11113\",\"folderId\":\"11114\",\"issueId\":\"%s\","
                + "\"projectId\":\"11111\",\"versionId\":\"11112\"}";
        when(zephyrSquadFacade.createExecution(String.format(executionBody, ISSUE_ID1))).thenReturn(111);
        when(zephyrSquadFacade.createExecution(String.format(executionBody, ISSUE_ID2))).thenReturn(222);
        zephyrSquadExporter.exportResults();
        verify(zephyrSquadFacade).updateExecutionStatus(STATUS_UPDATE_EXECUTION_ID, STATUS_UPDATE_JSON);
        verify(zephyrSquadFacade).updateExecutionStatus("222", "{\"status\":\"101\"}");
    }

    @Test
    void testExportResultsWithOnlyStatusUpdate() throws IOException, URISyntaxException, JiraConfigurationException
    {
        when(zephyrExporterProperties.getUpdateExecutionStatusesOnly()).thenReturn(true);
        when(testCaseParser.createTestCases(any())).thenReturn(List.of(
                new TestCase(TEST_CASE_KEY1, TestCaseStatus.SKIPPED),
                new TestCase(TEST_CASE_KEY2, TestCaseStatus.PASSED)));
        when(zephyrSquadFacade.prepareConfiguration()).thenReturn(prepareTestConfiguration());
        mockJiraIssueRetrieve(TEST_CASE_KEY1, ISSUE_ID1);
        mockJiraIssueRetrieve(TEST_CASE_KEY2, ISSUE_ID2);
        when(zephyrSquadFacade.findExecutionId(ISSUE_ID1)).thenReturn(OptionalInt.of(111));
        when(zephyrSquadFacade.findExecutionId(ISSUE_ID2)).thenReturn(OptionalInt.empty());
        zephyrSquadExporter.exportResults();
        verify(zephyrSquadFacade).updateExecutionStatus(STATUS_UPDATE_EXECUTION_ID, STATUS_UPDATE_JSON);
        verifyNoMoreInteractions(zephyrSquadFacade);
        assertThat(testLogger.getLoggingEvents(), is(List.of(info("Test case result for {} was not exported, "
                + "because execution does not exist", TEST_CASE_KEY2))));
    }

    private ZephyrConfiguration prepareTestConfiguration()
    {
        ZephyrConfiguration configuration = new ZephyrConfiguration();
        configuration.setProjectId("11111");
        configuration.setVersionId("11112");
        configuration.setCycleId("11113");
        configuration.setFolderId("11114");
        Map<TestCaseStatus, String> map = new EnumMap<>(TestCaseStatus.class);
        map.put(TestCaseStatus.SKIPPED, "-1");
        map.put(TestCaseStatus.PASSED, PASSED_STATUS_ID);
        configuration.setTestStatusPerZephyrStatusMapping(map);
        return configuration;
    }

    private void mockJiraIssueRetrieve(String issueKey, String issueId) throws IOException, JiraConfigurationException
    {
        JiraEntity issue = new JiraEntity();
        issue.setId(issueId);
        when(jiraFacade.getIssue(issueKey)).thenReturn(issue);
    }
}
