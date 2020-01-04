/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.ui.web.configuration;

import java.net.URI;

import org.vividus.ui.web.action.IJavascriptActions;
import org.vividus.util.UriUtils;

public class WebApplicationConfiguration
{
    private static final String TEST_ENVIRONMENT = "test";

    private int mobileScreenResolutionWidthThreshold;
    private int tabletScreenResolutionWidthThreshold;
    private String applicationEnvironmentType;
    private final ThreadLocal<ConfigurationStorage> configurationStorage;

    private IJavascriptActions javascriptActions;

    public WebApplicationConfiguration(String mainApplicationPageUrlString, AuthenticationMode authenticationMode)
    {
        configurationStorage = ThreadLocal.withInitial(() ->
            new ConfigurationStorage(mainApplicationPageUrlString, authenticationMode));
    }

    public boolean isTestEnvironment()
    {
        return TEST_ENVIRONMENT.equals(applicationEnvironmentType);
    }

    public URI getMainApplicationPageUrl()
    {
        return configurationStorage.get().getMainApplicationPageUri();
    }

    public AuthenticationMode getAuthenticationMode()
    {
        return configurationStorage.get().authenticationMode;
    }

    public void changeAuthenticationMode(AuthenticationMode authenticationMode)
    {
        configurationStorage.get().setAuthenticationMode(authenticationMode);
    }

    public String getBasicAuthUser()
    {
        return configurationStorage.get().basicAuthUser;
    }

    public int getMobileScreenResolutionWidthThreshold()
    {
        return mobileScreenResolutionWidthThreshold;
    }

    public void setMobileScreenResolutionWidthThreshold(int mobileScreenResolutionWidthThreshold)
    {
        this.mobileScreenResolutionWidthThreshold = mobileScreenResolutionWidthThreshold;
    }

    public int getTabletScreenResolutionWidthThreshold()
    {
        return tabletScreenResolutionWidthThreshold;
    }

    public void setTabletScreenResolutionWidthThreshold(int tabletScreenResolutionWidthThreshold)
    {
        this.tabletScreenResolutionWidthThreshold = tabletScreenResolutionWidthThreshold;
    }

    public String getHost()
    {
        return configurationStorage.get().mainApplicationPageUri == null
                ? null
                : configurationStorage.get().mainApplicationPageUri.getHost();
    }

    public boolean isMobileViewport()
    {
        return getViewportWidth() <= mobileScreenResolutionWidthThreshold;
    }

    public boolean isTabletViewport()
    {
        return getViewportWidth() <= tabletScreenResolutionWidthThreshold;
    }

    private int getViewportWidth()
    {
        return javascriptActions.getViewportSize().getWidth();
    }

    public void setApplicationEnvironmentType(String applicationEnvironmentType)
    {
        this.applicationEnvironmentType = applicationEnvironmentType;
    }

    public void setJavascriptActions(IJavascriptActions javascriptActions)
    {
        this.javascriptActions = javascriptActions;
    }

    private static final class ConfigurationStorage
    {
        private URI mainApplicationPageUri;
        private AuthenticationMode authenticationMode;
        private String mainApplicationPageUrl;
        private String basicAuthUser;

        ConfigurationStorage(String mainApplicationPageUrl, AuthenticationMode authenticationMode)
        {
            if (null != mainApplicationPageUrl)
            {
                basicAuthUser = UriUtils.createUri(mainApplicationPageUrl).getUserInfo();
                this.mainApplicationPageUrl = mainApplicationPageUrl;
                this.authenticationMode = authenticationMode;
                updateUri();
            }
        }

        private URI getMainApplicationPageUri()
        {
            return mainApplicationPageUri;
        }

        private void setAuthenticationMode(AuthenticationMode authenticationMode)
        {
            this.authenticationMode = authenticationMode;
            updateUri();
        }

        private void updateUri()
        {
            this.mainApplicationPageUri = null != authenticationMode
                    ? authenticationMode.getUrl(mainApplicationPageUrl, basicAuthUser)
                    : UriUtils.createUri(mainApplicationPageUrl);
        }
    }
}
