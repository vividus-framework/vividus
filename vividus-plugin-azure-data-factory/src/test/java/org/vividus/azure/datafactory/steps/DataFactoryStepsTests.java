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

package org.vividus.azure.datafactory.steps;

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.datafactory.DataFactoryManager;
import com.azure.resourcemanager.datafactory.models.CreateRunResponse;
import com.azure.resourcemanager.datafactory.models.PipelineRun;
import com.azure.resourcemanager.datafactory.models.PipelineRuns;
import com.azure.resourcemanager.datafactory.models.Pipelines;
import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.ISoftAssert;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class DataFactoryStepsTests
{
    private static final String SUCCEEDED = "Succeeded";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(DataFactorySteps.class);

    @Mock private AzureProfile azureProfile;
    @Mock private TokenCredential tokenCredential;
    @Mock private ISoftAssert softAssert;

    @Test
    void shouldRunSuccessfulPipeline()
    {
        shouldRunPipeline(mock(PipelineRun.class), SUCCEEDED, List.of());
    }

    @Test
    void shouldRunFailingPipeline()
    {
        var errorMessage = "error message";
        PipelineRun finalPipelineRunState = mock(PipelineRun.class);
        when(finalPipelineRunState.message()).thenReturn(errorMessage);
        var actualStatus = "Failed";

        shouldRunPipeline(finalPipelineRunState, actualStatus,
                List.of(error("The pipeline run message: {}", errorMessage)));
    }

    private void shouldRunPipeline(PipelineRun finalPipelineRunState, String finalRunStatus,
            List<LoggingEvent> additionalLoggingEvents)
    {
        String pipelineName = "pipelineName";
        String factoryName = "factoryName";
        String resourceGroupName = "resourceGroupName";
        String runId = "run-id";

        executeSteps((dataFactoryManager, steps) -> {
            var createRunResponse = mock(CreateRunResponse.class);
            when(createRunResponse.runId()).thenReturn(runId);

            var pipelines = mock(Pipelines.class);
            when(pipelines.createRun(resourceGroupName, factoryName, pipelineName)).thenReturn(createRunResponse);

            when(dataFactoryManager.pipelines()).thenReturn(pipelines);

            var firstRunStatus = "InProgress";
            var pipelineRun1 = mock(PipelineRun.class);
            when(pipelineRun1.status()).thenReturn(firstRunStatus);

            when(finalPipelineRunState.runEnd()).thenReturn(OffsetDateTime.now());
            when(finalPipelineRunState.status()).thenReturn(finalRunStatus);

            var pipelineRuns = mock(PipelineRuns.class);
            when(pipelineRuns.get(resourceGroupName, factoryName, runId))
                    .thenReturn(pipelineRun1)
                    .thenReturn(finalPipelineRunState);

            when(dataFactoryManager.pipelineRuns()).thenReturn(pipelineRuns);

            when(softAssert.assertEquals("The pipeline run status", SUCCEEDED, finalRunStatus)).thenReturn(
                    SUCCEEDED.equals(finalRunStatus));

            steps.runPipeline(pipelineName, factoryName, resourceGroupName, Duration.ofSeconds(2));

            List<LoggingEvent> loggingEvents = new ArrayList<>();
            loggingEvents.add(info("The ID of the created pipeline run is {}", runId));
            var currentStatusLogMessageFormat = "The current pipeline run status is \"{}\"";
            loggingEvents.add(info(currentStatusLogMessageFormat, firstRunStatus));
            loggingEvents.add(info(currentStatusLogMessageFormat, finalRunStatus));
            loggingEvents.addAll(additionalLoggingEvents);

            assertThat(logger.getLoggingEvents(), is(loggingEvents));
        });
    }

    private void executeSteps(BiConsumer<DataFactoryManager, DataFactorySteps> consumer)
    {
        try (MockedStatic<DataFactoryManager> dataFactoryManagerStaticMock = mockStatic(DataFactoryManager.class))
        {
            var dataFactoryManager = mock(DataFactoryManager.class);
            dataFactoryManagerStaticMock.when(() -> DataFactoryManager.authenticate(tokenCredential, azureProfile))
                    .thenReturn(dataFactoryManager);
            var steps = new DataFactorySteps(azureProfile, tokenCredential, softAssert);
            consumer.accept(dataFactoryManager, steps);
        }
    }
}
