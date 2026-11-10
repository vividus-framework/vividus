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

import java.util.Optional;
import java.util.function.Supplier;

import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionCreationRetryOnHttpConnectTimeoutHandler implements SessionCreationRetryHandler
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SessionCreationRetryOnHttpConnectTimeoutHandler.class);

    private final boolean retrySessionCreationOnHttpConnectTimeout;

    public SessionCreationRetryOnHttpConnectTimeoutHandler(boolean retrySessionCreationOnHttpConnectTimeout)
    {
        this.retrySessionCreationOnHttpConnectTimeout = retrySessionCreationOnHttpConnectTimeout;
    }

    @Override
    public Optional<RemoteWebDriver> handleError(SessionNotCreatedException sessionNotCreatedException,
            Supplier<RemoteWebDriver> remoteWebDriverFactory)
    {
        if (retrySessionCreationOnHttpConnectTimeout && isHttpConnectTimeout(sessionNotCreatedException))
        {
            LOGGER.warn("Failed to create a new session due to HTTP connect timout", sessionNotCreatedException);
            LOGGER.warn("Retrying to create a new session");
            return Optional.ofNullable(remoteWebDriverFactory.get());
        }
        return Optional.empty();
    }

    private static boolean isHttpConnectTimeout(SessionNotCreatedException e)
    {
        return e.getMessage().contains("Message: java.net.http.HttpConnectTimeoutException: HTTP connect timed out");
    }
}
