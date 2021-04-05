/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.bdd.mobileapp.steps;

import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.Validate;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.mobileapp.action.DeviceActions;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.GenericWebDriverManager;
import org.vividus.ui.action.JavascriptActions;

import io.appium.java_client.android.nativekey.AndroidKey;
import io.appium.java_client.android.nativekey.KeyEvent;
import io.appium.java_client.android.nativekey.PressesKey;

public class DeviceSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceSteps.class);

    private final DeviceActions deviceActions;
    private final String folderForFileUpload;
    private final GenericWebDriverManager genericWebDriverManager;
    private final JavascriptActions javascriptActions;
    private final IWebDriverProvider webDriverProvider;

    public DeviceSteps(String folderForFileUpload, DeviceActions deviceActions,
            GenericWebDriverManager genericWebDriverManager, JavascriptActions javascriptActions,
            IWebDriverProvider webDriverProvider)
    {
        this.folderForFileUpload = folderForFileUpload;
        this.deviceActions = deviceActions;
        this.genericWebDriverManager = genericWebDriverManager;
        this.javascriptActions = javascriptActions;
        this.webDriverProvider = webDriverProvider;
    }

    /**
     * Uploads a file by <b>filePath</b> to a device
     * @param filePath path of a file to upload
     */
    @When("I upload file `$filePath` to device")
    public void uploadFileToDevice(String filePath)
    {
        LOGGER.atInfo().addArgument(filePath)
                       .addArgument(folderForFileUpload)
                       .log("Uploading file '{}' to a device at '{}' folder");

        String fileName = FilenameUtils.getName(filePath);
        deviceActions.pushFile(Paths.get(folderForFileUpload, fileName).toString(), filePath);
    }

    /**
     * Presses the key
     * <br>
     * See <a href="https://github.com/appium/appium-xcuitest-driver#mobile-pressbutton">iOS keys</a> and
     * <a href="https://appium.github.io/java-client/io/appium/java_client/android/nativekey/AndroidKey.html">
     * Android keys</a> for available values
     * <br>
     * Example:
     * <br>
     * <code>
     * When I press $key key
     * </code>
     *
     * @param key the key to press
     */
    @When("I press $key key")
    public void pressKey(String key)
    {
        pressKeys(List.of(key));
    }

    /**
     * Presses the keys
     * <br>
     * See <a href="https://github.com/appium/appium-xcuitest-driver#mobile-pressbutton">iOS keys</a> and
     * <a href="https://appium.github.io/java-client/io/appium/java_client/android/nativekey/AndroidKey.html">
     * Android keys</a> for available values
     * <br>
     * Example:
     * <br>
     * <code>
     * When I press keys:
     * <br>
     * |key |
     * <br>
     * |Home|
     * </code>
     *
     * @param keys the keys to press
     */
    @When("I press keys:$keys")
    public void pressKeys(ExamplesTable keys)
    {
        pressKeys(keys.getColumn("key"));
    }

    private void pressKeys(List<String> keys)
    {
        if (genericWebDriverManager.isIOSNativeApp() || genericWebDriverManager.isTvOS())
        {
            keys.forEach(key -> javascriptActions.executeScript("mobile: pressButton", Map.of("name", key)));
        }
        else
        {
            PressesKey pressesKey = webDriverProvider.getUnwrapped(PressesKey.class);
            keys.stream()
                .map(key ->
                {
                    AndroidKey androidKey = EnumUtils.getEnumIgnoreCase(AndroidKey.class, key);
                    Validate.isTrue(androidKey != null, "Unsupported Android key: %s", key);
                    return androidKey;
                })
                .map(KeyEvent::new)
                .forEach(pressesKey::pressKey);
        }
    }
}
