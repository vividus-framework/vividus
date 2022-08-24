/*
 * Copyright 2019-2022 the original author or authors.
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
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.util.ResourceUtils;

import io.appium.java_client.PushesFiles;

public class DeviceActions
{
    private final IWebDriverProvider webDriverProvider;

    public DeviceActions(IWebDriverProvider webDriverProvider)
    {
        this.webDriverProvider = webDriverProvider;
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

        webDriverProvider.getUnwrapped(PushesFiles.class).pushFile(deviceFilePath, base64Bytes);
    }

    /**
     * Deletes a file by <b>deviceFilePath</b> from the device
     * @param deviceFilePath the path to an existing remote file on the device. This variable can be predefined
     * with bundle id to delete file inside an application bundle. See details for
     * <a href="https://github.com/appium/appium-xcuitest-driver#mobile-deletefile">iOS</a>,
     * <a href="https://github.com/appium/appium-uiautomator2-driver#mobile-deletefile">Android</a>
     */
    public void deleteFile(String deviceFilePath)
    {
        Map<String, Object> args = Map.of("remotePath", deviceFilePath);
        webDriverProvider.getUnwrapped(JavascriptExecutor.class).executeScript("mobile:deleteFile", args);
    }
}
