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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.datafactory.DataFactoryManager;
import com.azure.resourcemanager.datafactory.fluent.models.PipelineRunInner;
import com.azure.resourcemanager.datafactory.fluent.models.PipelineRunsQueryResponseInner;
import com.azure.resourcemanager.datafactory.models.CreateRunResponse;
import com.azure.resourcemanager.datafactory.models.PipelineRun;
import com.azure.resourcemanager.datafactory.models.PipelineRuns;
import com.azure.resourcemanager.datafactory.models.PipelineRunsQueryResponse;
import com.azure.resourcemanager.datafactory.models.Pipelines;
import com.azure.resourcemanager.datafactory.models.RunFilterParameters;
import com.azure.resourcemanager.datafactory.models.RunQueryFilterOperand;
import com.azure.resourcemanager.datafactory.models.RunQueryFilterOperator;
import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.lang3.function.FailableBiConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.datafactory.steps.DataFactorySteps.RunFilter;
import org.vividus.azure.datafactory.steps.DataFactorySteps.RunFilterType;
import org.vividus.azure.util.InnersJacksonAdapter;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.softassert.ISoftAssert;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class DataFactoryStepsTests
{
    private static final String RESOURCE_GROUP_NAME = "resourceGroupName";
    private static final String FACTORY_NAME = "factoryName";
    private static final String PIPELINE_NAME = "pipelineName";
    private static final Duration WAIT_TIMEOUT = Duration.ofSeconds(2);

    private static final String SUCCEEDED = "Succeeded";
    private static final String FAILED = "Failed";

    private static final String FILTER_LOG_MESSAGE = "Collecting pipeline runs filtered by: {}";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(DataFactorySteps.class);

    @Mock private AzureProfile azureProfile;
    @Mock private TokenCredential tokenCredential;
    @Spy private final InnersJacksonAdapter innersJacksonAdapter = new InnersJacksonAdapter();
    @Mock private IBddVariableContext bddVariableContext;
    @Mock private ISoftAssert softAssert;

    @ParameterizedTest
    @ValueSource(strings = { SUCCEEDED, FAILED })
    void shouldRunSuccessfulPipeline(String runStatus) throws IOException
    {
        shouldRunPipeline(mock(PipelineRun.class), runStatus, runStatus, List.of());
    }

    @Test
    void shouldRunFailingPipeline() throws IOException
    {
        var errorMessage = "error message";
        PipelineRun finalPipelineRunState = mock(PipelineRun.class);
        when(finalPipelineRunState.message()).thenReturn(errorMessage);

        shouldRunPipeline(finalPipelineRunState, FAILED, SUCCEEDED,
                List.of(error("The pipeline run message: {}", errorMessage)));
    }

    @Test
    void shouldCollectPipelineRunsSuccessfully() throws IOException
    {
        shouldCollectPipelineRuns();
        assertThat(logger.getLoggingEvents(), is(List.of(info(FILTER_LOG_MESSAGE,
                "{\"lastUpdatedAfter\":\"2021-11-14T21:00:00Z\",\"lastUpdatedBefore\":\"2021-11-15T21:00:00Z\","
                        + "\"filters\":[{\"operand\":\"PipelineName\",\"operator\":\"Equals\","
                        + "\"values\":[\"pipelineName\"]}]}")))
        );
    }

    @Test
    void shouldCollectPipelineRunsWithErrorOnFiltersLogging() throws IOException
    {
        var ioException = new IOException("IO error");
        when(innersJacksonAdapter.serializeToJson(any())).thenThrow(ioException).thenCallRealMethod();
        shouldCollectPipelineRuns();
        assertThat(logger.getLoggingEvents(),
                is(List.of(info(FILTER_LOG_MESSAGE, "<unable to log filters: IO error>"))));
    }

    private void shouldRunPipeline(PipelineRun finalPipelineRunState, String finalRunStatus, String expectedRunStatus,
            List<LoggingEvent> extraLoggingEvents) throws IOException
    {
        String runId = "run-id";

        executeSteps((dataFactoryManager, steps) -> {
            var createRunResponse = mock(CreateRunResponse.class);
            when(createRunResponse.runId()).thenReturn(runId);

            var pipelines = mock(Pipelines.class);
            when(pipelines.createRun(RESOURCE_GROUP_NAME, FACTORY_NAME, PIPELINE_NAME)).thenReturn(createRunResponse);

            when(dataFactoryManager.pipelines()).thenReturn(pipelines);

            var firstRunStatus = "InProgress";
            var pipelineRun1 = mock(PipelineRun.class);
            when(pipelineRun1.status()).thenReturn(firstRunStatus);

            when(finalPipelineRunState.runEnd()).thenReturn(OffsetDateTime.now());
            when(finalPipelineRunState.status()).thenReturn(finalRunStatus);

            var pipelineRuns = mock(PipelineRuns.class);
            when(pipelineRuns.get(RESOURCE_GROUP_NAME, FACTORY_NAME, runId))
                    .thenReturn(pipelineRun1)
                    .thenReturn(finalPipelineRunState);

            when(dataFactoryManager.pipelineRuns()).thenReturn(pipelineRuns);

            when(softAssert.assertEquals("The pipeline run status", expectedRunStatus, finalRunStatus)).thenReturn(
                    expectedRunStatus.equals(finalRunStatus));

            List<LoggingEvent> loggingEvents = new ArrayList<>();
            loggingEvents.add(info("The ID of the created pipeline run is {}", runId));
            var currentStatusLogMessageFormat = "The current pipeline run status is \"{}\"";
            loggingEvents.add(info(currentStatusLogMessageFormat, firstRunStatus));
            loggingEvents.add(info(currentStatusLogMessageFormat, finalRunStatus));
            loggingEvents.addAll(extraLoggingEvents);

            steps.runPipeline(PIPELINE_NAME, FACTORY_NAME, RESOURCE_GROUP_NAME, WAIT_TIMEOUT, expectedRunStatus);

            assertThat(logger.getLoggingEvents(), is(loggingEvents));
        });
    }

    private void shouldCollectPipelineRuns() throws IOException
    {
        executeSteps((dataFactoryManager, steps) -> {
            var pipelineRunInner = new PipelineRunInner().withAdditionalProperties(Map.of("key", "PipelineRunInner"));

            var innerQueryResponse = mock(PipelineRunsQueryResponseInner.class);
            when(innerQueryResponse.value()).thenReturn(List.of(pipelineRunInner));

            var queryResponse = mock(PipelineRunsQueryResponse.class);
            when(queryResponse.innerModel()).thenReturn(innerQueryResponse);

            var pipelineRuns = mock(PipelineRuns.class);
            var runFilterParametersCaptor = ArgumentCaptor.forClass(RunFilterParameters.class);
            when(pipelineRuns.queryByFactory(eq(RESOURCE_GROUP_NAME), eq(FACTORY_NAME),
                    runFilterParametersCaptor.capture())).thenReturn(queryResponse);

            when(dataFactoryManager.pipelineRuns()).thenReturn(pipelineRuns);

            var filter1 = new RunFilter();
            filter1.setFilterType(RunFilterType.LAST_UPDATED_AFTER);
            filter1.setFilterValue(OffsetDateTime.parse("2021-11-15T00:00:00+03:00"));
            var filter2 = new RunFilter();
            filter2.setFilterType(RunFilterType.LAST_UPDATED_BEFORE);
            filter2.setFilterValue(OffsetDateTime.parse("2021-11-16T00:00:00+03:00"));

            var scopes = Set.of(VariableScope.SCENARIO);
            var variableName = "varName";
            steps.collectPipelineRuns(PIPELINE_NAME, List.of(filter1, filter2), FACTORY_NAME, RESOURCE_GROUP_NAME,
                    scopes, variableName);

            verify(bddVariableContext).putVariable(scopes, variableName, "[{\"key\":\"PipelineRunInner\"}]");

            var runFilterParameters = runFilterParametersCaptor.getValue();
            assertEquals(filter1.getFilterValue(), runFilterParameters.lastUpdatedAfter());
            assertEquals(filter2.getFilterValue(), runFilterParameters.lastUpdatedBefore());
            var runQueryFilters = runFilterParameters.filters();
            assertEquals(1, runQueryFilters.size());
            var runQueryFilter = runQueryFilters.get(0);
            assertEquals(RunQueryFilterOperand.PIPELINE_NAME, runQueryFilter.operand());
            assertEquals(RunQueryFilterOperator.EQUALS, runQueryFilter.operator());
            assertEquals(List.of(PIPELINE_NAME), runQueryFilter.values());
        });
    }

    private void executeSteps(FailableBiConsumer<DataFactoryManager, DataFactorySteps, IOException> consumer)
            throws IOException
    {
        try (MockedStatic<DataFactoryManager> dataFactoryManagerStaticMock = mockStatic(DataFactoryManager.class))
        {
            var dataFactoryManager = mock(DataFactoryManager.class);
            dataFactoryManagerStaticMock.when(() -> DataFactoryManager.authenticate(tokenCredential, azureProfile))
                    .thenReturn(dataFactoryManager);
            var steps = new DataFactorySteps(azureProfile, tokenCredential, innersJacksonAdapter, bddVariableContext,
                    softAssert);
            consumer.accept(dataFactoryManager, steps);
        }
    }
}
