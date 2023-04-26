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

package org.vividus.mobitru.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.vividus.mobitru.client.exception.MobitruDeviceSearchException;
import org.vividus.mobitru.client.exception.MobitruDeviceTakeException;
import org.vividus.mobitru.client.exception.MobitruOperationException;
import org.vividus.mobitru.client.model.Device;
import org.vividus.mobitru.client.model.DeviceSearchParameters;

@ExtendWith({ TestLoggerFactoryExtension.class, MockitoExtension.class })
class MobitruFacadeImplTests
{
    private static final String DEVICE_TAKEN_MESSAGE = "Device with configuration {} is taken";
    private static final String TRYING_TO_TAKE_DEVICE_MESSAGE = "Trying to take device with configuration {}";
    private static final String RETRY_TO_TAKE_DEVICE_MESSAGE = "Unable to take device, retrying attempt.";
    private static final String UNABLE_TO_TAKE_DEVICE_ERROR_FORMAT = "Unable to take device with configuration %s";
    private static final String UDID = "Z3CT103D2DZ";
    private static final String DEVICE_TYPE_CAPABILITY_NAME = "mobitru-device-search:type";
    private static final String PHONE = "phone";
    private static final String PLATFORM_NAME = "platformName";
    private static final String IOS = "ios";
    private static final String ANDROID = "android";
    private static final String NO_DEVICE = "no device";
    private static final String CAPABILITIES_JSON_PREFIX = "{\"desiredCapabilities\":{\"platformName\":\"Android\",";
    private static final String CAPABILITIES_JSON = CAPABILITIES_JSON_PREFIX
            + "\"platformVersion\":\"12\",\"deviceName\":\"SAMSUNG SM-G998B\",\"udid\":\"Z3CT103D2DZ\"}}";
    private static final Map<String, String> DEVICE_SEARCH_PARAMETERS = Map.of("type", PHONE);

    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(MobitruFacadeImpl.class);

    @Mock private MobitruClient mobitruClient;

    @InjectMocks private MobitruFacadeImpl mobitruFacadeImpl;

    @Test
    void shouldFindDeviceThenTakeItAndProvideItsUdid() throws MobitruOperationException
    {
        mobitruFacadeImpl.setWaitForDeviceTimeout(Duration.ofSeconds(20));
        var deviceSearchException = new MobitruDeviceSearchException(NO_DEVICE);
        var deviceTakeException = new MobitruDeviceTakeException(NO_DEVICE);
        var failedToTakeDeviceCapabilitiesJson = CAPABILITIES_JSON_PREFIX + "\"platformVersion\":\"13\","
                + "\"deviceName\":\"GOOGLE PIXEL 6\",\"udid\":\"777\"}}";
        var desiredCapabilities = new DesiredCapabilities(
                Map.of(PLATFORM_NAME, ANDROID, DEVICE_TYPE_CAPABILITY_NAME, PHONE));
        when(mobitruClient.findDevices(ANDROID, DEVICE_SEARCH_PARAMETERS)).thenThrow(deviceSearchException)
                .thenReturn(('[' + failedToTakeDeviceCapabilitiesJson + "," + CAPABILITIES_JSON + ']')
                        .getBytes(StandardCharsets.UTF_8));
        when(mobitruClient.takeDevice(failedToTakeDeviceCapabilitiesJson)).thenThrow(deviceTakeException);
        when(mobitruClient.takeDevice(CAPABILITIES_JSON))
                .thenReturn(CAPABILITIES_JSON.getBytes(StandardCharsets.UTF_8));
        assertEquals(UDID, mobitruFacadeImpl.takeDevice(desiredCapabilities));
        var failedTakeDevice = createDevice("13", "GOOGLE PIXEL 6", "777");
        var takenDevice = getTestDevice();
        var searchParameters = new DeviceSearchParameters(desiredCapabilities);
        assertEquals(List.of(LoggingEvent.info("Trying to find devices using {}", searchParameters),
                LoggingEvent.warn(deviceSearchException, "Unable to find devices, retrying attempt."),
                LoggingEvent.info("Found devices: {}", System.lineSeparator() + "Device 1: " + failedTakeDevice
                        + System.lineSeparator() + "Device 2: " + takenDevice),
                LoggingEvent.info(TRYING_TO_TAKE_DEVICE_MESSAGE, failedTakeDevice),
                LoggingEvent.warn(deviceTakeException, RETRY_TO_TAKE_DEVICE_MESSAGE),
                LoggingEvent.warn(String.format(UNABLE_TO_TAKE_DEVICE_ERROR_FORMAT, failedTakeDevice)),
                LoggingEvent.info(TRYING_TO_TAKE_DEVICE_MESSAGE, takenDevice),
                LoggingEvent.info(DEVICE_TAKEN_MESSAGE, takenDevice)), LOGGER.getLoggingEvents());
    }

    @Test
    void shouldTakeDeviceInUseAndProvideItsUdId() throws MobitruOperationException
    {
        mobitruFacadeImpl.setWaitForDeviceTimeout(Duration.ofSeconds(20));
        var exception = new MobitruDeviceTakeException(NO_DEVICE);
        var desiredCapabilities = new DesiredCapabilities(Map.of(PLATFORM_NAME, ANDROID));
        var requestedDevice = new Device();
        requestedDevice.setDesiredCapabilities(desiredCapabilities.asMap());
        when(mobitruClient.takeDevice("{\"desiredCapabilities\":{\"platformName\":\"ANDROID\"}}"))
                .thenThrow(exception)
                .thenReturn(CAPABILITIES_JSON.getBytes(StandardCharsets.UTF_8));
        assertEquals(UDID,
            mobitruFacadeImpl.takeDevice(desiredCapabilities));
        assertEquals(List.of(LoggingEvent.info(TRYING_TO_TAKE_DEVICE_MESSAGE, requestedDevice),
                        LoggingEvent.warn(exception, RETRY_TO_TAKE_DEVICE_MESSAGE),
                        LoggingEvent.info(DEVICE_TAKEN_MESSAGE, getTestDevice())), LOGGER.getLoggingEvents());
    }

    @ParameterizedTest
    @ValueSource(strings = { "appium:udid", "udid", "deviceName", "appium:deviceName" })
    void shouldThrowExceptionIfConflictingCapabilities(String capabilityName)
    {
        mobitruFacadeImpl.setWaitForDeviceTimeout(Duration.ofSeconds(20));
        var capabilities = new DesiredCapabilities(Map.of(capabilityName, "value", DEVICE_TYPE_CAPABILITY_NAME, PHONE));
        var exception = assertThrows(IllegalArgumentException.class, () -> mobitruFacadeImpl.takeDevice(capabilities));
        assertEquals(String.format("Conflicting capabilities are found. `%s` capability can not be specified along"
                + " with `mobitru-device-search:` capabilities", capabilityName), exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfNoDeviceFound() throws MobitruOperationException
    {
        mobitruFacadeImpl.setWaitForDeviceTimeout(Duration.ofSeconds(1));
        when(mobitruClient.findDevices(IOS, DEVICE_SEARCH_PARAMETERS))
                .thenThrow(new MobitruDeviceSearchException(NO_DEVICE));
        var capabilities = new DesiredCapabilities(Map.of(PLATFORM_NAME, IOS, DEVICE_TYPE_CAPABILITY_NAME, PHONE));
        var exception = assertThrows(MobitruOperationException.class,
                () -> mobitruFacadeImpl.takeDevice(capabilities));
        assertEquals("Unable to find devices using platform = ios, type = phone", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfFoundDeviceNotTaken() throws MobitruOperationException
    {
        mobitruFacadeImpl.setWaitForDeviceTimeout(Duration.ofSeconds(1));
        when(mobitruClient.findDevices(ANDROID, DEVICE_SEARCH_PARAMETERS))
                .thenReturn(('[' + CAPABILITIES_JSON + ']').getBytes(StandardCharsets.UTF_8));
        when(mobitruClient.takeDevice(CAPABILITIES_JSON))
                .thenThrow(new MobitruDeviceTakeException(NO_DEVICE));
        var capabilities = new DesiredCapabilities(Map.of(PLATFORM_NAME, ANDROID, DEVICE_TYPE_CAPABILITY_NAME, PHONE));
        var exception = assertThrows(MobitruDeviceTakeException.class,
                () -> mobitruFacadeImpl.takeDevice(capabilities));
        assertEquals(String.format(UNABLE_TO_TAKE_DEVICE_ERROR_FORMAT, getTestDevice()), exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfNoDeviceTaken() throws MobitruOperationException
    {
        mobitruFacadeImpl.setWaitForDeviceTimeout(Duration.ofSeconds(1));
        when(mobitruClient.takeDevice("{\"desiredCapabilities\":{\"platformName\":\"IOS\"}}"))
                .thenThrow(new MobitruDeviceTakeException(NO_DEVICE));
        var capabilities = new DesiredCapabilities(Map.of(PLATFORM_NAME, IOS));
        var exception = assertThrows(MobitruDeviceTakeException.class,
                () -> mobitruFacadeImpl.takeDevice(capabilities));
        assertEquals("Unable to take device with configuration {desiredCapabilities={platformName=IOS}}",
                exception.getMessage());
    }

    @Test
    void shouldInstallApplicationOnTheDevice() throws MobitruOperationException
    {
        when(mobitruClient.getArtifacts()).thenReturn(
                "[{\"realName\" : \"test.apk\", \"id\" : \"1\"}, {\"realName\" : \"app.apk\", \"id\" : \"2\"}]"
            .getBytes(StandardCharsets.UTF_8));
        mobitruFacadeImpl.installApp(UDID, "app.apk");
        verify(mobitruClient).installApp(UDID, "2");
    }

    @Test
    void shouldThrowAnExceptionWhenApplicationCannotBeLocated() throws MobitruOperationException
    {
        when(mobitruClient.getArtifacts()).thenReturn(
                ("[{\"realName\" : \"123.apk\", \"id\" : \"1\", \"uploadedBy\" : \"Mithrandir\", \"id\" : \"1\","
                        + " \"uploadedAt\" : \"33189300000\"}]")
                        .getBytes(StandardCharsets.UTF_8));
        var exception = assertThrows(MobitruOperationException.class,
            () -> mobitruFacadeImpl.installApp(UDID, "starfield.apk"));
        assertEquals("Unable to find application with the name `starfield.apk`. The available applications are: "
            + "[Application{id='1', realName='123.apk', uploadedBy='Mithrandir', uploadedAt=33189300000}]",
                exception.getMessage());
        verify(mobitruClient, never()).installApp(any(), any());
    }

    @Test
    void shouldStopUsingDevice() throws MobitruOperationException
    {
        var deviceId = "deviceid";
        mobitruFacadeImpl.returnDevice(deviceId);
        verify(mobitruClient).returnDevice(deviceId);
    }

    @Test
    void shouldWrapMapperException() throws MobitruOperationException
    {
        when(mobitruClient.getArtifacts()).thenReturn("[{".getBytes(StandardCharsets.UTF_8));
        var exception = assertThrows(MobitruOperationException.class,
            () -> mobitruFacadeImpl.installApp(UDID, null));
        assertInstanceOf(IOException.class, exception.getCause());
        verify(mobitruClient, never()).installApp(any(), any());
    }

    private Device createDevice(String platformVersion, String deviceName, String udid)
    {
        Map<String, Object> desiredCapabilities = new LinkedHashMap<>();
        desiredCapabilities.put(PLATFORM_NAME, "Android");
        desiredCapabilities.put("platformVersion", platformVersion);
        desiredCapabilities.put("deviceName", deviceName);
        desiredCapabilities.put("udid", udid);
        Device device = new Device();
        device.setDesiredCapabilities(desiredCapabilities);
        return device;
    }

    private Device getTestDevice()
    {
        return createDevice("12", "SAMSUNG SM-G998B", UDID);
    }
}
