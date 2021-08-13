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
import java.util.Map;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.microsoft.azure.AzureEnvironment;
import com.microsoft.azure.credentials.AzureTokenCredentials;
import com.microsoft.azure.management.datafactory.v2018_06_01.PipelineRun;
import com.microsoft.azure.management.datafactory.v2018_06_01.implementation.DataFactoryManager;

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

    public DataFactorySteps(AzureEnvironment environment, String tenantId, String subscriptionId,
            TokenCredential tokenCredential, ISoftAssert softAssert)
    {
        AzureTokenCredentials azureTokenCredentials = new AzureTokenCredentials(environment, tenantId)
        {
            @Override
            public String getToken(String resource)
            {
                return tokenCredential.getToken(new TokenRequestContext().addScopes(resource + "/.default"))
                        .map(AccessToken::getToken)
                        .block();
            }
        };
        this.dataFactoryManager = DataFactoryManager.configure().authenticate(azureTokenCredentials, subscriptionId);
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
     */
    @When("I run pipeline `$pipelineName` in Data Factory `$factoryName` from resource group `$resourceGroupName`"
            + " with wait timeout `$waitTimeout`")
    public void runPipeline(String pipelineName, String factoryName, String resourceGroupName, Duration waitTimeout)
    {
        String runId = dataFactoryManager
                .pipelines()
                .inner()
                .createRunWithServiceResponseAsync(resourceGroupName, factoryName, pipelineName, null, null, null, null,
                        Map.of())
                .toBlocking()
                .single()
                .body()
                .runId();
        LOGGER.info("The ID of the created pipeline run is {}", runId);
        PipelineRun pipelineRun = new DurationBasedWaiter(new WaitMode(waitTimeout, RETRY_TIMES)).wait(
                () -> getPipelineRun(resourceGroupName, factoryName, runId),
                run -> run.runEnd() != null);
        if (!softAssert.assertEquals("The pipeline run status", "Succeeded", pipelineRun.status()))
        {
            LOGGER.atError().addArgument(pipelineRun::message).log("The pipeline run message: {}");
        }
    }

    private PipelineRun getPipelineRun(String resourceGroupName, String factoryName, String runId)
    {
        PipelineRun pipelineRun = dataFactoryManager
                .pipelineRuns()
                .getAsync(resourceGroupName, factoryName, runId)
                .toBlocking()
                .single();
        LOGGER.atInfo().addArgument(pipelineRun::status).log("The current pipeline run status is \"{}\"");
        return pipelineRun;
    }
}
