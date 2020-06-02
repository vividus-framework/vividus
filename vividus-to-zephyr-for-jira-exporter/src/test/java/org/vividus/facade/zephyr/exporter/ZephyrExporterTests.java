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

package org.vividus.facade.zephyr.exporter;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.facade.jira.IJiraFacade;
import org.vividus.facade.zephyr.IZephyrFacade;
import org.vividus.facade.zephyr.ZephyrConfiguration;

import uk.org.lidalia.slf4jext.Level;

@ExtendWith({MockitoExtension.class, TestLoggerFactoryExtension.class})
class ZephyrExporterTests
{
    private static final String SRC = "src";
    private static final String TEST = "test";
    private static final String RESOURCES = "resources";
    private static final String TEST_CASES = "test-cases";
    private static final String EMPTY_KEY = "emptyKey";
    private static final String EXCEPTION = "exception";
    private static final Path PATH = Paths.get(SRC, TEST, RESOURCES, TEST_CASES);
    private static final String EXECUTION_BODY = "{\"cycleId\":\"11113\",\"folderId\":\"11114\",\"issueId\":\"%s\","
            + "\"projectId\":\"11111\",\"versionId\":\"11112\"}";
    private static final String PATH_TO_READING_EXCEPTION_JSON = SRC + File.separator + TEST + File.separator
            + RESOURCES + File.separator + EMPTY_KEY + File.separator + TEST_CASES + File.separator + EXCEPTION
            + File.separator + "readingException.json";
    private static final String JSON_FILES_STRING = "Json files: {}";

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(ZephyrExporter.class);

    @Mock
    private IJiraFacade jiraFacade;

    @Mock
    private IZephyrFacade zephyrFacade;

    @Mock
    private ZephyrExporterProperties zephyrExporterProperties;

    @InjectMocks
    private ZephyrExporter zephyrExporter;

    @Test
    void testConfigurationWithEmptyResultsToExport() throws IOException
    {
        Path emptyDirectoryPath = Paths.get(SRC, TEST, RESOURCES, "empty");
        when(zephyrExporterProperties.getSourceDirectory())
                .thenReturn(Files.createDirectory(emptyDirectoryPath));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> zephyrExporter.exportResults());
        assertEquals("Folder '" + emptyDirectoryPath + "' does not contain needed json files",
                exception.getMessage());
        verifyNoMoreInteractions(jiraFacade);
        verifyNoMoreInteractions(zephyrFacade);
        assertThat(testLogger.getLoggingEvents(), empty());
        Files.deleteIfExists(emptyDirectoryPath);
    }

    @Test
    void testExportWithEmptyKey() throws IOException
    {
        when(zephyrExporterProperties.getSourceDirectory())
                .thenReturn(Paths.get(SRC, TEST, RESOURCES, EMPTY_KEY));
        assertThrows(IllegalArgumentException.class, () -> zephyrExporter.exportResults());
        verifyNoMoreInteractions(jiraFacade);
        verifyNoMoreInteractions(zephyrFacade);
        assertThat(testLogger.getLoggingEvents().get(0).getLevel(), is(Level.INFO));
        assertThat(testLogger.getLoggingEvents().get(0).getMessage(), is(JSON_FILES_STRING));
    }

    @Test
    void testExportResultsProblemWithReading() throws IOException
    {
        List<File> files = List.of(new File(PATH_TO_READING_EXCEPTION_JSON));
        when(zephyrExporterProperties.getSourceDirectory())
                .thenReturn(Paths.get(SRC, TEST, RESOURCES, EMPTY_KEY, TEST_CASES, EXCEPTION));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> zephyrExporter.exportResults());
        assertEquals("Problem with reading values from json file " + PATH_TO_READING_EXCEPTION_JSON,
                exception.getMessage());
        verifyNoMoreInteractions(jiraFacade);
        verifyNoMoreInteractions(zephyrFacade);
        assertThat(testLogger.getLoggingEvents(), is(List.of(info(JSON_FILES_STRING, files))));
    }

    @Test
    void testExportResultsProblemWithSerializing() throws IOException
    {
        when(zephyrExporterProperties.getSourceDirectory()).thenReturn(PATH);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> zephyrExporter.exportResults());
        assertEquals("Problem with serializing execution to json file", exception.getMessage());
        assertThat(testLogger.getLoggingEvents().get(0).getLevel(), is(Level.INFO));
        assertThat(testLogger.getLoggingEvents().get(0).getMessage(), is(JSON_FILES_STRING));
        assertThat(testLogger.getLoggingEvents().size(), equalTo(3));
    }

    @Test
    void testExportResults() throws IOException
    {
        when(zephyrExporterProperties.getSourceDirectory()).thenReturn(PATH);
        ZephyrConfiguration configuration = new ZephyrConfiguration();
        configuration.setProjectId("11111");
        configuration.setVersionId("11112");
        configuration.setCycleId("11113");
        configuration.setFolderId("11114");
        when(zephyrFacade.prepareConfiguration()).thenReturn(configuration);
        String firstIssueId = "1";
        String secondIssueId = "2";
        when(jiraFacade.getIssueId("TEST-1")).thenReturn(firstIssueId);
        when(jiraFacade.getIssueId("TEST-2")).thenReturn(secondIssueId);
        when(zephyrFacade.createExecution(String.format(EXECUTION_BODY, firstIssueId))).thenReturn(111);
        when(zephyrFacade.createExecution(String.format(EXECUTION_BODY, secondIssueId))).thenReturn(222);
        zephyrExporter.exportResults();
        verify(zephyrFacade).updateExecutionStatus(111, "{\"status\":\"-1\"}");
        verify(zephyrFacade).updateExecutionStatus(222, "{\"status\":\"1\"}");
        assertThat(testLogger.getLoggingEvents().get(0).getMessage(), is(JSON_FILES_STRING));
        assertThat(testLogger.getLoggingEvents().get(0).getLevel(), is(Level.INFO));
        assertThat(testLogger.getLoggingEvents().size(), equalTo(3));
    }
}
