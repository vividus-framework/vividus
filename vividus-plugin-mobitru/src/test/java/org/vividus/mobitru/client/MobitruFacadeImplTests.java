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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.vividus.mobitru.client.model.ScreenRecording;

@ExtendWith({ TestLoggerFactoryExtension.class, MockitoExtension.class })
class MobitruFacadeImplTests
{
    private static final byte[] BINARY_RESPONSE = {1, 0, 1, 0};
    private static final String DEVICE_TAKEN_MESSAGE = "Device with configuration {} is taken";
    private static final String TRYING_TO_TAKE_DEVICE_MESSAGE = "Trying to take device with configuration {}";
    private static final String TRYING_TO_TAKE_DEVICE_UDID_MESSAGE = "Trying to take device with udid {}";
    private static final String RETRY_LOG_MESSAGE = "Unable to {}, retrying attempt";
    private static final String TAKE_DEVICE_LOG_MESSAGE = "take device";
    private static final String DOWNLOAD_RECORDING_LOG_MESSAGE = "download device screen recording";
    private static final String UNABLE_TO_TAKE_DEVICE_WITH_CONFIGURATION_ERROR_FORMAT =
            "Unable to take device with configuration %s";
    private static final String UNABLE_TO_TAKE_DEVICE_WITH_UDID_ERROR_FORMAT = "Unable to take device with udid %s";
    private static final String UDID = "Z3CT103D2DZ";
    private static final String RECORDING_ID = "15a02180-16f6-4eb9-b5e8-9e945f5e85fe";
    private static final String DEVICE_TYPE_CAPABILITY_NAME = "mobitru-device-search:type";
    private static final String PHONE = "phone";
    private static final String PLATFORM_NAME = "platformName";
    private static final String UDID_CAP = "udid";
    private static final String IOS = "ios";
    private static final String ANDROID = "android";
    private static final String NO_DEVICE = "no device";
    private static final String NO_RECORDING = "no recording";
    private static final String CAPABILITIES_JSON_PREFIX = "{\"desiredCapabilities\":{\"platformName\":\"Android\",";
    private static final String STOP_RECORDING_JSON = "{\"status\": \"recording has stopped\","
            +
            "\"recordingId\": \"15a02180-16f6-4eb9-b5e8-9e945f5e85fe\"}";
    private static final String CAPABILITIES_IOS_PLATFORM = "{\"desiredCapabilities\":{\"platformName\":\"IOS\"}}";
    private static final String CAPABILITIES_WITHOUT_UDID_JSON = CAPABILITIES_JSON_PREFIX
            + "\"platformVersion\":\"12\",\"deviceName\":\"SAMSUNG SM-G998B\"}}";
    private static final String CAPABILITIES_WITH_UDID_JSON = CAPABILITIES_JSON_PREFIX
            + "\"platformVersion\":\"12\",\"deviceName\":\"SAMSUNG SM-G998B\",\"udid\":\"Z3CT103D2DZ\"}}";
    private static final Map<String, String> DEVICE_SEARCH_PARAMETERS = Map.of("type", PHONE);

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(MobitruFacadeImpl.class);

    @Mock private MobitruClient mobitruClient;

    @InjectMocks private MobitruFacadeImpl mobitruFacadeImpl;

    @Test
    void shouldFindDeviceThenTakeItAndProvideItsUdid() throws MobitruOperationException, JsonProcessingException
    {
        mobitruFacadeImpl.setWaitForDeviceTimeout(Duration.ofSeconds(20));
        var deviceSearchException = new MobitruDeviceSearchException(NO_DEVICE);
        var deviceTakeException = new MobitruDeviceTakeException(NO_DEVICE);
        var failedToTakeDeviceCapabilitiesJson = CAPABILITIES_JSON_PREFIX + "\"platformVersion\":\"13\","
                + "\"deviceName\":\"GOOGLE PIXEL 6\",\"udid\":\"777\"}}";
        var desiredCapabilities = new DesiredCapabilities(
                Map.of(PLATFORM_NAME, ANDROID, DEVICE_TYPE_CAPABILITY_NAME, PHONE));
        when(mobitruClient.findDevices(ANDROID, DEVICE_SEARCH_PARAMETERS)).thenThrow(deviceSearchException)
                .thenReturn(('[' + failedToTakeDeviceCapabilitiesJson + "," + CAPABILITIES_WITH_UDID_JSON + ']')
                        .getBytes(StandardCharsets.UTF_8));
        when(mobitruClient.takeDevice(failedToTakeDeviceCapabilitiesJson)).thenThrow(deviceTakeException);
        when(mobitruClient.takeDevice(CAPABILITIES_WITH_UDID_JSON))
                .thenReturn(CAPABILITIES_WITH_UDID_JSON.getBytes(StandardCharsets.UTF_8));
        assertEquals(UDID, mobitruFacadeImpl.takeDevice(desiredCapabilities));
        var failedTakeDevice = createDevice("13", "GOOGLE PIXEL 6", "777");
        var takenDevice = getTestDevice();
        var searchParameters = new DeviceSearchParameters(desiredCapabilities);
        assertEquals(List.of(LoggingEvent.info("Trying to find devices using {}", searchParameters),
                LoggingEvent.warn(deviceSearchException, "Unable to find devices, retrying attempt."),
                LoggingEvent.info("Found devices: {}", System.lineSeparator() + "Device 1: " + failedTakeDevice
                        + System.lineSeparator() + "Device 2: " + takenDevice),
                LoggingEvent.info(TRYING_TO_TAKE_DEVICE_MESSAGE, failedTakeDevice),
                LoggingEvent.warn(deviceTakeException, RETRY_LOG_MESSAGE, TAKE_DEVICE_LOG_MESSAGE),
                LoggingEvent.warn(UNABLE_TO_TAKE_DEVICE_WITH_CONFIGURATION_ERROR_FORMAT.formatted(
                        OBJECT_MAPPER.writeValueAsString(failedTakeDevice))),
                LoggingEvent.info(TRYING_TO_TAKE_DEVICE_MESSAGE, takenDevice),
                LoggingEvent.info(DEVICE_TAKEN_MESSAGE, takenDevice)), LOGGER.getLoggingEvents());
    }

    @ParameterizedTest
    @ValueSource(strings = { "appium:udid", UDID_CAP})
    void shouldTakeDeviceByUdidAndProvideItsUdid(String capName) throws MobitruOperationException
    {
        mobitruFacadeImpl.setWaitForDeviceTimeout(Duration.ofSeconds(20));
        var deviceTakeException = new MobitruDeviceTakeException(NO_DEVICE);
        var desiredCapabilities = new DesiredCapabilities(Map.of(capName, UDID));
        when(mobitruClient.takeDeviceBySerial(UDID)).thenThrow(deviceTakeException).
                thenReturn(CAPABILITIES_WITH_UDID_JSON.getBytes(StandardCharsets.UTF_8));
        assertEquals(UDID, mobitruFacadeImpl.takeDevice(desiredCapabilities));
        var takenDevice = getTestDevice();
        assertEquals(List.of(
                LoggingEvent.info(TRYING_TO_TAKE_DEVICE_UDID_MESSAGE, UDID),
                LoggingEvent.warn(deviceTakeException, RETRY_LOG_MESSAGE, TAKE_DEVICE_LOG_MESSAGE),
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
                .thenReturn(CAPABILITIES_WITH_UDID_JSON.getBytes(StandardCharsets.UTF_8));
        assertEquals(UDID,
            mobitruFacadeImpl.takeDevice(desiredCapabilities));
        assertEquals(List.of(LoggingEvent.info(TRYING_TO_TAKE_DEVICE_MESSAGE, requestedDevice),
                        LoggingEvent.warn(exception, RETRY_LOG_MESSAGE, TAKE_DEVICE_LOG_MESSAGE),
                        LoggingEvent.info(DEVICE_TAKEN_MESSAGE, getTestDevice())), LOGGER.getLoggingEvents());
    }

    @Test
    void shouldStartDeviceSessionRecording()
    {
        assertDoesNotThrow(() -> mobitruFacadeImpl.startDeviceScreenRecording(UDID));
    }

    @Test
    void shouldDownloadDeviceSessionRecording() throws MobitruOperationException
    {
        var noRecordingException = new MobitruDeviceTakeException(NO_RECORDING);
        when(mobitruClient.stopDeviceScreenRecording(UDID)).
                thenReturn(STOP_RECORDING_JSON.getBytes(StandardCharsets.UTF_8));
        when(mobitruClient.downloadDeviceScreenRecording(RECORDING_ID)).thenThrow(noRecordingException).
                thenReturn(BINARY_RESPONSE);
        assertEquals(new ScreenRecording(RECORDING_ID, BINARY_RESPONSE),
                mobitruFacadeImpl.stopDeviceScreenRecording(UDID));
        assertEquals(List.of(LoggingEvent.debug(noRecordingException, RETRY_LOG_MESSAGE,
                DOWNLOAD_RECORDING_LOG_MESSAGE)), LOGGER.getLoggingEvents());
    }

    @ParameterizedTest
    @ValueSource(strings = { "appium:udid", UDID_CAP, "deviceName", "appium:deviceName" })
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
                .thenReturn(('[' + CAPABILITIES_WITHOUT_UDID_JSON + ']').getBytes(StandardCharsets.UTF_8));
        when(mobitruClient.takeDevice(CAPABILITIES_WITHOUT_UDID_JSON))
                .thenThrow(new MobitruDeviceTakeException(NO_DEVICE));
        var capabilities = new DesiredCapabilities(Map.of(PLATFORM_NAME, ANDROID, DEVICE_TYPE_CAPABILITY_NAME, PHONE));
        var exception = assertThrows(MobitruDeviceTakeException.class,
                () -> mobitruFacadeImpl.takeDevice(capabilities));
        assertEquals(UNABLE_TO_TAKE_DEVICE_WITH_CONFIGURATION_ERROR_FORMAT.formatted(CAPABILITIES_WITHOUT_UDID_JSON),
                exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfNoDeviceWithUdid() throws MobitruOperationException
    {
        mobitruFacadeImpl.setWaitForDeviceTimeout(Duration.ofSeconds(1));
        var desiredCapabilities = new DesiredCapabilities(Map.of(UDID_CAP, UDID));
        when(mobitruClient.takeDeviceBySerial(UDID)).thenThrow(new MobitruDeviceTakeException(NO_DEVICE));
        var exception = assertThrows(MobitruDeviceTakeException.class,
                () -> mobitruFacadeImpl.takeDevice(desiredCapabilities));
        assertEquals(UNABLE_TO_TAKE_DEVICE_WITH_UDID_ERROR_FORMAT.formatted(UDID), exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfNoDeviceTaken() throws MobitruOperationException
    {
        mobitruFacadeImpl.setWaitForDeviceTimeout(Duration.ofSeconds(1));
        when(mobitruClient.takeDevice(CAPABILITIES_IOS_PLATFORM))
                .thenThrow(new MobitruDeviceTakeException(NO_DEVICE));
        var capabilities = new DesiredCapabilities(Map.of(PLATFORM_NAME, IOS));
        var exception = assertThrows(MobitruDeviceTakeException.class,
                () -> mobitruFacadeImpl.takeDevice(capabilities));
        assertEquals(UNABLE_TO_TAKE_DEVICE_WITH_CONFIGURATION_ERROR_FORMAT.formatted(CAPABILITIES_IOS_PLATFORM),
                exception.getMessage());
    }

    @Test
    void shouldInstallApplicationOnTheDevice() throws MobitruOperationException
    {
        when(mobitruClient.getArtifacts()).thenReturn(
                "[{\"realName\" : \"test.apk\", \"id\" : \"1\"}, {\"realName\" : \"app.apk\", \"id\" : \"2\"}]"
            .getBytes(StandardCharsets.UTF_8));
        InstallApplicationOptions options = mock();
        mobitruFacadeImpl.installApp(UDID, "app.apk", options);
        verify(mobitruClient).installApp(UDID, "2", options);
    }

    @Test
    void shouldThrowAnExceptionWhenApplicationCannotBeLocated() throws MobitruOperationException
    {
        when(mobitruClient.getArtifacts()).thenReturn(
                ("[{\"realName\" : \"123.apk\", \"id\" : \"1\", \"uploadedBy\" : \"Mithrandir\", \"id\" : \"1\","
                        + " \"uploadedAt\" : \"33189300000\"}]")
                        .getBytes(StandardCharsets.UTF_8));
        InstallApplicationOptions options = mock();
        var exception = assertThrows(MobitruOperationException.class,
            () -> mobitruFacadeImpl.installApp(UDID, "starfield.apk", options));
        assertEquals("Unable to find application with the name `starfield.apk`. The available applications are: "
            + "[Application{id='1', realName='123.apk', uploadedBy='Mithrandir', uploadedAt=33189300000}]",
                exception.getMessage());
        verify(mobitruClient, never()).installApp(any(), any(), any());
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
        InstallApplicationOptions options = mock();
        var exception = assertThrows(MobitruOperationException.class,
            () -> mobitruFacadeImpl.installApp(UDID, null, options));
        assertInstanceOf(IOException.class, exception.getCause());
        verify(mobitruClient, never()).installApp(any(), any(), any());
    }

    private Device createDevice(String platformVersion, String deviceName, String udid)
    {
        Map<String, Object> desiredCapabilities = new LinkedHashMap<>();
        desiredCapabilities.put(PLATFORM_NAME, "Android");
        desiredCapabilities.put("platformVersion", platformVersion);
        desiredCapabilities.put("deviceName", deviceName);
        desiredCapabilities.put(UDID_CAP, udid);
        Device device = new Device();
        device.setDesiredCapabilities(desiredCapabilities);
        return device;
    }

    private Device getTestDevice()
    {
        return createDevice("12", "SAMSUNG SM-G998B", UDID);
    }
}
