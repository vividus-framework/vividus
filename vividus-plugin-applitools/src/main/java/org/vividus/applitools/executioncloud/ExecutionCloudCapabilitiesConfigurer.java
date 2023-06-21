/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.applitools.executioncloud;

import java.net.URI;
import java.util.Map;

import com.google.common.eventbus.Subscribe;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.context.RunContext;
import org.vividus.selenium.AbstractDesiredCapabilitiesConfigurer;
import org.vividus.selenium.event.WebDriverCreateEvent;

public class ExecutionCloudCapabilitiesConfigurer extends AbstractDesiredCapabilitiesConfigurer
{
    private static final String APPLITOOLS_OPTIONS = "applitools:options";

    private final URI serverUri;
    private final String apiKey;
    private final String appName;
    private final String batchName;

    protected ExecutionCloudCapabilitiesConfigurer(String apiKey, URI serverUri, String appName, String batchName,
            RunContext runContext)
    {
        super(runContext);
        this.apiKey = apiKey;
        this.serverUri = serverUri;
        this.appName = appName;
        this.batchName = batchName;
    }

    @Override
    public void configure(DesiredCapabilities desiredCapabilities)
    {
        putNestedCapability(desiredCapabilities, APPLITOOLS_OPTIONS, "eyesServerUrl", serverUri.toString());
        putNestedCapability(desiredCapabilities, APPLITOOLS_OPTIONS, "apiKey", apiKey);
    }

    @Subscribe
    public void onSessionStart(WebDriverCreateEvent event)
    {
        consumeTestName(testName ->
        {
            JavascriptExecutor executor = (JavascriptExecutor) event.getWebDriver();
            executor.executeScript("applitools:startTest", Map.of(
                "testName", testName,
                "appName", appName,
                "batch", Map.of("name", batchName)
            ));
        });
    }
}
