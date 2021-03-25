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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.WrapsDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.BrowserType;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.vividus.proxy.IProxy;
import org.vividus.selenium.driver.TextFormattingWebDriver;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.property.IPropertyParser;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class WebDriverFactoryTests
{
    private static final String SESSION_CAPABILITIES = "Session capabilities:\n{}";
    private static final String CAPS_AS_STRING = String.format("{%n  \"key\" : \"value\"%n}");
    private static final Map<String, Object> CAPS = Map.of("key", "value");
    private static final String SELENIUM_CAPABILITIES = "selenium.capabilities.";
    private static final String SELENIUM_GRID_CAPABILITIES = "selenium.grid.capabilities.";
    private static final String FALSE = "false";
    private static final String TRUE = "true";
    private static final String KEY2 = "key2";
    private static final String KEY1 = "key1";
    private static final String BINARY_PATH_PROPERTY_FORMAT = "web.driver.%s.binary-path";
    private static final String COMMAND_LINE_ARGUMENTS_PROPERTY_FORMAT = "web.driver.%s.command-line-arguments";
    private static final String EXPERIMENTAL_OPTIONS_PROPERTY_FORMAT = "web.driver.%s.experimental-options";
    private static final URI URL = URI.create("http://test");
    private static final String PATH = "testPath";
    private static final String ARG_1 = "--arg1";
    private static final String ARG_2 = "--arg2";
    private static final String ARGS = ARG_1 + " " + ARG_2;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AbstractWebDriverFactory.class);

    @Mock(extraInterfaces = HasCapabilities.class)
    private WebDriver driver;

    @Mock
    private RemoteWebDriver remoteWebDriver;

    @Mock
    private IRemoteWebDriverFactory remoteWebDriverFactory;

    @Mock
    private ITimeoutConfigurer timeoutConfigurer;

    @Mock
    private IPropertyParser propertyParser;

    @Mock
    private IProxy proxy;

    private WebDriverFactory webDriverFactory;

    @BeforeEach
    void beforeEach()
    {
        webDriverFactory = new WebDriverFactory(remoteWebDriverFactory, propertyParser,
                new JsonUtils(), timeoutConfigurer, proxy);
    }

    @ParameterizedTest
    @CsvSource({ "true, true", "false," })
    void testGetWebDriverWithWebDriverType(boolean proxyStarted,
            Boolean acceptsInsecureCers) throws IllegalAccessException
    {
        mockCapabilities((HasCapabilities) driver);
        WebDriverType webDriverType = mock(WebDriverType.class);
        webDriverFactory.setWebDriverType(webDriverType);
        WebDriverConfiguration configuration = mock(WebDriverConfiguration.class);
        Map<String, Object> capablities = Map.of(KEY1, TRUE, KEY2, FALSE);
        when(propertyParser.getPropertyValuesTreeByPrefix(SELENIUM_CAPABILITIES)).thenReturn(capablities);
        when(proxy.isStarted()).thenReturn(proxyStarted);
        configuration.setBinaryPath(Optional.of(PATH));
        injectConfigurations(webDriverType, configuration);
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities(Map.of(KEY2, true));
        when(webDriverType.getWebDriver(argThat(capabilities -> {
            Assertions.assertAll(() -> assertTrue((boolean) capabilities.getCapability(KEY1)),
                                 () -> assertTrue((boolean) capabilities.getCapability(KEY2)),
                                 () -> assertEquals(acceptsInsecureCers,
                                         capabilities.getCapability(CapabilityType.ACCEPT_INSECURE_CERTS)));
            return true;
        }), eq(configuration))).thenReturn(driver);
        mockTimeouts(driver);
        WebDriver actualDriver = webDriverFactory.getWebDriver(desiredCapabilities);
        assertThat(actualDriver, instanceOf(TextFormattingWebDriver.class));
        assertEquals(driver, ((WrapsDriver) actualDriver).getWrappedDriver());
        assertLogger();
        verify(propertyParser, never()).getPropertyValuesTreeByPrefix(SELENIUM_GRID_CAPABILITIES);
    }

    @Test
    void testGetWebDriverWithWebDriverTypeWOBinary() throws IllegalAccessException
    {
        mockCapabilities((HasCapabilities) driver);
        WebDriverConfiguration configuration = new WebDriverConfiguration();
        WebDriverType webDriverType = mock(WebDriverType.class);
        webDriverFactory.setWebDriverType(webDriverType);
        injectConfigurations(webDriverType, configuration);
        DesiredCapabilities desiredCapabilities = mock(DesiredCapabilities.class);
        when(webDriverType.getWebDriver(new DesiredCapabilities(), configuration)).thenReturn(driver);
        Timeouts timeouts = mockTimeouts(driver);
        assertEquals(driver, ((WrapsDriver) webDriverFactory.getWebDriver(desiredCapabilities)).getWrappedDriver());
        verify(timeoutConfigurer).configure(timeouts);
        assertLogger();
    }

    @Test
    void testGetWebDriverWithWebDriverTypeAndBinaryPathConfiguration()
    {
        mockCapabilities((HasCapabilities) driver);
        WebDriverType webDriverType = mock(WebDriverType.class);
        when(webDriverType.isBinaryPathSupported()).thenReturn(Boolean.TRUE);
        webDriverFactory.setWebDriverType(webDriverType);
        lenient().when(propertyParser.getPropertyValue("web.driver." + webDriverType + ".driver-executable-path"))
                .thenReturn(PATH);
        lenient().when(propertyParser.getPropertyValue(String.format(BINARY_PATH_PROPERTY_FORMAT, webDriverType)))
                .thenReturn(PATH);
        DesiredCapabilities desiredCapabilities = mock(DesiredCapabilities.class);
        when(webDriverType.getWebDriver(eq(new DesiredCapabilities()),
                argThat(config -> Optional.of(PATH).equals(config.getBinaryPath())
                        && Optional.of(PATH).equals(config.getDriverExecutablePath())))).thenReturn(driver);
        Timeouts timeouts = mockTimeouts(driver);
        assertEquals(driver,
                ((WrapsDriver) webDriverFactory.getWebDriver(desiredCapabilities)).getWrappedDriver());
        verify(timeoutConfigurer).configure(timeouts);
        assertLogger();
    }

    @Test
    void testGetWebDriverWithWebDriverTypeAndCommandLineArgumentsConfiguration()
    {
        mockCapabilities((HasCapabilities) driver);
        WebDriverType webDriverType = mock(WebDriverType.class);
        when(webDriverType.isCommandLineArgumentsSupported()).thenReturn(Boolean.TRUE);
        webDriverFactory.setWebDriverType(webDriverType);
        lenient().when(propertyParser.getPropertyValue(String.format(BINARY_PATH_PROPERTY_FORMAT, webDriverType)))
                .thenReturn(null);
        lenient().when(
                propertyParser.getPropertyValue(String.format(COMMAND_LINE_ARGUMENTS_PROPERTY_FORMAT, webDriverType)))
                .thenReturn(ARGS);
        DesiredCapabilities desiredCapabilities = mock(DesiredCapabilities.class);
        when(webDriverType.getWebDriver(eq(new DesiredCapabilities()),
                argThat(config -> Arrays.equals(new String[] { ARG_1, ARG_2 }, config.getCommandLineArguments()))))
                .thenReturn(driver);
        Timeouts timeouts = mockTimeouts(driver);
        assertEquals(driver,
                ((WrapsDriver) webDriverFactory.getWebDriver(desiredCapabilities)).getWrappedDriver());
        verify(timeoutConfigurer).configure(timeouts);
        assertLogger();
    }

    @Test
    void testGetWebDriverWithWebDriverTypeAndExperimentalOptionsConfiguration()
    {
        mockCapabilities((HasCapabilities) driver);
        WebDriverType webDriverType = mock(WebDriverType.class);
        webDriverFactory.setWebDriverType(webDriverType);
        lenient().when(propertyParser.getPropertyValue(String.format(BINARY_PATH_PROPERTY_FORMAT, webDriverType)))
                .thenReturn(null);
        lenient().when(
                propertyParser.getPropertyValue(String.format(EXPERIMENTAL_OPTIONS_PROPERTY_FORMAT, webDriverType)))
                .thenReturn("{\"mobileEmulation\": {\"deviceName\": \"iPhone 8\"}}");
        DesiredCapabilities desiredCapabilities = mock(DesiredCapabilities.class);
        when(webDriverType.getWebDriver(eq(new DesiredCapabilities()),
                argThat(config ->  Map.of("mobileEmulation", Map.of("deviceName", "iPhone 8"))
                        .equals(config.getExperimentalOptions())))).thenReturn(driver);
        Timeouts timeouts = mockTimeouts(driver);
        assertEquals(driver,
                ((WrapsDriver) webDriverFactory.getWebDriver(desiredCapabilities)).getWrappedDriver());
        verify(timeoutConfigurer).configure(timeouts);
        assertLogger();
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
        mockCapabilities(remoteWebDriver);
        setRemoteDriverUrl();
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        when(remoteWebDriverFactory.getRemoteWebDriver(URL.toURL(), desiredCapabilities)).thenReturn(remoteWebDriver);
        Timeouts timeouts = mockTimeouts(remoteWebDriver);
        assertEquals(remoteWebDriver,
                ((WrapsDriver) webDriverFactory.getRemoteWebDriver(desiredCapabilities)).getWrappedDriver());
        verify(timeoutConfigurer).configure(timeouts);
        assertLogger();
    }

    @ParameterizedTest
    @CsvSource({"CHROME, true", "IEXPLORE,", "SAFARI,"})
    void shouldSetAcceptInsecureCertsForSupportingBrowsers(String type, Boolean acceptsInsecureCerts)
            throws MalformedURLException
    {
        mockCapabilities(remoteWebDriver);
        setRemoteDriverUrl();
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setBrowserName(type);
        when(proxy.isStarted()).thenReturn(true);
        when(remoteWebDriverFactory.getRemoteWebDriver(any(URL.class), argThat(capabilities -> {
            assertEquals(acceptsInsecureCerts, capabilities.getCapability(CapabilityType.ACCEPT_INSECURE_CERTS));
            return true;
        }))).thenReturn(remoteWebDriver);
        Timeouts timeouts = mockTimeouts(remoteWebDriver);
        desiredCapabilities.setCapability(InternetExplorerDriver.NATIVE_EVENTS, false);
        assertEquals(remoteWebDriver,
                ((WrapsDriver) webDriverFactory.getRemoteWebDriver(desiredCapabilities)).getWrappedDriver());
        verify(timeoutConfigurer).configure(timeouts);
        assertLogger();
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetRemoteWebDriverFirefoxDriver() throws MalformedURLException
    {
        mockCapabilities(remoteWebDriver);
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
        assertLogger("{%n  \"acceptInsecureCerts\" : true,%n"
                        + "  \"browserName\" : \"firefox\",%n"
                        + "  \"moz:firefoxOptions\" : {%n"
                        + "    \"args\" : [ ],%n"
                        + "    \"prefs\" : {%n"
                        + "      \"startup.homepage_welcome_url.additional\" : \"about:blank\"%n"
                        + "    }%n  }%n}");
    }

    @Test
    void testGetRemoteWebDriverMarionetteDriver() throws Exception
    {
        mockCapabilities(remoteWebDriver);
        setRemoteDriverUrl();
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setBrowserName("marionette");
        when(remoteWebDriverFactory.getRemoteWebDriver(URL.toURL(), desiredCapabilities))
                .thenReturn(remoteWebDriver);
        Timeouts timeouts = mockTimeouts(remoteWebDriver);
        assertEquals(remoteWebDriver,
            ((WrapsDriver) webDriverFactory.getRemoteWebDriver(desiredCapabilities)).getWrappedDriver());
        verify(timeoutConfigurer).configure(timeouts);
        assertLogger();
    }

    @Test
    void testGetRemoteWebDriverIEDriver() throws Exception
    {
        mockCapabilities(remoteWebDriver);
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
        assertLogger();
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
        mockCapabilities(remoteWebDriver);
        setRemoteDriverUrl();
        DesiredCapabilities desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setBrowserName(BrowserType.CHROME);
        desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
        when(remoteWebDriverFactory.getRemoteWebDriver(URL.toURL(), desiredCapabilities)).thenReturn(remoteWebDriver);
        Timeouts timeouts = mockTimeouts(remoteWebDriver);
        assertEquals(remoteWebDriver,
                ((WrapsDriver) webDriverFactory.getRemoteWebDriver(desiredCapabilities)).getWrappedDriver());
        verify(timeoutConfigurer).configure(timeouts);
        assertLogger();
    }

    private static void mockCapabilities(HasCapabilities hasCapabilities)
    {
        Capabilities capabilities = mock(Capabilities.class);
        when(hasCapabilities.getCapabilities()).thenReturn(capabilities);
        when(capabilities.asMap()).thenReturn(CAPS);
    }

    private void assertLogger()
    {
        assertThat(logger.getLoggingEvents(), hasItem(info(SESSION_CAPABILITIES, CAPS_AS_STRING)));
    }

    private void assertLogger(String sessionCaps)
    {
        assertThat(logger.getLoggingEvents(), is(List.of(
                info("Requested capabilities:\n{}", String.format(sessionCaps)),
                info(SESSION_CAPABILITIES, CAPS_AS_STRING))));
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
            throws IllegalAccessException
    {
        Map<WebDriverType, WebDriverConfiguration> configurations = new ConcurrentHashMap<>();
        configurations.put(webDriverType, configuration);
        FieldUtils.writeField(webDriverFactory, "configurations", configurations, true);
    }
}
