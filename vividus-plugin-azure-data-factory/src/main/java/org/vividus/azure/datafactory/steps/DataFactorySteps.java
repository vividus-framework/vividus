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

import java.time.Duration;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.datafactory.DataFactoryManager;
import com.azure.resourcemanager.datafactory.models.PipelineRun;

import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.wait.DurationBasedWaiter;
import org.vividus.util.wait.WaitMode;

public class DataFactorySteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DataFactorySteps.class);
    private static final int RETRY_TIMES = 10;

    private final DataFactoryManager dataFactoryManager;
    private final ISoftAssert softAssert;

    public DataFactorySteps(AzureProfile azureProfile, TokenCredential tokenCredential, ISoftAssert softAssert)
    {
        this.dataFactoryManager = DataFactoryManager.authenticate(tokenCredential, azureProfile);
        this.softAssert = softAssert;
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

    private PipelineRun getPipelineRun(String resourceGroupName, String factoryName, String runId)
    {
        PipelineRun pipelineRun = dataFactoryManager.pipelineRuns().get(resourceGroupName, factoryName, runId);
        LOGGER.atInfo().addArgument(pipelineRun::status).log("The current pipeline run status is \"{}\"");
        return pipelineRun;
    }
}
