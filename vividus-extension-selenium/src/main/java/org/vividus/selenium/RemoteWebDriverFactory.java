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

package org.vividus.selenium;

import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.openqa.selenium.Capabilities;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.http.ClientConfig;
import org.vividus.selenium.manager.GenericWebDriverManager;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;

public class RemoteWebDriverFactory implements IRemoteWebDriverFactory
{
    private static final String DEFAULT_HTTP_VERSION = "HTTP_1_1";

    private final Duration readTimeout;
    private final Duration connectionTimeout;
    private final RemoteWebDriverUrlProvider remoteWebDriverUrlProvider;
    private final List<SessionCreationRetryHandler> sessionCreationRetryHandlers;

    public RemoteWebDriverFactory(Duration readTimeout, Duration connectionTimeout,
            RemoteWebDriverUrlProvider remoteWebDriverUrlProvider,
            List<SessionCreationRetryHandler> sessionCreationRetryHandlers)
    {
        this.readTimeout = readTimeout;
        this.connectionTimeout = connectionTimeout;
        this.remoteWebDriverUrlProvider = remoteWebDriverUrlProvider;
        this.sessionCreationRetryHandlers = sessionCreationRetryHandlers;
    }

    @Override
    public RemoteWebDriver getRemoteWebDriver(Capabilities capabilities)
    {
        URL remoteDriverUrl = remoteWebDriverUrlProvider.getRemoteDriverUrl();
        ClientConfig clientConfig = ClientConfig.defaultConfig()
                .baseUrl(remoteDriverUrl)
                .readTimeout(readTimeout)
                .connectionTimeout(connectionTimeout);
        try
        {
            return createRemoteWebDriver(capabilities, clientConfig);
        }
        catch (SessionNotCreatedException e)
        {
            return sessionCreationRetryHandlers.stream()
                    .map(handler -> handler.handleError(e, () -> createRemoteWebDriver(capabilities, clientConfig)))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .orElseThrow(() -> e);
        }
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
