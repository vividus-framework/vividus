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

package org.vividus.mobileapp.steps;

import java.nio.file.Paths;

import org.apache.commons.io.FilenameUtils;
import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.mobileapp.action.DeviceActions;

public class DeviceSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceSteps.class);

    private final DeviceActions deviceActions;
    private final String folderForFileUpload;

    public DeviceSteps(String folderForFileUpload, DeviceActions deviceActions)
    {
        this.folderForFileUpload = folderForFileUpload;
        this.deviceActions = deviceActions;
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
     * Deletes a file by <b>filePath</b> from the device
     * Example:
     * <br>
     * <code>
     * When I delete file /sdcard/myfile.txt from device
     * </code>
     *
     * @param filePath the path to an existing remote file on the device. This variable can be predefined with bundle
     * id to delete file inside an application bundle.
     * <p>See details for
     * <a href="https://github.com/appium/appium-xcuitest-driver#mobile-deletefile">iOS</a>,
     * <a href="https://github.com/appium/appium-uiautomator2-driver#mobile-deletefile">Android</a>
     */
    @When("I delete file `$filePath` from device")
    public void deleteFileFromDevice(String filePath)
    {
        deviceActions.deleteFile(filePath);
    }
}
