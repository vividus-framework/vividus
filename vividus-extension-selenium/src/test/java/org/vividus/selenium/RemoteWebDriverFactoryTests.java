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

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Platform;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.internal.Either;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.Dialect;
import org.openqa.selenium.remote.NewSessionPayload;
import org.openqa.selenium.remote.ProtocolHandshake;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.RemoteWebDriverBuilder;
import org.openqa.selenium.remote.Response;
import org.openqa.selenium.remote.http.ClientConfig;
import org.openqa.selenium.remote.http.HttpHandler;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobilePlatform;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class RemoteWebDriverFactoryTests
{
    private static final URI GRID_URI = URI.create("http://sel.grid");

    private static final String HTTP_CONNECT_TIMED_OUT = "Response code 500. Message: "
            + "java.net.http.HttpConnectTimeoutException: HTTP connect timed out";
    private static final Duration READ_TIMEOUT = Duration.ofMinutes(3);

    @Mock private RemoteWebDriverUrlProvider provider;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(
            SessionCreationRetryOnHttpConnectTimeoutHandler.class);

    @BeforeEach
    void init() throws URISyntaxException
    {
        URL url = mock();
        when(provider.getRemoteDriverUrl()).thenReturn(url);
        when(url.toURI()).thenReturn(GRID_URI);
    }

    private static void assertClientConfig(ClientConfig clientConfig, String expectedHttpVersion)
    {
        assertEquals(GRID_URI, clientConfig.baseUri());
        assertEquals(READ_TIMEOUT, clientConfig.readTimeout());
        assertEquals(expectedHttpVersion, clientConfig.version());
    }

    static Stream<Arguments> platforms()
    {
        return Stream.of(
                arguments(Platform.IOS, IOSDriver.class),
                arguments(MobilePlatform.TVOS, IOSDriver.class),
                arguments(Platform.ANDROID, AndroidDriver.class)
        );
    }

    @MethodSource("platforms")
    @ParameterizedTest
    void shouldCreateMobileDriver(Object platform, Class<?> webDriveClass)
    {
        var capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.PLATFORM_NAME, platform);
        try (var driver = mockConstruction(webDriveClass,
                (mock, context) -> {
                    var arguments = context.arguments();
                    assertEquals(2, arguments.size());
                    assertAll(
                            () -> {
                                var argument1 = arguments.get(0);
                                assertInstanceOf(ClientConfig.class, argument1);
                                assertClientConfig((ClientConfig) argument1, "HTTP_1_1");
                            },
                            () -> assertEquals(capabilities, arguments.get(1))
                    );
                }))
        {
            var actualDriver = new RemoteWebDriverFactory(READ_TIMEOUT, provider, List.of()).getRemoteWebDriver(
                    capabilities);
            assertEquals(driver.constructed(), List.of(actualDriver));
            assertThat(logger.getLoggingEvents(), is(empty()));
        }
    }

    @Test
    void shouldCreateRemoteDriver()
    {
        var capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.PLATFORM_NAME, Platform.WINDOWS);
        try (var driverMock = mockStatic(RemoteWebDriver.class))
        {
            var remoteWebDriverBuilder = mock(RemoteWebDriverBuilder.class, RETURNS_SELF);
            RemoteWebDriver remoteWebDriver = mock();
            when(remoteWebDriverBuilder.build()).thenReturn(remoteWebDriver);
            driverMock.when(RemoteWebDriver::builder).thenReturn(remoteWebDriverBuilder);
            var actualDriver = new RemoteWebDriverFactory(READ_TIMEOUT, provider, List.of()).getRemoteWebDriver(
                    capabilities);
            assertEquals(remoteWebDriver, actualDriver);
            assertThat(logger.getLoggingEvents(), is(empty()));
            var clientConfigArgumentCaptor = ArgumentCaptor.forClass(ClientConfig.class);
            InOrder ordered = inOrder(remoteWebDriverBuilder);
            ordered.verify(remoteWebDriverBuilder).config(clientConfigArgumentCaptor.capture());
            ordered.verify(remoteWebDriverBuilder).oneOf(capabilities);
            ordered.verify(remoteWebDriverBuilder).build();
            ordered.verifyNoMoreInteractions();
            ClientConfig clientConfig = clientConfigArgumentCaptor.getValue();
            assertClientConfig(clientConfig, null);
        }
    }

    @Test
    void shouldRetrySessionCreationOnHttpConnectTimeoutSuccessfully()
    {
        var capabilities = Map.of(CapabilityType.PLATFORM_NAME, Platform.LINUX.name());
        var desiredCapabilities = new DesiredCapabilities(capabilities);
        var sessionId = "mocked-session-id";
        var exception = new SessionNotCreatedException(HTTP_CONNECT_TIMED_OUT);
        try (var ignored = mockConstruction(ProtocolHandshake.class, (mock, context) -> {
            if (context.getCount() == 1)
            {
                when(mock.createSession(any(HttpHandler.class), any(NewSessionPayload.class))).thenReturn(
                        Either.left(exception));
            }
            else
            {
                var response = new Response();
                response.setValue(capabilities);
                response.setSessionId(sessionId);
                response.setState("success");
                ProtocolHandshake.Result result = mock();
                when(result.getDialect()).thenReturn(Dialect.W3C);
                when(result.createResponse()).thenReturn(response);
                when(mock.createSession(any(HttpHandler.class), any(NewSessionPayload.class))).thenReturn(
                        Either.right(result));
            }
        }))
        {
            List<SessionCreationRetryHandler> retryHandlers = List.of(
                    new SessionCreationRetryOnHttpConnectTimeoutHandler(true));
            var actualDriver = new RemoteWebDriverFactory(READ_TIMEOUT, provider, retryHandlers).getRemoteWebDriver(
                    desiredCapabilities);
            assertEquals(sessionId, actualDriver.getSessionId().toString());
            assertThat(logger.getLoggingEvents(), is(List.of(
                    warn(exception, "Failed to create a new session due to HTTP connect timout"),
                    warn("Retrying to create a new session")
            )));
        }
    }

    static Stream<Arguments> exceptions()
    {
        return Stream.of(
                arguments(new SessionNotCreatedException(
                        "Response code 500. Message: java.net.http.HttpTimeoutException: HTTP read timed out"), true),
                arguments(new SessionNotCreatedException(HTTP_CONNECT_TIMED_OUT), false)
        );
    }

    @MethodSource("exceptions")
    @ParameterizedTest
    void shouldRetrySessionCreationOnHttpConnectTimeoutUnsuccessfully(SessionNotCreatedException exception,
            boolean retrySessionCreationOnHttpConnectTimeout)
    {
        var capabilities = Map.of(CapabilityType.PLATFORM_NAME, Platform.LINUX.name());
        var desiredCapabilities = new DesiredCapabilities(capabilities);
        try (var ignored = mockConstruction(ProtocolHandshake.class, (mock, context) ->
            when(mock.createSession(any(HttpHandler.class), any(NewSessionPayload.class))).thenReturn(
                    Either.left(exception))
        ))
        {
            List<SessionCreationRetryHandler> retryHandlers = List.of(
                    new SessionCreationRetryOnHttpConnectTimeoutHandler(retrySessionCreationOnHttpConnectTimeout));
            var remoteWebDriverFactory = new RemoteWebDriverFactory(READ_TIMEOUT, provider, retryHandlers);
            var actualException = assertThrows(SessionNotCreatedException.class,
                    () -> remoteWebDriverFactory.getRemoteWebDriver(desiredCapabilities));
            assertEquals(exception, actualException);
            assertThat(logger.getLoggingEvents(), is(empty()));
        }
    }
}
