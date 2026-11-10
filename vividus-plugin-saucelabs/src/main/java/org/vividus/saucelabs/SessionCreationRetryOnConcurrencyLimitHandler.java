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

package org.vividus.saucelabs;

import java.time.Duration;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.vividus.selenium.SessionCreationRetryHandler;
import org.vividus.util.wait.DurationBasedWaiter;

public class SessionCreationRetryOnConcurrencyLimitHandler implements SessionCreationRetryHandler
{
    private final Duration freeDeviceWaitTimeout;
    private final Duration pollingTimeout;

    public SessionCreationRetryOnConcurrencyLimitHandler(Duration freeDeviceWaitTimeout, Duration pollingTimeout)
    {
        this.freeDeviceWaitTimeout = freeDeviceWaitTimeout;
        this.pollingTimeout = pollingTimeout;
    }

    @Override
    public Optional<RemoteWebDriver> handleError(SessionNotCreatedException sessionNotCreatedException,
            Supplier<RemoteWebDriver> remoteWebDriverFactory)
    {
        if (!freeDeviceWaitTimeout.isZero() && isConcurrencyLimitExceeded(sessionNotCreatedException))
        {
            RemoteWebDriver remoteWebDriver = new DurationBasedWaiter(freeDeviceWaitTimeout, pollingTimeout).wait(
                    () ->
                    {
                        try
                        {
                            return remoteWebDriverFactory.get();
                        }
                        catch (SessionNotCreatedException e)
                        {
                            if (isConcurrencyLimitExceeded(e))
                            {
                                return null;
                            }
                            throw e;
                        }
                    }, Objects::nonNull);
            return Optional.ofNullable(remoteWebDriver);
        }
        return Optional.empty();
    }

    private static boolean isConcurrencyLimitExceeded(SessionNotCreatedException sessionNotCreatedException)
    {
        return sessionNotCreatedException.getMessage().contains("CCYAbuse - too many jobs");
    }
}
