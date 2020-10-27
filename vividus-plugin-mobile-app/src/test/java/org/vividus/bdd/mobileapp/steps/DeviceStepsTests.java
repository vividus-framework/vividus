/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.bdd.mobileapp.steps;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Navigation;
import org.vividus.mobileapp.action.DeviceActions;
import org.vividus.selenium.IWebDriverProvider;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class DeviceStepsTests
{
    private static final String DEVICE_FOLDER = "/device/folder";

    @Mock private DeviceActions deviceActions;
    @Mock private IWebDriverProvider webDriverProvider;
    private DeviceSteps deviceSteps;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(DeviceSteps.class);

    @BeforeEach
    void init()
    {
        deviceSteps = new DeviceSteps(DEVICE_FOLDER, deviceActions, webDriverProvider);
    }

    @Test
    void shoulUploadFileToDevice()
    {
        String fileName = "file.txt";
        String filePath = Paths.get("/local/fs/", fileName).toString();

        deviceSteps.uploadFileToDevice(filePath);

        verify(deviceActions).pushFile(Paths.get(DEVICE_FOLDER, fileName).toString(), filePath);
        assertThat(logger.getLoggingEvents(), is(List.of(
            info("Uploading file '{}' to a device at '{}' folder", filePath, DEVICE_FOLDER)
        )));
    }

    @Test
    void shoudNavigateBack()
    {
        WebDriver webDriver = mock(WebDriver.class);
        Navigation navigation = mock(Navigation.class);

        when(webDriverProvider.get()).thenReturn(webDriver);
        when(webDriver.navigate()).thenReturn(navigation);

        deviceSteps.navigateBack();

        verify(navigation).back();
    }
}
