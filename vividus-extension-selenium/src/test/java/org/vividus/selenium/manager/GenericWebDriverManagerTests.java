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

package org.vividus.selenium.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
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

import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobilePlatform;
import io.appium.java_client.remote.SupportsContextSwitching;

@ExtendWith(MockitoExtension.class)
class GenericWebDriverManagerTests
{
    private static final String NATIVE_APP_CONTEXT = "NATIVE_APP";
    private static final String WEBVIEW_CONTEXT = "WEBVIEW_1";

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IWebDriverManagerContext webDriverManagerContext;
    @InjectMocks private GenericWebDriverManager driverManager;

    private WebDriver mockWebDriver(Object platform)
    {
        var capabilities = mock(Capabilities.class);
        when(capabilities.getCapability(CapabilityType.PLATFORM_NAME)).thenReturn(platform);

        var webDriver = mock(WebDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(((HasCapabilities) webDriver).getCapabilities()).thenReturn(capabilities);
        return webDriver;
    }

    private void mockMobileDriverContext(SupportsContextSwitching contextSwitchingDriver,
            Set<String> returnContextHandles)
    {
        when(webDriverProvider.getUnwrapped(SupportsContextSwitching.class)).thenReturn(contextSwitchingDriver);
        when(contextSwitchingDriver.getContext()).thenReturn(WEBVIEW_CONTEXT);
        when(contextSwitchingDriver.getContextHandles()).thenReturn(returnContextHandles);
    }

    private GenericWebDriverManager spyIsMobile(boolean returnValue)
    {
        var spy = Mockito.spy(driverManager);
        doReturn(returnValue).when(spy).isMobile();
        return spy;
    }

    @Test
    void testPerformActionInNativeContextNotMobile()
    {
        var webDriverConfigured = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(webDriverConfigured);
        GenericWebDriverManager spy = spyIsMobile(false);
        spy.performActionInNativeContext(webDriver -> assertEquals(webDriverConfigured, webDriver));
        verifyNoInteractions(webDriverConfigured);
    }

    @Test
    void testPerformActionInNativeContext()
    {
        var contextSwitchingDriver = mock(SupportsContextSwitching.class,
                withSettings().extraInterfaces(HasCapabilities.class));
        Set<String> contexts = new HashSet<>();
        contexts.add(WEBVIEW_CONTEXT);
        contexts.add(NATIVE_APP_CONTEXT);
        mockMobileDriverContext(contextSwitchingDriver, contexts);
        var spy = spyIsMobile(true);
        spy.performActionInNativeContext(webDriver -> assertEquals(contextSwitchingDriver, webDriver));
        verify(contextSwitchingDriver).context(NATIVE_APP_CONTEXT);
        verify(contextSwitchingDriver).context(WEBVIEW_CONTEXT);
    }

    @Test
    void testPerformActionInNativeContextException()
    {
        var contextSwitchingDriver = mock(SupportsContextSwitching.class,
                withSettings().extraInterfaces(HasCapabilities.class));
        mockMobileDriverContext(contextSwitchingDriver, new HashSet<>());
        var spy = spyIsMobile(true);
        var exception = assertThrows(IllegalStateException.class,
                () -> spy.performActionInNativeContext(webDriver -> assertEquals(contextSwitchingDriver, webDriver)));
        assertEquals("MobileDriver doesn't have context: " + NATIVE_APP_CONTEXT, exception.getMessage());
    }

    @Test
    void testPerformActionInNativeContextSwitchNotNeeded()
    {
        var contextSwitchingDriver = mock(SupportsContextSwitching.class);
        when(webDriverProvider.getUnwrapped(SupportsContextSwitching.class)).thenReturn(contextSwitchingDriver);
        when(contextSwitchingDriver.getContext()).thenReturn(NATIVE_APP_CONTEXT);
        var spy = spyIsMobile(true);
        spy.performActionInNativeContext(webDriver -> assertEquals(contextSwitchingDriver, webDriver));
        verify(contextSwitchingDriver, never()).context(NATIVE_APP_CONTEXT);
        verify(contextSwitchingDriver, never()).getContextHandles();
    }

    static Stream<Arguments> mobileArguments()
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
            arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isIOS, null, false)
        );
        // CHECKSTYLE:ON
    }

    @ParameterizedTest
    @MethodSource("mobileArguments")
    void testIsPlatform(Predicate<GenericWebDriverManager> test, Object platform, boolean expected)
    {
        mockWebDriver(platform);
        assertEquals(expected, test.test(driverManager));
    }

    static Stream<Arguments> mobileNativeArguments()
    {
        // CHECKSTYLE:OFF
        return Stream.of(
                arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isAndroidNativeApp, MobilePlatform.ANDROID, true),
                arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isAndroidNativeApp, MobilePlatform.IOS, false),
                arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isIOSNativeApp, MobilePlatform.IOS, true),
                arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isIOSNativeApp, "ios", true),
                arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isIOSNativeApp, MobilePlatform.ANDROID, false)
        );
        // CHECKSTYLE:ON
    }

    @ParameterizedTest
    @MethodSource("mobileNativeArguments")
    void testIsMobileNativeApp(Predicate<GenericWebDriverManager> test, Object platform, boolean expected)
    {
        driverManager.setMobileApp(true);
        mockWebDriver(platform);
        assertEquals(expected, test.test(driverManager));
    }

    static Stream<Arguments> notMobileNativeArguments()
    {
        return Stream.of(
                arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isAndroidNativeApp),
                arguments((Predicate<GenericWebDriverManager>) GenericWebDriverManager::isIOSNativeApp)
        );
    }

    @ParameterizedTest
    @MethodSource("notMobileNativeArguments")
    void testIsNotMobileNativeApp(Predicate<GenericWebDriverManager> test)
    {
        driverManager.setMobileApp(false);
        assertFalse(test.test(driverManager));
    }

    @Test
    void testGetSize()
    {
        WebDriver webDriver = mockWebDriver(Platform.WINDOWS);
        var screenSize = mock(Dimension.class);
        mockSizeRetrieval(webDriver, screenSize);
        assertEquals(screenSize, driverManager.getSize());
    }

    @Test
    void shouldGetScreenSizeForMobileApp()
    {
        var dimension = new Dimension(375, 667);
        var iOSDriver = mock(IOSDriver.class);
        lenient().when(webDriverProvider.getUnwrapped(SupportsContextSwitching.class)).thenReturn(iOSDriver);
        when(iOSDriver.getContext()).thenReturn(NATIVE_APP_CONTEXT);
        mockWebDriver(MobilePlatform.IOS);
        mockSizeRetrieval(iOSDriver, dimension);
        assertEquals(dimension, driverManager.getSize());
        verify(webDriverManagerContext).putParameter(WebDriverManagerParameter.SCREEN_SIZE, dimension);
    }

    private void mockSizeRetrieval(WebDriver webDriver, Dimension screenSize)
    {
        var options = mock(Options.class);
        when(webDriver.manage()).thenReturn(options);
        var window = mock(Window.class);
        when(options.window()).thenReturn(window);
        when(window.getSize()).thenReturn(screenSize);
    }

    @Test
    void testGetNativeApplicationViewportCached()
    {
        mockWebDriver(MobilePlatform.IOS);
        var dimension = new Dimension(375, 667);
        when(webDriverManagerContext.getParameter(WebDriverManagerParameter.SCREEN_SIZE)).thenReturn(dimension);
        assertEquals(dimension, driverManager.getSize());
    }
}
