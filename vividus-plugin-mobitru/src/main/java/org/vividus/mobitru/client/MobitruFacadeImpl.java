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

package org.vividus.mobitru.client;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.function.FailableFunction;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.mobitru.client.model.Application;
import org.vividus.mobitru.client.model.Device;
import org.vividus.util.wait.DurationBasedWaiter;
import org.vividus.util.wait.WaitMode;
import org.vividus.util.wait.Waiter;

public class MobitruFacadeImpl implements MobitruFacade
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MobitruFacadeImpl.class);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static final int RETRY_TIMES = 20;

    private final MobitruClient mobitruClient;
    private Duration waitForDeviceTimeout;

    public MobitruFacadeImpl(MobitruClient mobitruClient)
    {
        this.mobitruClient = mobitruClient;
    }

    @Override
    public String takeDevice(DesiredCapabilities desiredCapabilities) throws MobitruOperationException
    {
        Waiter waiter = new DurationBasedWaiter(new WaitMode(waitForDeviceTimeout, RETRY_TIMES));
        Device requestedDevice = new Device();
        requestedDevice.setDesiredCapabilities(desiredCapabilities.asMap());
        String capabilities = performMapperOperation(mapper -> mapper.writeValueAsString(requestedDevice));
        byte[] receivedDevice = waiter.wait(() -> {
            try
            {
                return mobitruClient.takeDevice(capabilities);
            }
            catch (MobitruDeviceSearchException e)
            {
                LOGGER.warn("Unable to take device, retrying attempt.", e);
                return null;
            }
        }, Objects::nonNull);
        if (null == receivedDevice)
        {
            throw new MobitruOperationException(String.format("Unable to find device using %s", desiredCapabilities));
        }
        Device device = performMapperOperation(mapper -> mapper.readValue(receivedDevice, Device.class));
        return (String) device.getDesiredCapabilities().get("udid");
    }

    @Override
    public void installApp(String deviceId, String appRealName) throws MobitruOperationException
    {
        mobitruClient.installApp(deviceId, findApp(appRealName));
    }

    @Override
    public void returnDevice(String deviceId) throws MobitruOperationException
    {
        mobitruClient.returnDevice(deviceId);
    }

    private String findApp(String appRealName) throws MobitruOperationException
    {
        byte[] appsResponse = mobitruClient.getArtifacts();
        List<Application> applications = performMapperOperation(mapper -> mapper.readerForListOf(Application.class)
            .readValue(appsResponse));
        return applications.stream().filter(a -> appRealName.equals(a.getRealName())).findFirst().orElseThrow(
                () -> new MobitruOperationException(String.format("Unable to find application with the name `%s`."
                                + " The available applications are: %s",
                        appRealName, applications))).getId();
    }

    private <R> R performMapperOperation(FailableFunction<ObjectMapper, R, IOException> operation)
            throws MobitruOperationException
    {
        try
        {
            return operation.apply(OBJECT_MAPPER);
        }
        catch (IOException e)
        {
            throw new MobitruOperationException(e);
        }
    }

    public void setWaitForDeviceTimeout(Duration waitForDeviceTimeout)
    {
        this.waitForDeviceTimeout = waitForDeviceTimeout;
    }
}
