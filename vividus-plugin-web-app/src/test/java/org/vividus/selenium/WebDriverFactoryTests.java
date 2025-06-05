/*
 * Copyright 2019-2025 the original author or authors.
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
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.mockito.Mock;
import org.mockito.MockedStatic.Verification;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.HasAuthentication;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.UsernameAndPassword;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriver.Options;
import org.openqa.selenium.WebDriver.Timeouts;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.Browser;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.support.decorators.Decorated;
import org.openqa.selenium.support.events.WebDriverListener;
import org.vividus.proxy.IProxy;
import org.vividus.selenium.authentication.BasicAuthCredentials;
import org.vividus.selenium.driver.TextFormattingWebDriver;
import org.vividus.ui.web.listener.WebDriverListenerFactory;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.property.IPropertyParser;
import org.vividus.util.property.PropertyMappedCollection;

import io.appium.java_client.android.AndroidDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class WebDriverFactoryTests
{
    private static final String SESSION_CAPABILITIES = "Session capabilities:\n{}";
    private static final String CAPS_AS_STRING = String.format("{%n  \"key\" : \"value\"%n}");
    private static final String PROPERTY_FORMAT = "web.driver.%s.%s";
    private static final String BINARY_PATH = "binary-path";
    private static final String COMMAND_LINE_ARGUMENTS = "command-line-arguments";
    private static final String EXPERIMENTAL_OPTIONS = "experimental-options";
    private static final String CLI_ARGS_NOT_SUPPORTED = "Configuring of command-line-arguments is not supported for ";
    private static final String ARG_1 = "--arg1";
    private static final String ARG_2 = "--arg2";
    private static final String ARGS = ARG_1 + " " + ARG_2;
    private static final String CHROME_BROWSER_NAME = Browser.CHROME.browserName();
    private static final String SAFARI_BROWSER_NAME = Browser.SAFARI.browserName();
    private static final String EDGE_BROWSER_NAME = Browser.EDGE.browserName();
    private static final String FIREFOX_BROWSER_NAME = Browser.FIREFOX.browserName();
    private static final String OPERA_BROWSER_NAME = Browser.OPERA.browserName();
    private static final String DRIVER_PATH = "/path/to/driver";
    private static final String BROWSER_NAME = "browserName";
    private static final PropertyMappedCollection<BasicAuthCredentials> CREDENTIALS = new PropertyMappedCollection<>(
            Map.of());

    private final TestLogger logger = TestLoggerFactory.getTestLogger(GenericWebDriverFactory.class);

    @Mock private RemoteWebDriver remoteWebDriver;
    @Mock private IRemoteWebDriverFactory remoteWebDriverFactory;
    @Mock private IPropertyParser propertyParser;
    @Mock private IProxy proxy;
    @Mock private WebDriverStartContext webDriverStartContext;
    @Mock private TimeoutConfigurer timeoutConfigurer;

    @ParameterizedTest
    @CsvSource({
            "true, true",
            "false,"
    })
    void testGetWebDriverWithWebDriverType(boolean proxyStarted, Boolean acceptsInsecureCerts)
            throws ReflectiveOperationException
    {
        var key1 = "key1";
        var key2 = "key2";
        when(propertyParser.getPropertyValuesTreeByPrefix("selenium.capabilities.")).thenReturn(
                Map.of(key1, "true", key2, "false"));
        when(proxy.isStarted()).thenReturn(proxyStarted);
        testGetChromeWebDriver(new DesiredCapabilities(Map.of(key2, true)), null, DRIVER_PATH, null, args -> { },
                chromeOptions -> assertAll(
                        () -> assertEquals(Boolean.TRUE, chromeOptions.getCapability(key1)),
                        () -> assertEquals(Boolean.TRUE, chromeOptions.getCapability(key2)),
                        () -> assertEquals(acceptsInsecureCerts,
                                chromeOptions.getCapability(CapabilityType.ACCEPT_INSECURE_CERTS))
                ));

        verify(propertyParser, never()).getPropertyValuesTreeByPrefix("selenium.grid.capabilities.");
    }

    @Test
    @ClearSystemProperty(key = ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY)
    void testGetWebDriverWithCommandLineArgumentsConfigurationAndBinaryPath() throws ReflectiveOperationException
    {
        when(propertyParser.getPropertyValue(PROPERTY_FORMAT, CHROME_BROWSER_NAME, COMMAND_LINE_ARGUMENTS)).thenReturn(
                ARGS);
        try (var webDriverManagerStaticMock = mockStatic(WebDriverManager.class))
        {
            WebDriverManager webDriverManagerMock = mock();
            webDriverManagerStaticMock.when(WebDriverManager::chromedriver).thenReturn(webDriverManagerMock);
            testGetChromeWebDriver(new DesiredCapabilities(), "/path/to/binary", null, null,
                    args -> assertThat(args, hasItems(ARG_1, ARG_2)), chromeOptions -> { });
            verify(webDriverManagerMock).setup();
        }
    }

    @Test
    @ClearSystemProperty(key = ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY)
    void testGetWebDriverWithCommandLineArgumentsOverride() throws ReflectiveOperationException
    {
        when(webDriverStartContext.get(WebDriverStartParameters.COMMAND_LINE_ARGUMENTS)).thenReturn(ARGS);
        testGetChromeWebDriver(new DesiredCapabilities(), null, DRIVER_PATH, null,
                args -> assertThat(args, hasItems(ARG_1, ARG_2)), chromeOptions -> { });
    }

    @SuppressWarnings("unchecked")
    @Test
    @ClearSystemProperty(key = ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY)
    void testGetWebDriverWithWebDriverTypeAndExperimentalOptionsConfiguration() throws ReflectiveOperationException
    {
        testGetChromeWebDriver(new DesiredCapabilities(), null, DRIVER_PATH,
                "{\"mobileEmulation\": {\"deviceName\": \"iPhone 8\"}}", args -> { },
                chromeOptions -> {
                    var chromeOptionsCapabilities = (Map<String, Object>) chromeOptions.getCapability(
                            ChromeOptions.CAPABILITY);
                    assertEquals(Map.of("deviceName", "iPhone 8"), chromeOptionsCapabilities.get("mobileEmulation"));
                });
    }

    @SuppressWarnings("unchecked")
    private void testGetChromeWebDriver(DesiredCapabilities desiredCapabilities, String binaryPath, String driverPath,
            String experimentalOptions, Consumer<List<String>> argsValidator,
            Consumer<ChromeOptions> capabilitiesValidator) throws ReflectiveOperationException
    {
        var webDriverType = WebDriverType.CHROME;
        WebDriverListenerFactory webDriverListenerFactory = mock();
        var webDriverFactory = new WebDriverFactory(false, remoteWebDriverFactory, propertyParser, new JsonUtils(),
                proxy, webDriverStartContext, Optional.empty(), Optional.empty(), timeoutConfigurer, CREDENTIALS,
                List.of(webDriverListenerFactory));
        webDriverFactory.setWebDriverType(webDriverType);
        lenient().when(propertyParser.getPropertyValue(PROPERTY_FORMAT, CHROME_BROWSER_NAME, BINARY_PATH)).thenReturn(
                binaryPath);
        lenient().when(propertyParser.getPropertyValue(PROPERTY_FORMAT, CHROME_BROWSER_NAME, "driver-executable-path"))
                .thenReturn(driverPath);
        lenient().when(propertyParser.getPropertyValue(PROPERTY_FORMAT, CHROME_BROWSER_NAME, EXPERIMENTAL_OPTIONS))
                .thenReturn(experimentalOptions);
        Timeouts timeouts = mock();
        try (var chromeDriverMockedConstruction = mockConstruction(ChromeDriver.class, (mock, context) -> {
            mockCapabilities(mock);
            Options options = mock();
            when(mock.manage()).thenReturn(options);
            when(options.timeouts()).thenReturn(timeouts);
            assertEquals(1, context.getCount());
            var arguments = context.arguments();
            assertEquals(1, arguments.size());
            var chromeOptions = (ChromeOptions) arguments.get(0);
            var chromeOptionsCapabilities = (Map<String, Object>) chromeOptions.getCapability(ChromeOptions.CAPABILITY);
            assertAll(() -> {
                var args = (List<String>) chromeOptionsCapabilities.get("args");
                argsValidator.accept(args);
            }, () -> {
                var actualBinary = (String) chromeOptionsCapabilities.get("binary");
                assertEquals(binaryPath, actualBinary);
            }, () -> {
                capabilitiesValidator.accept(chromeOptions);
            });
        }))
        {
            testWebDriverCreation(desiredCapabilities, webDriverFactory, webDriverListenerFactory,
                    () -> chromeDriverMockedConstruction.constructed().get(0));
            assertEquals(driverPath, System.getProperty(ChromeDriverService.CHROME_DRIVER_EXE_PROPERTY));
            verify(timeoutConfigurer).configure(timeouts);
        }
        assertLogger();
    }

    @Test
    void testGetWebDriverWithCommandLineArgumentsOverrideNotSupported()
    {
        var webDriverType = WebDriverType.SAFARI;
        var webDriverFactory = new WebDriverFactory(false, remoteWebDriverFactory, propertyParser, new JsonUtils(),
                proxy, webDriverStartContext, Optional.empty(), Optional.empty(), timeoutConfigurer,
                CREDENTIALS, List.of());
        webDriverFactory.setWebDriverType(webDriverType);
        when(propertyParser.getPropertyValue(PROPERTY_FORMAT, SAFARI_BROWSER_NAME, BINARY_PATH)).thenReturn(null);
        when(webDriverStartContext.get(WebDriverStartParameters.COMMAND_LINE_ARGUMENTS)).thenReturn(ARG_1);
        DesiredCapabilities desiredCapabilities = mock();
        var exception = assertThrows(UnsupportedOperationException.class,
                () -> webDriverFactory.createWebDriver(desiredCapabilities));
        assertEquals(CLI_ARGS_NOT_SUPPORTED + webDriverType, exception.getMessage());
    }

    @Test
    void testGetWebDriverWithWebDriverTypeAndInvalidBinaryPathConfiguration()
    {
        var webDriverType = WebDriverType.SAFARI;
        var webDriverFactory = new WebDriverFactory(false, remoteWebDriverFactory, propertyParser, new JsonUtils(),
                proxy, webDriverStartContext, Optional.empty(), Optional.empty(), timeoutConfigurer,
                CREDENTIALS, List.of());
        webDriverFactory.setWebDriverType(webDriverType);
        when(propertyParser.getPropertyValue(PROPERTY_FORMAT, SAFARI_BROWSER_NAME, BINARY_PATH)).thenReturn("testPath");
        DesiredCapabilities desiredCapabilities = mock();
        var exception = assertThrows(UnsupportedOperationException.class,
                () -> webDriverFactory.createWebDriver(desiredCapabilities));
        assertEquals("Configuring of binary-path is not supported for " + webDriverType, exception.getMessage());
    }

    @Test
    void testGetWebDriverWithWebDriverTypeAndInvalidCommandLineArgumentsConfiguration()
    {
        var webDriverType = WebDriverType.SAFARI;
        var webDriverFactory = new WebDriverFactory(false, remoteWebDriverFactory, propertyParser, new JsonUtils(),
                proxy, webDriverStartContext, Optional.empty(), Optional.empty(), timeoutConfigurer,
                CREDENTIALS, List.of());
        webDriverFactory.setWebDriverType(webDriverType);
        when(propertyParser.getPropertyValue(PROPERTY_FORMAT, SAFARI_BROWSER_NAME, BINARY_PATH)).thenReturn(null);
        when(propertyParser.getPropertyValue(PROPERTY_FORMAT, SAFARI_BROWSER_NAME, COMMAND_LINE_ARGUMENTS)).thenReturn(
                ARG_1);
        DesiredCapabilities desiredCapabilities = mock();
        var exception = assertThrows(UnsupportedOperationException.class,
                () -> webDriverFactory.createWebDriver(desiredCapabilities));
        assertEquals(CLI_ARGS_NOT_SUPPORTED + webDriverType, exception.getMessage());
    }

    @Test
    void testGetRemoteWebDriver() throws ReflectiveOperationException
    {
        mockCapabilities(remoteWebDriver);
        var desiredCapabilities = new DesiredCapabilities();
        when(remoteWebDriverFactory.getRemoteWebDriver(desiredCapabilities)).thenReturn(remoteWebDriver);
        assertRemoteWebDriverCreation(desiredCapabilities);
        assertLogger();
    }

    @ParameterizedTest
    @CsvSource({
            "chrome, true",
            "internet explorer,",
            "SAFARI,"
    })
    void shouldSetAcceptInsecureCertsForSupportingBrowsers(String type, Boolean acceptsInsecureCerts)
            throws ReflectiveOperationException
    {
        mockCapabilities(remoteWebDriver);
        var desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setBrowserName(type);
        when(proxy.isStarted()).thenReturn(true);
        when(remoteWebDriverFactory.getRemoteWebDriver(argThat(capabilities -> {
            assertEquals(acceptsInsecureCerts, capabilities.getCapability(CapabilityType.ACCEPT_INSECURE_CERTS));
            return true;
        }))).thenReturn(remoteWebDriver);
        assertRemoteWebDriverCreation(desiredCapabilities);
        assertLogger();
    }

    @Test
    @SuppressWarnings("unchecked")
    void testGetRemoteWebDriverFirefoxDriver() throws ReflectiveOperationException
    {
        mockCapabilities(remoteWebDriver);
        var desiredCapabilities = new DesiredCapabilities(new FirefoxOptions());
        when(remoteWebDriverFactory.getRemoteWebDriver(argThat(caps -> {
            var options = (Map<String, Object>) caps.getCapability(FirefoxOptions.FIREFOX_OPTIONS);
            var prefs = (Map<String, Object>) options.get("prefs");
            return "about:blank".equals(prefs.get("startup.homepage_welcome_url.additional")) && "firefox".equals(
                    caps.getBrowserName());
        }))).thenReturn(remoteWebDriver);
        assertRemoteWebDriverCreation(desiredCapabilities);
        var sessionCaps = "{%n"
                + "  \"acceptInsecureCerts\" : true,%n"
                + "  \"browserName\" : \"firefox\",%n"
                + "  \"moz:firefoxOptions\" : {%n"
                + "    \"prefs\" : {%n"
                + "      \"remote.active-protocols\" : 1,%n"
                + "      \"startup.homepage_welcome_url.additional\" : \"about:blank\"%n"
                + "    }%n"
                + "  }%n}";
        assertThat(logger.getLoggingEvents(),
                is(List.of(info("Requested capabilities:\n{}", String.format(sessionCaps)),
                        info(SESSION_CAPABILITIES, CAPS_AS_STRING))));
    }

    @Test
    void testGetRemoteWebDriverMarionetteDriver() throws ReflectiveOperationException
    {
        mockCapabilities(remoteWebDriver);
        var desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setBrowserName("marionette");
        when(remoteWebDriverFactory.getRemoteWebDriver(desiredCapabilities)).thenReturn(remoteWebDriver);
        assertRemoteWebDriverCreation(desiredCapabilities);
        assertLogger();
    }

    @Test
    void testGetRemoteWebDriverIEDriver() throws ReflectiveOperationException
    {
        mockCapabilities(remoteWebDriver);
        var desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setBrowserName(Browser.IE.browserName());
        when(remoteWebDriverFactory.getRemoteWebDriver(any(DesiredCapabilities.class))).thenReturn(remoteWebDriver);
        assertRemoteWebDriverCreation(desiredCapabilities);
        assertLogger();
    }

    @Test
    void testGetRemoteWebDriverIsChromeWithAdditionalOptions() throws ReflectiveOperationException
    {
        var args = "disable-blink-features=BlockCredentialedSubresources";
        var chromeOptions = new ChromeOptions();
        chromeOptions.addArguments(args);
        chromeOptions.setExperimentalOption("w3c", Boolean.TRUE);
        lenient().doReturn(args).when(propertyParser).getPropertyValue(PROPERTY_FORMAT, CHROME_BROWSER_NAME,
                COMMAND_LINE_ARGUMENTS);
        lenient().doReturn("{\"w3c\":true}").when(propertyParser).getPropertyValue(PROPERTY_FORMAT, CHROME_BROWSER_NAME,
                EXPERIMENTAL_OPTIONS);
        testGetRemoteWebDriverIsChrome(chromeOptions);
    }

    @Test
    void testGetRemoteWebDriverIsChromeWithoutAdditionalOptions() throws ReflectiveOperationException
    {
        testGetRemoteWebDriverIsChrome(new ChromeOptions());
    }

    static Stream<Arguments> chromiumBrowsers()
    {
        return Stream.of(
                arguments(WebDriverType.CHROME, CHROME_BROWSER_NAME, ChromeDriver.class,
                        (Verification) WebDriverManager::chromedriver),
                arguments(WebDriverType.EDGE, EDGE_BROWSER_NAME, EdgeDriver.class,
                        (Verification) WebDriverManager::edgedriver),
                arguments(WebDriverType.OPERA, OPERA_BROWSER_NAME, ChromeDriver.class,
                        (Verification) WebDriverManager::operadriver)
        );
    }

    @ParameterizedTest
    @MethodSource("chromiumBrowsers")
    void shouldConfigureBasicAuthCredentialsForChromiumBrowsers(WebDriverType browserType, String browserName,
            Class<RemoteWebDriver> mockDriverClass, Verification webDriverManagerVerification)
    {
        Pattern url1 = Pattern.compile(".*site1.*");
        String user1 = "admin";
        String pass1 = "qwerty123";
        BasicAuthCredentials credentials1 = new BasicAuthCredentials();
        credentials1.setUrlRegex(url1);
        credentials1.setUsername(user1);
        credentials1.setPassword(pass1);
        Pattern url2 = Pattern.compile(".*site2.*");
        String user2 = "user";
        String pass2 = "password";
        BasicAuthCredentials credentials2 = new BasicAuthCredentials();
        credentials2.setUrlRegex(url2);
        credentials2.setUsername(user2);
        credentials2.setPassword(pass2);
        Map<String, BasicAuthCredentials> credentials = Map.of("site1", credentials1, "site2", credentials2);
        PropertyMappedCollection<BasicAuthCredentials> creds = new PropertyMappedCollection<>(credentials);

        var webDriverFactory = new WebDriverFactory(false, remoteWebDriverFactory, propertyParser, new JsonUtils(),
                proxy, webDriverStartContext, Optional.empty(), Optional.empty(), timeoutConfigurer, creds, List.of());
        webDriverFactory.setWebDriverType(browserType);

        try (var mockedConstruction = mockConstruction(mockDriverClass, (mock, context) ->
        {
            when(mock.getCapabilities()).thenReturn(new DesiredCapabilities(Map.of(BROWSER_NAME, browserName)));
            mockTimeouts(mock);
        });
            var webDriverManagerStaticMock = mockStatic(WebDriverManager.class))
        {
            WebDriverManager webDriverManager = mock();
            webDriverManagerStaticMock.when(webDriverManagerVerification).thenReturn(webDriverManager);
            webDriverFactory.createWebDriver(new DesiredCapabilities());
            WebDriver driver = mockedConstruction.constructed().get(0);
            HasAuthentication hasAuthentication = (HasAuthentication) driver;
            verify(hasAuthentication).register(argThat(p -> p.test(URI.create("https://site1.com"))), argThat(c ->
            {
                UsernameAndPassword usernameAndPassword = (UsernameAndPassword) c.get();
                return usernameAndPassword.username().equals(user1) && usernameAndPassword.password().equals(pass1);
            }));
            verify(hasAuthentication).register(argThat(p -> p.test(URI.create("https://site2.com"))), argThat(c ->
            {
                UsernameAndPassword usernameAndPassword = (UsernameAndPassword) c.get();
                return usernameAndPassword.username().equals(user2) && usernameAndPassword.password().equals(pass2);
            }));
            verify(webDriverManager).setup();
        }
    }

    static Stream<Arguments> nonChromiumOrMobileBrowsers()
    {
        return Stream.of(
                arguments(FIREFOX_BROWSER_NAME, FirefoxDriver.class),
                arguments(SAFARI_BROWSER_NAME, SafariDriver.class),
                arguments(CHROME_BROWSER_NAME, AndroidDriver.class),
                arguments(SAFARI_BROWSER_NAME, io.appium.java_client.safari.SafariDriver.class));
    }

    @ParameterizedTest
    @MethodSource("nonChromiumOrMobileBrowsers")
    void shouldNotConfigureBasicAuthCredentialsForRemoteNonChromiumOrMobileBrowsers(String browserName,
            Class<RemoteWebDriver> mockDriverClass)
    {
        var credentialsMock = mock(PropertyMappedCollection.class);
        var webDriverFactory = new WebDriverFactory(true, remoteWebDriverFactory, propertyParser, new JsonUtils(),
                proxy, webDriverStartContext, Optional.empty(), Optional.empty(), timeoutConfigurer,
                credentialsMock, List.of());
        var remoteDriver = mock(mockDriverClass);
        when(remoteWebDriverFactory.getRemoteWebDriver(any())).thenReturn(remoteDriver);
        when(remoteDriver.getCapabilities()).thenReturn(new DesiredCapabilities(Map.of(BROWSER_NAME, browserName)));
        mockTimeouts(remoteDriver);
        webDriverFactory.createWebDriver(new DesiredCapabilities());
        verifyNoInteractions(credentialsMock);
    }

    private void mockTimeouts(WebDriver driver)
    {
        Options options = mock();
        Timeouts timeouts = mock();
        when(driver.manage()).thenReturn(options);
        when(options.timeouts()).thenReturn(timeouts);
    }

    private void testGetRemoteWebDriverIsChrome(ChromeOptions chromeOptions) throws ReflectiveOperationException
    {
        mockCapabilities(remoteWebDriver);
        var desiredCapabilities = new DesiredCapabilities();
        desiredCapabilities.setBrowserName(Browser.CHROME.browserName());
        desiredCapabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions);
        when(remoteWebDriverFactory.getRemoteWebDriver(desiredCapabilities)).thenReturn(remoteWebDriver);
        assertRemoteWebDriverCreation(desiredCapabilities);
        assertLogger();
    }

    private void assertRemoteWebDriverCreation(DesiredCapabilities desiredCapabilities)
            throws ReflectiveOperationException
    {
        Options options = mock();
        when(remoteWebDriver.manage()).thenReturn(options);
        Timeouts timeouts = mock();
        when(options.timeouts()).thenReturn(timeouts);

        WebDriverListenerFactory webDriverListenerFactory = mock();
        var webDriverFactory = new WebDriverFactory(true, remoteWebDriverFactory, propertyParser, new JsonUtils(),
                proxy, webDriverStartContext, Optional.empty(), Optional.empty(), timeoutConfigurer, CREDENTIALS,
                List.of(webDriverListenerFactory));
        testWebDriverCreation(desiredCapabilities, webDriverFactory, webDriverListenerFactory, () -> remoteWebDriver);
        verify(timeoutConfigurer).configure(timeouts);
    }

    @SuppressWarnings("unchecked")
    private static void testWebDriverCreation(DesiredCapabilities desiredCapabilities,
            WebDriverFactory webDriverFactory, WebDriverListenerFactory webDriverListenerFactory,
            Supplier<WebDriver> expectedSupplier) throws ReflectiveOperationException
    {
        WebDriverListener webDriverListener = mock();
        when(webDriverListenerFactory.createListener(any(WebDriver.class))).thenReturn(webDriverListener);

        var decoratedDriver = webDriverFactory.createWebDriver(desiredCapabilities);

        assertThat(decoratedDriver, instanceOf(Decorated.class));
        var textFormattingDriver = ((Decorated<TextFormattingWebDriver>) decoratedDriver).getOriginal();
        assertEquals(expectedSupplier.get(), textFormattingDriver.getWrappedDriver());

        decoratedDriver.getCurrentUrl();
        verify(webDriverListener).beforeAnyWebDriverCall(textFormattingDriver,
                WebDriver.class.getMethod("getCurrentUrl"), null);
    }

    private static void mockCapabilities(HasCapabilities hasCapabilities)
    {
        Capabilities capabilities = mock();
        when(hasCapabilities.getCapabilities()).thenReturn(capabilities);
        when(capabilities.asMap()).thenReturn(Map.of("key", "value"));
    }

    private void assertLogger()
    {
        assertThat(logger.getLoggingEvents(), hasItem(info(SESSION_CAPABILITIES, CAPS_AS_STRING)));
    }
}
