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

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.jira.JiraConfigurationException;
import org.vividus.jira.JiraFacade;
import org.vividus.jira.model.JiraEntity;
import org.vividus.util.ResourceUtils;
import org.vividus.zephyr.configuration.ZephyrConfiguration;
import org.vividus.zephyr.configuration.ZephyrExporterProperties;
import org.vividus.zephyr.facade.ZephyrFacade;
import org.vividus.zephyr.model.TestCaseLevel;
import org.vividus.zephyr.model.TestCaseStatus;

@ExtendWith({MockitoExtension.class, TestLoggerFactoryExtension.class})
class ZephyrExporterTests
{
    public static final String CREATEREPORTS = "createreports";
    public static final String UPDATEREPORTS = "updatereports";
    private static final String TEST_CASE_KEY1 = "TEST-1";
    private static final String TEST_CASE_KEY2 = "TEST-2";
    private static final String STORY_TEST_CASE_KEY1 = "STORY-1";
    private static final String ISSUE_ID1 = "1";
    private static final String ISSUE_ID2 = "2";
    private static final String STATUS_UPDATE_JSON = "{\"status\":\"-1\"}";
    private static final String SCENARIO_TITLE = "scenarioTitle";
    private static final String SECOND_SCENARIO_TITLE = "secondScenarioTitle";
    private static final String STORY_TITLE = "storyPath";
    private static final String EXPORTING_SCENARIO = "Exporting {} scenario";
    private static final String EXPORTING_SCENARIO_FROM_STORY = "Exporting scenarios from {} story";
    private static final String EXPORTING_STORY = "Exporting {} story";
    private static final String ERROR_MESSAGE = "Got an error while exporting ";

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(ZephyrExporter.class);

    @Mock private JiraFacade jiraFacade;
    @Mock private ZephyrFacade zephyrFacade;
    @Spy private ZephyrExporterProperties zephyrExporterProperties;
    @InjectMocks private ZephyrExporter zephyrExporter;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(ZephyrExporter.class);

    @Test
    void testExportExecutionResults() throws IOException, URISyntaxException, JiraConfigurationException
    {
        when(zephyrFacade.prepareConfiguration()).thenReturn(prepareTestConfiguration());
        mockJiraIssueRetrieve(TEST_CASE_KEY1, ISSUE_ID1);
        mockJiraIssueRetrieve(TEST_CASE_KEY2, ISSUE_ID2);
        String executionBody = "{\"cycleId\":\"11113\",\"folderId\":\"11114\",\"issueId\":\"%s\","
            + "\"projectId\":\"11111\",\"versionId\":\"11112\"}";
        when(zephyrFacade.createExecution(String.format(executionBody, ISSUE_ID1))).thenReturn(111);
        when(zephyrFacade.createExecution(String.format(executionBody, ISSUE_ID2))).thenReturn(222);
        URI jsonResultsUri = getJsonResultsUri(CREATEREPORTS);
        zephyrExporterProperties.setLevel(TestCaseLevel.SCENARIO);
        zephyrExporterProperties.setSourceDirectory(Paths.get(jsonResultsUri));
        zephyrExporter.exportResults();
        verify(zephyrFacade).updateExecutionStatus(111, STATUS_UPDATE_JSON);
        verify(zephyrFacade).updateExecutionStatus(222, "{\"status\":\"1\"}");
    }

    @Test
    void testExportExecutionResultsWithOnlyStatusUpdate()
        throws IOException, URISyntaxException, JiraConfigurationException
    {
        when(zephyrExporterProperties.getUpdateExecutionStatusesOnly()).thenReturn(true);
        when(zephyrFacade.prepareConfiguration()).thenReturn(prepareTestConfiguration());
        mockJiraIssueRetrieve(TEST_CASE_KEY1, ISSUE_ID1);
        mockJiraIssueRetrieve(TEST_CASE_KEY2, ISSUE_ID2);
        when(zephyrFacade.findExecutionId(ISSUE_ID1)).thenReturn(OptionalInt.of(111));
        when(zephyrFacade.findExecutionId(ISSUE_ID2)).thenReturn(OptionalInt.empty());
        URI jsonResultsUri = getJsonResultsUri(UPDATEREPORTS);
        zephyrExporterProperties.setLevel(TestCaseLevel.SCENARIO);
        zephyrExporterProperties.setSourceDirectory(Paths.get(jsonResultsUri));

        zephyrExporter.exportResults();

        verify(zephyrFacade).updateExecutionStatus(111, STATUS_UPDATE_JSON);
        assertThat(testLogger.getLoggingEvents(), is(List.of(info("Test case result for {} was not exported, "
            + "because execution does not exist", TEST_CASE_KEY2))));
    }

    @Test
    void shouldFailIfExecutionResultsDirectoryIsEmpty(@TempDir Path sourceDirectory)
    {
        zephyrExporterProperties.setSourceDirectory(sourceDirectory);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            zephyrExporter::exportResults);

        assertEquals(String.format("The directory '%s' does not contain needed JSON files", sourceDirectory),
            exception.getMessage());
        assertThat(logger.getLoggingEvents(), empty());
    }

    @Test
    void shouldExportNewTestWithStoryLevel() throws URISyntaxException, IOException, JiraConfigurationException
    {
        URI jsonResultsUri = getJsonResultsUri(CREATEREPORTS);
        zephyrExporterProperties.setLevel(TestCaseLevel.STORY);
        zephyrExporterProperties.setSourceDirectory(Paths.get(jsonResultsUri));
        zephyrExporterProperties.setExportResults(true);
        when(zephyrFacade.createTestCase(any())).thenReturn(STORY_TEST_CASE_KEY1);
        when(zephyrFacade.prepareConfiguration()).thenReturn(prepareTestConfiguration());
        mockJiraIssueRetrieve(STORY_TEST_CASE_KEY1, ISSUE_ID1);

        zephyrExporter.exportResults();

        assertThat(logger.getLoggingEvents(), is(Collections.singletonList(info(EXPORTING_STORY, STORY_TITLE))));
    }

    @Test
    void shouldExportNewTestWithScenarioLevel() throws URISyntaxException, IOException, JiraConfigurationException
    {
        URI jsonResultsUri = getJsonResultsUri(CREATEREPORTS);
        zephyrExporterProperties.setLevel(TestCaseLevel.SCENARIO);
        zephyrExporterProperties.setSourceDirectory(Paths.get(jsonResultsUri));
        zephyrExporterProperties.setExportResults(true);
        when(jiraFacade.getIssue(any())).thenReturn(new JiraEntity());
        when(zephyrFacade.createTestCase(any())).thenReturn(ISSUE_ID1);
        when(zephyrFacade.prepareConfiguration()).thenReturn(prepareTestConfiguration());

        zephyrExporter.exportResults();

        verify(zephyrFacade, times(2)).createTestCase(any());
        assertThat(logger.getLoggingEvents(), is(List.of(info(EXPORTING_SCENARIO_FROM_STORY, STORY_TITLE),
            info(EXPORTING_SCENARIO, SCENARIO_TITLE),
            info(EXPORTING_SCENARIO, SECOND_SCENARIO_TITLE))));
    }

    @Test
    void shouldUpdateTestWithStoryLevel() throws URISyntaxException, IOException, JiraConfigurationException
    {
        URI jsonResultsUri = getJsonResultsUri(UPDATEREPORTS);
        zephyrExporterProperties.setLevel(TestCaseLevel.STORY);
        zephyrExporterProperties.setSourceDirectory(Paths.get(jsonResultsUri));
        zephyrExporterProperties.setExportResults(true);
        zephyrExporterProperties.setUpdateCasesOnExport(true);

        when(zephyrFacade.prepareConfiguration()).thenReturn(prepareTestConfiguration());
        mockJiraIssueRetrieve(STORY_TEST_CASE_KEY1, ISSUE_ID1);

        zephyrExporter.exportResults();

        verify(zephyrFacade).updateTestCase(any(), any());
        assertThat(logger.getLoggingEvents(), is(List.of(info(EXPORTING_STORY, STORY_TITLE))));
    }

    static Stream<Object> levels()
    {
        return Stream.of(
            arguments(TestCaseLevel.STORY, List.of(info(EXPORTING_STORY, STORY_TITLE)), List.of(ERROR_MESSAGE)),
            arguments(TestCaseLevel.SCENARIO, List.of(info(EXPORTING_SCENARIO_FROM_STORY, STORY_TITLE),
                    info(EXPORTING_SCENARIO, SCENARIO_TITLE), info(EXPORTING_SCENARIO, SECOND_SCENARIO_TITLE)),
                List.of(ERROR_MESSAGE, ERROR_MESSAGE)
            )
        );
    }

    @ParameterizedTest
    @MethodSource("levels")
    void shouldNotExportTestWithExceptionDifferentLevel(TestCaseLevel level, List<LoggingEvent> events,
                                                        List<String> errors)
        throws URISyntaxException, IOException, JiraConfigurationException
    {
        URI jsonResultsUri = getJsonResultsUri(CREATEREPORTS);
        zephyrExporterProperties.setSourceDirectory(Paths.get(jsonResultsUri));
        zephyrExporterProperties.setExportResults(true);
        zephyrExporterProperties.setLevel(level);
        zephyrExporterProperties.setUpdateCasesOnExport(false);

        IOException exception = mock(IOException.class);
        doThrow(exception).when(zephyrFacade).createTestCase(any());

        zephyrExporter.exportResults();
        ArrayList<LoggingEvent> allEvents = new ArrayList<>(events);
        errors.forEach(message -> allEvents.add(error(exception, message + level.getLevel())));

        assertThat(logger.getLoggingEvents(), containsInAnyOrder(allEvents.toArray()));
    }

    @Test
    void shouldUpdateTestWithScenarioLevel() throws URISyntaxException, IOException, JiraConfigurationException
    {
        URI jsonResultsUri = getJsonResultsUri(UPDATEREPORTS);
        zephyrExporterProperties.setLevel(TestCaseLevel.SCENARIO);
        zephyrExporterProperties.setSourceDirectory(Paths.get(jsonResultsUri));
        zephyrExporterProperties.setUpdateCasesOnExport(true);
        zephyrExporterProperties.setExportResults(true);

        when(zephyrFacade.prepareConfiguration()).thenReturn(prepareTestConfiguration());
        mockJiraIssueRetrieve(TEST_CASE_KEY1, ISSUE_ID1);
        mockJiraIssueRetrieve(TEST_CASE_KEY2, ISSUE_ID2);
        zephyrExporter.exportResults();

        verify(zephyrFacade, times(2)).updateTestCase(any(), any());

        assertThat(logger.getLoggingEvents(), is(List.of(info(EXPORTING_SCENARIO_FROM_STORY, STORY_TITLE),
            info(EXPORTING_SCENARIO, SCENARIO_TITLE),
            info(EXPORTING_SCENARIO, SECOND_SCENARIO_TITLE))));
    }

    private ZephyrConfiguration prepareTestConfiguration()
    {
        ZephyrConfiguration configuration = new ZephyrConfiguration();
        configuration.setProjectId("11111");
        configuration.setVersionId("11112");
        configuration.setCycleId("11113");
        configuration.setFolderId("11114");
        Map<TestCaseStatus, Integer> map = new EnumMap<>(TestCaseStatus.class);
        map.put(TestCaseStatus.FAILED, -1);
        map.put(TestCaseStatus.PASSED, 1);
        configuration.setTestStatusPerZephyrIdMapping(map);
        return configuration;
    }

    private void mockJiraIssueRetrieve(String issueKey, String issueId) throws IOException, JiraConfigurationException
    {
        JiraEntity issue = new JiraEntity();
        issue.setId(issueId);
        when(jiraFacade.getIssue(issueKey)).thenReturn(issue);
    }

    public URI getJsonResultsUri(String resource) throws URISyntaxException
    {
        return ResourceUtils.findResource(getClass(), resource).toURI();
    }
}
