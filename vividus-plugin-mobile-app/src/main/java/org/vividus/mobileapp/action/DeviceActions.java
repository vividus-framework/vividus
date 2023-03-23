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

package org.vividus.mobileapp.action;

import java.util.Base64;
import java.util.Map;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.ScreenOrientation;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.session.WebDriverSessionAttributes;
import org.vividus.selenium.session.WebDriverSessionInfo;

import io.appium.java_client.PullsFiles;
import io.appium.java_client.PushesFiles;
import io.appium.java_client.remote.SupportsRotation;

public class DeviceActions
{
    private final IWebDriverProvider webDriverProvider;
    private final WebDriverSessionInfo webDriverSessionInfo;

    public DeviceActions(IWebDriverProvider webDriverProvider, WebDriverSessionInfo webDriverSessionInfo)
    {
        this.webDriverProvider = webDriverProvider;
        this.webDriverSessionInfo = webDriverSessionInfo;
    }

    /**
     * Pushes a file to the device.
     * @param deviceFilePath The destination file path on a device.
     * @param content The content of the file to be pushed.
     */
    public void pushFile(String deviceFilePath, byte[] content)
    {
        byte[] base64Bytes = Base64.getEncoder().encode(content);
        webDriverProvider.getUnwrapped(PushesFiles.class).pushFile(deviceFilePath, base64Bytes);
    }

    /**
     * Deletes a file by <b>deviceFilePath</b> from the device
     * @param deviceFilePath the path to an existing remote file on the device. This variable can be predefined
     * with bundle id to delete file inside an application bundle. See details for
     * <a href="https://appium.github.io/appium-xcuitest-driver/4.19/execute-methods/#mobile-deletefile">iOS</a>,
     * <a href="https://github.com/appium/appium-uiautomator2-driver#mobile-deletefile">Android</a>
     */
    public void deleteFile(String deviceFilePath)
    {
        Map<String, Object> args = Map.of("remotePath", deviceFilePath);
        webDriverProvider.getUnwrapped(JavascriptExecutor.class).executeScript("mobile:deleteFile", args);
    }

    /**
     * Sets the current screen orientation
     * @param orientation The screen orientation
     */
    public void rotate(ScreenOrientation orientation)
    {
        try
        {
            webDriverProvider.getUnwrapped(SupportsRotation.class).rotate(orientation);
        }
        finally
        {
            webDriverSessionInfo.reset(WebDriverSessionAttributes.SCREEN_SIZE);
        }
    }

    /**
     * Pulls a file at <b>deviceFilePath</b> from the device
     * @param deviceFilePath The path to an existing remote file on the device. This parameter can be predefined
     * with bundle id, then the file will be pulled from the root of the corresponding application container.
     * See details for
     * <a href="https://appium.github.io/appium-xcuitest-driver/4.19/execute-methods/#mobile-pullfile">iOS</a>,
     * <a href="https://github.com/appium/appium-uiautomator2-driver#mobile-pullfile">Android</a>
     *
     * @return The content of the downloaded file.
     */
    public byte[] pullFile(String deviceFilePath)
    {
        return webDriverProvider.getUnwrapped(PullsFiles.class).pullFile(deviceFilePath);
    }
}
