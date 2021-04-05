/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.selenium;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;

import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.vividus.selenium.manager.GenericWebDriverManager;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

@ExtendWith(MockitoExtension.class)
class RemoteWebDriverFactoryTests
{
    @Mock private URL url;
    @Mock private Capabilities capabilities;
    @InjectMocks private RemoteWebDriverFactory remoteWebDriverFactory;

    @SuppressWarnings("rawtypes")
    @Test
    void shouldCreateDriverForIOS()
    {
        try (MockedConstruction<IOSDriver> driver = mockConstruction(IOSDriver.class);
                MockedStatic<GenericWebDriverManager> manager = mockStatic(GenericWebDriverManager.class))
        {
            manager.when(() -> GenericWebDriverManager.isIOS(capabilities)).thenReturn(true);
            checkDriver(remoteWebDriverFactory.getRemoteWebDriver(url, capabilities), driver.constructed());
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void shouldCreateDriverForTvOS()
    {
        try (MockedConstruction<IOSDriver> driver = mockConstruction(IOSDriver.class);
                MockedStatic<GenericWebDriverManager> manager = mockStatic(GenericWebDriverManager.class))
        {
            manager.when(() -> GenericWebDriverManager.isIOS(capabilities)).thenReturn(false);
            manager.when(() -> GenericWebDriverManager.isTvOS(capabilities)).thenReturn(true);
            checkDriver(remoteWebDriverFactory.getRemoteWebDriver(url, capabilities), driver.constructed());
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void shouldCreateDriverForAndroid()
    {
        try (MockedConstruction<AndroidDriver> driver = mockConstruction(AndroidDriver.class);
                MockedStatic<GenericWebDriverManager> manager = mockStatic(GenericWebDriverManager.class))
        {
            manager.when(() -> GenericWebDriverManager.isIOS(capabilities)).thenReturn(false);
            manager.when(() -> GenericWebDriverManager.isTvOS(capabilities)).thenReturn(false);
            manager.when(() -> GenericWebDriverManager.isAndroid(capabilities)).thenReturn(true);
            checkDriver(remoteWebDriverFactory.getRemoteWebDriver(url, capabilities), driver.constructed());
        }
    }

    @Test
    void shouldCreateRemoteWebDriver()
    {
        try (MockedConstruction<RemoteWebDriver> driver = mockConstruction(RemoteWebDriver.class);
                MockedStatic<GenericWebDriverManager> manager = mockStatic(GenericWebDriverManager.class))
        {
            manager.when(() -> GenericWebDriverManager.isIOS(capabilities)).thenReturn(false);
            manager.when(() -> GenericWebDriverManager.isTvOS(capabilities)).thenReturn(false);
            manager.when(() -> GenericWebDriverManager.isAndroid(capabilities)).thenReturn(false);
            checkDriver(remoteWebDriverFactory.getRemoteWebDriver(url, capabilities), driver.constructed());
        }
    }

    private void checkDriver(RemoteWebDriver actual, List<? extends WebDriver> drivers)
    {
        assertThat(drivers, hasSize(1));
        assertEquals(drivers.get(0), actual);
    }
}
