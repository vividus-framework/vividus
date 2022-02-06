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

package org.vividus.selenium.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.awt.GraphicsEnvironment;
import java.awt.Toolkit;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Browser;
import org.vividus.selenium.IWebDriverProvider;

@ExtendWith(MockitoExtension.class)
class WebDriverManagerTests
{
    @Mock private IWebDriverProvider webDriverProvider;
    @InjectMocks private WebDriverManager webDriverManager;

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
        WebDriver webDriver = mockDriverWithBrowserCapability(browserInCapabilities);
        when(webDriverProvider.get()).thenReturn(webDriver);
        assertEquals(expected, webDriverManager.isBrowserAnyOf(browserToCheck));
    }

    @ParameterizedTest
    @MethodSource("browserChecks")
    void testIsWebDriverBrowserAnyOf(Browser browserInCapabilities, Browser browserToCheck, boolean expected)
    {
        WebDriver webDriver = mockDriverWithBrowserCapability(browserInCapabilities);
        assertEquals(expected, WebDriverManager.isBrowserAnyOf(webDriver, browserToCheck));
    }

    @ParameterizedTest
    @MethodSource("browserChecks")
    void testIsBrowser(Browser browserInCapabilities, Browser browserToCheck, boolean expected)
    {
        Capabilities capabilities = mockCapabilitiesWithBrowser(browserInCapabilities);
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
        Capabilities capabilities = mockCapabilitiesWithBrowser(browserInCapabilities);
        var webDriver = mock(WebDriver.class, withSettings().extraInterfaces(HasCapabilities.class));
        when(((HasCapabilities) webDriver).getCapabilities()).thenReturn(capabilities);
        return webDriver;
    }

    @Test
    void shouldCheckWindowFitsLocalScreen()
    {
        try (MockedStatic<GraphicsEnvironment> graphicsEnvironment = mockStatic(GraphicsEnvironment.class);
                MockedStatic<Toolkit> toolkit = mockStatic(Toolkit.class))
        {
            when(webDriverProvider.isRemoteExecution()).thenReturn(false);
            graphicsEnvironment.when(GraphicsEnvironment::isHeadless).thenReturn(false);
            var defaultToolkit = mock(Toolkit.class);
            toolkit.when(Toolkit::getDefaultToolkit).thenReturn(defaultToolkit);
            var screenSize = mock(java.awt.Dimension.class);
            when(defaultToolkit.getScreenSize()).thenReturn(screenSize);
            var result = webDriverManager.checkWindowFitsScreen(new Dimension(800, 600),
                    (fitsScreen, screenResolution) -> {
                        assertFalse(fitsScreen);
                        assertEquals(new Dimension(0, 0), screenResolution);
                    });
            assertEquals(Optional.of(false), result);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCheckWindowSizeInHeadlessEnvironment()
    {
        try (MockedStatic<GraphicsEnvironment> graphicsEnvironment = mockStatic(GraphicsEnvironment.class))
        {
            when(webDriverProvider.isRemoteExecution()).thenReturn(false);
            graphicsEnvironment.when(GraphicsEnvironment::isHeadless).thenReturn(true);
            var resultHandler = mock(BiConsumer.class);
            var result = webDriverManager.checkWindowFitsScreen(new Dimension(800, 600), resultHandler);
            assertEquals(Optional.empty(), result);
            verifyNoInteractions(resultHandler);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldCheckWindowSizeWhenRemoteResolutionIsUnknown()
    {
        webDriverManager.setRemoteScreenResolution(Optional.empty());
        when(webDriverProvider.isRemoteExecution()).thenReturn(true);
        var resultHandler = mock(BiConsumer.class);
        var result = webDriverManager.checkWindowFitsScreen(new Dimension(800, 600), resultHandler);
        assertEquals(Optional.empty(), result);
        verifyNoInteractions(resultHandler);
    }

    @ParameterizedTest
    @CsvSource({
            "801, 601, true",
            "800, 600, true",
            "799, 600, false",
            "800, 599, false"
    })
    void shouldCheckWindowFitsRemoteScreen(int width, int height, boolean expected)
    {
        var remoteScreenResolution = new Dimension(width, height);
        webDriverManager.setRemoteScreenResolution(Optional.of(remoteScreenResolution));
        when(webDriverProvider.isRemoteExecution()).thenReturn(true);
        var result = webDriverManager.checkWindowFitsScreen(new Dimension(800, 600),
                (fitsScreen, screenResolution) -> {
                    assertEquals(expected, fitsScreen);
                    assertEquals(remoteScreenResolution, screenResolution);
                });
        assertEquals(Optional.of(expected), result);
    }
}
