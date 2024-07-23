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

package org.vividus.ui.web.playwright.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.playwright.action.AbstractPlaywrightActions;
import org.vividus.ui.web.playwright.action.PlaywrightActions;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

@ExtendWith(MockitoExtension.class)
class ParametersToPlaywrightActionConverterTests
{
    private static final String TEXT = "text";

    private static final PlaywrightActions.Click CLICK = new PlaywrightActions.Click();
    private static final PlaywrightActions.ClickAndHold CLICK_AND_HOLD = new PlaywrightActions.ClickAndHold();
    private static final PlaywrightActions.Release RELEASE = new PlaywrightActions.Release();
    private static final PlaywrightActions.EnterText ENTER_TEXT = new PlaywrightActions.EnterText();

    @Mock private StringToPlaywrightLocatorConverter locatorConverter;
    private ParametersToPlaywrightActionConverter actionConverter;

    @BeforeEach
    void beforeEach()
    {
        actionConverter = new ParametersToPlaywrightActionConverter(locatorConverter,
                List.of(CLICK, CLICK_AND_HOLD, ENTER_TEXT, RELEASE));
    }

    @Test
    void shouldConvertValueLocator()
    {
        var by = "By.caseSensitiveText(" + TEXT + ")";
        var locator = mock(PlaywrightLocator.class);
        when(locatorConverter.convertValue(by, null)).thenReturn(locator);
        var value = """
                |type           |argument                  |
                |CLICK          |By.caseSensitiveText(text)|
                |CLICK_AND_HOLD |By.caseSensitiveText(text)|""";
        var actions = asActions(value);
        assertThat(actions, hasSize(2));
        assertEquals(locator, actions.get(0).getArgument());
        assertEquals(locator, actions.get(1).getArgument());
        verify(locatorConverter, times(2)).convertValue(by, null);
        verifyNoMoreInteractions(locator);
    }

    @Test
    void shouldConvertNullableAction()
    {
        var value = """
                |type   |argument|
                |CLICK  |        |
                |RELEASE|        |""";
        var actions = asActions(value);
        assertThat(actions, hasSize(2));
        assertNull(actions.get(0).getArgument());
        assertNull(actions.get(1).getArgument());
        verifyNoMoreInteractions(locatorConverter);
    }

    @Test
    void shouldConvertValueString()
    {
        var value = "|type      |argument|\n"
                  + "|ENTER_TEXT|text    |";
        var actions = asActions(value);
        assertThat(actions, hasSize(1));
        assertEquals(TEXT, actions.get(0).getArgument());
        verifyNoMoreInteractions(locatorConverter);
    }

    @Test
    void shouldThrowExceptionIfMandatoryArgumentAbsent()
    {
        var value = "|type       |argument|\n"
                  + "|ENTER_TEXT |        |";
        var exception = assertThrows(IllegalArgumentException.class, () -> asActions(value));
        assertEquals("Argument is mandatory for action 'ENTER_TEXT'", exception.getMessage());
    }

    @Test
    void shouldThrowExceptionIfInvalidActionType()
    {
        var value = "|type        |argument|\n"
                  + "|WHEEL_CLICK |        |";
        var exception = assertThrows(IllegalArgumentException.class, () -> asActions(value));
        assertEquals("There is no action: WHEEL_CLICK", exception.getMessage());
    }

    private List<AbstractPlaywrightActions> asActions(String table)
    {
        return new ExamplesTable(table).getRowsAsParameters()
                .stream()
                .map(p -> actionConverter.convertValue(p, null))
                .toList();
    }
}
