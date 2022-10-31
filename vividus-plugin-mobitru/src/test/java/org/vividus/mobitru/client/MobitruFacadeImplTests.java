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
import java.util.List;
import java.util.Map;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.remote.DesiredCapabilities;

@ExtendWith({ TestLoggerFactoryExtension.class, MockitoExtension.class })
class MobitruFacadeImplTests
{
    private static final String UDID = "Z3CT103D2DZ";
    private static final String PLATFORM_NAME = "platformName";

    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(MobitruFacadeImpl.class);

    @Mock private MobitruClient mobitruClient;

    @InjectMocks private MobitruFacadeImpl mobitruFacadeImpl;

    @Test
    void shouldTakeDeviceInUseAndProvideItsUdId() throws MobitruOperationException
    {
        mobitruFacadeImpl.setWaitForDeviceTimeout(Duration.ofSeconds(20));
        var exception = new MobitruDeviceSearchException("no device");
        when(mobitruClient.takeDevice("{\"desiredCapabilities\":{\"platformName\":\"ANDROID\"}}"))
                .thenThrow(exception)
                .thenReturn(("{\"desiredCapabilities\": { \"platformName\": \"Android\","
                    + "\"platformVersion\": \"12\",\"deviceName\": \"SAMSUNG SM-G998B\","
                    + "\"udid\": \"Z3CT103D2DZ\"}}").getBytes(StandardCharsets.UTF_8));
        assertEquals(UDID,
            mobitruFacadeImpl.takeDevice(new DesiredCapabilities(Map.of(PLATFORM_NAME, "android"))));
        assertEquals(List.of(LoggingEvent.warn(exception, "Unable to take device, retrying attempt.")),
            LOGGER.getLoggingEvents());
    }

    @Test
    void shouldThrowExceptionIfNoDeviceFound() throws MobitruOperationException
    {
        mobitruFacadeImpl.setWaitForDeviceTimeout(Duration.ofSeconds(1));
        when(mobitruClient.takeDevice("{\"desiredCapabilities\":{\"platformName\":\"IOS\"}}"))
                .thenThrow(new MobitruDeviceSearchException("device not found"));
        var capabilities = new DesiredCapabilities(Map.of(PLATFORM_NAME, "ios"));
        var exception = assertThrows(MobitruOperationException.class,
                () -> mobitruFacadeImpl.takeDevice(capabilities));
        assertEquals("Unable to find device using Capabilities {platformName: IOS}", exception.getMessage());
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
}
