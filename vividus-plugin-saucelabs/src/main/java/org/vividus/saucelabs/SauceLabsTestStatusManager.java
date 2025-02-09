/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.saucelabs;

import java.io.IOException;
import java.time.Duration;

import com.google.common.eventbus.Subscribe;
import com.saucelabs.saucerest.SauceREST;
import com.saucelabs.saucerest.model.jobs.Job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.cloud.AbstractCloudTestStatusManager;
import org.vividus.selenium.cloud.CloudTestStatusMapping;
import org.vividus.selenium.event.AfterWebDriverQuitEvent;
import org.vividus.testcontext.TestContext;
import org.vividus.ui.action.JavascriptActions;
import org.vividus.util.wait.DurationBasedWaiter;

public class SauceLabsTestStatusManager extends AbstractCloudTestStatusManager
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SauceLabsTestStatusManager.class);

    private final JavascriptActions javascriptActions;
    private final SauceREST sauceRestClient;
    private final DurationBasedWaiter sessionCompletionWaiter;

    public SauceLabsTestStatusManager(IWebDriverProvider webDriverProvider, TestContext testContext,
            JavascriptActions javascriptActions, SauceREST sauceRestClient, DurationBasedWaiter sessionCompletionWaiter)
    {
        super(new CloudTestStatusMapping("passed", "failed"), webDriverProvider, testContext);
        this.javascriptActions = javascriptActions;
        this.sauceRestClient = sauceRestClient;
        this.sessionCompletionWaiter = sessionCompletionWaiter;
    }

    @Override
    protected void updateCloudTestStatus(String sessionId, String status) throws UpdateCloudTestStatusException
    {
        javascriptActions.executeScript("sauce:job-result=" + status);
    }

    @Subscribe
    public final void waitForSessionCompletion(AfterWebDriverQuitEvent event)
    {
        String sessionId = event.sessionId();
        boolean completed = sessionCompletionWaiter.wait(() -> isTestExecutionCompleted(sessionId));
        if (!completed)
        {
            LOGGER.atError()
                    .addArgument(() -> Duration.ofMillis(sessionCompletionWaiter.getDurationInMillis()).toSeconds())
                    .addArgument(sessionId)
                    .log("Timeout occurred after waiting {} seconds for the SauceLabs job with ID '{}' to complete");
        }
    }

    private boolean isTestExecutionCompleted(String sessionId)
    {
        try
        {
            Job jobDetails = sauceRestClient.getJobsEndpoint().getJobDetails(sessionId);
            String jobStatus = jobDetails.status;
            LOGGER.debug("Execution status of the SauceLabs job with ID '{}' is {}", sessionId, jobStatus);
            return "complete".equals(jobStatus);
        }
        catch (IOException e)
        {
            LOGGER.error("Unable to get the status of the SauceLabs job  with ID '{}'", sessionId, e);
        }
        return false;
    }
}
