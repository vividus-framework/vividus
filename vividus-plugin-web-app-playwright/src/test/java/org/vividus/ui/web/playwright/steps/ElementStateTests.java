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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.microsoft.playwright.Locator;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.playwright.assertions.PlaywrightLocatorAssertions;

@ExtendWith(MockitoExtension.class)
class ElementStateTests
{
    private static final int NO_WAIT_TIMEOUT = 0;

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

    private static Stream<Arguments> checkVisibilityStateProvider()
    {
        return Stream.of(
                Arguments.of(ElementState.VISIBLE, true, (Function<Locator, Boolean>) Locator::isVisible),
                Arguments.of(ElementState.VISIBLE, false, (Function<Locator, Boolean>) Locator::isVisible),
                Arguments.of(ElementState.NOT_VISIBLE, true, (Function<Locator, Boolean>) Locator::isHidden),
                Arguments.of(ElementState.NOT_VISIBLE, false, (Function<Locator, Boolean>) Locator::isHidden)
                        );
    }

    @ParameterizedTest
    @MethodSource("stateProvider")
    void shouldAssertElementState(ElementState state, BiConsumer<Locator, Boolean> assertion)
    {
        shouldAssertElementState(state::assertElementState, false, assertion);
    }

    @ParameterizedTest
    @MethodSource("stateProvider")
    void shouldWaitForElementState(ElementState state, BiConsumer<Locator, Boolean> assertion)
    {
        shouldAssertElementState(state::waitForElementState, true, assertion);
    }

    @ParameterizedTest
    @MethodSource("checkVisibilityStateProvider")
    void shouldCheckElementVisibility(ElementState state, boolean isInState, Function<Locator, Boolean> isState)
    {
        Mockito.when(isState.apply(locator)).thenReturn(isInState);
        assertEquals(isInState, state.isElementState(locator));
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void shouldCheckIsElementEnabled(boolean isEnable)
    {
        var state = ElementState.ENABLED;
        var optionsCaptor = ArgumentCaptor.forClass(Locator.IsEnabledOptions.class);
        Mockito.when(locator.isEnabled(optionsCaptor.capture())).thenReturn(isEnable);
        assertEquals(isEnable, state.isElementState(locator));
        assertEquals(NO_WAIT_TIMEOUT, optionsCaptor.getValue().timeout);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void shouldCheckIsElementDisabled(boolean isDisabled)
    {
        var state = ElementState.DISABLED;
        var optionsCaptor = ArgumentCaptor.forClass(Locator.IsDisabledOptions.class);
        Mockito.when(locator.isDisabled(optionsCaptor.capture())).thenReturn(isDisabled);
        assertEquals(isDisabled, state.isElementState(locator));
        assertEquals(NO_WAIT_TIMEOUT, optionsCaptor.getValue().timeout);
    }

    @ParameterizedTest
    @CsvSource({
            "SELECTED, true, true",
            "SELECTED, false, false",
            "NOT_SELECTED, false, true",
            "NOT_SELECTED, true, false"
    })
    void shouldCheckIsElementSelected(ElementState state, boolean isChecked, boolean isInState)
    {
        var optionsCaptor = ArgumentCaptor.forClass(Locator.IsCheckedOptions.class);
        Mockito.when(locator.isChecked(optionsCaptor.capture())).thenReturn(isChecked);
        assertEquals(isInState, state.isElementState(locator));
        assertEquals(NO_WAIT_TIMEOUT, optionsCaptor.getValue().timeout);
    }

    private void shouldAssertElementState(Consumer<Locator> stateConsumer, boolean waitForState,
            BiConsumer<Locator, Boolean> assertion)
    {
        try (MockedStatic<PlaywrightLocatorAssertions> mocked = Mockito.mockStatic(PlaywrightLocatorAssertions.class))
        {
            stateConsumer.accept(locator);
            mocked.verify(() -> assertion.accept(locator, waitForState));
        }
    }
}
