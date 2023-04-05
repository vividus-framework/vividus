/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.ui.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.reflect.TypeToken;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.interactions.Actions;
import org.vividus.converter.ui.StringToLocatorConverter;
import org.vividus.testdouble.TestAtomicActionFactories;
import org.vividus.ui.action.AtomicAction;
import org.vividus.ui.action.AtomicActionFactory;
import org.vividus.ui.action.search.Locator;

@ExtendWith(MockitoExtension.class)
class ParametersToAtomicActionConverterTests
{
    private static final String TEXT = "text";

    private static final AtomicActionFactory<Actions, ?> CLICK = new TestAtomicActionFactories.Click();
    private static final AtomicActionFactory<Actions, ?> DOUBLE_CLICK = new TestAtomicActionFactories.DoubleClick();
    private static final AtomicActionFactory<Actions, ?> RELEASE = new TestAtomicActionFactories.Release();
    private static final AtomicActionFactory<Actions, ?> ENTER_TEXT = new TestAtomicActionFactories.EnterText();

    @Mock private StringToLocatorConverter stringToLocatorConverter;
    private ParametersToAtomicActionConverter<Actions> converter;

    @BeforeEach
    void beforeEach()
    {
        converter = new ParametersToAtomicActionConverter<>(stringToLocatorConverter,
                Set.of(CLICK, DOUBLE_CLICK, ENTER_TEXT, RELEASE));
    }

    @SuppressWarnings("rawtypes")
    static Stream<Arguments> targetTypes()
    {
        return Stream.of(
                arguments(String.class, false),
                arguments(List.class, false),
                arguments(new TypeToken<List<AtomicAction<Actions>>>() { }.getType(), false),
                arguments(new TypeToken<AtomicAction>() { }.getType(), false),
                arguments(new TypeToken<AtomicAction<Actions>>() { }.getType(), true)
        );
    }

    @MethodSource("targetTypes")
    @ParameterizedTest
    void shouldProvideResultOnCheckIfCanConvertToTargetType(Type targetType, boolean canConvert)
    {
        assertEquals(canConvert, converter.canConvertTo(targetType));
    }

    @Test
    void testConvertValueLocator()
    {
        var by = "By.caseSensitiveText(" + TEXT + ")";
        var locator = mock(Locator.class);
        when(stringToLocatorConverter.convertValue(by, null)).thenReturn(locator);
        var value = "|type        |argument                  |\n"
                  + "|CLICK       |By.caseSensitiveText(text)|\n"
                  + "|DOUBLE_CLICK|By.caseSensitiveText(text)|";
        var actions = asActions(value);
        assertThat(actions, hasSize(2));
        verifySequenceAction(actions.get(0), CLICK, locator);
        verifySequenceAction(actions.get(1), DOUBLE_CLICK, locator);
        verify(stringToLocatorConverter, times(2)).convertValue(by, null);
        verifyNoMoreInteractions(stringToLocatorConverter);
    }

    @Test
    void testConvertNullableAction()
    {
        var value = "|type   |argument|\n"
                  + "|CLICK  |        |\n"
                  + "|RELEASE|        |";
        var actions = asActions(value);
        assertThat(actions, hasSize(2));
        verifySequenceAction(actions.get(0), CLICK, null);
        verifySequenceAction(actions.get(1), RELEASE, null);
        verifyNoMoreInteractions(stringToLocatorConverter);
    }

    @Test
    void testConvertValueString()
    {
        var value = "|type      |argument|\n"
                  + "|ENTER_TEXT|text    |";
        var actions = asActions(value);
        assertThat(actions, hasSize(1));
        verifySequenceAction(actions.get(0), ENTER_TEXT, TEXT);
        verifyNoMoreInteractions(stringToLocatorConverter);
    }

    @Test
    void testShouldThrowExceptionIfMandatoryArgumentAbsent()
    {
        var value = "|type       |argument|\n"
                  + "|ENTER_TEXT |        |";
        var exception = assertThrows(IllegalArgumentException.class, () -> asActions(value));
        assertEquals("Argument is mandatory for action 'ENTER_TEXT'", exception.getMessage());
    }

    private List<AtomicAction<Actions>> asActions(String table)
    {
        return new ExamplesTable(table).getRowsAsParameters()
                                       .stream()
                                       .map(p -> converter.convertValue(p, null))
                                       .collect(Collectors.toList());
    }

    private static void verifySequenceAction(AtomicAction<Actions> action,
            AtomicActionFactory<Actions, ?> expectedFactory, Object expectedArgument)
    {
        assertEquals(expectedFactory, action.getActionFactory());
        assertEquals(expectedArgument, action.getArgument());
    }
}
