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

package org.vividus.mobileapp.steps;

import java.nio.file.Paths;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.ScreenOrientation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.context.VariableContext;
import org.vividus.mobileapp.action.DeviceActions;
import org.vividus.steps.DataWrapper;
import org.vividus.util.ResourceUtils;
import org.vividus.variable.VariableScope;

public class DeviceSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceSteps.class);

    private final DeviceActions deviceActions;
    private final String folderForFileUpload;
    private final VariableContext variableContext;

    public DeviceSteps(String folderForFileUpload, DeviceActions deviceActions, VariableContext variableContext)
    {
        this.folderForFileUpload = folderForFileUpload;
        this.deviceActions = deviceActions;
        this.variableContext = variableContext;
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
                       .log("Uploading file '{}' from resources to a device at '{}' folder");

        String fileName = FilenameUtils.getName(filePath);
        byte[] fileBytes = ResourceUtils.loadResourceAsByteArray(getClass(), filePath);
        uploadFileToDevice(fileName, fileBytes);
    }

    /**
     * Upload the file with data to the device.
     * @param data The data to store as a file.
     * @param fileName The name of the uploaded file.
     */
    @When("I upload file with name `$fileName` and data `$data` to device")
    public void uploadFileToDevice(String fileName, DataWrapper data)
    {
        LOGGER.info("Uploading file with name `{}` to the device at '{}' folder", fileName, folderForFileUpload);
        uploadFileToDevice(fileName, data.getBytes());
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
     * <a href="https://appium.github.io/appium-xcuitest-driver/4.19/execute-methods/#mobile-deletefile">iOS</a>,
     * <a href="https://github.com/appium/appium-uiautomator2-driver#mobile-deletefile">Android</a>
     */
    @When("I delete file `$filePath` from device")
    public void deleteFileFromDevice(String filePath)
    {
        deviceActions.deleteFile(filePath);
    }

    /**
     * Downloads a file at <b>filePath</b> from the device and save its content to <b>scopes</b> variables with name
     * <b>variableName</b>.<br>
     * Example:
     * <br>
     * <code>
     * When I download file `$filePath` from device and save its content to $scopes variable `$variableName`
     * </code>
     *
     * @param filePath The path to an existing remote file on the device. This parameter can be predefined
     * with bundle id, then the file will be pulled from the root of the corresponding application container.
     * See details for
     * <a href="https://appium.github.io/appium-xcuitest-driver/4.19/execute-methods/#mobile-pullfile">iOS</a>,
     * <a href="https://github.com/appium/appium-uiautomator2-driver#mobile-pullfile">Android</a>
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable scopes.<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName The variable name to store the file content.
     */
    @When("I download file `$filePath` from device and save its content to $scopes variable `$variableName`")
    public void downloadFileFromDevice(String filePath, Set<VariableScope> scopes, String variableName)
    {
        byte[] data = deviceActions.pullFile(filePath);
        variableContext.putVariable(scopes, variableName, data);
    }

    /**
     * Changes the device screen orientation to the specified value.
     *
     * @param orientation The device screen orientation, either <b>landscape</b> or <b>portrait</b>.
     */
    @When("I change device screen orientation to $orientation")
    public void changeDeviceScreenOrientation(ScreenOrientation orientation)
    {
        deviceActions.rotate(orientation);
    }

    private void uploadFileToDevice(String fileName, byte[] data)
    {
        String deviceFilePath = Paths.get(folderForFileUpload, fileName).toString();
        deviceActions.pushFile(deviceFilePath, data);
    }
}
