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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.EnvironmentCredential;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.datafactory.v2018_06_01.PipelineRun;
import com.microsoft.azure.management.datafactory.v2018_06_01.PipelineRuns;
import com.microsoft.azure.management.datafactory.v2018_06_01.Pipelines;
import com.microsoft.azure.management.datafactory.v2018_06_01.implementation.CreateRunResponseInner;
import com.microsoft.azure.management.datafactory.v2018_06_01.implementation.DataFactoryManager;
import com.microsoft.azure.management.datafactory.v2018_06_01.implementation.DataFactoryManager.Configurable;
import com.microsoft.azure.management.datafactory.v2018_06_01.implementation.PipelinesInner;
import com.microsoft.rest.ServiceResponse;

import org.joda.time.DateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.ISoftAssert;

import reactor.core.publisher.Mono;
import rx.Observable;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class DataFactoryStepsTests
{
    private static final String SUCCEEDED = "Succeeded";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(DataFactorySteps.class);

    @Mock private ISoftAssert softAssert;

    @Test
    void shouldRunSuccessfulPipeline() throws IOException
    {
        shouldRunPipeline(mock(PipelineRun.class), SUCCEEDED, List.of());
    }

    @Test
    void shouldRunFailingPipeline() throws IOException
    {
        String errorMessage = "error message";
        PipelineRun finalPipelineRunState = mock(PipelineRun.class);
        when(finalPipelineRunState.message()).thenReturn(errorMessage);
        String actualStatus = "Failed";

        shouldRunPipeline(finalPipelineRunState, actualStatus,
                List.of(error("The pipeline run message: {}", errorMessage)));
    }

    private void shouldRunPipeline(PipelineRun finalPipelineRunState, String finalRunStatus,
            List<LoggingEvent> additionalLoggingEvents) throws IOException
    {
        String pipelineName = "pipelineName";
        String factoryName = "factoryName";
        String resourceGroupName = "resourceGroupName";
        String runId = "run-id";

        executeSteps((dataFactoryManager, steps) -> {
            PipelinesInner pipelinesInner = mock(PipelinesInner.class);
            Observable<ServiceResponse<CreateRunResponseInner>> observablePipeline = Observable.from(
                    List.of(new ServiceResponse<>(new CreateRunResponseInner().withRunId(runId), null)));

            when(pipelinesInner.createRunWithServiceResponseAsync(resourceGroupName, factoryName, pipelineName, null,
                    null, null, null, Map.of())).thenReturn(observablePipeline);

            Pipelines pipelines = mock(Pipelines.class);
            when(pipelines.inner()).thenReturn(pipelinesInner);

            when(dataFactoryManager.pipelines()).thenReturn(pipelines);

            String firstRunStatus = "InProgress";
            PipelineRun pipelineRun1 = mock(PipelineRun.class);
            when(pipelineRun1.status()).thenReturn(firstRunStatus);

            when(finalPipelineRunState.runEnd()).thenReturn(DateTime.now());
            when(finalPipelineRunState.status()).thenReturn(finalRunStatus);

            PipelineRuns pipelineRuns = mock(PipelineRuns.class);
            when(pipelineRuns.getAsync(resourceGroupName, factoryName, runId))
                    .thenReturn(Observable.from(List.of(pipelineRun1)))
                    .thenReturn(Observable.from(List.of(finalPipelineRunState)));

            when(dataFactoryManager.pipelineRuns()).thenReturn(pipelineRuns);

            when(softAssert.assertEquals("The pipeline run status", SUCCEEDED, finalRunStatus)).thenReturn(
                    SUCCEEDED.equals(finalRunStatus));

            steps.runPipeline(pipelineName, factoryName, resourceGroupName, Duration.ofSeconds(2));

            List<LoggingEvent> loggingEvents = new ArrayList<>();
            loggingEvents.add(info("The ID of the created pipeline run is {}", runId));
            String currentStatusLogMessageFormat = "The current pipeline run status is \"{}\"";
            loggingEvents.add(info(currentStatusLogMessageFormat, firstRunStatus));
            loggingEvents.add(info(currentStatusLogMessageFormat, finalRunStatus));
            loggingEvents.addAll(additionalLoggingEvents);

            assertThat(logger.getLoggingEvents(), is(loggingEvents));
        });
    }

    @SuppressWarnings({ "try", "unchecked" })
    private void executeSteps(BiConsumer<DataFactoryManager, DataFactorySteps> consumer) throws IOException
    {
        AzureEnvironment environment = AzureEnvironment.AZURE;
        String tenantId = "tenant-id";
        String subscriptionId = "subscription-id";

        EnvironmentCredential credential = mock(EnvironmentCredential.class);
        try (MockedConstruction<EnvironmentCredentialBuilder> ignoredBuilder = mockConstruction(
                EnvironmentCredentialBuilder.class, (mock, context) -> {
                    assertEquals(1, context.getCount());
                    assertEquals(List.of(), context.arguments());
                    when(mock.build()).thenReturn(credential);
                });
            MockedStatic<DataFactoryManager> managerFactory = mockStatic(DataFactoryManager.class))
        {
            ArgumentCaptor<AzureTokenCredentials> azureTokenCredentialsCaptor = ArgumentCaptor.forClass(
                    AzureTokenCredentials.class);

            DataFactoryManager manager = mock(DataFactoryManager.class);

            Configurable configurable = mock(Configurable.class);
            when(configurable.authenticate(azureTokenCredentialsCaptor.capture(), eq(subscriptionId))).thenReturn(
                    manager);

            managerFactory.when(DataFactoryManager::configure).thenReturn(configurable);

            DataFactorySteps steps = new DataFactorySteps(environment, tenantId, subscriptionId, softAssert);

            String token = "token";
            ArgumentCaptor<TokenRequestContext> tokenRequestContextCaptor = ArgumentCaptor.forClass(
                    TokenRequestContext.class);
            when(credential.getToken(tokenRequestContextCaptor.capture())).thenReturn(
                    Mono.fromSupplier(() -> new AccessToken(token, OffsetDateTime.MAX)));

            String resource = "resource";
            assertEquals(token, azureTokenCredentialsCaptor.getValue().getToken(resource));

            assertEquals(List.of(resource + "/.default"), tokenRequestContextCaptor.getValue().getScopes());

            consumer.accept(manager, steps);
        }
    }
}
