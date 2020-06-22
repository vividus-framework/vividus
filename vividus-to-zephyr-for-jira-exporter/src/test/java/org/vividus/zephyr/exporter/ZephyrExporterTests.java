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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.jira.JiraFacade;
import org.vividus.jira.model.JiraEntity;
import org.vividus.zephyr.IZephyrFacade;
import org.vividus.zephyr.ZephyrConfiguration;

import uk.org.lidalia.slf4jext.Level;

@ExtendWith({MockitoExtension.class, TestLoggerFactoryExtension.class})
class ZephyrExporterTests
{
    private static final String TEST_CASES = "/test-cases";
    private static final String EXECUTION_BODY = "{\"cycleId\":\"11113\",\"folderId\":\"11114\",\"issueId\":\"%s\","
            + "\"projectId\":\"11111\",\"versionId\":\"11112\"}";
    private static final String JSON_FILES_STRING = "Json files: {}";
    private static final String TEST_KEY = "TEST-1";

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(ZephyrExporter.class);

    @Mock
    private JiraFacade jiraFacade;

    @Mock
    private IZephyrFacade zephyrFacade;

    @Mock
    private ZephyrExporterProperties zephyrExporterProperties;

    @InjectMocks
    private ZephyrExporter zephyrExporter;

    @Test
    void testConfigurationWithEmptyResultsToExport(@TempDir Path tempDir) throws IOException
    {
        Path emptyDirectoryPath = tempDir.resolve("empty");
        Files.createDirectory(emptyDirectoryPath);
        when(zephyrExporterProperties.getSourceDirectory()).thenReturn(emptyDirectoryPath);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> zephyrExporter.exportResults());
        assertEquals("Folder '" + emptyDirectoryPath + "' does not contain needed json files",
                exception.getMessage());
        verifyNoMoreInteractions(jiraFacade);
        verifyNoMoreInteractions(zephyrFacade);
        assertThat(testLogger.getLoggingEvents(), empty());
    }

    @Test
    void testExportWithEmptyKey() throws URISyntaxException
    {
        Path sourceDirectory = Paths.get(getClass().getResource("/emptyKey").toURI());
        when(zephyrExporterProperties.getSourceDirectory()).thenReturn(sourceDirectory);
        assertThrows(IllegalArgumentException.class, () -> zephyrExporter.exportResults());
        verifyNoInteractions(jiraFacade, zephyrFacade);
        assertThat(testLogger.getLoggingEvents().get(0).getLevel(), is(Level.INFO));
        assertThat(testLogger.getLoggingEvents().get(0).getMessage(), is(JSON_FILES_STRING));
    }

    @Test
    void testExportResultsProblemWithReading() throws URISyntaxException
    {
        Path sourceDirectory = Paths.get(getClass().getResource("/emptyKey/test-cases/exception").toURI());
        when(zephyrExporterProperties.getSourceDirectory()).thenReturn(sourceDirectory);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> zephyrExporter.exportResults());
        assertThat(exception.getMessage(), matchesRegex(
                "Problem with reading values from json file .*/emptyKey/test-cases/exception/readingException.json"));
        verifyNoInteractions(jiraFacade, zephyrFacade);
        List<File> files = List.of(sourceDirectory.resolve("readingException.json").toFile());
        assertThat(testLogger.getLoggingEvents(), is(List.of(info(JSON_FILES_STRING, files))));
    }

    @Test
    void testExportResultsProblemWithSerializing() throws IOException, URISyntaxException
    {
        mockJiraIssueRetrieve(TEST_KEY, "42");
        Path sourceDirectory = Paths.get(getClass().getResource(TEST_CASES).toURI());
        when(zephyrExporterProperties.getSourceDirectory()).thenReturn(sourceDirectory);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> zephyrExporter.exportResults());
        assertEquals("Problem with serializing execution to json file", exception.getMessage());
        assertThat(testLogger.getLoggingEvents().get(0).getLevel(), is(Level.INFO));
        assertThat(testLogger.getLoggingEvents().get(0).getMessage(), is(JSON_FILES_STRING));
        assertThat(testLogger.getLoggingEvents().size(), equalTo(3));
    }

    @Test
    void testExportResults() throws IOException, URISyntaxException
    {
        Path sourceDirectory = Paths.get(getClass().getResource(TEST_CASES).toURI());
        when(zephyrExporterProperties.getSourceDirectory()).thenReturn(sourceDirectory);
        ZephyrConfiguration configuration = new ZephyrConfiguration();
        configuration.setProjectId("11111");
        configuration.setVersionId("11112");
        configuration.setCycleId("11113");
        configuration.setFolderId("11114");
        when(zephyrFacade.prepareConfiguration()).thenReturn(configuration);
        String issueId1 = "1";
        String issueId2 = "2";
        mockJiraIssueRetrieve(TEST_KEY, issueId1);
        mockJiraIssueRetrieve("TEST-2", issueId2);
        when(zephyrFacade.createExecution(String.format(EXECUTION_BODY, issueId1))).thenReturn(111);
        when(zephyrFacade.createExecution(String.format(EXECUTION_BODY, issueId2))).thenReturn(222);
        zephyrExporter.exportResults();
        verify(zephyrFacade).updateExecutionStatus(111, "{\"status\":\"-1\"}");
        verify(zephyrFacade).updateExecutionStatus(222, "{\"status\":\"1\"}");
        assertThat(testLogger.getLoggingEvents().get(0).getMessage(), is(JSON_FILES_STRING));
        assertThat(testLogger.getLoggingEvents().get(0).getLevel(), is(Level.INFO));
        assertThat(testLogger.getLoggingEvents().size(), equalTo(3));
    }

    private void mockJiraIssueRetrieve(String issueKey, String issueId) throws IOException
    {
        JiraEntity issue = new JiraEntity();
        issue.setId(issueId);
        when(jiraFacade.getIssue(issueKey)).thenReturn(issue);
    }
}
