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

package org.vividus.selenium;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.powermock.reflect.Whitebox;
import org.vividus.selenium.driver.TextFormattingWebDriver;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.property.IPropertyParser;

@ExtendWith(MockitoExtension.class)
class WebDriverFactoryTests
{
    private static final String BINARY_PATH_PROPERTY_FORMAT = "web.driver.%s.binary-path";
    private static final String COMMAND_LINE_ARGUMENTS_PROPERTY_FORMAT = "web.driver.%s.command-line-arguments";
    private static final String EXPERIMENTAL_OPTIONS_PROPERTY_FORMAT = "web.driver.%s.experimental-options";
    private static final URI URL = URI.create("http://test");
    private static final String PATH = "testPath";
    private static final String ARG_1 = "--arg1";
    private static final String ARG_2 = "--arg2";
    private static final String ARGS = ARG_1 + " " + ARG_2;

    @Mock
    private WebDriver driver;

    @Mock
    private RemoteWebDriver remoteWebDriver;

    @Mock
    private IRemoteWebDriverFactory remoteWebDriverFactory;

    @Mock
    private ITimeoutConfigurer timeoutConfigurer;

    @Mock
    private IPropertyParser propertyParser;

    @InjectMocks
    private WebDriverFactory webDriverFactory;

    @BeforeEach
    void beforeEach()
    {
        webDriverFactory.setJsonUtils(new JsonUtils(PropertyNamingStrategy.LOWER_CASE));
    }

    @Test
    void testGetWebDriverWithWebDriverType() throws Exception
    {
        WebDriverType webDriverType = mock(WebDriverType.class);
        webDriverFactory.setWebDriverType(webDriverType);
        WebDriverConfiguration configuration = mock(WebDriverConfiguration.class);
        configuration.setBinaryPath(Optional.of(PATH));
        injectConfigurations(webDriverType, configuration);
        DesiredCapabilities desiredCapabilities = mock(DesiredCapabilities.class);
        when(webDriverType.getWebDriver(desiredCapabilities, configuration)).thenReturn(driver);
        mockTimeouts(driver);
        WebDriver actualDriver = webDriverFactory.getWebDriver(desiredCapabilities);
        assertThat(actualDriver, instanceOf(TextFormattingWebDriver.class));
        assertEquals(driver, ((WrapsDriver) actualDriver).getWrappedDriver());
    }

    @Test
    void testGetWebDriverWithWebDriverTypeWOBinary() throws Exception
    {
        WebDriverConfiguration configuration = new WebDriverConfiguration();
        WebDriverType webDriverType = mock(WebDriverType.class);
        webDriverFactory.setWebDriverType(webDriverType);
        injectConfigurations(webDriverType, configuration);
        DesiredCapabilities desiredCapabilities = mock(DesiredCapabilities.class);
        when(webDriverType.getWebDriver(desiredCapabilities, configuration)).thenReturn(driver);
        Timeouts timeouts = mockTimeouts(driver);
        assertEquals(driver, ((WrapsDriver) webDriverFactory.getWebDriver(desiredCapabilities)).getWrappedDriver());
        verify(timeoutConfigurer).configure(timeouts);
    }

    @Test
    void testGetWebDriverWithWebDriverTypeAndBinaryPathConfiguration()
    {
        WebDriverType webDriverType = mock(WebDriverType.class);
        when(webDriverType.isBinaryPathSupported()).thenReturn(Boolean.TRUE);
        webDriverFactory.setWebDriverType(webDriverType);
        lenient().when(propertyParser.getPropertyValue("web.driver." + webDriverType + ".driver-executable-path"))
                .thenReturn(PATH);
        lenient().when(propertyParser.getPropertyValue(String.format(BINARY_PATH_PROPERTY_FORMAT, webDriverType)))
                .thenReturn(PATH);
        DesiredCapabilities desiredCapabilities = mock(DesiredCapabilities.class);
        when(webDriverType.getWebDriver(eq(desiredCapabilities),
                argThat(config -> Optional.of(PATH).equals(config.getBinaryPath())
                        && Optional.of(PATH).equals(config.getDriverExecutablePath())))).thenReturn(driver);
        Timeouts timeouts = mockTimeouts(driver);
        assertEquals(driver,
                ((WrapsDriver) webDriverFactory.getWebDriver(desiredCapabilities)).getWrappedDriver());
        verify(timeoutConfigurer).configure(timeouts);
    }

    @Test
    void testGetWebDriverWithWebDriverTypeAndCommandLineArgumentsConfiguration()
    {
        WebDriverType webDriverType = mock(WebDriverType.class);
        when(webDriverType.isCommandLineArgumentsSupported()).thenReturn(Boolean.TRUE);
        webDriverFactory.setWebDriverType(webDriverType);
        lenient().when(propertyParser.getPropertyValue(String.format(BINARY_PATH_PROPERTY_FORMAT, webDriverType)))
                .thenReturn(null);
        lenient().when(
                propertyParser.getPropertyValue(String.format(COMMAND_LINE_ARGUMENTS_PROPERTY_FORMAT, webDriverType)))
                .thenReturn(ARGS);
        DesiredCapabilities desiredCapabilities = mock(DesiredCapabilities.class);
        when(webDriverType.getWebDriver(eq(desiredCapabilities),
                argThat(config -> Arrays.equals(new String[] { ARG_1, ARG_2 }, config.getCommandLineArguments()))))
                .thenReturn(driver);
        Timeouts timeouts = mockTimeouts(driver);
        assertEquals(driver,
                ((WrapsDriver) webDriverFactory.getWebDriver(desiredCapabilities)).getWrappedDriver());
        verify(timeoutConfigurer).configure(timeouts);
    }

    @Test
    void testGetWebDriverWithWebDriverTypeAndExperimentalOptionsConfiguration()
    {
        WebDriverType webDriverType = mock(WebDriverType.class);
        webDriverFactory.setWebDriverType(webDriverType);
        lenient().when(propertyParser.getPropertyValue(String.format(BINARY_PATH_PROPERTY_FORMAT, webDriverType)))
                .thenReturn(null);
        lenient().when(
                propertyParser.getPropertyValue(String.format(EXPERIMENTAL_OPTIONS_PROPERTY_FORMAT, webDriverType)))
                .thenReturn("{\"mobileEmulation\": {\"deviceName\": \"iPhone 8\"}}");
        DesiredCapabilities desiredCapabilities = mock(DesiredCapabilities.class);
        when(webDriverType.getWebDriver(eq(desiredCapabilities),
                argThat(config ->  Map.of("mobileEmulation", Map.of("deviceName", "iPhone 8"))
                        .equals(config.getExperimentalOptions())))).thenReturn(driver);
        Timeouts timeouts = mockTimeouts(driver);
        assertEquals(driver,
                ((WrapsDriver) webDriverFactory.getWebDriver(desiredCapabilities)).getWrappedDriver());
        verify(timeoutConfigurer).configure(timeouts);
    }

    @Test
    void testGetWebDriverWithWebDriverTypeAndInvalidBinaryPathConfiguration()
    {
        WebDriverType webDriverType = mock(WebDriverType.class);
        webDriverFactory.setWebDriverType(webDriverType);
        when(webDriverType.isBinaryPathSupported()).thenReturn(Boolean.FALSE);
        when(propertyParser.getPropertyValue(String.format(BINARY_PATH_PROPERTY_FORMAT, webDriverType))).thenReturn(
                PATH);
        DesiredCapabilities desiredCapabilities = mock(DesiredCapabilities.class);
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
            () -> webDriverFactory.getWebDriver(desiredCapabilities));
        assertEquals("Configuring of binary-path is not supported for " + webDriverType, exception.getMessage());
    }

    @Test
    void testGetWebDriverWithWebDriverTypeAndInvalidCommandLineArgumentsConfiguration()
    {
        WebDriverType webDriverType = mock(WebDriverType.class);
        webDriverFactory.setWebDriverType(webDriverType);
        when(webDriverType.isCommandLineArgumentsSupported()).thenReturn(Boolean.FALSE);
        lenient().when(propertyParser.getPropertyValue(String.format(BINARY_PATH_PROPERTY_FORMAT, webDriverType)))
                .thenReturn(null);
        lenient().when(
                propertyParser.getPropertyValue(String.format(COMMAND_LINE_ARGUMENTS_PROPERTY_FORMAT, webDriverType)))
                .thenReturn(ARG_1);
        DesiredCapabilities desiredCapabilities = mock(DesiredCapabilities.class);
        UnsupportedOperationException exception = assertThrows(UnsupportedOperationException.class,
            () -> webDriverFactory.getWebDriver(desiredCapabilities));
        assertEquals("Configuring of command-line-arguments is not supported for " + webDriverType,
                exception.getMessage());
    }

    @Test
    void testGetRemoteWebDriver() throws Exception
    {
        setRemoteDriverUrl();
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        when(remoteWebDriverFactory.getRemoteWebDriver(URL.toURL(), desiredCapabilities)).thenReturn(remoteWebDriver);
        Timeouts timeouts = mockTimeouts(remoteWebDriver);
        assertEquals(remoteWebDriver,
                ((WrapsDriver) webDriverFactory.getRemoteWebDriver(desiredCapabilities)).getWrappedDriver());
        verify(timeoutConfigurer).configure(timeouts);
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetRemoteWebDriverFirefoxDriver() throws MalformedURLException
    {
        setRemoteDriverUrl();
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities(new FirefoxOptions());
        when(remoteWebDriverFactory.getRemoteWebDriver(eq(URL.toURL()), argThat(caps ->
        {
            Map<String, Object> options = (Map<String, Object>) caps.getCapability(FirefoxOptions.FIREFOX_OPTIONS);
            Map<String, Object> prefs = (Map<String, Object>) options.get("prefs");
            return "about:blank".equals(prefs.get("startup.homepage_welcome_url.additional"))
                    && "firefox".equals(caps.getBrowserName());
        }))).thenReturn(remoteWebDriver);
        Timeouts timeouts = mockTimeouts(remoteWebDriver);
        assertEquals(remoteWebDriver,
                ((WrapsDriver) webDriverFactory.getRemoteWebDriver(desiredCapabilities)).getWrappedDriver());
        verify(timeoutConfigurer).configure(timeouts);
    }

    @Test
    void testGetRemoteWebDriverMarionetteDriver() throws Exception
    {
        setRemoteDriverUrl();
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setBrowserName("marionette");
        when(remoteWebDriverFactory.getRemoteWebDriver(URL.toURL(), desiredCapabilities))
                .thenReturn(remoteWebDriver);
        Timeouts timeouts = mockTimeouts(remoteWebDriver);
        assertEquals(remoteWebDriver,
            ((WrapsDriver) webDriverFactory.getRemoteWebDriver(desiredCapabilities)).getWrappedDriver());
        verify(timeoutConfigurer).configure(timeouts);
    }

    @Test
    void testGetRemoteWebDriverIEDriver() throws Exception
    {
        setRemoteDriverUrl();
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setBrowserName(BrowserType.IEXPLORE);
        when(remoteWebDriverFactory.getRemoteWebDriver(any(URL.class), any(DesiredCapabilities.class)))
                .thenReturn(remoteWebDriver);
        Timeouts timeouts = mockTimeouts(remoteWebDriver);
        desiredCapabilities.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false);
        assertEquals(remoteWebDriver,
                ((WrapsDriver) webDriverFactory.getRemoteWebDriver(desiredCapabilities)).getWrappedDriver());
        verify(timeoutConfigurer).configure(timeouts);
    }

    @Test
    void testGetRemoteWebDriverIsChromeWithAdditionalOptions() throws Exception
    {
        String args = "disable-blink-features=BlockCredentialedSubresources";
        ChromeOptions chromeOptions = new ChromeOptions();
        chromeOptions.addArguments(args);
        chromeOptions.setExperimentalOption("w3c", Boolean.TRUE);
        lenient().doReturn(args).when(propertyParser).getPropertyValue(
                String.format(COMMAND_LINE_ARGUMENTS_PROPERTY_FORMAT, WebDriverType.CHROME));
        lenient().doReturn("{\"w3c\":true}").when(propertyParser).getPropertyValue(
                String.format(EXPERIMENTAL_OPTIONS_PROPERTY_FORMAT, WebDriverType.CHROME));
        testGetRemoteWebDriverIsChrome(chromeOptions);
    }

    @Test
    void testGetRemoteWebDriverIsChromeWithoutAdditionalOptions() throws Exception
    {
        testGetRemoteWebDriverIsChrome(new ChromeOptions());
    }

    private void testGetRemoteWebDriverIsChrome(ChromeOptions chromeOptions) throws Exception
    {
        setRemoteDriverUrl();
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setBrowserName(BrowserType.CHROME);
        desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
        when(remoteWebDriverFactory.getRemoteWebDriver(URL.toURL(), desiredCapabilities)).thenReturn(remoteWebDriver);
        Timeouts timeouts = mockTimeouts(remoteWebDriver);
        assertEquals(remoteWebDriver,
                ((WrapsDriver) webDriverFactory.getRemoteWebDriver(desiredCapabilities)).getWrappedDriver());
        verify(timeoutConfigurer).configure(timeouts);
    }

    private static Timeouts mockTimeouts(WebDriver webDriver)
    {
        Options options = mock(Options.class);
        when(webDriver.manage()).thenReturn(options);
        Timeouts timeouts = mock(Timeouts.class);
        when(options.timeouts()).thenReturn(timeouts);
        return timeouts;
    }

    private void setRemoteDriverUrl() throws MalformedURLException
    {
        webDriverFactory.setRemoteDriverUrl(URL.toURL());
    }

    private void injectConfigurations(WebDriverType webDriverType, WebDriverConfiguration configuration)
            throws ReflectiveOperationException
    {
        Map<WebDriverType, WebDriverConfiguration> configurations = new ConcurrentHashMap<>();
        configurations.put(webDriverType, configuration);
        Whitebox.setInternalState(webDriverFactory, "configurations", configurations);
    }
}
