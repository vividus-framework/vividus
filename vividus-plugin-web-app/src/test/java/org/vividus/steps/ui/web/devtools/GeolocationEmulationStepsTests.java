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

package org.vividus.steps.ui.web.devtools;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.cdp.BrowserPermissions;
import org.vividus.selenium.manager.WebDriverManager;
import org.vividus.ui.web.cdp.CdpClient;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class GeolocationEmulationStepsTests
{
    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(GeolocationEmulationSteps.class);

    @Mock private CdpClient cdpClient;
    @Mock private WebDriverManager webDriverManager;
    @InjectMocks private GeolocationEmulationSteps steps;

    static Stream<Arguments> geolocationState()
    {
        return Stream.of(
            arguments(false, 1, List.of(info("The browser has been granted the permission to track geolocation"))),
            arguments(true, 0, List.of())
        );
    }

    @MethodSource("geolocationState")
    @ParameterizedTest
    void shouldEmulateGeolocation(boolean initialState, int permissionSetInvocations, List<LoggingEvent> messages)
    {
        BrowserPermissions permissions = new BrowserPermissions();
        permissions.setGeolocationEnabled(initialState);
        when(webDriverManager.getBrowserPermissions()).thenReturn(permissions);

        double latitude = 55.410_014;
        double longitude = 28.628_027;

        steps.emulateGeolocation(latitude, longitude);

        verify(cdpClient, times(permissionSetInvocations)).executeCdpCommand("Browser.grantPermissions", Map.of(
                "permissions", List.of("geolocation")
        ));
        verify(cdpClient).executeCdpCommand("Emulation.setGeolocationOverride", Map.of(
                "latitude", latitude,
                "longitude", longitude,
                "accuracy", 1
        ));
        assertTrue(permissions.isGeolocationEnabled());
        assertThat(testLogger.getLoggingEvents(), is(messages));
    }

    @Test
    void shouldClearGeolocationParameters()
    {
        steps.clearGeolocationParameters();
        verify(cdpClient).executeCdpCommand("Emulation.clearGeolocationOverride");
    }
}
