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

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpConnectTimeoutException;
import java.net.http.HttpTimeoutException;
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
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Platform;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.HttpCommandExecutor;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.Response;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.MobilePlatform;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class RemoteWebDriverFactoryTests
{
    private static final URI GRID_URI = URI.create("http://sel.grid");

    private static final String COULD_NOT_START_A_NEW_SESSION = "Could not start a new session";
    private static final String HTTP_CONNECT_TIMED_OUT = "HTTP connect timed out";

    @Mock private URL url;
    @Mock private RemoteWebDriverUrlProvider provider;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(RemoteWebDriverFactory.class);

    @BeforeEach
    void init()
    {
        when(provider.getRemoteDriverUrl()).thenReturn(url);
    }

    static Stream<Arguments> platforms()
    {
        return Stream.of(
                arguments(Platform.IOS, true, IOSDriver.class),
                arguments(Platform.IOS, false, RemoteWebDriver.class),
                arguments(MobilePlatform.TVOS, true, IOSDriver.class),
                arguments(MobilePlatform.TVOS, false, RemoteWebDriver.class),
                arguments(Platform.ANDROID, true, AndroidDriver.class),
                arguments(Platform.ANDROID, false, RemoteWebDriver.class),
                arguments(Platform.WINDOWS, true, RemoteWebDriver.class),
                arguments(Platform.WINDOWS, false, RemoteWebDriver.class)
        );
    }

    @MethodSource("platforms")
    @ParameterizedTest
    void shouldCreateDriver(Object platform, boolean useW3C, Class<?> webDriveClass)
    {
        var capabilities = new DesiredCapabilities();
        capabilities.setCapability(CapabilityType.PLATFORM_NAME, platform);
        try (var driver = mockConstruction(webDriveClass,
                (mock, context) -> assertEquals(context.arguments(), List.of(url, capabilities))))
        {
            var actualDriver = new RemoteWebDriverFactory(useW3C, false, provider).getRemoteWebDriver(capabilities);
            assertEquals(driver.constructed(), List.of(actualDriver));
            assertThat(logger.getLoggingEvents(), is(empty()));
        }
    }

    @Test
    void shouldRetrySessionCreationOnHttpConnectTimeoutSuccessfully() throws URISyntaxException
    {
        var capabilities = Map.of(CapabilityType.PLATFORM_NAME, Platform.LINUX.name());
        var desiredCapabilities = new DesiredCapabilities(capabilities);
        var sessionId = "mocked-session-id";
        var exception = new SessionNotCreatedException(COULD_NOT_START_A_NEW_SESSION,
                new TimeoutException(HTTP_CONNECT_TIMED_OUT, new HttpConnectTimeoutException(HTTP_CONNECT_TIMED_OUT))
        );
        try (var ignored = mockConstruction(HttpCommandExecutor.class, (mock, context) -> {
            if (context.getCount() == 1)
            {
                when(mock.execute(any())).thenThrow(exception);
            }
            else
            {
                var response = new Response();
                response.setValue(capabilities);
                response.setSessionId(sessionId);
                when(mock.execute(any())).thenReturn(response);
            }
        }))
        {
            when(url.toURI()).thenReturn(GRID_URI);
            var actualDriver = new RemoteWebDriverFactory(true, true, provider).getRemoteWebDriver(desiredCapabilities);
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
                arguments(new SessionNotCreatedException(COULD_NOT_START_A_NEW_SESSION,
                        new TimeoutException("HttpTimeoutException: HTTP read timed out",
                                new HttpTimeoutException("HTTP read timed out")
                        )), true),
                arguments(new SessionNotCreatedException(COULD_NOT_START_A_NEW_SESSION, new IOException("IOException")),
                        true),
                arguments(new SessionNotCreatedException(COULD_NOT_START_A_NEW_SESSION,
                        new TimeoutException(HTTP_CONNECT_TIMED_OUT,
                                new HttpConnectTimeoutException(HTTP_CONNECT_TIMED_OUT))), false)
        );
    }

    @MethodSource("exceptions")
    @ParameterizedTest
    void shouldRetrySessionCreationOnHttpConnectTimeoutUnsuccessfully(SessionNotCreatedException exception,
            boolean retrySessionCreationOnHttpConnectTimeout) throws URISyntaxException
    {
        var capabilities = Map.of(CapabilityType.PLATFORM_NAME, Platform.LINUX.name());
        var desiredCapabilities = new DesiredCapabilities(capabilities);
        try (var ignored = mockConstruction(HttpCommandExecutor.class,
                (mock, context) -> when(mock.execute(any())).thenThrow(exception)))
        {
            when(url.toURI()).thenReturn(GRID_URI);
            var remoteWebDriverFactory = new RemoteWebDriverFactory(true, retrySessionCreationOnHttpConnectTimeout,
                    provider);
            var actualException = assertThrows(SessionNotCreatedException.class,
                    () -> remoteWebDriverFactory.getRemoteWebDriver(desiredCapabilities));
            assertEquals(exception, actualException);
            assertThat(logger.getLoggingEvents(), is(empty()));
        }
    }
}
