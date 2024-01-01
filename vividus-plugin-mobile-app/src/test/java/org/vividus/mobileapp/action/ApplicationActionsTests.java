/*
 * Copyright 2019-2024 the original author or authors.
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
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.HasCapabilities;
import org.openqa.selenium.MutableCapabilities;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.ISoftAssert;

import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.appmanagement.ApplicationState;

@ExtendWith(MockitoExtension.class)
class ApplicationActionsTests
{
    private static final String BUNDLE_ID = "bundleId";
    private static final String UNKNOWN_BUNDLE_ID = "unknown bundle id";
    private static final String APP = "app";
    private static final String APP_NAME = "vividus-mobile.app";

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private ISoftAssert softAssert;
    @InjectMocks private ApplicationActions applicationActions;

    @Test
    void shouldActivateApp()
    {
        InteractsWithApps driver = mockInteractingWithAppsDriver();
        when(driver.isAppInstalled(BUNDLE_ID)).thenReturn(true);
        applicationActions.activateApp(BUNDLE_ID);
        verify(driver).activateApp(BUNDLE_ID);
    }

    @Test
    void shouldNotActivateNotInstalledApp()
    {
        InteractsWithApps driver = mockInteractingWithAppsDriver();
        when(driver.isAppInstalled(UNKNOWN_BUNDLE_ID)).thenReturn(false);
        Exception exception = assertThrows(IllegalArgumentException.class,
            () -> applicationActions.activateApp(UNKNOWN_BUNDLE_ID));
        assertEquals(
            String.format("Application with the bundle identifier '%s' is not installed on the device",
                    UNKNOWN_BUNDLE_ID),
            exception.getMessage());
    }

    @Test
    void shouldTerminateApp()
    {
        InteractsWithApps driver = mockInteractingWithAppsDriver();
        when(driver.queryAppState(BUNDLE_ID)).thenReturn(ApplicationState.RUNNING_IN_FOREGROUND);
        when(driver.terminateApp(BUNDLE_ID)).thenReturn(true);
        applicationActions.terminateApp(BUNDLE_ID);
        verify(driver).terminateApp(BUNDLE_ID);
    }

    @Test
    void shouldReinstallApp()
    {
        InteractsWithApps driver = mockInteractingWithAppsDriver();
        HasCapabilities hasCapabilities = mockCapabilities();
        when(driver.removeApp(BUNDLE_ID)).thenReturn(true);

        applicationActions.reinstallApplication(BUNDLE_ID);
        verify(driver).removeApp(BUNDLE_ID);
        verify(driver).installApp(APP_NAME);
        verify(hasCapabilities).getCapabilities();
    }

    @Test
    void shouldThrowExceptionIfRemovingFailure()
    {
        InteractsWithApps driver = mockInteractingWithAppsDriver();
        mockCapabilities();
        when(driver.removeApp(BUNDLE_ID)).thenReturn(false);

        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> applicationActions.reinstallApplication(BUNDLE_ID));
        assertEquals(
                String.format("Unable to remove mobile application with the bundle identifier '%s'",
                        BUNDLE_ID),
                exception.getMessage());
        verifyNoMoreInteractions(driver);
    }

    @Test
    void shouldNotTerminateNotInstalledApp()
    {
        InteractsWithApps driver = mockInteractingWithAppsDriver();
        when(driver.queryAppState(UNKNOWN_BUNDLE_ID)).thenReturn(ApplicationState.NOT_RUNNING);
        applicationActions.terminateApp(UNKNOWN_BUNDLE_ID);
        verify(softAssert).recordFailedAssertion(
                "Application with the bundle identifier 'unknown bundle id' is not running on the device");
    }

    @Test
    void shouldNotTerminateNotRunningApp()
    {
        InteractsWithApps driver = mockInteractingWithAppsDriver();
        when(driver.queryAppState(UNKNOWN_BUNDLE_ID)).thenReturn(ApplicationState.NOT_INSTALLED);
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> applicationActions.terminateApp(UNKNOWN_BUNDLE_ID));
        assertEquals(String.format("Application with the bundle identifier" + " '%s' is not installed on the device",
                UNKNOWN_BUNDLE_ID), exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfTerminationFailure()
    {
        InteractsWithApps driver = mockInteractingWithAppsDriver();
        when(driver.queryAppState(UNKNOWN_BUNDLE_ID)).thenReturn(ApplicationState.RUNNING_IN_FOREGROUND);
        when(driver.terminateApp(UNKNOWN_BUNDLE_ID)).thenReturn(false);
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> applicationActions.terminateApp(UNKNOWN_BUNDLE_ID));
        assertEquals(
                String.format("Unable to terminate mobile application with the bundle identifier '%s'",
                        UNKNOWN_BUNDLE_ID),
                exception.getMessage());
    }

    private InteractsWithApps mockInteractingWithAppsDriver()
    {
        InteractsWithApps driver = mock(InteractsWithApps.class);
        when(webDriverProvider.getUnwrapped(InteractsWithApps.class)).thenReturn(driver);
        return driver;
    }

    private HasCapabilities mockCapabilities()
    {
        HasCapabilities hasCapabilities = mock(HasCapabilities.class);
        when(hasCapabilities.getCapabilities()).thenReturn(new MutableCapabilities(Map.of(APP, APP_NAME)));
        when(webDriverProvider.getUnwrapped(HasCapabilities.class)).thenReturn(hasCapabilities);
        return hasCapabilities;
    }
}
