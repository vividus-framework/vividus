/*
 * Copyright 2019 the original author or authors.
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import com.tngtech.java.junit.dataprovider.DataProvider;
import com.tngtech.java.junit.dataprovider.DataProviderRunner;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.ie.InternetExplorerDriverService;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.opera.OperaDriver;
import org.openqa.selenium.opera.OperaOptions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import io.github.bonigarcia.wdm.WebDriverManager;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(DataProviderRunner.class)
@PowerMockIgnore("io.github.lukehutch.fastclasspathscanner.classpath.*")
public class WebDriverTypeTests
{
    private static final String OPTION_VALUE = "iPhone 8";
    private static final String OPTION_KEY = "deviceName";
    private static final String MOBILE_EMULATION = "mobileEmulation";
    private static final String ARGUMENT = "allow-external-pages";
    private static final String PATH = "path";

    @SuppressWarnings("unchecked")
    private static DesiredCapabilities testGetFirefoxWebDriver(WebDriverConfiguration configuration) throws Exception
    {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        FirefoxOptions firefoxOptions = new FirefoxOptions();
        whenNew(FirefoxOptions.class).withArguments(desiredCapabilities).thenReturn(firefoxOptions);
        whenNew(FirefoxOptions.class).withNoArguments().thenReturn(firefoxOptions);
        FirefoxDriver expected = mock(FirefoxDriver.class);
        whenNew(FirefoxDriver.class).withParameterTypes(FirefoxOptions.class).withArguments(firefoxOptions)
                .thenReturn(expected);
        WebDriver actual = WebDriverType.FIREFOX.getWebDriver(desiredCapabilities, configuration);
        assertEquals(expected, actual);
        Map<String, Object> options = (Map<String, Object>) desiredCapabilities
                .getCapability(FirefoxOptions.FIREFOX_OPTIONS);
        Map<String, Object> prefs = (Map<String, Object>) options.get("prefs");
        assertEquals("about:blank", prefs.get("startup.homepage_welcome_url.additional"));
        return desiredCapabilities;
    }

    @SuppressWarnings("unchecked")
    private static DesiredCapabilities testGetIExploreWebDriver(WebDriverConfiguration configuration) throws Exception
    {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        InternetExplorerOptions internetExplorerOptions = new InternetExplorerOptions();
        whenNew(InternetExplorerOptions.class).withArguments(desiredCapabilities).thenReturn(internetExplorerOptions);
        whenNew(InternetExplorerOptions.class).withNoArguments().thenReturn(internetExplorerOptions);
        InternetExplorerDriver expected = mock(InternetExplorerDriver.class);
        whenNew(InternetExplorerDriver.class)
                .withParameterTypes(InternetExplorerDriverService.class, InternetExplorerOptions.class)
                .withArguments(null, internetExplorerOptions).thenReturn(expected);
        WebDriver actual = WebDriverType.IEXPLORE.getWebDriver(desiredCapabilities, configuration);
        assertEquals(expected, actual);
        Map<String, Object> options = (Map<String, Object>) desiredCapabilities.getCapability("se:ieOptions");
        assertTrue((boolean) options.get(InternetExplorerDriver.REQUIRE_WINDOW_FOCUS));
        return desiredCapabilities;
    }

    private static void testGetChromeWebDriver(WebDriverConfiguration configuration, ChromeOptions chromeOptions)
            throws Exception
    {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        ChromeDriver expected = mock(ChromeDriver.class);
        whenNew(ChromeDriver.class).withParameterTypes(ChromeOptions.class).withArguments(chromeOptions)
                .thenReturn(expected);
        WebDriver actual = WebDriverType.CHROME.getWebDriver(desiredCapabilities, configuration);
        assertEquals(expected, actual);
    }

    @Test
    @PrepareForTest(fullyQualifiedNames = "org.vividus.selenium.WebDriverType$1")
    public void testGetFirefoxWebDriver() throws Exception
    {
        testGetFirefoxWebDriver(new WebDriverConfiguration());
    }

    @Test
    @PrepareForTest(fullyQualifiedNames = "org.vividus.selenium.WebDriverType$1")
    public void testGetFirefoxWebDriverWithBinaryPath() throws Exception
    {
        WebDriverConfiguration configuration = new WebDriverConfiguration();
        configuration.setBinaryPath(Optional.of(PATH));
        DesiredCapabilities desiredCapabilities = testGetFirefoxWebDriver(configuration);
        assertEquals(PATH, desiredCapabilities.getCapability(FirefoxDriver.BINARY));
    }

    @Test
    @PrepareForTest(fullyQualifiedNames = "org.vividus.selenium.WebDriverType$1")
    public void testGetFirefoxWebDriverWithCommandLineArguments() throws Exception
    {
        String argument = "headless";
        WebDriverConfiguration configuration = new WebDriverConfiguration();
        configuration.setCommandLineArguments(Optional.of(argument));
        DesiredCapabilities desiredCapabilities = testGetFirefoxWebDriver(configuration);
        FirefoxOptions expected = new FirefoxOptions();
        expected.addArguments(argument);
        assertEquals(expected.asMap(), desiredCapabilities.asMap());
    }

    @Test
    @PrepareForTest(fullyQualifiedNames = "org.vividus.selenium.WebDriverType$2")
    public void testGetIExploreWebDriver() throws Exception
    {
        testGetIExploreWebDriver(new WebDriverConfiguration());
    }

    @Test
    @PrepareForTest(fullyQualifiedNames = "org.vividus.selenium.WebDriverType$2")
    public void testGetIExploreWebDriverWithCommandLineArguments() throws Exception
    {
        String argument = "private";
        WebDriverConfiguration configuration = new WebDriverConfiguration();
        configuration.setCommandLineArguments(Optional.of(argument));
        DesiredCapabilities desiredCapabilities = testGetIExploreWebDriver(configuration);
        assertEquals(argument, desiredCapabilities.getCapability(InternetExplorerDriver.IE_SWITCHES));
    }

    @Test
    @PrepareForTest(fullyQualifiedNames = "org.vividus.selenium.WebDriverType$3")
    public void testGetChromeWebDriver() throws Exception
    {
        testGetChromeWebDriver(new WebDriverConfiguration(), new ChromeOptions());
    }

    @Test
    @PrepareForTest(fullyQualifiedNames = "org.vividus.selenium.WebDriverType$3")
    public void testGetChromeWebDriverWithAdditionalOptions() throws Exception
    {
        Map<String, String> experimentalOptionValue = singletonMap(OPTION_KEY, OPTION_VALUE);
        WebDriverConfiguration configuration = new WebDriverConfiguration();
        configuration.setBinaryPath(Optional.of(PATH));
        configuration.setCommandLineArguments(Optional.of(ARGUMENT));
        configuration.setExperimentalOptions(singletonMap(MOBILE_EMULATION, experimentalOptionValue));
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.setBinary(PATH);
        chromeOptions.addArguments(ARGUMENT);
        chromeOptions.setExperimentalOption(MOBILE_EMULATION, experimentalOptionValue);
        testGetChromeWebDriver(configuration, chromeOptions);
    }

    @Test
    @PrepareForTest(fullyQualifiedNames = "org.vividus.selenium.WebDriverType$4")
    public void testGetSafariWebDriver() throws Exception
    {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        SafariDriver expected = mock(SafariDriver.class);
        whenNew(SafariDriver.class).withParameterTypes(SafariOptions.class)
                .withArguments(SafariOptions.fromCapabilities(desiredCapabilities)).thenReturn(expected);
        WebDriver actual = WebDriverType.SAFARI.getWebDriver(desiredCapabilities, new WebDriverConfiguration());
        assertEquals(expected, actual);
    }

    @Test
    @PrepareForTest(fullyQualifiedNames = "org.vividus.selenium.WebDriverType$5")
    public void testGetEdgeWebDriver() throws Exception
    {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        EdgeOptions edgeOptions = new EdgeOptions();
        edgeOptions.merge(desiredCapabilities);
        EdgeDriver expected = mock(EdgeDriver.class);
        whenNew(EdgeDriver.class).withParameterTypes(EdgeOptions.class).withArguments(edgeOptions).thenReturn(expected);
        WebDriver actual = WebDriverType.EDGE.getWebDriver(desiredCapabilities, new WebDriverConfiguration());
        assertEquals(expected, actual);
    }

    @Test
    @PrepareForTest(fullyQualifiedNames = "org.vividus.selenium.WebDriverType$6")
    public void testGetOperaWebDriver() throws Exception
    {
        testGetOperaWebDriver(new WebDriverConfiguration(), new OperaOptions());
    }

    private static void testGetOperaWebDriver(WebDriverConfiguration configuration, OperaOptions operaOptions)
            throws Exception
    {
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        OperaDriver expected = mock(OperaDriver.class);
        whenNew(OperaDriver.class).withParameterTypes(OperaOptions.class).withArguments(operaOptions)
                .thenReturn(expected);
        WebDriver actual = WebDriverType.OPERA.getWebDriver(desiredCapabilities, configuration);
        assertEquals(expected, actual);
    }

    @Test
    @PrepareForTest(fullyQualifiedNames = "org.vividus.selenium.WebDriverType$6")
    public void testGetOperaWebDriverWithAdditionalOptions() throws Exception
    {
        Map<String, String> experimentalOptionValue = singletonMap(OPTION_KEY, OPTION_VALUE);
        WebDriverConfiguration configuration = new WebDriverConfiguration();
        configuration.setBinaryPath(Optional.of(PATH));
        configuration.setCommandLineArguments(Optional.of(ARGUMENT));
        configuration.setExperimentalOptions(singletonMap(MOBILE_EMULATION, experimentalOptionValue));
        OperaOptions operaOptions = new OperaOptions();
        operaOptions.setBinary(PATH);
        operaOptions.addArguments(ARGUMENT);
        operaOptions.setExperimentalOption(MOBILE_EMULATION, experimentalOptionValue);
        testGetOperaWebDriver(configuration, operaOptions);
    }

    @Test
    @DataProvider({
        "FIREFOX,   true",
        "IEXPLORE,  false",
        "CHROME,    true",
        "SAFARI,    false",
        "EDGE,      false",
        "OPERA,     true"
        })
    public void testIsBinaryPathSupported(WebDriverType type, boolean binaryPathSupported)
    {
        assertEquals(binaryPathSupported, type.isBinaryPathSupported());
    }

    @Test
    @DataProvider({
        "FIREFOX,   true",
        "IEXPLORE,  true",
        "CHROME,    true",
        "SAFARI,    false",
        "EDGE,      false",
        "OPERA,     true"
        })
    public void testIsCommandLineArgumentsSupported(WebDriverType type, boolean commandLineArgumentsSupported)
    {
        assertEquals(commandLineArgumentsSupported, type.isCommandLineArgumentsSupported());
    }

    @Test
    @DataProvider({
            "FIREFOX,   true",
            "IEXPLORE,  true",
            "CHROME,    false",
            "SAFARI,    false",
            "EDGE,      false",
            "OPERA,     false"
    })
    public void testIsUseW3C(WebDriverType type, boolean useW3C)
    {
        assertEquals(useW3C, type.isUseW3C());
    }

    @Test
    @DataProvider({
            "FIREFOX,   false",
            "IEXPLORE,  false",
            "CHROME,    false",
            "SAFARI,    false",
            "EDGE,      true",
            "OPERA,     false"
    })
    public void testGetDriverSpecificCapabilities(WebDriverType type, boolean empty)
    {
        assertEquals(type.getDriverSpecificCapabilities().isEmpty(), empty);
    }

    @Test
    @DataProvider({
        "FIREFOX,   webdriver.gecko.driver",
        "IEXPLORE,  webdriver.ie.driver",
        "CHROME,    webdriver.chrome.driver",
        "EDGE,      webdriver.edge.driver",
        "OPERA,     webdriver.opera.driver"
        })
    public void testSetDriverExecutablePath(WebDriverType type, String propertyName)
    {
        type.setDriverExecutablePath(Optional.of(PATH));
        assertEquals(PATH, System.getProperty(propertyName));
    }

    @Test
    @DataProvider("SAFARI")
    public void testSetNonNullDriverExecutablePathWhenUnsupported(WebDriverType type)
    {
        assertThrows(UnsupportedOperationException.class, () -> type.setDriverExecutablePath(Optional.of(PATH)));
    }

    @Test
    @DataProvider("SAFARI")
    public void testSetNullDriverExecutablePathWhenUnsupported(WebDriverType type)
    {
        type.setDriverExecutablePath(Optional.empty());
    }

    @Test
    @PrepareForTest(value = WebDriverManager.class, fullyQualifiedNames = "org.vividus.selenium.WebDriverType$1")
    public void testSetFirefoxDriverExecutablePathViaAutomaticManager()
    {
        testSetDriverExecutablePathViaAutomaticManager(WebDriverType.FIREFOX, WebDriverManager::firefoxdriver);
    }

    @Test
    @PrepareForTest(value = WebDriverManager.class, fullyQualifiedNames = "org.vividus.selenium.WebDriverType$2")
    public void testSetIExploreDriverExecutablePathViaAutomaticManager()
    {
        testSetDriverExecutablePathViaAutomaticManager(WebDriverType.IEXPLORE, WebDriverManager::iedriver);
    }

    @Test
    @PrepareForTest(value = WebDriverManager.class, fullyQualifiedNames = "org.vividus.selenium.WebDriverType$3")
    public void testSetChromeDriverExecutablePathViaAutomaticManager()
    {
        testSetDriverExecutablePathViaAutomaticManager(WebDriverType.CHROME, WebDriverManager::chromedriver);
    }

    @Test
    @PrepareForTest(value = WebDriverManager.class, fullyQualifiedNames = "org.vividus.selenium.WebDriverType$6")
    public void testSetOperaDriverExecutablePathViaAutomaticManager()
    {
        testSetDriverExecutablePathViaAutomaticManager(WebDriverType.OPERA, WebDriverManager::operadriver);
    }

    @Test
    @PrepareForTest(value = WebDriverManager.class, fullyQualifiedNames = "org.vividus.selenium.WebDriverType$5")
    public void testSetEdgeDriverExecutablePathViaAutomaticManager()
    {
        testSetDriverExecutablePathViaAutomaticManager(WebDriverType.EDGE, WebDriverManager::edgedriver);
    }

    private static void testSetDriverExecutablePathViaAutomaticManager(WebDriverType type,
            Supplier<WebDriverManager> managerSupplier)
    {
        WebDriverManager webDriverManager = mock(WebDriverManager.class);
        PowerMockito.mockStatic(WebDriverManager.class);
        when(managerSupplier.get()).thenReturn(webDriverManager);
        type.setDriverExecutablePath(Optional.empty());
        verify(webDriverManager).setup();
    }
}
