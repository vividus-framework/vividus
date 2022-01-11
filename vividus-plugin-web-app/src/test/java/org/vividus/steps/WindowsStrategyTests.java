/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.steps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.vividus.ui.web.action.IWindowsActions;

class WindowsStrategyTests
{
    // CHECKSTYLE:OFF
    static Stream<Arguments> strategiesSource()
    {
        return Stream.of(
                Arguments.of(WindowsStrategy.CLOSE_ALL_EXCEPT_ONE, (Consumer<IWindowsActions>) wa -> verify(wa).closeAllWindowsExceptOne()),
                Arguments.of(WindowsStrategy.DO_NOTHING,           (Consumer<IWindowsActions>) Mockito::verifyNoInteractions)
                );
    }
    // CHECKSTYLE:ON

    @MethodSource("strategiesSource")
    @ParameterizedTest
    void shouldProcessActiveWindowsAccordingToStrategy(WindowsStrategy strategy, Consumer<IWindowsActions> verification)
    {
        IWindowsActions windowsAction = mock(IWindowsActions.class);
        strategy.apply(windowsAction);
        verification.accept(windowsAction);
    }
}
