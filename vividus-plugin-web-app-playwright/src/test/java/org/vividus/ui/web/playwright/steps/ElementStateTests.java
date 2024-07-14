/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.web.playwright.steps;

import java.util.function.BiConsumer;
import java.util.stream.Stream;

import com.microsoft.playwright.Locator;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.playwright.assertions.PlaywrightLocatorAssertions;

@ExtendWith(MockitoExtension.class)
class ElementStateTests
{
    @Mock
    private Locator locator;

    private static Stream<Arguments> stateProvider()
    {
        return Stream.of(Arguments.of(ElementState.ENABLED,
                        (BiConsumer<Locator, Boolean>) PlaywrightLocatorAssertions::assertElementEnabled),
                Arguments.of(ElementState.DISABLED,
                        (BiConsumer<Locator, Boolean>) PlaywrightLocatorAssertions::assertElementDisabled),
                Arguments.of(ElementState.SELECTED,
                        (BiConsumer<Locator, Boolean>) PlaywrightLocatorAssertions::assertElementSelected),
                Arguments.of(ElementState.NOT_SELECTED,
                        (BiConsumer<Locator, Boolean>) PlaywrightLocatorAssertions::assertElementNotSelected),
                Arguments.of(ElementState.VISIBLE,
                        (BiConsumer<Locator, Boolean>) PlaywrightLocatorAssertions::assertElementVisible),
                Arguments.of(ElementState.NOT_VISIBLE,
                        (BiConsumer<Locator, Boolean>) PlaywrightLocatorAssertions::assertElementHidden));
    }

    @ParameterizedTest
    @MethodSource("stateProvider")
    void shouldAssertElementState(ElementState state, BiConsumer<Locator, Boolean> assertion)
    {
        try (MockedStatic<PlaywrightLocatorAssertions> mocked = Mockito.mockStatic(PlaywrightLocatorAssertions.class))
        {
            state.assertElementState(locator);
            mocked.verify(() -> assertion.accept(locator, false));
        }
    }
}
