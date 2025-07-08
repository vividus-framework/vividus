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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.remote.RemoteWebDriver;

class SessionCreationRetryOnConcurrencyLimitHandlerTests
{
    private static final String CONCURRENCY_LIMIT_MESSAGE = "Could not start a new session. Response code 400. "
            + "Message: CCYAbuse - too many jobs";

    private Duration waitForFreeDeviceTimeout = Duration.ofSeconds(10);
    private final Duration pollingTimeout = Duration.ofSeconds(1);

    private SessionCreationRetryOnConcurrencyLimitHandler getHandler()
    {
        return new SessionCreationRetryOnConcurrencyLimitHandler(waitForFreeDeviceTimeout, pollingTimeout);
    }

    @Test
    void shouldCreateNewSessionFromFirstAttempt()
    {
        var exception = new SessionNotCreatedException(CONCURRENCY_LIMIT_MESSAGE);
        Supplier<RemoteWebDriver> factory = mock();
        RemoteWebDriver webDriver = mock();
        when(factory.get()).thenReturn(webDriver);

        var result = getHandler().handleError(exception, factory);

        assertEquals(Optional.of(webDriver), result);
    }

    @Test
    void shouldCreateNewSessionFromSecondAttempt()
    {
        var exception = new SessionNotCreatedException(CONCURRENCY_LIMIT_MESSAGE);
        Supplier<RemoteWebDriver> factory = mock();
        RemoteWebDriver webDriver = mock();
        when(factory.get())
                .thenThrow(new SessionNotCreatedException(CONCURRENCY_LIMIT_MESSAGE))
                .thenReturn(webDriver);

        var result = getHandler().handleError(exception, factory);

        assertEquals(Optional.of(webDriver), result);
    }

    @Test
    void shouldStopAttemptsOnNonConcurrencyLimitError()
    {
        var exception = new SessionNotCreatedException(CONCURRENCY_LIMIT_MESSAGE);
        Supplier<RemoteWebDriver> factory = mock();
        var unknownException = new SessionNotCreatedException(
            "Could not start a new session. Response code 500. Message: java.util.concurrent.TimeoutException");
        when(factory.get())
                .thenThrow(new SessionNotCreatedException(CONCURRENCY_LIMIT_MESSAGE))
                .thenThrow(unknownException);

        var handler = getHandler();
        var thrownException = assertThrows(SessionNotCreatedException.class,
                () -> handler.handleError(exception, factory));

        assertEquals(unknownException, thrownException);
        verify(factory, times(2)).get();
    }

    @Test
    void shouldFailToCreateNewSessionDueToWaitTimeout()
    {
        var exception = new SessionNotCreatedException(CONCURRENCY_LIMIT_MESSAGE);
        Supplier<RemoteWebDriver> factory = mock();
        when(factory.get()).thenReturn(null);

        waitForFreeDeviceTimeout = Duration.ZERO;
        var result = getHandler().handleError(exception, factory);

        assertEquals(Optional.empty(), result);
    }

    @Test
    void shouldNotTryToCreateSessionOnNonConcurrencyLimitError()
    {
        var exception = new SessionNotCreatedException("Some other error");
        Supplier<RemoteWebDriver> factory = mock();

        var result = getHandler().handleError(exception, factory);

        assertEquals(Optional.empty(), result);
        verifyNoInteractions(factory);
    }
}
