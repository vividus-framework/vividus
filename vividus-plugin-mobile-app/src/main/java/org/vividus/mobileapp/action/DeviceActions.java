/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.mobileapp.action;

import java.util.Base64;

import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;
import org.vividus.util.ResourceUtils;

public class DeviceActions
{
    private final IWebDriverProvider webDriverProvider;
    private final GenericWebDriverManager genericWebDriverManager;

    public DeviceActions(IWebDriverProvider webDriverProvider, GenericWebDriverManager genericWebDriverManager)
    {
        this.webDriverProvider = webDriverProvider;
        this.genericWebDriverManager = genericWebDriverManager;
    }

    /**
     * Pushes a <b>resource</b> onto a device at the <b>deviceFilePath</b> path
     * @param deviceFilePath destination file path on a device
     * @param resource resource to push
     */
    public void pushFile(String deviceFilePath, String resource)
    {
        byte[] fileBytes = ResourceUtils.loadResourceAsByteArray(getClass(), resource);
        byte[] base64Bytes = Base64.getEncoder().encode(fileBytes);

        if (genericWebDriverManager.isAndroidNativeApp())
        {
            webDriverProvider.getUnwrapped(io.appium.java_client.android.PushesFiles.class).pushFile(deviceFilePath,
                    base64Bytes);
        }
        else
        {
            webDriverProvider.getUnwrapped(io.appium.java_client.ios.PushesFiles.class).pushFile(deviceFilePath,
                    base64Bytes);
        }
    }
}
