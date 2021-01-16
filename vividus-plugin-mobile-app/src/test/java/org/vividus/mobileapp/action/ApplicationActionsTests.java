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

package org.vividus.mobileapp.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.IWebDriverProvider;

import io.appium.java_client.InteractsWithApps;

@ExtendWith(MockitoExtension.class)
class ApplicationActionsTests
{
    @Mock private IWebDriverProvider webDriverProvider;
    @InjectMocks private ApplicationActions applicationActions;

    @Test
    void shouldActivateApp()
    {
        InteractsWithApps driver = mockInteractingWithAppsDriver();
        String bundleId = "bundleId";
        when(driver.isAppInstalled(bundleId)).thenReturn(true);
        applicationActions.activateApp(bundleId);
        verify(driver).activateApp(bundleId);
    }

    @Test
    void shouldNotActivateNotInstalledApp()
    {
        InteractsWithApps driver = mockInteractingWithAppsDriver();
        String bundleId = "unknown bundle id";
        when(driver.isAppInstalled(bundleId)).thenReturn(false);
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> applicationActions.activateApp(bundleId));
        assertEquals(
            String.format("Application with the bundle identifier '%s' is not installed on the device", bundleId),
            exception.getMessage());
    }

    private InteractsWithApps mockInteractingWithAppsDriver()
    {
        InteractsWithApps driver = mock(InteractsWithApps.class);
        when(webDriverProvider.getUnwrapped(InteractsWithApps.class)).thenReturn(driver);
        return driver;
    }
}
