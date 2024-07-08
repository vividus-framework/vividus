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

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.assertions.PlaywrightSoftAssert;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

@ExtendWith(MockitoExtension.class)
class DropdownStepsTests
{
    private static final String EXPECTED_OPTION_VALUE = "Red";
    private static final String OPTION_ASSERTION = "The option \"" + EXPECTED_OPTION_VALUE
            + "\" is present in dropdown";

    @Mock private Locator dropdown;

    @Mock private UiContext uiContext;
    @Mock private PlaywrightSoftAssert playwrightSoftAssert;
    @Mock private ISoftAssert softAssert;
    @InjectMocks private DropdownSteps dropdownSteps;

    @Test
    void shouldSelectOptionInDropdown()
    {
        shouldValidateDropdownSelection(EXPECTED_OPTION_VALUE, true);
        verify(dropdown).selectOption(EXPECTED_OPTION_VALUE);
    }

    @ParameterizedTest
    @CsvSource({ "Green", "," })
    void shouldNotTrySelectOptionInDropdownIfOptionNotFound(String actualOptionValue)
    {
        shouldValidateDropdownSelection(actualOptionValue, false);
        verifyNoMoreInteractions(dropdown);
    }

    private void shouldValidateDropdownSelection(String actualOptionValue, boolean expectedCondition)
    {
        PlaywrightLocator incomingLocator = mock();
        Locator options = mock();
        Locator option = mock();

        when(uiContext.locateElement(incomingLocator)).thenReturn(dropdown);
        when(dropdown.getByRole(AriaRole.OPTION)).thenReturn(options);
        when(options.all()).thenReturn(List.of(option));
        when(option.getAttribute("value")).thenReturn(actualOptionValue);

        try (var playwrightAssertionsStaticMock = mockStatic(PlaywrightAssertions.class))
        {
            LocatorAssertions locatorAssertions = mock();
            playwrightAssertionsStaticMock.when(() -> PlaywrightAssertions.assertThat(dropdown)).thenReturn(
                    locatorAssertions);
            doNothing().when(playwrightSoftAssert).runAssertion(
                    eq("One unique dropdown by the locator must be present in the context"), argThat(runnable ->
                    {
                        runnable.run();
                        return true;
                    }));
            dropdownSteps.selectOptionInDropdown(EXPECTED_OPTION_VALUE, incomingLocator);

            verify(locatorAssertions).hasCount(1);
            verify(softAssert).assertTrue(OPTION_ASSERTION, expectedCondition);
        }
    }
}
