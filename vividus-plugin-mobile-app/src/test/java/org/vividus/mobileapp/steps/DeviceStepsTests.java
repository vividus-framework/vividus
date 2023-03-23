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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.ScreenOrientation;
import org.vividus.context.VariableContext;
import org.vividus.mobileapp.action.DeviceActions;
import org.vividus.steps.DataWrapper;
import org.vividus.variable.VariableScope;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class DeviceStepsTests
{
    private static final byte[] BINARY_CONTENT = {1, 0, 1};
    private static final String DEVICE_FOLDER = "/device/folder";
    private static final String FILE_NAME = "data.txt";
    private static final String DEVICE_FILE_PATH = Paths.get(DEVICE_FOLDER, FILE_NAME).toString();

    @Mock private DeviceActions deviceActions;
    @Mock private VariableContext variableContext;
    private DeviceSteps deviceSteps;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(DeviceSteps.class);

    @BeforeEach
    void init()
    {
        deviceSteps = new DeviceSteps(DEVICE_FOLDER, deviceActions, variableContext);
    }

    static Stream<Arguments> dataProvider()
    {
        // @formatter:off
        return Stream.of(
                Arguments.of(BINARY_CONTENT, BINARY_CONTENT),
                Arguments.of("Hello",        new byte[] {72, 101, 108, 108, 111})
        );
        // @formatter:on
    }

    @Test
    void shouldUploadFileToDevice()
    {
        byte[] resourceContent = { 58, 41, 10 };

        deviceSteps.uploadFileToDevice(FILE_NAME);
        assertThat(logger.getLoggingEvents(),
                is(List.of(info("Uploading file '{}' from resources to a device at '{}' folder", FILE_NAME,
                        DEVICE_FOLDER))));
        verify(deviceActions).pushFile(DEVICE_FILE_PATH, resourceContent);
    }

    @ParameterizedTest
    @MethodSource("dataProvider")
    void shouldUploadFileWithSpecifiedDataToDevice(Object data, byte[] bytes)
    {
        deviceSteps.uploadFileToDevice(FILE_NAME, new DataWrapper(data));
        assertThat(logger.getLoggingEvents(),
                is(List.of(info("Uploading file with name `{}` to the device at '{}' folder", FILE_NAME,
                        DEVICE_FOLDER))));
        verify(deviceActions).pushFile(DEVICE_FILE_PATH, bytes);
    }

    @Test
    void shouldDeleteFileFromDevice()
    {
        deviceSteps.deleteFileFromDevice(DEVICE_FILE_PATH);
        verify(deviceActions).deleteFile(DEVICE_FILE_PATH);
    }

    @Test
    void shouldDownloadFileFromDevice()
    {
        var variableScopes = Set.of(VariableScope.SCENARIO);
        var variableName = "variableName";
        when(deviceActions.pullFile(DEVICE_FILE_PATH)).thenReturn(BINARY_CONTENT);
        deviceSteps.downloadFileFromDevice(DEVICE_FILE_PATH, variableScopes, variableName);
        verify(variableContext).putVariable(variableScopes, variableName, BINARY_CONTENT);
    }

    @Test
    void shouldChangeDeviceScreenOrientation()
    {
        deviceSteps.changeDeviceScreenOrientation(ScreenOrientation.LANDSCAPE);
        verify(deviceActions).rotate(ScreenOrientation.LANDSCAPE);
    }
}
