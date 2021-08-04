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

package org.vividus.azure.devops.facade;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.devops.client.AzureDevOpsClient;
import org.vividus.azure.devops.client.model.AddOperation;
import org.vividus.azure.devops.configuration.AzureDevOpsExporterOptions;
import org.vividus.bdd.model.jbehave.Step;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class AzureDevOpsFacadeTests
{
    private static final String TITLE = "title";
    private static final String STEPS = "<steps id=\"0\" last=\"2\"><step id=\"2\" type=\"ActionStep\">"
            + "<description/><parameterizedString isformatted=\"true\">step</parameterizedString>"
            + "<parameterizedString isformatted=\"true\"/></step></steps>";
    private static final String PROJECT = "project";
    private static final String AREA = "area";

    @Captor private ArgumentCaptor<List<AddOperation>> operationsCaptor;
    @Mock private AzureDevOpsClient client;
    private AzureDevOpsFacade facade;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AzureDevOpsFacade.class);

    @BeforeEach
    void init()
    {
        AzureDevOpsExporterOptions options = new AzureDevOpsExporterOptions();
        options.setProject(PROJECT);
        options.setArea(AREA);
        this.facade = new AzureDevOpsFacade(client, options);
    }

    @Test
    void shouldCreateTestCase() throws IOException
    {
        when(client.createTestCase(operationsCaptor.capture())).thenReturn("{\"id\": 1}");
        facade.createTestCase(TITLE, List.of(createStep()));
        assertOperations();
        assertThat(logger.getLoggingEvents(), is(List.of(
            info("Creating Test Case"),
            info("Test Case with ID {} has been created", 1)
        )));
    }

    @Test
    void shouldUpdateTestCase() throws IOException
    {
        String testCaseId = "test-case-id";
        facade.updateTestCase(testCaseId, TITLE, List.of(createStep()));
        verify(client).updateTestCase(eq(testCaseId), operationsCaptor.capture());
        assertOperations();
        assertThat(logger.getLoggingEvents(), is(List.of(
            info("Updating Test Case with ID {}", testCaseId),
            info("Test Case with ID {} has been updated", testCaseId)
        )));
    }

    private void assertOperations()
    {
        List<AddOperation> operations = operationsCaptor.getValue();
        assertThat(operations, hasSize(3));
        assertAll(
            () -> assertOperation(operations.get(0), "/fields/System.AreaPath", PROJECT + '\\' + AREA),
            () -> assertOperation(operations.get(1), "/fields/System.Title", TITLE),
            () -> assertOperation(operations.get(2), "/fields/Microsoft.VSTS.TCM.Steps", STEPS)
        );
    }

    private void assertOperation(AddOperation operation, String path, String value)
    {
        assertAll(
            () -> assertEquals(path, operation.getPath()),
            () -> assertEquals(value, operation.getValue())
        );
    }

    private static Step createStep()
    {
        Step step = new Step();
        step.setValue("step");
        return step;
    }
}
