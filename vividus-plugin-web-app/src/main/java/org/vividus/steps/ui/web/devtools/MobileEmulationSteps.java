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

package org.vividus.steps.ui.web.devtools;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Map;

import com.google.common.eventbus.EventBus;

import org.jbehave.core.annotations.When;
import org.openqa.selenium.chromium.HasCdp;
import org.openqa.selenium.remote.Browser;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.ui.web.event.DeviceMetricsOverrideEvent;
import org.vividus.util.json.JsonUtils;

public class MobileEmulationSteps
{
    private final IWebDriverProvider webDriverProvider;
    private final IWebDriverManager webDriverManager;
    private final JsonUtils jsonUtils;
    private final EventBus eventBus;

    public MobileEmulationSteps(IWebDriverProvider webDriverProvider, IWebDriverManager webDriverManager,
            JsonUtils jsonUtils, EventBus eventBus)
    {
        this.webDriverProvider = webDriverProvider;
        this.webDriverManager = webDriverManager;
        this.jsonUtils = jsonUtils;
        this.eventBus = eventBus;
    }

    /**
     * Emulates mobile device using the provided configuration.
     *
     * <p>
     * <strong>The step is only supported by Chrome browser.</strong>
     * </p>
     *
     * @param jsonConfiguration The JSON containing device metrics to override.
     */
    @SuppressWarnings("unchecked")
    @When("I emulate mobile device with configuration:`$jsonConfiguration`")
    public void overrideDeviceMetrics(String jsonConfiguration)
    {
        executeCdpCommand("Emulation.setDeviceMetricsOverride", jsonUtils.toObject(jsonConfiguration, Map.class));
        eventBus.post(new DeviceMetricsOverrideEvent());
    }

    /**
     * Resets the mobile device emulation returning the browser to its initial state.
     *
     * <p>
     * <strong>The step is only supported by Chrome browser.</strong>
     * </p>
     */
    @When("I reset mobile device emulation")
    public void clearDeviceMetrics()
    {
        executeCdpCommand("Emulation.clearDeviceMetricsOverride", Map.of());
        eventBus.post(new DeviceMetricsOverrideEvent());
    }

    private void executeCdpCommand(String command, Map<String, Object> metrics)
    {
        isTrue(webDriverManager.isBrowserAnyOf(Browser.CHROME), "The step is only supported by Chrome browser.");
        HasCdp hasCdp = webDriverProvider.getUnwrapped(HasCdp.class);
        hasCdp.executeCdpCommand(command, metrics);
    }
}
