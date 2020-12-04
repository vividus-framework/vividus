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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.jira.JiraFacade;
import org.vividus.jira.model.JiraEntity;
import org.vividus.zephyr.configuration.ZephyrConfiguration;
import org.vividus.zephyr.facade.ZephyrFacade;
import org.vividus.zephyr.model.TestCase;
import org.vividus.zephyr.model.TestCaseStatus;
import org.vividus.zephyr.parser.TestCaseParser;

@ExtendWith(MockitoExtension.class)
class ZephyrExporterTests
{
    @Mock
    private JiraFacade jiraFacade;

    @Mock
    private ZephyrFacade zephyrFacade;

    @Mock
    private TestCaseParser testCaseParser;

    @InjectMocks
    private ZephyrExporter zephyrExporter;

    @Test
    void testExportResults() throws IOException, URISyntaxException
    {
        String test1 = "TEST-1";
        String test2 = "TEST-2";
        when(testCaseParser.createTestCases(any())).thenReturn(List.of(new TestCase(test1, TestCaseStatus.SKIPPED),
                new TestCase(test2, TestCaseStatus.PASSED)));
        when(zephyrFacade.prepareConfiguration()).thenReturn(prepareTestConfiguration());
        String issueId1 = "1";
        String issueId2 = "2";
        mockJiraIssueRetrieve(test1, issueId1);
        mockJiraIssueRetrieve(test2, issueId2);
        String executionBody = "{\"cycleId\":\"11113\",\"folderId\":\"11114\",\"issueId\":\"%s\","
                + "\"projectId\":\"11111\",\"versionId\":\"11112\"}";
        when(zephyrFacade.createExecution(String.format(executionBody, issueId1))).thenReturn(111);
        when(zephyrFacade.createExecution(String.format(executionBody, issueId2))).thenReturn(222);
        zephyrExporter.exportResults();
        verify(zephyrFacade).updateExecutionStatus(111, "{\"status\":\"-1\"}");
        verify(zephyrFacade).updateExecutionStatus(222, "{\"status\":\"1\"}");
    }

    private ZephyrConfiguration prepareTestConfiguration()
    {
        ZephyrConfiguration configuration = new ZephyrConfiguration();
        configuration.setProjectId("11111");
        configuration.setVersionId("11112");
        configuration.setCycleId("11113");
        configuration.setFolderId("11114");
        Map<TestCaseStatus, Integer> map = new EnumMap<>(TestCaseStatus.class);
        map.put(TestCaseStatus.SKIPPED, -1);
        map.put(TestCaseStatus.PASSED, 1);
        configuration.setTestStatusPerZephyrIdMapping(map);
        return configuration;
    }

    private void mockJiraIssueRetrieve(String issueKey, String issueId) throws IOException
    {
        JiraEntity issue = new JiraEntity();
        issue.setId(issueId);
        when(jiraFacade.getIssue(issueKey)).thenReturn(issue);
    }
}
