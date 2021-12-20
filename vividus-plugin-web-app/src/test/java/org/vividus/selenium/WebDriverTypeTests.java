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

import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
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
        WebDriverConfiguration configuration = new WebDriverConfiguration();
        configuration.setBinaryPath(Optional.of(PATH));
        FirefoxOptions expected = new FirefoxOptions();
        expected.setBinary(PATH);
        testGetFirefoxWebDriver(configuration, expected);
    }

    @Test
    void testGetFirefoxWebDriverWithCommandLineArguments()
    {
        String argument = "headless";
        WebDriverConfiguration configuration = new WebDriverConfiguration();
        configuration.setCommandLineArguments(argument);
        FirefoxOptions expected = new FirefoxOptions();
        expected.addArguments(argument);
        testGetFirefoxWebDriver(configuration, expected);
    }

    private static void testGetFirefoxWebDriver(WebDriverConfiguration configuration, FirefoxOptions expected)
    {
        try (MockedConstruction<FirefoxDriver> firefoxDriverMock = mockConstruction(FirefoxDriver.class,
                (mock, context) -> {
                    assertEquals(1, context.getCount());
                    expected.addPreference("startup.homepage_welcome_url.additional", "about:blank");
                    assertEquals(List.of(expected), context.arguments());
                }))
        {
            DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
            WebDriver actual = WebDriverType.FIREFOX.getWebDriver(desiredCapabilities, configuration);
            assertEquals(firefoxDriverMock.constructed().get(0), actual);
        }
    }

    @Test
    void testGetIExploreWebDriver()
    {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        InternetExplorerOptions expected = new InternetExplorerOptions(
                desiredCapabilities.merge(new InternetExplorerOptions().requireWindowFocus()));
        testGetIExploreWebDriver(new WebDriverConfiguration(), expected);
    }

    @Test
    void testGetIExploreWebDriverWithCommandLineArguments()
    {
        String argument = "private";
        WebDriverConfiguration configuration = new WebDriverConfiguration();
        configuration.setCommandLineArguments(new String[] { argument });
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        InternetExplorerOptions expected = new InternetExplorerOptions(
                desiredCapabilities.merge(new InternetExplorerOptions().requireWindowFocus()));
        expected.addCommandSwitches(argument);
        expected.useCreateProcessApiToLaunchIe();
        testGetIExploreWebDriver(configuration, expected);
    }

    private static void testGetIExploreWebDriver(WebDriverConfiguration configuration, InternetExplorerOptions expected)
    {
        try (MockedConstruction<InternetExplorerDriver> internetExplorerDriverMock = mockConstruction(
                InternetExplorerDriver.class, (mock, context) -> {
                    assertEquals(1, context.getCount());
                    expected.requireWindowFocus();
                    assertEquals(List.of(expected), context.arguments());
                }))
        {
            DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
            WebDriver actual = WebDriverType.IEXPLORE.getWebDriver(desiredCapabilities, configuration);
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
        Map<String, String> experimentalOptionValue = singletonMap(OPTION_KEY, OPTION_VALUE);
        WebDriverConfiguration configuration = new WebDriverConfiguration();
        configuration.setBinaryPath(Optional.of(PATH));
        configuration.setCommandLineArguments(new String[] { ARGUMENT });
        configuration.setExperimentalOptions(singletonMap(MOBILE_EMULATION, experimentalOptionValue));
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setBinary(PATH);
        chromeOptions.addArguments(ARGUMENT);
        chromeOptions.setExperimentalOption(MOBILE_EMULATION, experimentalOptionValue);
        testGetChromeWebDriver(configuration, chromeOptions);
    }

    private static void testGetChromeWebDriver(WebDriverConfiguration configuration, ChromeOptions chromeOptions)
    {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        try (MockedConstruction<ChromeDriver> chromeDriverMock = mockConstruction(ChromeDriver.class,
                (mock, context) -> {
                    assertEquals(1, context.getCount());
                    assertEquals(List.of(chromeOptions), context.arguments());
                }))
        {
            WebDriver actual = WebDriverType.CHROME.getWebDriver(desiredCapabilities, configuration);
            assertEquals(chromeDriverMock.constructed().get(0), actual);
        }
    }

    @Test
    void testGetSafariWebDriver()
    {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        try (MockedConstruction<SafariDriver> safariDriverMock = mockConstruction(SafariDriver.class,
                (mock, context) -> {
                    assertEquals(1, context.getCount());
                    assertEquals(List.of(SafariOptions.fromCapabilities(desiredCapabilities)), context.arguments());
                }))
        {
            WebDriver actual = WebDriverType.SAFARI.getWebDriver(desiredCapabilities, new WebDriverConfiguration());
            assertEquals(safariDriverMock.constructed().get(0), actual);
        }
    }

    @Test
    void testGetEdgeChromiumWebDriver()
    {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        EdgeOptions edgeOptions = new EdgeOptions();
        edgeOptions.merge(desiredCapabilities);
        try (MockedConstruction<EdgeDriver> edgeDriverMock = mockConstruction(EdgeDriver.class, (mock, context) -> {
            assertEquals(1, context.getCount());
            assertEquals(List.of(edgeOptions), context.arguments());
        }))
        {
            WebDriver actual = WebDriverType.EDGE_CHROMIUM.getWebDriver(desiredCapabilities,
                    new WebDriverConfiguration());
            assertEquals(edgeDriverMock.constructed().get(0), actual);
        }
    }

    @Test
    void testGetOperaWebDriver()
    {
        testGetOperaWebDriver(new WebDriverConfiguration(), new OperaOptions());
    }

    @Test
    void testGetOperaWebDriverWithAdditionalOptions()
    {
        Map<String, String> experimentalOptionValue = singletonMap(OPTION_KEY, OPTION_VALUE);
        WebDriverConfiguration configuration = new WebDriverConfiguration();
        configuration.setBinaryPath(Optional.of(PATH));
        configuration.setCommandLineArguments(new String[] { ARGUMENT });
        configuration.setExperimentalOptions(singletonMap(MOBILE_EMULATION, experimentalOptionValue));
        OperaOptions operaOptions = new OperaOptions();
        operaOptions.setBinary(PATH);
        operaOptions.addArguments(ARGUMENT);
        operaOptions.setExperimentalOption(MOBILE_EMULATION, experimentalOptionValue);
        testGetOperaWebDriver(configuration, operaOptions);
    }

    private static void testGetOperaWebDriver(WebDriverConfiguration configuration, OperaOptions operaOptions)
    {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        try (MockedConstruction<OperaDriver> operaDriverMock = mockConstruction(OperaDriver.class, (mock, context) -> {
            assertEquals(1, context.getCount());
            assertEquals(List.of(operaOptions), context.arguments());
        }))
        {
            WebDriver actual = WebDriverType.OPERA.getWebDriver(desiredCapabilities, configuration);
            assertEquals(operaDriverMock.constructed().get(0), actual);
        }
    }

    @ParameterizedTest
    @CsvSource({
        "FIREFOX,       true",
        "IEXPLORE,      false",
        "CHROME,        true",
        "SAFARI,        false",
        "EDGE_CHROMIUM, false",
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
        "EDGE_CHROMIUM, false",
        "OPERA,         true"
    })
    void testIsCommandLineArgumentsSupported(WebDriverType type, boolean commandLineArgumentsSupported)
    {
        assertEquals(commandLineArgumentsSupported, type.isCommandLineArgumentsSupported());
    }

    @ParameterizedTest
    @CsvSource({
        "FIREFOX,       true",
        "IEXPLORE,      true",
        "CHROME,        false",
        "SAFARI,        false",
        "EDGE_CHROMIUM, false",
        "OPERA,         false"
    })
    void testIsUseW3C(WebDriverType type, boolean useW3C)
    {
        assertEquals(useW3C, type.isUseW3C());
    }

    @ParameterizedTest
    @CsvSource({
        "FIREFOX,       false",
        "IEXPLORE,      false",
        "CHROME,        false",
        "SAFARI,        false",
        "EDGE_CHROMIUM, true",
        "OPERA,         false"
    })
    void testGetDriverSpecificCapabilities(WebDriverType type, boolean empty)
    {
        assertEquals(type.getDriverSpecificCapabilities().isEmpty(), empty);
    }

    @ParameterizedTest
    @CsvSource({
        "FIREFOX,       webdriver.gecko.driver",
        "IEXPLORE,      webdriver.ie.driver",
        "CHROME,        webdriver.chrome.driver",
        "EDGE_CHROMIUM, webdriver.edge.driver",
        "OPERA,         webdriver.opera.driver"
    })
    void testSetDriverExecutablePath(WebDriverType type, String propertyName)
    {
        type.setDriverExecutablePath(Optional.of(PATH));
        assertEquals(PATH, System.getProperty(propertyName));
    }

    @Test
    void testSetNonNullDriverExecutablePathWhenUnsupported()
    {
        Optional<String> path = Optional.of(PATH);
        assertThrows(UnsupportedOperationException.class, () -> WebDriverType.SAFARI.setDriverExecutablePath(path));
    }

    @Test
    void testSetNullDriverExecutablePathWhenUnsupported()
    {
        WebDriverType.SAFARI.setDriverExecutablePath(Optional.empty());
    }

    @Test
    void testSetFirefoxDriverExecutablePathViaAutomaticManager()
    {
        testSetDriverExecutablePathViaAutomaticManager(WebDriverType.FIREFOX, WebDriverManager::firefoxdriver);
    }

    @Test
    void testSetIExploreDriverExecutablePathViaAutomaticManager()
    {
        testSetDriverExecutablePathViaAutomaticManager(WebDriverType.IEXPLORE, WebDriverManager::iedriver);
    }

    @Test
    void testSetChromeDriverExecutablePathViaAutomaticManager()
    {
        testSetDriverExecutablePathViaAutomaticManager(WebDriverType.CHROME, WebDriverManager::chromedriver);
    }

    @Test
    void testSetOperaDriverExecutablePathViaAutomaticManager()
    {
        testSetDriverExecutablePathViaAutomaticManager(WebDriverType.OPERA, WebDriverManager::operadriver);
    }

    private static void testSetDriverExecutablePathViaAutomaticManager(WebDriverType type,
            Supplier<WebDriverManager> managerSupplier)
    {
        try (MockedStatic<WebDriverManager> webDriverManagerMock = mockStatic(WebDriverManager.class))
        {
            WebDriverManager webDriverManager = mock(WebDriverManager.class);
            webDriverManagerMock.when(managerSupplier::get).thenReturn(webDriverManager);
            type.setDriverExecutablePath(Optional.empty());
            verify(webDriverManager).setup();
        }
    }
}
