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

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jbehave.core.annotations.When;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.bidi.module.Permission;
import org.openqa.selenium.bidi.permissions.PermissionState;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.util.UriUtils;

public class BrowserPermissionsSteps
{
    private final IWebDriverProvider webDriverProvider;

    public BrowserPermissionsSteps(IWebDriverProvider webDriverProvider)
    {
        this.webDriverProvider = webDriverProvider;
    }

    /**
     * Configures desired browser permission for currently opened page. You might need to refresh the page to make
     * permission changes applied.
     * <br>
     * <br>
     * <b>IMPORTANT</b>: The steps requires current page to be non-blank page.
     *
     * @param permission The browser permission to configure, please see
     * <a href="https://www.w3.org/TR/permissions/#permissions">Permissions API</a>
     * @param state The state, either <b>denied</b>, <b>granted</b> or <b>prompt</b>.
     */
    @When("I set state of `$permission` browser permission to `$state`")
    public void setBrowserPermission(String permission, PermissionState state)
    {
        URI origin = UriUtils.createUri(webDriverProvider.get().getCurrentUrl());
        Validate.isTrue(origin.getHost() != null, "Current page should not be a blank");
        configureBrowserPermissions(List.of(new BrowserPermission(permission, state, origin)));
    }

    /**
     * Configures desired browser permission for the specified origin.
     *
     * @param permission The browser permission to configure, please see
     * <a href="https://www.w3.org/TR/permissions/#permissions">Permissions API</a>
     * @param state The state, either <b>denied</b>, <b>granted</b> or <b>prompt</b>.
     * @param origin The origin to configure permission for.
     */
    @When("I set state of `$permission` browser permission to `$state` for `$origin` origin")
    public void setBrowserPermissionForOrigin(String permission, PermissionState state, URI origin)
    {
        configureBrowserPermissions(List.of(new BrowserPermission(permission, state, origin)));
    }

    /**
     * Configures multiple browser permissions
     * <br>
     * Parameters:
     * <ul>
     * <li><b>permissionName</b> - The browser permission to configure, please see
     * <a href="https://www.w3.org/TR/permissions/#permissions">Permissions API</a></li>
     * <li><b>state</b> - The state, either <b>denied</b>, <b>granted</b> or <b>prompt</b>.</li>
     * <li><b>origin</b> - The origin to configure permission for.</li>
     * </ul>
     * @param permissions The permissions to configure
     */
    @When("I configure browser permissions:$permissions")
    public void configureBrowserPermissions(List<BrowserPermission> permissions)
    {
        WebDriver driver = webDriverProvider.getUnwrapped(RemoteWebDriver.class);
        Permission permission = new Permission(driver);
        permissions.forEach(prmsn ->
        {
            String name = Stream.of(prmsn.getPermissionName().split(" "))
                                .map(StringUtils::capitalize)
                                .collect(Collectors.collectingAndThen(Collectors.joining(), StringUtils::uncapitalize));
            permission.setPermission(Map.of("name", name), prmsn.getState(), prmsn.getOrigin().toString());
        });
    }
}
