/*
 * Copyright 2019-2026 the original author or authors.
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

package org.vividus.selenium.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Window;
import org.openqa.selenium.remote.CapabilityType;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.session.WebDriverSessionAttributes;
import org.vividus.selenium.session.WebDriverSessionInfo;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobilePlatform;
import io.appium.java_client.remote.SupportsContextSwitching;

@ExtendWith(MockitoExtension.class)
class GenericWebDriverManagerTests
{
    private static final String NATIVE_APP_CONTEXT = "NATIVE_APP";
    private static final String WEBVIEW_CONTEXT = "WEBVIEW_1";

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private WebDriverSessionInfo webDriverSessionInfo;
    @InjectMocks private GenericWebDriverManager driverManager;

    private <T extends WebDriver> T mockWebDriver(Class<T> classToMock, Object platform)
    {
        Capabilities capabilities = mock();
        when(capabilities.getCapability(CapabilityType.PLATFORM_NAME)).thenReturn(platform);

        T webDriver = mock(classToMock, withSettings().extraInterfaces(HasCapabilities.class));
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(((HasCapabilities) webDriver).getCapabilities()).thenReturn(capabilities);
        return webDriver;
    }

    @Test
    void shouldPerformActionInNativeContext()
    {
        IOSDriver driver = mock();
        when(webDriverProvider.getUnwrapped(SupportsContextSwitching.class)).thenReturn(driver);
        when(driver.getContext()).thenReturn(WEBVIEW_CONTEXT);
        driverManager.performActionInNativeContext(wD -> assertEquals(driver, wD));
        verify(driver).context(NATIVE_APP_CONTEXT);
        verify(driver).context(WEBVIEW_CONTEXT);
        verifyNoMoreInteractions(driver);
    }

    @Test
    void shouldPerformActionInNativeContextWhenSwitchIsNotNeeded()
    {
        AndroidDriver driver = mock();
        when(webDriverProvider.getUnwrapped(SupportsContextSwitching.class)).thenReturn(driver);
        when(driver.getContext()).thenReturn(NATIVE_APP_CONTEXT);
        driverManager.performActionInNativeContext(wD -> assertEquals(driver, wD));
        verifyNoMoreInteractions(driver);
    }

    static Stream<Arguments> platformsData()
    {
        // CHECKSTYLE:OFF
        return Stream.of(
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isMobile, MobilePlatform.IOS, true),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isMobile, MobilePlatform.ANDROID, true),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isMobile, MobilePlatform.WINDOWS, false),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isMobile, Platform.IOS, true),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isMobile, Platform.ANDROID, true),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isMobile, Platform.WINDOWS, false),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isIOS, MobilePlatform.IOS, true),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isIOS, MobilePlatform.ANDROID, false),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isIOS, Platform.IOS, true),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isIOS, Platform.ANDROID, false),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isAndroid, MobilePlatform.IOS, false),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isAndroid, MobilePlatform.ANDROID, true),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isAndroid, Platform.IOS, false),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isAndroid, Platform.ANDROID, true),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isTvOS, MobilePlatform.TVOS, true),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isTvOS, MobilePlatform.IOS, false),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isTvOS, Platform.IOS, false),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isIOS, null, false),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isMacOs, Platform.MAC, true),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isMacOs, Platform.WINDOWS, false),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isMacOs, Platform.LINUX, false),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isMacOs, Platform.UNIX, false),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isMacOs, Platform.IOS, false),
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isMacOs, null, false)
        );
        // CHECKSTYLE:ON
    }

    @ParameterizedTest
    @MethodSource("platformsData")
    void testIsPlatform(Predicate<GenericWebDriverManager> test, Object platform, boolean expected)
    {
        mockWebDriver(WebDriver.class, platform);
        assertEquals(expected, test.test(driverManager));
    }

    @ParameterizedTest
    @CsvSource({
            "NATIVE_APP, true",
            "WEBVIEW_1,   false"
    })
    void shouldCheckIfContextIsSwitchedToMobileNative(String currentContext, boolean expected)
    {
        var driver = mockWebDriver(IOSDriver.class, MobilePlatform.IOS);
        when(webDriverProvider.getUnwrapped(SupportsContextSwitching.class)).thenReturn(driver);
        when(driver.getContext()).thenReturn(currentContext);
        assertEquals(expected, driverManager.isContextSwitchedToMobileNative());
    }

    @Test
    void shouldNotCheckIfContextIsSwitchedToNativeForDesktop()
    {
        var driver = mockWebDriver(WebDriver.class, Platform.WINDOWS);
        assertFalse(driverManager.isContextSwitchedToMobileNative());
        verifyNoMoreInteractions(webDriverProvider, driver);
    }

    @Test
    void testGetSize()
    {
        WebDriver webDriver = mockWebDriver(WebDriver.class, Platform.WINDOWS);
        Dimension screenSize = mock();
        mockSizeRetrieval(webDriver, screenSize);
        assertEquals(screenSize, driverManager.getSize());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldGetScreenSizeForMobileApp()
    {
        var dimension = new Dimension(375, 667);
        IOSDriver iOSDriver = mockWebDriver(IOSDriver.class, MobilePlatform.IOS);
        when(webDriverProvider.getUnwrapped(SupportsContextSwitching.class)).thenReturn(iOSDriver);
        when(iOSDriver.getContext()).thenReturn(NATIVE_APP_CONTEXT);
        mockSizeRetrieval(iOSDriver, dimension);
        when(webDriverSessionInfo.get(eq(WebDriverSessionAttributes.SCREEN_SIZE),
                any(Supplier.class))).thenAnswer(invocation -> ((Supplier<?>) invocation.getArguments()[1]).get());
        assertEquals(dimension, driverManager.getSize());
    }

    private void mockSizeRetrieval(WebDriver webDriver, Dimension screenSize)
    {
        Options options = mock();
        when(webDriver.manage()).thenReturn(options);
        Window window = mock();
        when(options.window()).thenReturn(window);
        when(window.getSize()).thenReturn(screenSize);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testGetNativeApplicationViewportCached()
    {
        mockWebDriver(IOSDriver.class, MobilePlatform.IOS);
        var dimension = new Dimension(375, 667);
        when(webDriverSessionInfo.get(eq(WebDriverSessionAttributes.SCREEN_SIZE),
                any(Supplier.class))).thenReturn(dimension);
        assertEquals(dimension, driverManager.getSize());
    }
}
