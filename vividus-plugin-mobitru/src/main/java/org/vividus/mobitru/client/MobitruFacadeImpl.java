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

import java.io.IOException;
import java.time.Duration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.function.FailableSupplier;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.spi.LoggingEventBuilder;
import org.vividus.mobitru.client.exception.MobitruDeviceSearchException;
import org.vividus.mobitru.client.exception.MobitruDeviceTakeException;
import org.vividus.mobitru.client.exception.MobitruOperationException;
import org.vividus.mobitru.client.model.Application;
import org.vividus.mobitru.client.model.Device;
import org.vividus.mobitru.client.model.DeviceSearchParameters;
import org.vividus.mobitru.client.model.ScreenRecording;
import org.vividus.util.wait.DurationBasedWaiter;
import org.vividus.util.wait.RetryTimesBasedWaiter;
import org.vividus.util.wait.WaitMode;
import org.vividus.util.wait.Waiter;

public class MobitruFacadeImpl implements MobitruFacade
{
    private static final Logger LOGGER = LoggerFactory.getLogger(MobitruFacadeImpl.class);

    private static final String UDID = "udid";
    private static final String APPIUM_UDID = "appium:udid";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static final int RETRY_TIMES = 20;
    private static final Duration DOWNLOAD_RECORDING_POOLING_TIMEOUT = Duration.ofSeconds(5);
    private static final int DOWNLOAD_RECORDING_RETRY_COUNT = 10;

    private final MobitruClient mobitruClient;
    private Duration waitForDeviceTimeout;

    public MobitruFacadeImpl(MobitruClient mobitruClient)
    {
        this.mobitruClient = mobitruClient;
    }

    @Override
    public String takeDevice(DesiredCapabilities desiredCapabilities) throws MobitruOperationException
    {
        if (isSearchForDevice(desiredCapabilities))
        {
            DeviceSearchParameters deviceSearchParameters = new DeviceSearchParameters(desiredCapabilities);
            List<Device> foundDevices = findDevices(deviceSearchParameters);
            return takeDevice(foundDevices);
        }

        Map<String, Object> capabilities = desiredCapabilities.asMap();
        Object deviceUdid = capabilities.getOrDefault(APPIUM_UDID, capabilities.get(UDID));
        if (deviceUdid != null)
        {
            //use different API in case if udid is provided in capabilities
            //it's required in some cases like if the device is already taken
            LOGGER.info("Trying to take device with udid {}", deviceUdid);
            return takeDevice(() -> mobitruClient.takeDeviceBySerial(String.valueOf(deviceUdid)),
                    () -> "Unable to take device with udid " + deviceUdid, getDefaultDeviceWaiter());
        }
        Device device = new Device();
        device.setDesiredCapabilities(desiredCapabilities.asMap());
        return takeDevice(device, getDefaultDeviceWaiter());
    }

    private Waiter getDefaultDeviceWaiter()
    {
        return new DurationBasedWaiter(new WaitMode(waitForDeviceTimeout, RETRY_TIMES));
    }

    @Override
    public void installApp(String deviceId, String appRealName, InstallApplicationOptions options)
            throws MobitruOperationException
    {
        mobitruClient.installApp(deviceId, findApp(appRealName), options);
    }

    @Override
    public void returnDevice(String deviceId) throws MobitruOperationException
    {
        mobitruClient.returnDevice(deviceId);
    }

    @Override
    public void startDeviceScreenRecording(String deviceId) throws MobitruOperationException
    {
        mobitruClient.startDeviceScreenRecording(deviceId);
    }

    @Override
    public String stopDeviceScreenRecording(String deviceId) throws MobitruOperationException
    {
        byte[] receivedRecordingInfo = mobitruClient.stopDeviceScreenRecording(deviceId);
        ScreenRecording screenRecordingInfo = performMapperOperation(mapper ->
                mapper.readValue(receivedRecordingInfo, ScreenRecording.class));
        return screenRecordingInfo.getRecordingId();
    }

    @Override
    public byte[] downloadDeviceScreenRecording(String recordingId) throws MobitruOperationException
    {
        Waiter waiter = new RetryTimesBasedWaiter(DOWNLOAD_RECORDING_POOLING_TIMEOUT,
                DOWNLOAD_RECORDING_RETRY_COUNT);
        Optional<byte[]> receivedRecording = performWaiterOperation(waiter,
                () -> mobitruClient.downloadDeviceScreenRecording(recordingId),
                LOGGER.atDebug(), "download device screen recording");
        return receivedRecording.orElseThrow(() -> new MobitruOperationException(
                String.format("Unable to download recording with id %s", recordingId)));
    }

    private String takeDevice(Device device, Waiter deviceWaiter) throws MobitruOperationException
    {
        LOGGER.info("Trying to take device with configuration {}", device);
        String capabilities = performMapperOperation(mapper -> mapper.writeValueAsString(device));
        return takeDevice(() -> mobitruClient.takeDevice(capabilities),
                () -> "Unable to take device with configuration " + capabilities, deviceWaiter);
    }

    private String takeDevice(FailableSupplier<byte[], MobitruOperationException> takeDeviceActions,
            Supplier<String> unableToTakeDeviceErrorMessage, Waiter deviceWaiter) throws MobitruOperationException
    {
        Optional<byte[]> receivedDevice = performWaiterOperation(deviceWaiter, takeDeviceActions,
                LOGGER.atWarn(), "take device");
        if (receivedDevice.isEmpty())
        {
            throw new MobitruDeviceTakeException(unableToTakeDeviceErrorMessage.get());
        }
        Device takenDevice = performMapperOperation(mapper -> mapper.readValue(receivedDevice.get(), Device.class));
        LOGGER.info("Device with configuration {} is taken", takenDevice);
        return (String) takenDevice.getDesiredCapabilities().get(UDID);
    }

    private String takeDevice(List<Device> devices) throws MobitruOperationException
    {
        Waiter waiter = new RetryTimesBasedWaiter(Duration.ZERO, 1);
        Iterator<Device> devicesIterator = devices.iterator();
        while (true)
        {
            try
            {
                return takeDevice(devicesIterator.next(), waiter);
            }
            catch (MobitruDeviceTakeException e)
            {
                if (!devicesIterator.hasNext())
                {
                    throw e;
                }
                LOGGER.atWarn().log(e::getMessage);
            }
        }
    }

    private List<Device> findDevices(DeviceSearchParameters deviceSearchConfiguration) throws MobitruOperationException
    {
        LOGGER.info("Trying to find devices using {}", deviceSearchConfiguration);
        Waiter waiter = new DurationBasedWaiter(new WaitMode(waitForDeviceTimeout, RETRY_TIMES));
        Optional<byte[]> receivedDevices = waiter.wait(() ->
        {
            try
            {
                return Optional.of(mobitruClient.findDevices(deviceSearchConfiguration.getPlatform(),
                        deviceSearchConfiguration.getParameters()));
            }
            catch (MobitruDeviceSearchException e)
            {
                LOGGER.warn("Unable to find devices, retrying attempt.", e);
                return Optional.empty();
            }
        }, Optional::isPresent);
        if (receivedDevices.isEmpty())
        {
            throw new MobitruOperationException(
                    String.format("Unable to find devices using %s", deviceSearchConfiguration));
        }
        List<Device> devices = performMapperOperation(
                mapper -> mapper.readerForListOf(Device.class).readValue(receivedDevices.get()));
        logFoundDevices(devices);
        return devices;
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

    private Optional<byte[]> performWaiterOperation(Waiter waiter,
                                                    FailableSupplier<byte[], MobitruOperationException> operation,
                                                    LoggingEventBuilder operationLoggerBuilder, String operationTitle)
    {
        return waiter.wait(() -> {
            try
            {
                return Optional.of(operation.get());
            }
            catch (MobitruOperationException e)
            {
                operationLoggerBuilder
                        .setCause(e)
                        .log("Unable to {}, retrying attempt", operationTitle);
                return Optional.empty();
            }
        }, Optional::isPresent);
    }

    private boolean isSearchForDevice(DesiredCapabilities desiredCapabilities)
    {
        Map<String, Object> capabilities = desiredCapabilities.asMap();
        boolean containsSearchCapabilities = capabilities.keySet().stream()
                .anyMatch(key -> key.startsWith("mobitru-device-search:"));
        if (containsSearchCapabilities)
        {
            Optional<String> conflictingCapability = Stream.of(UDID, APPIUM_UDID, "deviceName", "appium:deviceName")
                    .filter(capabilities::containsKey).findFirst();
            Validate.isTrue(conflictingCapability.isEmpty(),
                    "Conflicting capabilities are found. `%s` capability can not be specified along with "
                            + "`mobitru-device-search:` capabilities",
                    conflictingCapability.orElse(null));
        }
        return containsSearchCapabilities;
    }

    private void logFoundDevices(List<Device> devices)
    {
        LOGGER.atInfo().addArgument(() ->
        {
            StringBuilder devicesDescription = new StringBuilder();
            for (int i = 0; i < devices.size(); i++)
            {
                devicesDescription.append(System.lineSeparator()).append("Device ").append(i + 1).append(": ")
                        .append(devices.get(i));
            }
            return devicesDescription.toString();
        }).log("Found devices: {}");
    }

    public void setWaitForDeviceTimeout(Duration waitForDeviceTimeout)
    {
        this.waitForDeviceTimeout = waitForDeviceTimeout;
    }
}
