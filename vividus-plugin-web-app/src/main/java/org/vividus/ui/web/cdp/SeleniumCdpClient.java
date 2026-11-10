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

package org.vividus.ui.web.cdp;

import static org.apache.commons.lang3.Validate.isTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.google.common.eventbus.EventBus;

import org.openqa.selenium.chromium.HasCdp;
import org.openqa.selenium.remote.Browser;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.ui.web.event.DeviceMetricsOverrideEvent;
import org.vividus.util.json.JsonUtils;

public class SeleniumCdpClient implements CdpClient
{
    private static final List<String> DEVICE_OVERRIDE_COMMANDS = Arrays.asList(
            "Emulation.clearDeviceMetricsOverride",
            "Emulation.setDeviceMetricsOverride"
    );
    private final IWebDriverProvider webDriverProvider;
    private final IWebDriverManager webDriverManager;
    private final JsonUtils jsonUtils;
    private final EventBus eventBus;

    public SeleniumCdpClient(IWebDriverProvider webDriverProvider, IWebDriverManager webDriverManager,
                             JsonUtils jsonUtils, EventBus eventBus)
    {
        this.webDriverProvider = webDriverProvider;
        this.webDriverManager = webDriverManager;
        this.jsonUtils = jsonUtils;
        this.eventBus = eventBus;
    }

    @Override
    public void executeCdpCommand(String command)
    {
        executeCdpCommand(command, Map.of());
    }

    @Override
    public void executeCdpCommand(String command, String argsAsJsonString)
    {
        executeCdpCommand(command, jsonUtils.toObject(argsAsJsonString, Map.class));
    }

    @Override
    public void executeCdpCommand(String command, Map<String, Object> parameters)
    {
        isTrue(webDriverManager.isBrowserAnyOf(Browser.CHROME), "The step is only supported by Chrome browser.");
        HasCdp hasCdp = webDriverProvider.getUnwrapped(HasCdp.class);
        hasCdp.executeCdpCommand(command, parameters);
        afterMetricsOverride(command);
    }

    private void afterMetricsOverride(String command)
    {
        if (DEVICE_OVERRIDE_COMMANDS.contains(command))
        {
            eventBus.post(new DeviceMetricsOverrideEvent());
        }
    }
}
