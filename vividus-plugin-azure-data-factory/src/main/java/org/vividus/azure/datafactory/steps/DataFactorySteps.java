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

import java.io.IOException;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.datafactory.DataFactoryManager;
import com.azure.resourcemanager.datafactory.fluent.models.PipelineRunInner;
import com.azure.resourcemanager.datafactory.models.PipelineRun;
import com.azure.resourcemanager.datafactory.models.RunFilterParameters;
import com.azure.resourcemanager.datafactory.models.RunQueryFilter;
import com.azure.resourcemanager.datafactory.models.RunQueryFilterOperand;
import com.azure.resourcemanager.datafactory.models.RunQueryFilterOperator;

import org.jbehave.core.annotations.AsParameters;
import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.azure.util.InnersJacksonAdapter;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.wait.DurationBasedWaiter;
import org.vividus.util.wait.WaitMode;

public class DataFactorySteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DataFactorySteps.class);
    private static final int RETRY_TIMES = 10;

    private final DataFactoryManager dataFactoryManager;
    private final InnersJacksonAdapter innersJacksonAdapter;
    private final IBddVariableContext bddVariableContext;
    private final ISoftAssert softAssert;

    public DataFactorySteps(AzureProfile azureProfile, TokenCredential tokenCredential,
            InnersJacksonAdapter innersJacksonAdapter, IBddVariableContext bddVariableContext, ISoftAssert softAssert)
    {
        this.dataFactoryManager = DataFactoryManager.authenticate(tokenCredential, azureProfile);
        this.softAssert = softAssert;
        this.innersJacksonAdapter = innersJacksonAdapter;
        this.bddVariableContext = bddVariableContext;
    }

    /**
     * Creates a run of a pipeline in Data Factory, waits for its completion or until the timeout is reached and
     * validates the run status is "Succeeded".
     *
     * @param pipelineName      The name of the pipeline to run.
     * @param factoryName       The name of the factory.
     * @param resourceGroupName The name of the resource group of the factory.
     * @param waitTimeout       The maximum duration of time to wait for the pipeline completion.
     * @deprecated Use step:
     * When I run pipeline `$pipelineName` in Data Factory `$factoryName` from resource group `$resourceGroupName`
     * with wait timeout `$waitTimeout` and expect run status to be equal to `$expectedPipelineRunStatus`
     */
    @When("I run pipeline `$pipelineName` in Data Factory `$factoryName` from resource group `$resourceGroupName`"
            + " with wait timeout `$waitTimeout`")
    @Deprecated(since = "0.3.8", forRemoval = true)
    public void runPipeline(String pipelineName, String factoryName, String resourceGroupName, Duration waitTimeout)
    {
        LOGGER.warn(
                "This step is deprecated and will be removed in VIVIDUS 0.4.0. The replacement is \"When I run "
                        + "pipeline `$pipelineName` in Data Factory `$factoryName` from resource group "
                        + "`$resourceGroupName` with wait timeout `$waitTimeout` and expect run status to be equal to"
                        + " `$expectedPipelineRunStatus`\"");
        runPipeline(pipelineName, factoryName, resourceGroupName, waitTimeout, "Succeeded");
    }

    /**
     * Creates a run of a pipeline in Data Factory, waits for its completion or until the timeout is reached and
     * validates the run status is equal to the expected one.
     *
     * @param pipelineName              The name of the pipeline to run.
     * @param factoryName               The name of the factory.
     * @param resourceGroupName         The name of the resource group of the factory.
     * @param waitTimeout               The maximum duration of time to wait for the pipeline completion.
     * @param expectedPipelineRunStatus The expected pipeline run status, e.g. Succeeded
     */
    @When(value = "I run pipeline `$pipelineName` in Data Factory `$factoryName` from resource group "
            + "`$resourceGroupName` with wait timeout `$waitTimeout` and expect run status to be equal to "
            + "`$expectedPipelineRunStatus`", priority = 1)
    public void runPipeline(String pipelineName, String factoryName, String resourceGroupName, Duration waitTimeout,
            String expectedPipelineRunStatus)
    {
        String runId = dataFactoryManager.pipelines().createRun(resourceGroupName, factoryName, pipelineName).runId();
        LOGGER.info("The ID of the created pipeline run is {}", runId);
        PipelineRun pipelineRun = new DurationBasedWaiter(new WaitMode(waitTimeout, RETRY_TIMES)).wait(
                () -> getPipelineRun(resourceGroupName, factoryName, runId),
                run -> run.runEnd() != null);
        if (!softAssert.assertEquals("The pipeline run status", expectedPipelineRunStatus, pipelineRun.status()))
        {
            LOGGER.atError().addArgument(pipelineRun::message).log("The pipeline run message: {}");
        }
    }

    /**
     * Collects pipeline runs in Data factory based on input filter conditions.
     *
     * @param pipelineName      The name of the pipeline to find runs.
     * @param filters           The ExamplesTable with filters to be applied to the pipeline runs to limit the
     *                          resulting set. The supported filter types are:
     *                          <ul>
     *                          <li><code>LAST_UPDATED_AFTER</code> - the time at or after which the run event was
     *                          updated in ISO-8601 format.</li>
     *                          <li><code>LAST_UPDATED_BEFORE</code> - the time at or before which the run event was
     *                          updated in ISO-8601 format.</li>
     *                          </ul>
     *                          The filters can be combined in any order and in any composition, e.g.<br>
     *                          <code>
     *                          |filterType         |filterValue              |<br>
     *                          |last updated after |2021-11-15T00:00:00+03:00|<br>
     *                          |last updated before|2021-11-15T00:00:00+03:00|<br>
     *                          </code>
     * @param factoryName       The name of the factory.
     * @param resourceGroupName The name of the resource group of the factory.
     * @param scopes            The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                          scopes.<br>
     *                          <i>Available scopes:</i>
     *                          <ul>
     *                          <li><b>STEP</b> - the variable will be available only within the step,
     *                          <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                          <li><b>STORY</b> - the variable will be available within the whole story,
     *                          <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                          </ul>
     * @param variableName      The variable name to store the pipeline runs in JSON format.
     * @throws IOException if an I/O error occurs
     */
    @When("I collect runs of pipeline `$pipelineName` filtered by:$filters in Data Factory `$factoryName` from "
            + "resource group `$resourceGroupName` and save them as JSON to $scopes variable `$variableName`")
    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    public void collectPipelineRuns(String pipelineName, List<RunFilter> filters, String factoryName,
            String resourceGroupName, Set<VariableScope> scopes, String variableName) throws IOException
    {
        RunFilterParameters filterParameters = new RunFilterParameters()
                .withFilters(
                        List.of(
                                new RunQueryFilter()
                                        .withOperand(RunQueryFilterOperand.PIPELINE_NAME)
                                        .withOperator(RunQueryFilterOperator.EQUALS)
                                        .withValues(List.of(pipelineName))
                        )
                );
        filters.forEach(filter -> filter.getFilterType().addFilter(filterParameters, filter.getFilterValue()));
        LOGGER.atInfo().addArgument(() -> {
            try
            {
                return innersJacksonAdapter.serializeToJson(filterParameters);
            }
            catch (IOException e)
            {
                return "<unable to log filters: " + e.getMessage() + ">";
            }
        }).log("Collecting pipeline runs filtered by: {}");
        List<PipelineRunInner> runs = dataFactoryManager.pipelineRuns().queryByFactory(resourceGroupName, factoryName,
                filterParameters).innerModel().value();
        bddVariableContext.putVariable(scopes, variableName, innersJacksonAdapter.serializeToJson(runs));
    }

    private PipelineRun getPipelineRun(String resourceGroupName, String factoryName, String runId)
    {
        PipelineRun pipelineRun = dataFactoryManager.pipelineRuns().get(resourceGroupName, factoryName, runId);
        LOGGER.atInfo().addArgument(pipelineRun::status).log("The current pipeline run status is \"{}\"");
        return pipelineRun;
    }

    @AsParameters
    public static class RunFilter
    {
        private RunFilterType filterType;
        private OffsetDateTime filterValue;

        public RunFilterType getFilterType()
        {
            return filterType;
        }

        public void setFilterType(RunFilterType filterType)
        {
            this.filterType = filterType;
        }

        public OffsetDateTime getFilterValue()
        {
            return filterValue;
        }

        public void setFilterValue(OffsetDateTime filterValue)
        {
            this.filterValue = filterValue;
        }
    }

    public enum RunFilterType
    {
        LAST_UPDATED_AFTER(RunFilterParameters::withLastUpdatedAfter),
        LAST_UPDATED_BEFORE(RunFilterParameters::withLastUpdatedBefore);

        private final BiConsumer<RunFilterParameters, OffsetDateTime> filterSetter;

        RunFilterType(BiConsumer<RunFilterParameters, OffsetDateTime> filterSetter)
        {
            this.filterSetter = filterSetter;
        }

        public void addFilter(RunFilterParameters filterParameters, OffsetDateTime filterValue)
        {
            filterSetter.accept(filterParameters, filterValue);
        }
    }
}
