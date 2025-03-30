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

@SuppressWarnings("PMD.ImplicitFunctionalInterface")
public interface SessionCreationRetryHandler
{
    /**
     * Checks whether the error from {@link SessionNotCreatedException} is recoverable. If it is, tries to create a
     * new {@link RemoteWebDriver} using the provided factory.
     *
     * @param sessionNotCreatedException The original exception thrown when a session is not created at the first
     *                                   attempt.
     * @param remoteWebDriverFactory     The factory to create a new {@link RemoteWebDriver}.
     * @return An {@link Optional} containing the new {@link RemoteWebDriver} if the error is recoverable, otherwise
     * an empty {@link Optional}.
     */
    Optional<RemoteWebDriver> handleError(SessionNotCreatedException sessionNotCreatedException,
            Supplier<RemoteWebDriver> remoteWebDriverFactory);
}
