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
import java.time.Duration;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.http.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.selenium.manager.GenericWebDriverManager;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

public class RemoteWebDriverFactory implements IRemoteWebDriverFactory
{
    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteWebDriverFactory.class);
    private static final String DEFAULT_HTTP_VERSION = "HTTP_1_1";

    private final boolean retrySessionCreationOnHttpConnectTimeout;
    private final Duration readTimeout;

    private final RemoteWebDriverUrlProvider remoteWebDriverUrlProvider;

    public RemoteWebDriverFactory(boolean retrySessionCreationOnHttpConnectTimeout, Duration readTimeout,
            RemoteWebDriverUrlProvider remoteWebDriverUrlProvider)
    {
        this.retrySessionCreationOnHttpConnectTimeout = retrySessionCreationOnHttpConnectTimeout;
        this.readTimeout = readTimeout;
        this.remoteWebDriverUrlProvider = remoteWebDriverUrlProvider;
    }

    @Override
    public RemoteWebDriver getRemoteWebDriver(Capabilities capabilities)
    {
        URL remoteDriverUrl = remoteWebDriverUrlProvider.getRemoteDriverUrl();
        ClientConfig clientConfig = ClientConfig.defaultConfig()
                .baseUrl(remoteDriverUrl)
                .readTimeout(readTimeout);
        try
        {
            return createRemoteWebDriver(capabilities, clientConfig);
        }
        catch (SessionNotCreatedException e)
        {
            if (retrySessionCreationOnHttpConnectTimeout && isHttpConnectTimeout(e))
            {
                LOGGER.warn("Failed to create a new session due to HTTP connect timout", e);
                LOGGER.warn("Retrying to create a new session");
                return createRemoteWebDriver(capabilities, clientConfig);
            }
            throw e;
        }
    }

    private static boolean isHttpConnectTimeout(SessionNotCreatedException e)
    {
        return e.getMessage().contains("Message: java.net.http.HttpConnectTimeoutException: HTTP connect timed out");
    }

    private RemoteWebDriver createRemoteWebDriver(Capabilities capabilities, ClientConfig clientConfig)
    {
        if (GenericWebDriverManager.isIOS(capabilities) || GenericWebDriverManager.isTvOS(capabilities))
        {
            // https://github.com/SeleniumHQ/selenium/issues/12918
            return new IOSDriver(clientConfig.version(DEFAULT_HTTP_VERSION), capabilities);
        }
        else if (GenericWebDriverManager.isAndroid(capabilities))
        {
            // https://github.com/SeleniumHQ/selenium/issues/12918
            return new AndroidDriver(clientConfig.version(DEFAULT_HTTP_VERSION), capabilities);
        }
        return (RemoteWebDriver) RemoteWebDriver.builder()
                .config(clientConfig)
                .oneOf(capabilities)
                .build();
    }
}
