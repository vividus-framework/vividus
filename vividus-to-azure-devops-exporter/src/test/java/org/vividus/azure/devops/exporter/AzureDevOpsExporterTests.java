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

package org.vividus.azure.devops.exporter;

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.devops.client.model.WorkItem;
import org.vividus.azure.devops.configuration.AzureDevOpsExporterOptions;
import org.vividus.azure.devops.facade.AzureDevOpsFacade;
import org.vividus.model.jbehave.Scenario;
import org.vividus.model.jbehave.Step;
import org.vividus.output.SyntaxException;
import org.vividus.util.ResourceUtils;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class AzureDevOpsExporterTests
{
    private static final String EXPORT_SCENARIO_LOG = "Exporting {} scenario";
    private static final String STORY_PATH = "storyPath";
    private static final String EXPORT_STORY_LOG = "Exporting scenarios from {} story";
    private static final String CREATE_TITLE = "Create test case";
    private static final String UPDATE_TITLE = "Update test case";
    private static final Integer NEW_TEST_CASE_ID = 111;
    private static final Integer UPDATE_TEST_CASE_ID = 222;
    private static final String RESOURCE_FOLDER = "export";

    @Captor private ArgumentCaptor<Scenario> scenarioCaptor;
    @Mock private AzureDevOpsFacade facade;
    private AzureDevOpsExporter exporter;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AzureDevOpsExporter.class);

    @Test
    void shouldExportScenariosAndCreateTestRun() throws IOException, SyntaxException
    {
        AzureDevOpsExporterOptions options = new AzureDevOpsExporterOptions();
        options.setCreateTestRun(true);
        init(RESOURCE_FOLDER, options);

        when(facade.createTestCase(eq(STORY_PATH), scenarioCaptor.capture()))
                .thenReturn(createWorkItem(NEW_TEST_CASE_ID));

        exporter.exportResults();

        verify(facade).updateTestCase(eq(UPDATE_TEST_CASE_ID), eq(STORY_PATH), scenarioCaptor.capture());
        verify(facade).createTestCase(eq(STORY_PATH), any());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<Integer, Scenario>> exportedScenariosCaptor = ArgumentCaptor.forClass(Map.class);
        verify(facade).createTestRun(exportedScenariosCaptor.capture());

        assertThat(logger.getLoggingEvents(), is(List.of(
            info(EXPORT_STORY_LOG, STORY_PATH),
            info(EXPORT_SCENARIO_LOG, CREATE_TITLE),
            info(EXPORT_SCENARIO_LOG, UPDATE_TITLE),
            info("Skip export of {} scenario", "Skip test case")
        )));

        scenarioCaptor.getAllValues().forEach(this::assertScenario);

        Map<Integer, Scenario> exportedScenarios = exportedScenariosCaptor.getValue();
        assertThat(exportedScenarios, hasKey(UPDATE_TEST_CASE_ID));
        assertThat(exportedScenarios, hasKey(NEW_TEST_CASE_ID));
    }

    @Test
    void shouldExportScenariosAndNotCreateTestRun() throws IOException, SyntaxException
    {
        AzureDevOpsExporterOptions options = new AzureDevOpsExporterOptions();
        init(RESOURCE_FOLDER, options);

        when(facade.createTestCase(eq(STORY_PATH), scenarioCaptor.capture()))
                .thenReturn(createWorkItem(NEW_TEST_CASE_ID));

        exporter.exportResults();

        verify(facade).updateTestCase(eq(UPDATE_TEST_CASE_ID), eq(STORY_PATH), scenarioCaptor.capture());
        verify(facade).createTestCase(eq(STORY_PATH), any());
        verify(facade, never()).createTestRun(any());
    }

    @Test
    void shouldLogErrorsWhileExport() throws IOException, SyntaxException
    {
        init("error");

        IOException exception = mock(IOException.class);
        doThrow(exception).when(facade).createTestCase(eq(STORY_PATH), any());

        exporter.exportResults();

        assertThat(logger.getLoggingEvents(), is(List.of(
            info(EXPORT_STORY_LOG, STORY_PATH),
            info(EXPORT_SCENARIO_LOG, "Error test case"),
            error(exception, "Got an error while exporting"),
            info("No scenarios were exported")
        )));

        verify(facade, never()).createTestRun(any());
    }

    private void init(String directory)
    {
        init(directory, new AzureDevOpsExporterOptions());
    }

    private void init(String directory, AzureDevOpsExporterOptions options)
    {
        Path output = ResourceUtils.loadFile(getClass(), directory).toPath();
        options.setJsonResultsDirectory(output);
        this.exporter = new AzureDevOpsExporter(options, facade);
    }

    private void assertScenario(Scenario scenario)
    {
        List<Step> steps = scenario.collectSteps();
        assertThat(steps, hasSize(3));
        assertAll(
            () -> assertEquals("Given I setup test environment", steps.get(0).getValue()),
            () -> assertEquals("When I perform action on test environment", steps.get(1).getValue()),
            () -> assertEquals("Then I verify changes on test environment", steps.get(2).getValue())
        );
    }

    private WorkItem createWorkItem(Integer id)
    {
        WorkItem workItem = new WorkItem();
        workItem.setId(id);
        return workItem;
    }
}
