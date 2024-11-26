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

package org.vividus.selenium.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Browser;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.cdp.BrowserPermissions;
import org.vividus.selenium.cdp.CdpWebDriverSessionAttribute;
import org.vividus.selenium.session.WebDriverSessionInfo;

@ExtendWith(MockitoExtension.class)
class WebDriverManagerTests
{
    @Mock private WebDriverSessionInfo webDriverSessionInfo;
    @Mock private IWebDriverProvider webDriverProvider;
    private WebDriverManager webDriverManager;

    @BeforeEach
    void beforeEach()
    {
        webDriverManager = new WebDriverManager(false, webDriverProvider, webDriverSessionInfo);
    }

    static Stream<Arguments> browserChecks()
    {
        return Stream.of(
                arguments(Browser.FIREFOX,  Browser.FIREFOX, true),
                arguments(Browser.CHROME,   Browser.CHROME,  true),
                arguments(Browser.EDGE,     Browser.EDGE,    true),
                arguments(Browser.IE,       Browser.EDGE,    false),
                arguments(Browser.HTMLUNIT, Browser.SAFARI,  false)
        );
    }

    @ParameterizedTest
    @MethodSource("browserChecks")
    void testIsBrowserAnyOf(Browser browserInCapabilities, Browser browserToCheck, boolean expected)
    {
        var webDriver = mockDriverWithBrowserCapability(browserInCapabilities);
        when(webDriverProvider.get()).thenReturn(webDriver);
        assertEquals(expected, webDriverManager.isBrowserAnyOf(browserToCheck));
    }

    @ParameterizedTest
    @MethodSource("browserChecks")
    void testIsWebDriverBrowserAnyOf(Browser browserInCapabilities, Browser browserToCheck, boolean expected)
    {
        var webDriver = mockDriverWithBrowserCapability(browserInCapabilities);
        assertEquals(expected, WebDriverManager.isBrowserAnyOf(webDriver, browserToCheck));
    }

    @ParameterizedTest
    @MethodSource("browserChecks")
    void testIsBrowser(Browser browserInCapabilities, Browser browserToCheck, boolean expected)
    {
        var capabilities = mockCapabilitiesWithBrowser(browserInCapabilities);
        assertEquals(expected, WebDriverManager.isBrowser(capabilities, browserToCheck));
    }

    private Capabilities mockCapabilitiesWithBrowser(Browser browserInCapabilities)
    {
        var capabilities = mock(Capabilities.class);
        when(capabilities.getBrowserName()).thenReturn(browserInCapabilities.browserName());
        return capabilities;
    }

    private WebDriver mockDriverWithBrowserCapability(Browser browserInCapabilities)
    {
        var capabilities = mockCapabilitiesWithBrowser(browserInCapabilities);
        var webDriver = mock(WebDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        when(((HasCapabilities) webDriver).getCapabilities()).thenReturn(capabilities);
        return webDriver;
    }

    @Test
    void shouldReturnLocalScreenSize()
    {
        try (var graphicsEnvironment = mockStatic(GraphicsEnvironment.class); var toolkit = mockStatic(Toolkit.class))
        {
            when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
            graphicsEnvironment.when(GraphicsEnvironment::isHeadless).thenReturn(false);
            var defaultToolkit = mock(Toolkit.class);
            toolkit.when(Toolkit::getDefaultToolkit).thenReturn(defaultToolkit);
            var width = 1920;
            var height = 1080;
            var screenSize = new java.awt.Dimension(width, height);
            when(defaultToolkit.getScreenSize()).thenReturn(screenSize);
            var result = webDriverManager.getScreenResolution();
            assertEquals(Optional.of(new Dimension(width, height)), result);
        }
    }

    @Test
    void shouldReturnEmptyScreenSizeInHeadlessEnvironment()
    {
        try (var graphicsEnvironment = mockStatic(GraphicsEnvironment.class))
        {
            when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
            graphicsEnvironment.when(GraphicsEnvironment::isHeadless).thenReturn(true);
            var result = webDriverManager.getScreenResolution();
            assertEquals(Optional.empty(), result);
        }
    }

    @Test
    void shouldReturnEmptyScreenSizeWhenRemoteResolutionIsUnknown()
    {
        webDriverManager = new WebDriverManager(true, webDriverProvider, webDriverSessionInfo);
        webDriverManager.setRemoteScreenResolution(null);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        var result = webDriverManager.getScreenResolution();
        assertEquals(Optional.empty(), result);
    }

    @Test
    void shouldReturnTrueForChromeHeadlessShell()
    {
        var capabilities = mock(Capabilities.class);
        when(capabilities.getBrowserName()).thenReturn("chrome-headless-shell");

        assertTrue(WebDriverManager.isBrowser(capabilities, Browser.CHROME));
    }

    @Test
    void shouldReturnBrowserPermissions()
    {
        BrowserPermissions permissions = new BrowserPermissions();
        when(webDriverSessionInfo.get(eq(CdpWebDriverSessionAttribute.BROWSER_PERMISSIONS), any()))
                .thenReturn(permissions);
        BrowserPermissions actual = webDriverManager.getBrowserPermissions();
        assertEquals(permissions, actual);
    }

    @ParameterizedTest
    @CsvSource({
            "false, false, false",
            "true, false, false",
            "true, true, true"
    })
    void testIsRemoteExecution(boolean initialized, boolean remote, boolean expected)
    {
        webDriverManager = new WebDriverManager(remote, webDriverProvider, webDriverSessionInfo);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(initialized);

        assertEquals(expected, webDriverManager.isRemoteExecution());
    }
}
