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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Collectors;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.converter.ui.StringToLocatorConverter;
import org.vividus.testdouble.TestSequenceActionType;
import org.vividus.ui.action.SequenceAction;
import org.vividus.ui.action.search.Locator;

@ExtendWith(MockitoExtension.class)
class AbstractParametersToSequenceActionConverterTests
{
    private static final String TEXT = "text";

    @Mock private StringToLocatorConverter stringToLocatorConverter;

    @InjectMocks private TestParametersToSequenceActionConverter converter;

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
        verifySequenceAction(actions.get(0), TestSequenceActionType.CLICK, locator);
        verifySequenceAction(actions.get(1), TestSequenceActionType.DOUBLE_CLICK, locator);
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
        verifySequenceAction(actions.get(0), TestSequenceActionType.CLICK, null);
        verifySequenceAction(actions.get(1), TestSequenceActionType.RELEASE, null);
        verifyNoMoreInteractions(stringToLocatorConverter);
    }

    @Test
    void testConvertValueString()
    {
        var value = "|type      |argument|\n"
                  + "|ENTER_TEXT|text    |";
        var actions = asActions(value);
        assertThat(actions, hasSize(1));
        verifySequenceAction(actions.get(0), TestSequenceActionType.ENTER_TEXT, TEXT);
        verifyNoMoreInteractions(stringToLocatorConverter);
    }

    @Test
    void testShouldThrowExceptionIfMandatoryArgumentAbsent()
    {
        var value = "|type        |argument|\n"
                  + "|DOUBLE_CLICK|        |";
        var exception = assertThrows(IllegalArgumentException.class,
                () -> asActions(value));
        assertEquals("Argument is mandatory for action 'DOUBLE_CLICK'", exception.getMessage());
    }

    private List<SequenceAction<TestSequenceActionType>> asActions(String table)
    {
        return new ExamplesTable(table).getRowsAsParameters()
                                       .stream()
                                       .map(p -> converter.convertValue(p, null))
                                       .collect(Collectors.toList());
    }

    private static void verifySequenceAction(SequenceAction<TestSequenceActionType> action,
            TestSequenceActionType expectedType, Object expectedArgument)
    {
        assertEquals(expectedType, action.getType());
        assertEquals(expectedArgument, action.getArgument());
    }

    private static final class TestParametersToSequenceActionConverter
            extends AbstractParametersToSequenceActionConverter<TestSequenceActionType>
    {
        private TestParametersToSequenceActionConverter(StringToLocatorConverter stringToLocatorConverter)
        {
            super(stringToLocatorConverter, TestSequenceActionType.class);
        }
    }
}
