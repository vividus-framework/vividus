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

package org.vividus.mobileapp.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.mobileapp.action.NetworkActions;
import org.vividus.mobileapp.action.NetworkActions.NetworkMode;
import org.vividus.mobileapp.action.NetworkActions.NetworkToggle;
import org.vividus.mobileapp.configuration.MobileEnvironment;
import org.vividus.selenium.manager.GenericWebDriverManager;

@ExtendWith(MockitoExtension.class)
class NetworkStepsTests
{
    @Mock private MobileEnvironment mobileEnvironment;
    @Mock private GenericWebDriverManager genericWebDriverManager;
    @Mock private NetworkActions networkActions;
    @InjectMocks private NetworkSteps networkSteps;

    @ParameterizedTest
    @EnumSource(value = NetworkMode.class, names = {"WIFI_AND_MOBILE_DATA", "AIRPLANE_MODE"})
    void shouldFailToEnableUnsupportedNetworkConnectionForIOS(NetworkMode mode)
    {
        when(genericWebDriverManager.isAndroid()).thenReturn(false);
        when(genericWebDriverManager.isIOS()).thenReturn(true);
        when(mobileEnvironment.isRealDevice()).thenReturn(true);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> networkSteps.changeNetworkConnection(NetworkToggle.OFF, mode));
        assertEquals(String.format("%s is not supported for iOS", mode), exception.getMessage());
    }

    @Test
    void shouldFailForIOSSimulator()
    {
        when(genericWebDriverManager.isIOS()).thenReturn(true);
        when(mobileEnvironment.isRealDevice()).thenReturn(false);
        testUnsupportedPlatform();
    }

    @Test
    void shouldFailForUnsupportedPlatform()
    {
        when(genericWebDriverManager.isIOS()).thenReturn(false);
        testUnsupportedPlatform();
    }

    private void testUnsupportedPlatform()
    {
        when(genericWebDriverManager.isAndroid()).thenReturn(false);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> networkSteps.changeNetworkConnection(NetworkToggle.ON, NetworkMode.WIFI));
        assertEquals("Network connection can be changed only for Android emulators, Android and iOS real devices",
                exception.getMessage());
    }

    @ParameterizedTest
    @EnumSource(value = NetworkMode.class, names = {"WIFI", "MOBILE_DATA"})
    void shouldEnableNetworkConnectionForIOS(NetworkMode mode)
    {
        when(genericWebDriverManager.isAndroid()).thenReturn(false);
        when(genericWebDriverManager.isIOS()).thenReturn(true);
        when(mobileEnvironment.isRealDevice()).thenReturn(true);
        networkSteps.changeNetworkConnection(NetworkToggle.ON, mode);
        verify(networkActions).changeNetworkConnectionState(NetworkToggle.ON, mode);
    }

    @ParameterizedTest
    @EnumSource(NetworkMode.class)
    void shouldEnableNetworkConnectionForAndroid(NetworkMode mode)
    {
        when(genericWebDriverManager.isAndroid()).thenReturn(true);
        networkSteps.changeNetworkConnection(NetworkToggle.OFF, mode);
        verify(networkActions).changeNetworkConnectionState(NetworkToggle.OFF, mode);
    }
}
