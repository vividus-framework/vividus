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

package org.vividus.selenium;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import io.github.bonigarcia.wdm.WebDriverManager;

class WebDriverTypeTests
{
    private static final String OPTION_VALUE = "iPhone 8";
    private static final String OPTION_KEY = "deviceName";
    private static final String MOBILE_EMULATION = "mobileEmulation";
    private static final String ARGUMENT = "allow-external-pages";
    private static final String PATH = "path";

    @Test
    void testGetFirefoxWebDriver()
    {
        testGetFirefoxWebDriver(new WebDriverConfiguration(), new FirefoxOptions());
    }

    @Test
    void testGetFirefoxWebDriverWithBinaryPath()
    {
        var configuration = new WebDriverConfiguration();
        configuration.setBinaryPath(Optional.of(PATH));
        var expected = new FirefoxOptions();
        expected.setBinary(PATH);
        testGetFirefoxWebDriver(configuration, expected);
    }

    @Test
    void testGetFirefoxWebDriverWithCommandLineArguments()
    {
        var argument = "headless";
        var configuration = new WebDriverConfiguration();
        configuration.setCommandLineArguments(argument);
        var expected = new FirefoxOptions();
        expected.addArguments(argument);
        testGetFirefoxWebDriver(configuration, expected);
    }

    private static void testGetFirefoxWebDriver(WebDriverConfiguration configuration, FirefoxOptions expected)
    {
        try (var firefoxDriverMock = mockConstruction(FirefoxDriver.class,
                (mock, context) -> {
                    assertEquals(1, context.getCount());
                    expected.addPreference("startup.homepage_welcome_url.additional", "about:blank");
                    assertEquals(List.of(expected), context.arguments());
                }))
        {
            var desiredCapabilities = new DesiredCapabilities();
            var actual = WebDriverType.FIREFOX.getWebDriver(desiredCapabilities, configuration);
            assertEquals(firefoxDriverMock.constructed().get(0), actual);
        }
    }

    @Test
    void testGetIExploreWebDriver()
    {
        var expected = new InternetExplorerOptions()
                .requireWindowFocus();
        testGetIExploreWebDriver(new DesiredCapabilities(), new WebDriverConfiguration(), expected);
    }

    @Test
    void testGetIExploreWebDriverWithIEOptions()
    {
        Map<String, Object> ieOptions = new HashMap<>();
        ieOptions.put(InternetExplorerDriver.ENABLE_PERSISTENT_HOVERING, true);
        var desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setCapability(InternetExplorerOptions.IE_OPTIONS, ieOptions);
        var expected = new InternetExplorerOptions()
                .requireWindowFocus()
                .enablePersistentHovering();
        testGetIExploreWebDriver(desiredCapabilities, new WebDriverConfiguration(), expected);
    }

    @Test
    void testGetIExploreWebDriverWithCommandLineArguments()
    {
        var argument = "private";
        var configuration = new WebDriverConfiguration();
        configuration.setCommandLineArguments(argument);
        var expected = new InternetExplorerOptions()
                .requireWindowFocus()
                .addCommandSwitches(argument)
                .useCreateProcessApiToLaunchIe();
        testGetIExploreWebDriver(new DesiredCapabilities(), configuration, expected);
    }

    private static void testGetIExploreWebDriver(DesiredCapabilities inputDesiredCapabilities,
            WebDriverConfiguration configuration, InternetExplorerOptions expected)
    {
        try (var internetExplorerDriverMock = mockConstruction(
                InternetExplorerDriver.class, (mock, context) -> {
                    assertEquals(1, context.getCount());
                    assertEquals(List.of(expected), context.arguments());
                }))
        {
            var actual = WebDriverType.IEXPLORE.getWebDriver(inputDesiredCapabilities, configuration);
            assertEquals(internetExplorerDriverMock.constructed().get(0), actual);
        }
    }

    @Test
    void testGetChromeWebDriver()
    {
        testGetChromeWebDriver(new WebDriverConfiguration(), new ChromeOptions());
    }

    @Test
    void testGetChromeWebDriverWithAdditionalOptions()
    {
        var experimentalOptionValue = Map.of(OPTION_KEY, OPTION_VALUE);
        var configuration = new WebDriverConfiguration();
        configuration.setBinaryPath(Optional.of(PATH));
        configuration.setCommandLineArguments(ARGUMENT);
        configuration.setExperimentalOptions(Map.of(MOBILE_EMULATION, experimentalOptionValue));
        var chromeOptions = new ChromeOptions();
        chromeOptions.setBinary(PATH);
        chromeOptions.addArguments(ARGUMENT);
        chromeOptions.setExperimentalOption(MOBILE_EMULATION, experimentalOptionValue);
        testGetChromeWebDriver(configuration, chromeOptions);
    }

    private static void testGetChromeWebDriver(WebDriverConfiguration configuration, ChromeOptions chromeOptions)
    {
        var desiredCapabilities = new DesiredCapabilities();
        try (var chromeDriverMock = mockConstruction(ChromeDriver.class,
                (mock, context) -> {
                    assertEquals(1, context.getCount());
                    assertEquals(List.of(chromeOptions), context.arguments());
                }))
        {
            var actual = WebDriverType.CHROME.getWebDriver(desiredCapabilities, configuration);
            assertEquals(chromeDriverMock.constructed().get(0), actual);
        }
    }

    @Test
    void testGetSafariWebDriver()
    {
        var desiredCapabilities = new DesiredCapabilities();
        try (var safariDriverMock = mockConstruction(SafariDriver.class,
                (mock, context) -> {
                    assertEquals(1, context.getCount());
                    assertEquals(List.of(SafariOptions.fromCapabilities(desiredCapabilities)), context.arguments());
                }))
        {
            var actual = WebDriverType.SAFARI.getWebDriver(desiredCapabilities, new WebDriverConfiguration());
            assertEquals(safariDriverMock.constructed().get(0), actual);
        }
    }

    @Test
    void testGetEdgeWebDriver()
    {
        var expected = new EdgeOptions();
        var configuration = new WebDriverConfiguration();
        testGetEdgeWebDriver(configuration, expected);
    }

    @Test
    void testGetEdgeWebDriverWithBinaryPath()
    {
        var expected = new EdgeOptions();
        expected.setBinary(PATH);
        var configuration = new WebDriverConfiguration();
        configuration.setBinaryPath(Optional.of(PATH));
        testGetEdgeWebDriver(configuration, expected);
    }

    private static void testGetEdgeWebDriver(WebDriverConfiguration configuration, EdgeOptions expected)
    {
        try (var edgeDriverMock = mockConstruction(EdgeDriver.class, (mock, context) -> {
            assertEquals(1, context.getCount());
            assertEquals(List.of(expected), context.arguments());
        }))
        {
            var actual = WebDriverType.EDGE.getWebDriver(new DesiredCapabilities(), configuration);
            assertEquals(edgeDriverMock.constructed().get(0), actual);
        }
    }

    @Test
    void testGetOperaWebDriver()
    {
        testGetOperaWebDriver(new WebDriverConfiguration(), new ChromeOptions());
    }

    @Test
    void testGetOperaWebDriverWithAdditionalOptions()
    {
        var experimentalOptionValue = Map.of(OPTION_KEY, OPTION_VALUE);
        var configuration = new WebDriverConfiguration();
        configuration.setBinaryPath(Optional.of(PATH));
        configuration.setCommandLineArguments(ARGUMENT);
        configuration.setExperimentalOptions(Map.of(MOBILE_EMULATION, experimentalOptionValue));
        var operaOptions = new ChromeOptions();
        operaOptions.setBinary(PATH);
        operaOptions.addArguments(ARGUMENT);
        operaOptions.setExperimentalOption(MOBILE_EMULATION, experimentalOptionValue);
        testGetOperaWebDriver(configuration, operaOptions);
    }

    private static void testGetOperaWebDriver(WebDriverConfiguration configuration, ChromeOptions operaOptions)
    {
        var desiredCapabilities = new DesiredCapabilities();
        try (var chromeDriverMock = mockConstruction(ChromeDriver.class,
                (mock, context) -> {
                    assertEquals(1, context.getCount());
                    assertEquals(List.of(operaOptions), context.arguments());
                }))
        {
            var actual = WebDriverType.OPERA.getWebDriver(desiredCapabilities, configuration);
            assertEquals(chromeDriverMock.constructed().get(0), actual);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "FIREFOX,       true",
        "IEXPLORE,      false",
        "CHROME,        true",
        "SAFARI,        false",
        "EDGE,          true",
        "OPERA,         true"
    })
    void testIsBinaryPathSupported(WebDriverType type, boolean binaryPathSupported)
    {
        assertEquals(binaryPathSupported, type.isBinaryPathSupported());
    }

    @ParameterizedTest
    @CsvSource({
        "FIREFOX,       true",
        "IEXPLORE,      true",
        "CHROME,        true",
        "SAFARI,        false",
        "EDGE,          false",
        "OPERA,         true"
    })
    void testIsCommandLineArgumentsSupported(WebDriverType type, boolean commandLineArgumentsSupported)
    {
        assertEquals(commandLineArgumentsSupported, type.isCommandLineArgumentsSupported());
    }

    @ParameterizedTest
    @CsvSource({
        "FIREFOX,       webdriver.gecko.driver",
        "IEXPLORE,      webdriver.ie.driver",
        "CHROME,        webdriver.chrome.driver",
        "SAFARI,        webdriver.safari.driver",
        "EDGE,          webdriver.edge.driver",
        "OPERA,         webdriver.chrome.driver"
    })
    void testSetDriverExecutablePath(WebDriverType type, String propertyName)
    {
        type.setDriverExecutablePath(Optional.of(PATH));
        assertEquals(PATH, System.getProperty(propertyName));
    }

    static Stream<Arguments> webDriverManagers()
    {
        return Stream.of(
                arguments(WebDriverType.FIREFOX, (Supplier<WebDriverManager>) WebDriverManager::firefoxdriver),
                arguments(WebDriverType.IEXPLORE, (Supplier<WebDriverManager>) WebDriverManager::iedriver),
                arguments(WebDriverType.CHROME, (Supplier<WebDriverManager>) WebDriverManager::chromedriver),
                arguments(WebDriverType.SAFARI, (Supplier<WebDriverManager>) WebDriverManager::safaridriver),
                arguments(WebDriverType.OPERA, (Supplier<WebDriverManager>) WebDriverManager::operadriver),
                arguments(WebDriverType.EDGE, (Supplier<WebDriverManager>) WebDriverManager::edgedriver)
        );
    }

    @ParameterizedTest
    @MethodSource("webDriverManagers")
    void testSetDriverExecutablePathViaAutomaticManager(WebDriverType type, Supplier<WebDriverManager> managerSupplier)
    {
        try (var webDriverManagerMock = mockStatic(WebDriverManager.class))
        {
            var webDriverManager = mock(WebDriverManager.class);
            webDriverManagerMock.when(managerSupplier::get).thenReturn(webDriverManager);
            type.setDriverExecutablePath(Optional.empty());
            verify(webDriverManager).setup();
        }
    }
}
