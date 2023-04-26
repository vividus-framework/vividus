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

package org.vividus.mobitru.selenium;

import java.util.Map;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.mobitru.client.MobitruFacade;
import org.vividus.mobitru.client.exception.MobitruOperationException;
import org.vividus.selenium.DesiredCapabilitiesAdjuster;

public class MobitruCapabilitiesAdjuster extends DesiredCapabilitiesAdjuster
{
    private static final String APPIUM_UDID = "appium:udid";
    private final MobitruFacade mobitruFacade;

    private String appFileName;

    public MobitruCapabilitiesAdjuster(MobitruFacade mobitruFacade)
    {
        this.mobitruFacade = mobitruFacade;
    }

    @Override
    public Map<String, Object> getExtraCapabilities(DesiredCapabilities desiredCapabilities)
    {
        String deviceId = null;
        try
        {
            deviceId = mobitruFacade.takeDevice(desiredCapabilities);
            mobitruFacade.installApp(deviceId, appFileName);
            Map<String, Object> capabilities = desiredCapabilities.asMap();
            if (capabilities.containsKey(APPIUM_UDID) || capabilities.containsKey("udid"))
            {
                return Map.of();
            }
            return Map.of(APPIUM_UDID, deviceId);
        }
        catch (MobitruOperationException outerException)
        {
            if (null != deviceId)
            {
                try
                {
                    mobitruFacade.returnDevice(deviceId);
                }
                catch (MobitruOperationException e)
                {
                    throw new IllegalStateException(e);
                }
            }
            throw new IllegalStateException(outerException);
        }
    }

    public void setAppFileName(String appFileName)
    {
        this.appFileName = appFileName;
    }
}
