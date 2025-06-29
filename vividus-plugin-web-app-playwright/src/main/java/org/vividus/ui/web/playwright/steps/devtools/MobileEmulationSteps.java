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

package org.vividus.ui.web.playwright.steps.devtools;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.microsoft.playwright.Page;

import org.jbehave.core.annotations.When;
import org.vividus.ui.web.playwright.BrowserContextProvider;
import org.vividus.ui.web.playwright.UiContext;

public class MobileEmulationSteps
{
    private static final Gson GSON = new Gson();

    private final BrowserContextProvider browserContextProvider;
    private final UiContext uiContext;

    public MobileEmulationSteps(BrowserContextProvider browserContextProvider, UiContext uiContext)
    {
        this.browserContextProvider = browserContextProvider;
        this.uiContext = uiContext;
    }

    /**
     * Emulates mobile device using the provided configuration.
     * <p>
     * <strong>The step is only supported by Chrome browser.</strong>
     * </p>
     * @param jsonConfiguration The JSON containing device metrics to override.
     */
    @When("I emulate mobile device with configuration:`$jsonConfiguration`")
    public void overrideDeviceMetrics(String jsonConfiguration)
    {
        JsonObject args = GSON.fromJson(jsonConfiguration, JsonObject.class);
        Page page = uiContext.getCurrentPage();
        browserContextProvider.getCdpSession(page).send("Emulation.setDeviceMetricsOverride", args);
    }

    /**
     * Resets the mobile device emulation returning the browser to its initial state.
     * <p>
     * <strong>The step is only supported by Chrome browser.</strong>
     * </p>
     */
    @When("I reset mobile device emulation")
    public void clearDeviceMetrics()
    {
        Page page = uiContext.getCurrentPage();
        browserContextProvider.getCdpSession(page).send("Emulation.clearDeviceMetricsOverride");
    }
}
