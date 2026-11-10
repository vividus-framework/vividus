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

package org.vividus.steps.ui.web.bidi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.bidi.module.Permission;
import org.openqa.selenium.bidi.permissions.PermissionState;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.util.UriUtils;

@ExtendWith(MockitoExtension.class)
class BrowserPermissionsStepsTests
{
    private static final String ORIGIN = "https://example.com";
    private static final String BROWSER_PERMISSION = "someBrowserPermission";

    @Mock private RemoteWebDriver driver;
    @Mock private IWebDriverProvider webDriverProvider;
    @InjectMocks private BrowserPermissionsSteps steps;

    @Test
    void shouldNotSetBrowserPermissionsIfCurrentPageIsBlank()
    {
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getCurrentUrl()).thenReturn("about:blank");

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
                () -> steps.setBrowserPermission(BROWSER_PERMISSION, PermissionState.GRANTED));
        assertEquals("Current page should not be a blank", thrown.getMessage());
    }

    @Test
    void shouldSetBrowserPermissions()
    {
        when(webDriverProvider.get()).thenReturn(driver);
        when(driver.getCurrentUrl()).thenReturn(ORIGIN);
        when(webDriverProvider.getUnwrapped(RemoteWebDriver.class)).thenReturn(driver);

        try (MockedConstruction<Permission> permission = Mockito.mockConstruction(Permission.class))
        {
            steps.setBrowserPermission("some browser permission", PermissionState.GRANTED);
            verifyPermission(permission.constructed().get(0), PermissionState.GRANTED);
        }
    }

    @Test
    void shouldSetBrowserPermissionsForOrigin()
    {
        when(webDriverProvider.getUnwrapped(RemoteWebDriver.class)).thenReturn(driver);

        try (MockedConstruction<Permission> permission = Mockito.mockConstruction(Permission.class))
        {
            steps.setBrowserPermissionForOrigin(BROWSER_PERMISSION, PermissionState.PROMPT, UriUtils.createUri(ORIGIN));
            verifyPermission(permission.constructed().get(0), PermissionState.PROMPT);
        }
    }

    @Test
    void shouldConfigureBrowserPermissions()
    {
        when(webDriverProvider.getUnwrapped(RemoteWebDriver.class)).thenReturn(driver);

        try (MockedConstruction<Permission> permission = Mockito.mockConstruction(Permission.class))
        {
            BrowserPermission browserPermissions = new BrowserPermission();
            browserPermissions.setOrigin(UriUtils.createUri(ORIGIN));
            browserPermissions.setPermissionName(BROWSER_PERMISSION);
            browserPermissions.setState(PermissionState.DENIED);

            steps.configureBrowserPermissions(List.of(browserPermissions));
            verifyPermission(permission.constructed().get(0), PermissionState.DENIED);
        }
    }

    private void verifyPermission(Permission permission, PermissionState state)
    {
        verify(permission).setPermission(Map.of("name", BROWSER_PERMISSION), state, ORIGIN);
    }
}
