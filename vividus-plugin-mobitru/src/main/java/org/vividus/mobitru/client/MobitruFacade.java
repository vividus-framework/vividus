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

package org.vividus.mobitru.client;

import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.mobitru.client.exception.MobitruOperationException;
import org.vividus.mobitru.client.model.ScreenRecording;

public interface MobitruFacade
{
    /**
     * Takes the device in use depending on the provided capabilities set.
     *
     * @param desiredCapabilities The set of the Appium capabilities to request a device. The <b>platformName</b> is
     *                            mandatory, <b>deviceName</b>, <b>platformVersion</b>, and <b>udid</b> are optional.
     * @return The UDID of the taken device.
     * @throws MobitruOperationException In case of any issues during device reservation.
     */
    String takeDevice(DesiredCapabilities desiredCapabilities) throws MobitruOperationException;

    /**
     * Start the screen recording on the device with specified UDID.
     *
     * @param deviceId The UDID of the device
     * @throws MobitruOperationException In case of any issues during start the recording.
     */
    void startDeviceScreenRecording(String deviceId) throws MobitruOperationException;

    /**
     * Stop the screen recording on the device with specified UDID.
     *
     * @param deviceId The UDID of the device
     * @return The ID of the recording artifact.
     * @throws MobitruOperationException In case of any issues during start the recording.
     */
    ScreenRecording stopDeviceScreenRecording(String deviceId) throws MobitruOperationException;

    /**
     * Installs the desired application on the device with specified UDID
     *
     * @param deviceId    The UDID of the device
     * @param appRealName The application to install filename.
     * @param options     Application installation options.
     * @throws MobitruOperationException In case of any issues during application installation.
     */
    void installApp(String deviceId, String appRealName, InstallApplicationOptions options)
            throws MobitruOperationException;

    /**
     * Returns the device with specified UDID to the devices pool.
     *
     * @param deviceId The UDID of the device
     * @throws MobitruOperationException In case of any issues during device returning.
     */
    void returnDevice(String deviceId) throws MobitruOperationException;
}
