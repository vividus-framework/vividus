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

package org.vividus.azure.devops.exporter;

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.devops.configuration.AzureDevOpsExporterOptions;
import org.vividus.azure.devops.facade.AzureDevOpsFacade;
import org.vividus.bdd.model.jbehave.Scenario;
import org.vividus.bdd.model.jbehave.Step;
import org.vividus.bdd.output.SyntaxException;
import org.vividus.util.ResourceUtils;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class AzureDevOpsExporterTests
{
    private static final String EXPORT_SCENARIO_LOG = "Exporting {} scenario";
    private static final String STORY_PATH = "storyPath";
    private static final String EXPORT_STORY_LOG = "Exporting scenarios from {} story";
    private static final String CREATE_TITLE = "Create test case";
    private static final String UPDATE_TITLE = "Update test case";

    @Captor private ArgumentCaptor<Scenario> scenarioCaptor;
    @Mock private AzureDevOpsFacade facade;
    private AzureDevOpsExporter exporter;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AzureDevOpsExporter.class);

    @Test
    void shouldExportScenarios() throws IOException, SyntaxException
    {
        init("export");

        exporter.exportResults();

        verify(facade).updateTestCase(eq("STUB-0"), eq(STORY_PATH), scenarioCaptor.capture());
        verify(facade).createTestCase(eq(STORY_PATH), scenarioCaptor.capture());

        assertThat(logger.getLoggingEvents(), is(List.of(
            info(EXPORT_STORY_LOG, STORY_PATH),
            info(EXPORT_SCENARIO_LOG, CREATE_TITLE),
            info(EXPORT_SCENARIO_LOG, UPDATE_TITLE),
            info("Skip export of {} scenario", "Skip test case")
        )));

        scenarioCaptor.getAllValues().forEach(this::assertScenario);
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
            error(exception, "Got an error while exporting")
        )));
    }

    private void init(String directory)
    {
        AzureDevOpsExporterOptions options = new AzureDevOpsExporterOptions();
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
}
