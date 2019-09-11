/*
 * Copyright 2019 the original author or authors.
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
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.List;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.ResourceUtils;

import org.vividus.facade.jira.IJiraFacade;
import org.vividus.facade.jira.model.Issue;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class ZephyrExporterTests
{
    private static final String NO_TEST_EXECUTION_RESULTS_TO_EXPORT_ARE_FOUND =
            "No test execution results to export are found";
    private static final ZonedDateTime NOW = ZonedDateTime.now();

    private final TestLogger logger = TestLoggerFactory.getTestLogger(ZephyrExporter.class);

    @Mock
    private IJiraFacade jiraFacade;

    @Mock
    private ZephyrExporterProperties zephyrExporterProperties;

    @InjectMocks
    private ZephyrExporter zephyrExporter;

    @Test
    void testConfigurationWithEmptyResultsToExport() throws IOException
    {
        when(zephyrExporterProperties.getSourceDirectory()).thenReturn(getFile("./").toPath());
        zephyrExporter.exportResults(NOW);
        verifyZeroInteractions(jiraFacade);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(info(NO_TEST_EXECUTION_RESULTS_TO_EXPORT_ARE_FOUND))));
    }

    @Test
    void testExportOfEmptyResults() throws IOException
    {
        when(zephyrExporterProperties.getSourceDirectory()).thenReturn(getFile("/empty_results").toPath());
        zephyrExporter.exportResults(NOW);
        verifyZeroInteractions(jiraFacade);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(getParsingLogMessage("/empty_results/BeforeStories.json"),
                info("Scenarios are not found"), info(NO_TEST_EXECUTION_RESULTS_TO_EXPORT_ARE_FOUND))));
    }

    @Test
    void testExportResults() throws IOException
    {
        String jiraProjectKey = "HSM";
        when(zephyrExporterProperties.getJiraProjectKey()).thenReturn(jiraProjectKey);
        when(zephyrExporterProperties.getSourceDirectory()).thenReturn(getFile("/test_results").toPath());
        LoggingEvent createIssueLogEvent = mockIssueCreation(jiraProjectKey, "HSM-1234");
        zephyrExporter.exportResults(NOW);
        assertThat(logger.getLoggingEvents(), equalTo(List.of(getParsingLogMessage("/test_results/SmokeTest.json"),
                info("Number of scenarios {}", 2), createIssueLogEvent)));
    }

    private LoggingEvent mockIssueCreation(String jiraProjectKey, String newTestKey) throws IOException
    {
        Issue test = new Issue();
        test.setKey(newTestKey);
        URL requestResource = getClass().getResource("/jira_requests/" + newTestKey + ".json");
        String createIssueRequest = IOUtils.toString(requestResource, StandardCharsets.UTF_8).trim();
        when(jiraFacade.createIssue(jiraProjectKey, createIssueRequest)).thenReturn(test);
        return info("Creating Zephyr Test: {}", createIssueRequest);
    }

    private LoggingEvent getParsingLogMessage(String path) throws FileNotFoundException
    {
        return info("Parsing {}", getFile(path));
    }

    private File getFile(String relativePath) throws FileNotFoundException
    {
        return ResourceUtils.getFile(getClass().getResource(relativePath));
    }
}
