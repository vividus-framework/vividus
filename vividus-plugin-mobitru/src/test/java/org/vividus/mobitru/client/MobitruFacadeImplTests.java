/*
 * Copyright 2019-2025 the original author or authors.
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

import static com.github.valfirst.slf4jtest.LoggingEvent.debug;
import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
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
import org.slf4j.event.Level;
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
    private static final String DEVICE_TAKE_EXCEPTION_LOG_MESSAGE = "{}";
    private static final String UDID = "Z3CT103D2DZ";
    private static final String RECORDING_ID = "15a02180-16f6-4eb9-b5e8-9e945f5e85fe";
    private static final String DEVICE_TYPE_CAPABILITY_NAME = "mobitru-device-search:type";
    private static final String PHONE = "phone";
    private static final String PLATFORM_NAME = "platformName";
    private static final String UDID_CAP = "udid";
    private static final String ANDROID = "android";
    private static final String NO_DEVICE = "no device";
    private static final String NO_RECORDING = "no recording";
    private static final String CAPABILITIES_JSON_PREFIX = "{\"desiredCapabilities\":{\"platformName\":\"Android\",";
    private static final String STOP_RECORDING_JSON = "{\"status\": \"recording has stopped\","
            + "\"recordingId\": \"15a02180-16f6-4eb9-b5e8-9e945f5e85fe\"}";
    private static final String CAPABILITIES_WITH_UDID_JSON = CAPABILITIES_JSON_PREFIX
            + "\"platformVersion\":\"12\",\"deviceName\":\"SAMSUNG SM-G998B\",\"udid\":\"Z3CT103D2DZ\"}}";
    private static final Map<String, String> DEVICE_SEARCH_PARAMETERS = Map.of("type", PHONE);
    private static final String CAPABILITIES_WITH_UDID_FORMAT = "{\"desiredCapabilities\":{\"udid\":\"%s\"}}";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(MobitruFacadeImpl.class);

    @Mock private MobitruClient mobitruClient;
    @InjectMocks private MobitruFacadeImpl mobitruFacade;

    @Test
    void shouldFindDeviceWithRetryAndTakeIt() throws MobitruOperationException
    {
        mobitruFacade.setWaitForDeviceTimeout(Duration.ofSeconds(5));

        var deviceSearchException = new MobitruDeviceSearchException(NO_DEVICE);
        var failedToTakeDeviceCapabilitiesJson = CAPABILITIES_JSON_PREFIX + "\"platformVersion\":\"13\","
                + "\"deviceName\":\"GOOGLE PIXEL 6\",\"udid\":\"777\"}}";
        var deviceTakeException = new MobitruDeviceTakeException(
                "Unable to take device with configuration %s. %s".formatted(failedToTakeDeviceCapabilitiesJson,
                        NO_DEVICE));
        var desiredCapabilities = new DesiredCapabilities(
                Map.of(PLATFORM_NAME, ANDROID, DEVICE_TYPE_CAPABILITY_NAME, PHONE));
        when(mobitruClient.findDevices(ANDROID, DEVICE_SEARCH_PARAMETERS))
                .thenThrow(deviceSearchException)
                .thenReturn(('[' + failedToTakeDeviceCapabilitiesJson + "," + CAPABILITIES_WITH_UDID_JSON + ']')
                        .getBytes(StandardCharsets.UTF_8));
        when(mobitruClient.takeDevice(failedToTakeDeviceCapabilitiesJson)).thenThrow(deviceTakeException);
        when(mobitruClient.takeDevice(CAPABILITIES_WITH_UDID_JSON))
                .thenReturn(CAPABILITIES_WITH_UDID_JSON.getBytes(StandardCharsets.UTF_8));
        assertEquals(UDID, mobitruFacade.takeDevice(desiredCapabilities));
        var failedTakeDevice = createDevice("13", "GOOGLE PIXEL 6", "777");
        var takenDevice = getTestDevice();
        var searchParameters = new DeviceSearchParameters(desiredCapabilities);
        assertEquals(List.of(info("Trying to find devices using {}", searchParameters),
                warn(deviceSearchException, "Unable to find devices, retrying attempt."),
                info("Found devices: {}", System.lineSeparator() + "Device 1: " + failedTakeDevice
                        + System.lineSeparator() + "Device 2: " + takenDevice),
                info(TRYING_TO_TAKE_DEVICE_MESSAGE, failedToTakeDeviceCapabilitiesJson),
                warn(DEVICE_TAKE_EXCEPTION_LOG_MESSAGE, deviceTakeException.getMessage()),
                info(TRYING_TO_TAKE_DEVICE_MESSAGE, CAPABILITIES_WITH_UDID_JSON),
                info(DEVICE_TAKEN_MESSAGE, takenDevice)), logger.getLoggingEvents()
        );
    }

    @Test
    void shouldTakeDeviceWitRetry() throws MobitruOperationException
    {
        mobitruFacade.setWaitForDeviceTimeout(Duration.ofSeconds(5));

        var deviceCapabilitiesJson = CAPABILITIES_WITH_UDID_FORMAT.formatted(UDID);
        var desiredCapabilities = new DesiredCapabilities(Map.of(UDID_CAP, UDID));
        var deviceTakeException = new MobitruDeviceTakeException(NO_DEVICE);
        when(mobitruClient.takeDevice(deviceCapabilitiesJson))
                .thenThrow(deviceTakeException)
                .thenReturn(CAPABILITIES_WITH_UDID_JSON.getBytes(StandardCharsets.UTF_8));
        assertEquals(UDID, mobitruFacade.takeDevice(desiredCapabilities));
        assertEquals(List.of(
                info(TRYING_TO_TAKE_DEVICE_MESSAGE, deviceCapabilitiesJson),
                warn("Unable to take device with configuration {}. {}. Retrying attempt...", deviceCapabilitiesJson,
                        deviceTakeException.getMessage()),
                info(DEVICE_TAKEN_MESSAGE, getTestDevice())), logger.getLoggingEvents()
        );
    }

    @Test
    void shouldTakeDeviceByUdids() throws MobitruOperationException
    {
        var firstDeviceId = "111";
        var secondDeviceId = UDID;
        var desiredCapabilities = new DesiredCapabilities(Map.of(
                PLATFORM_NAME, ANDROID,
                "mobitru-device-search:udids", firstDeviceId + ", " + UDID
        ));

        var deviceCapabilities1 = CAPABILITIES_WITH_UDID_FORMAT.formatted(firstDeviceId);
        var deviceCapabilities2 = CAPABILITIES_WITH_UDID_FORMAT.formatted(secondDeviceId);

        mobitruFacade.setWaitForDeviceTimeout(Duration.ofSeconds(5));

        var deviceTakeExceptionMessageFormat = "Unable to take device with configuration %s. Expected status code "
                + "`200` but got `404`. Response body: {\"error\":{\"message\":\"There are no suitable "
                + "devices found\"}}";

        var deviceTakeException1 = new MobitruDeviceTakeException(
                deviceTakeExceptionMessageFormat.formatted(deviceCapabilities1));
        when(mobitruClient.takeDevice(deviceCapabilities1)).thenThrow(deviceTakeException1);

        var deviceTakeException2 = new MobitruDeviceTakeException(
                deviceTakeExceptionMessageFormat.formatted(deviceCapabilities2));
        when(mobitruClient.takeDevice(deviceCapabilities2))
                .thenThrow(deviceTakeException2)
                .thenReturn(CAPABILITIES_WITH_UDID_JSON.getBytes(StandardCharsets.UTF_8));

        var actualDeviceId = mobitruFacade.takeDevice(desiredCapabilities);
        assertEquals(secondDeviceId, actualDeviceId);
        var takenDevice = getTestDevice();

        List<LoggingEvent> loggingEvents = logger.getLoggingEvents();
        assertThat(loggingEvents, hasItems(
                info(TRYING_TO_TAKE_DEVICE_MESSAGE, deviceCapabilities1),
                info(TRYING_TO_TAKE_DEVICE_MESSAGE, deviceCapabilities2),
                warn(DEVICE_TAKE_EXCEPTION_LOG_MESSAGE, deviceTakeException1.getMessage()),
                warn(DEVICE_TAKE_EXCEPTION_LOG_MESSAGE, deviceTakeException2.getMessage()),
                info(DEVICE_TAKEN_MESSAGE, takenDevice)
        ));
        assertEquals(1, loggingEvents.stream()
                .filter(l -> l.getLevel().equals(Level.WARN) && "{}. Retrying attempt...".equals(l.getMessage()))
                .filter(l -> ((String) l.getArguments().get(0)).matches(
                        "Unable to take device with any of configuration\\(s\\): .*(111.*Z3CT103D2DZ|Z3CT103D2DZ"
                                + ".*111).*"))
                .count());
    }

    @Test
    void shouldThrowExceptionWhenNoDeviceIsFound() throws MobitruOperationException
    {
        mobitruFacade.setWaitForDeviceTimeout(Duration.ofSeconds(0));

        when(mobitruClient.findDevices(ANDROID, DEVICE_SEARCH_PARAMETERS))
                .thenThrow(new MobitruDeviceSearchException(NO_DEVICE));
        var capabilities = new DesiredCapabilities(Map.of(PLATFORM_NAME, ANDROID, DEVICE_TYPE_CAPABILITY_NAME, PHONE));
        var exception = assertThrows(MobitruOperationException.class, () -> mobitruFacade.takeDevice(capabilities));
        assertEquals("Unable to find devices using platform = android, type = phone", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionWhenDeviceIsFoundButNotTaken() throws MobitruOperationException
    {
        mobitruFacade.setWaitForDeviceTimeout(Duration.ofSeconds(0));

        var deviceCapabilitiesJson = CAPABILITIES_JSON_PREFIX + "\"platformVersion\":\"12\",\"deviceName\":\"SAMSUNG "
                + "SM-G998B\"}}";
        when(mobitruClient.findDevices(ANDROID, DEVICE_SEARCH_PARAMETERS)).thenReturn(
                ('[' + deviceCapabilitiesJson + ']').getBytes(StandardCharsets.UTF_8));
        MobitruDeviceTakeException takeException = mock();
        when(mobitruClient.takeDevice(deviceCapabilitiesJson)).thenThrow(takeException);
        var capabilities = new DesiredCapabilities(Map.of(
                PLATFORM_NAME, ANDROID,
                DEVICE_TYPE_CAPABILITY_NAME, PHONE
        ));
        var exception = assertThrows(MobitruDeviceTakeException.class, () -> mobitruFacade.takeDevice(capabilities));
        assertEquals(takeException, exception);
    }

    @Test
    void shouldThrowExceptionWhenDeviceIsNotTaken() throws MobitruOperationException
    {
        mobitruFacade.setWaitForDeviceTimeout(Duration.ofSeconds(0));

        var deviceCapabilitiesJson = "{\"desiredCapabilities\":{\"platformName\":\"ANDROID\"}}";
        MobitruDeviceTakeException takeException = mock();
        when(mobitruClient.takeDevice(deviceCapabilitiesJson)).thenThrow(takeException);
        var capabilities = new DesiredCapabilities(Map.of(PLATFORM_NAME, ANDROID));
        var exception = assertThrows(MobitruDeviceTakeException.class, () -> mobitruFacade.takeDevice(capabilities));
        assertEquals(takeException, exception);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "appium:udid",
            UDID_CAP,
            "deviceName",
            "appium:deviceName"
    })
    void shouldThrowExceptionIfConflictingCapabilities(String capabilityName)
    {
        var capabilities = new DesiredCapabilities(Map.of(
                capabilityName, "value",
                DEVICE_TYPE_CAPABILITY_NAME, PHONE
        ));
        var exception = assertThrows(IllegalArgumentException.class, () -> mobitruFacade.takeDevice(capabilities));
        assertEquals(String.format("Conflicting capabilities are found. `%s` capability can not be specified along"
                + " with `mobitru-device-search:` capabilities", capabilityName), exception.getMessage());
    }

    @Test
    void shouldStartDeviceSessionRecording()
    {
        assertDoesNotThrow(() -> mobitruFacade.startDeviceScreenRecording(UDID));
    }

    @Test
    void shouldDownloadDeviceSessionRecording() throws MobitruOperationException
    {
        var noRecordingException = new MobitruOperationException(NO_RECORDING);
        when(mobitruClient.stopDeviceScreenRecording(UDID)).
                thenReturn(STOP_RECORDING_JSON.getBytes(StandardCharsets.UTF_8));
        byte[] binaryResponse = {1, 0, 1, 0};
        when(mobitruClient.downloadDeviceScreenRecording(RECORDING_ID))
                .thenThrow(noRecordingException)
                .thenReturn(binaryResponse);
        assertEquals(new ScreenRecording(RECORDING_ID, binaryResponse),
                mobitruFacade.stopDeviceScreenRecording(UDID));
        assertEquals(
                List.of(debug(noRecordingException, "Unable to download device screen recording, retrying attempt...")),
                logger.getLoggingEvents());
    }

    @Test
    void shouldThrowExceptionIfRecordingIsNotDownloaded() throws MobitruOperationException
    {
        var noRecordingException = new MobitruOperationException(NO_RECORDING);
        when(mobitruClient.stopDeviceScreenRecording(UDID)).
                thenReturn(STOP_RECORDING_JSON.getBytes(StandardCharsets.UTF_8));
        when(mobitruClient.downloadDeviceScreenRecording(RECORDING_ID)).thenThrow(noRecordingException);
        var exception = assertThrows(MobitruOperationException.class,
                () -> mobitruFacade.stopDeviceScreenRecording(UDID));
        assertEquals(String.format("Unable to download recording with id %s", RECORDING_ID),
                exception.getMessage());
    }

    @Test
    void shouldInstallApplicationOnTheDevice() throws MobitruOperationException
    {
        when(mobitruClient.getArtifacts()).thenReturn(
                "[{\"realName\" : \"test.apk\", \"id\" : \"1\"}, {\"realName\" : \"app.apk\", \"id\" : \"2\"}]"
            .getBytes(StandardCharsets.UTF_8));
        InstallApplicationOptions options = mock();
        mobitruFacade.installApp(UDID, "app.apk", options);
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
            () -> mobitruFacade.installApp(UDID, "starfield.apk", options));
        assertEquals("Unable to find application with the name `starfield.apk`. The available applications are: "
            + "[Application{id='1', realName='123.apk', uploadedBy='Mithrandir', uploadedAt=33189300000}]",
                exception.getMessage());
        verify(mobitruClient, never()).installApp(any(), any(), any());
    }

    @Test
    void shouldStopUsingDevice() throws MobitruOperationException
    {
        var deviceId = UDID;
        mobitruFacade.returnDevice(deviceId);
        verify(mobitruClient).returnDevice(deviceId);
    }

    @Test
    void shouldWrapMapperException() throws MobitruOperationException
    {
        when(mobitruClient.getArtifacts()).thenReturn("[{".getBytes(StandardCharsets.UTF_8));
        InstallApplicationOptions options = mock();
        var exception = assertThrows(MobitruOperationException.class,
            () -> mobitruFacade.installApp(UDID, null, options));
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
