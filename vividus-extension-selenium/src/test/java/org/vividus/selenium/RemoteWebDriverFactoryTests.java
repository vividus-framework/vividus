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

package org.vividus.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;

import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.vividus.selenium.manager.GenericWebDriverManager;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

@ExtendWith(MockitoExtension.class)
class RemoteWebDriverFactoryTests
{
    @Mock private URL url;
    @Mock private Capabilities capabilities;

    @SuppressWarnings("rawtypes")
    @Test
    void shouldCreateDriverForIOS()
    {
        try (MockedStatic<GenericWebDriverManager> manager = mockStatic(GenericWebDriverManager.class))
        {
            manager.when(() -> GenericWebDriverManager.isIOS(capabilities)).thenReturn(true);
            testWebDriverCreation(true, IOSDriver.class);
        }
    }

    @Test
    void shouldCreateRemoteWebDriverWhenNonW3cProtocolIsUsed()
    {
        testWebDriverCreation(false, RemoteWebDriver.class);
    }

    @SuppressWarnings("rawtypes")
    @Test
    void shouldCreateDriverForTvOS()
    {
        try (MockedStatic<GenericWebDriverManager> manager = mockStatic(GenericWebDriverManager.class))
        {
            manager.when(() -> GenericWebDriverManager.isIOS(capabilities)).thenReturn(false);
            manager.when(() -> GenericWebDriverManager.isTvOS(capabilities)).thenReturn(true);
            testWebDriverCreation(true, IOSDriver.class);
        }
    }

    @SuppressWarnings("rawtypes")
    @Test
    void shouldCreateDriverForAndroid()
    {
        try (MockedStatic<GenericWebDriverManager> manager = mockStatic(GenericWebDriverManager.class))
        {
            manager.when(() -> GenericWebDriverManager.isIOS(capabilities)).thenReturn(false);
            manager.when(() -> GenericWebDriverManager.isTvOS(capabilities)).thenReturn(false);
            manager.when(() -> GenericWebDriverManager.isAndroid(capabilities)).thenReturn(true);
            testWebDriverCreation(true, AndroidDriver.class);
        }
    }

    @Test
    void shouldCreateRemoteWebDriver()
    {
        try (MockedStatic<GenericWebDriverManager> manager = mockStatic(GenericWebDriverManager.class))
        {
            manager.when(() -> GenericWebDriverManager.isIOS(capabilities)).thenReturn(false);
            manager.when(() -> GenericWebDriverManager.isTvOS(capabilities)).thenReturn(false);
            manager.when(() -> GenericWebDriverManager.isAndroid(capabilities)).thenReturn(false);
            testWebDriverCreation(true, RemoteWebDriver.class);
        }
    }

    private <T> void testWebDriverCreation(boolean useW3C, Class<T> webDriveClass)
    {
        try (MockedConstruction<T> driver = mockConstruction(webDriveClass))
        {
            var actualDriver = new RemoteWebDriverFactory(useW3C).getRemoteWebDriver(url, capabilities);
            assertEquals(driver.constructed(), List.of(actualDriver));
        }
    }
}
