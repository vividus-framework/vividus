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

import java.net.URL;
import java.net.http.HttpConnectTimeoutException;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.manager.GenericWebDriverManager;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

public class RemoteWebDriverFactory implements IRemoteWebDriverFactory
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteWebDriverFactory.class);

    private final boolean useW3C;
    private final boolean retrySessionCreationOnHttpConnectTimeout;

    private final RemoteWebDriverUrlProvider remoteWebDriverUrlProvider;

    public RemoteWebDriverFactory(boolean useW3C, boolean retrySessionCreationOnHttpConnectTimeout,
            RemoteWebDriverUrlProvider remoteWebDriverUrlProvider)
    {
        this.useW3C = useW3C;
        this.retrySessionCreationOnHttpConnectTimeout = retrySessionCreationOnHttpConnectTimeout;
        this.remoteWebDriverUrlProvider = remoteWebDriverUrlProvider;
    }

    @Override
    public RemoteWebDriver getRemoteWebDriver(Capabilities capabilities)
    {
        try
        {
            return createRemoteWebDriver(capabilities);
        }
        catch (SessionNotCreatedException e)
        {
            if (retrySessionCreationOnHttpConnectTimeout && isHttpConnectTimeout(e))
            {
                LOGGER.warn("Failed to create a new session due to HTTP connect timout", e);
                LOGGER.warn("Retrying to create a new session");
                return createRemoteWebDriver(capabilities);
            }
            throw e;
        }
    }

    private static boolean isHttpConnectTimeout(SessionNotCreatedException e)
    {
        Throwable cause = e.getCause();
        return cause instanceof TimeoutException && cause.getCause() instanceof HttpConnectTimeoutException;
    }

    private RemoteWebDriver createRemoteWebDriver(Capabilities capabilities)
    {
        /* Selenium 4 declares that it only supports W3C and nevertheless still writes  JWP's "desiredCapabilities"
        into "createSession" JSON. Appium Java client eliminates that:https://github.com/appium/java-client/pull/1537.
        But still some clouds (e.g. SmartBear CrossBrowserTesting) are not prepared for W3c, so we should avoid using
        Appium drivers to create sessions for web tests. */
        URL remoteDriverUrl = remoteWebDriverUrlProvider.getRemoteDriverUrl();
        if (useW3C)
        {
            if (GenericWebDriverManager.isIOS(capabilities) || GenericWebDriverManager.isTvOS(capabilities))
            {
                return new IOSDriver(remoteDriverUrl, capabilities);
            }
            else if (GenericWebDriverManager.isAndroid(capabilities))
            {
                return new AndroidDriver(remoteDriverUrl, capabilities);
            }
        }
        return new RemoteWebDriver(remoteDriverUrl, capabilities);
    }
}
