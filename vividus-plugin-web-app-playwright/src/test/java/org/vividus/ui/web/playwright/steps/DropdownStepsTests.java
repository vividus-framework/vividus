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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.LocatorAssertions;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.microsoft.playwright.options.AriaRole;

import org.jbehave.core.model.ExamplesTable;
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
    private static final String ONE_DROPDOWN_ASSERTION_MESSAGE =
            "One unique dropdown by the locator must be present in the context";
    private static final String VALUE_ATTRIBUTE = "value";
    private static final String BLUE = "Blue";
    private static final String OPTION_TAG = "option";
    private static final String OPTION_SELECTED_EVALUATE_SCRIPT = "option => option.selected";
    private static final String SELECTED_OPTIONS_PRESENT_MESSAGE = "Selected options are present in dropdown";
    private static final String SELECTED_OPTION_MESSAGE = "Selected option in dropdown";

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

    @Test
    void shouldAddOptionToSelectionInDropdown()
    {
        PlaywrightLocator incomingLocator = mock();
        Locator options = mock();
        Locator selectedOption = mock();
        Locator newOption = mock();

        when(uiContext.locateElement(incomingLocator)).thenReturn(dropdown);
        when(dropdown.getByRole(AriaRole.OPTION)).thenReturn(options);
        when(options.all()).thenReturn(List.of(selectedOption, newOption));
        when(selectedOption.getAttribute(VALUE_ATTRIBUTE)).thenReturn(BLUE);
        when(selectedOption.evaluate(OPTION_SELECTED_EVALUATE_SCRIPT)).thenReturn(true);
        when(newOption.getAttribute(VALUE_ATTRIBUTE)).thenReturn(EXPECTED_OPTION_VALUE);
        when(newOption.evaluate(OPTION_SELECTED_EVALUATE_SCRIPT)).thenReturn(false);

        try (var playwrightAssertionsStaticMock = mockStatic(PlaywrightAssertions.class))
        {
            LocatorAssertions locatorAssertions = mock();
            playwrightAssertionsStaticMock.when(() -> PlaywrightAssertions.assertThat(dropdown)).thenReturn(
                    locatorAssertions);
            mockAssertionRunner();
            dropdownSteps.addOptionInDropdown(EXPECTED_OPTION_VALUE, incomingLocator);

            verify(softAssert).assertTrue(OPTION_ASSERTION, true);
            verify(dropdown).selectOption(new String[] { BLUE, EXPECTED_OPTION_VALUE });
        }
    }

    @Test
    void shouldValidateDropdownContainsOptions() throws Exception
    {
        PlaywrightLocator incomingLocator = mock();
        Locator optionsLocator = mock();
        Locator option = mock();

        when(uiContext.locateElement(incomingLocator)).thenReturn(dropdown);
        when(dropdown.locator(OPTION_TAG)).thenReturn(optionsLocator);
        when(optionsLocator.all()).thenReturn(List.of(option));
        when(option.textContent()).thenReturn(EXPECTED_OPTION_VALUE);
        when(option.evaluate(OPTION_SELECTED_EVALUATE_SCRIPT)).thenReturn(true);
        when(softAssert.assertEquals("Expected dropdown is of the same size as actual dropdown: ", 1, 1))
                .thenReturn(true);

        try (var playwrightAssertionsStaticMock = mockStatic(PlaywrightAssertions.class))
        {
            playwrightAssertionsStaticMock.when(() -> PlaywrightAssertions.assertThat(dropdown))
                    .thenReturn(mockDropdownPresence());
            mockAssertionRunner();

            ExamplesTable examplesTable = new ExamplesTable("|state|item|\n|true|Red|");
            dropdownSteps.doesDropdownContainOptions(incomingLocator, examplesTable);

            verify(softAssert).assertEquals("Text of actual option at position [1]", EXPECTED_OPTION_VALUE,
                    EXPECTED_OPTION_VALUE);
            verify(softAssert).assertEquals("State of actual option at position [1]", true, true);
        }
    }

    @Test
    void shouldValidateDropdownHasSelectedOption()
    {
        PlaywrightLocator incomingLocator = mock();
        Locator optionsLocator = mock();
        Locator option = mock();

        when(uiContext.locateElement(incomingLocator)).thenReturn(dropdown);
        when(dropdown.locator(OPTION_TAG)).thenReturn(optionsLocator);
        when(optionsLocator.all()).thenReturn(List.of(option));
        when(option.evaluate(OPTION_SELECTED_EVALUATE_SCRIPT)).thenReturn(true);
        when(option.textContent()).thenReturn(EXPECTED_OPTION_VALUE);
        when(softAssert.assertTrue(SELECTED_OPTIONS_PRESENT_MESSAGE, true)).thenReturn(true);

        try (var playwrightAssertionsStaticMock = mockStatic(PlaywrightAssertions.class))
        {
            playwrightAssertionsStaticMock.when(() -> PlaywrightAssertions.assertThat(dropdown))
                    .thenReturn(mockDropdownPresence());
            mockAssertionRunner();

            dropdownSteps.doesDropdownHaveFirstSelectedOption(incomingLocator, EXPECTED_OPTION_VALUE);

            verify(softAssert).assertEquals(SELECTED_OPTION_MESSAGE, EXPECTED_OPTION_VALUE,
                    EXPECTED_OPTION_VALUE);
        }
    }

    @Test
    void shouldNotAssertSelectedOptionWhenDropdownHasNoSelection()
    {
        PlaywrightLocator incomingLocator = mock();
        Locator optionsLocator = mock();

        when(uiContext.locateElement(incomingLocator)).thenReturn(dropdown);
        when(dropdown.locator(OPTION_TAG)).thenReturn(optionsLocator);
        when(optionsLocator.all()).thenReturn(List.of());

        try (var playwrightAssertionsStaticMock = mockStatic(PlaywrightAssertions.class))
        {
            playwrightAssertionsStaticMock.when(() -> PlaywrightAssertions.assertThat(dropdown))
                    .thenReturn(mockDropdownPresence());
            mockAssertionRunner();

            dropdownSteps.doesDropdownHaveFirstSelectedOption(incomingLocator, EXPECTED_OPTION_VALUE);

            verify(softAssert).assertTrue(SELECTED_OPTIONS_PRESENT_MESSAGE, false);
            verify(softAssert, never()).assertEquals(eq(SELECTED_OPTION_MESSAGE), eq(EXPECTED_OPTION_VALUE),
                    anyString());
        }
    }

    private LocatorAssertions mockDropdownPresence()
    {
        return mock();
    }

    private void mockAssertionRunner()
    {
        doNothing().when(playwrightSoftAssert).runAssertion(eq(ONE_DROPDOWN_ASSERTION_MESSAGE), argThat(runnable ->
        {
            runnable.run();
            return true;
        }));
    }

    private void shouldValidateDropdownSelection(String actualOptionValue, boolean expectedCondition)
    {
        PlaywrightLocator incomingLocator = mock();
        Locator options = mock();
        Locator option = mock();

        when(uiContext.locateElement(incomingLocator)).thenReturn(dropdown);
        when(dropdown.getByRole(AriaRole.OPTION)).thenReturn(options);
        when(options.all()).thenReturn(List.of(option));
        when(option.getAttribute(VALUE_ATTRIBUTE)).thenReturn(actualOptionValue);

        try (var playwrightAssertionsStaticMock = mockStatic(PlaywrightAssertions.class))
        {
            LocatorAssertions locatorAssertions = mock();
            playwrightAssertionsStaticMock.when(() -> PlaywrightAssertions.assertThat(dropdown)).thenReturn(
                    locatorAssertions);
            mockAssertionRunner();
            dropdownSteps.selectOptionInDropdown(EXPECTED_OPTION_VALUE, incomingLocator);

            verify(locatorAssertions).hasCount(1);
            verify(softAssert).assertTrue(OPTION_ASSERTION, expectedCondition);
        }
    }
}
