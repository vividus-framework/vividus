/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.playwright.network;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Request;

import org.jbehave.core.annotations.AfterScenario;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.BeforeScenario;
import org.jbehave.core.annotations.BeforeStory;
import org.jbehave.core.model.Meta;
import org.vividus.context.RunContext;
import org.vividus.testcontext.TestContext;

public class NetworkContext
{
    private static final Class<Request> NETWORK_RECORDINGS_CONTEXT_KEY = Request.class;
    private static final String PROXY_META = "proxy";

    private final TestContext testContext;
    private final RunContext runContext;
    private final boolean recordingEnabledGlobally;

    private boolean networkRecordingEnabled;

    public NetworkContext(TestContext testContext, RunContext runContext, boolean recordingEnabledGlobally)
    {
        this.testContext = testContext;
        this.runContext = runContext;
        this.recordingEnabledGlobally = recordingEnabledGlobally;
    }

    public void listenNetwork(BrowserContext browserContext)
    {
        List<Request> networkRecordings = testContext.get(NETWORK_RECORDINGS_CONTEXT_KEY,
                () -> Collections.synchronizedList(new ArrayList<>()));

        browserContext.onRequest(r ->
        {
            if (networkRecordingEnabled)
            {
                networkRecordings.add(r);
            }
        });
    }

    public List<Request> getNetworkRecordings()
    {
        return testContext.get(NETWORK_RECORDINGS_CONTEXT_KEY);
    }

    @BeforeStory
    public void beforeStory()
    {
        if (isNetworkRecordingEnabledOnStoryLevel())
        {
            networkRecordingEnabled = true;
        }
    }

    @BeforeScenario
    public void beforeScenario()
    {
        if (!networkRecordingEnabled && isNetworkRecordingEnabledOnScenarioLevel())
        {
            networkRecordingEnabled = true;
        }
    }

    @AfterScenario
    public void afterScenario()
    {
        if (networkRecordingEnabled)
        {
            networkRecordingEnabled = false;
        }
    }

    @AfterStory
    public void afterStory()
    {
        if (networkRecordingEnabled)
        {
            networkRecordingEnabled = false;
        }
    }

    private boolean isRecordingEnabledGlobally()
    {
        return recordingEnabledGlobally;
    }

    private boolean isNetworkRecordingEnabledOnStoryLevel()
    {
        return isRecordingEnabledGlobally()
                || isProxyMetaTagContainedIn(runContext.getRunningStory().getStory().getMeta());
    }

    private boolean isNetworkRecordingEnabledOnScenarioLevel()
    {
        return isNetworkRecordingEnabledOnStoryLevel() || isProxyMetaTagContainedIn(
                runContext.getRunningStory().getRunningScenario().getScenario().getMeta());
    }

    private boolean isProxyMetaTagContainedIn(Meta meta)
    {
        return meta.hasProperty(PROXY_META);
    }
}
