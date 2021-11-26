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

package org.vividus.converter.ui.web;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
import org.openqa.selenium.Point;
import org.vividus.converter.PointConverter;
import org.vividus.converter.ui.StringToLocatorConverter;
import org.vividus.steps.ui.web.model.SequenceAction;
import org.vividus.steps.ui.web.model.SequenceActionType;
import org.vividus.ui.action.search.Locator;

@ExtendWith(MockitoExtension.class)
class ParametersToSequenceActionConverterTests
{
    private static final String TEXT = "text";

    @Mock private StringToLocatorConverter stringToLocatorConverter;
    @Mock private PointConverter pointConverter;
    @InjectMocks private ParametersToSequenceActionConverter converter;

    @Test
    void testConvertValueLocator()
    {
        String by = "By.caseSensitiveText(" + TEXT + ")";
        Locator locator = mock(Locator.class);
        when(stringToLocatorConverter.convertValue(by, null)).thenReturn(locator);
        String value = "|type        |argument                  |\n"
                     + "|CLICK       |By.caseSensitiveText(text)|\n"
                     + "|DOUBLE_CLICK|By.caseSensitiveText(text)|";
        List<SequenceAction> actions = asActions(value);
        verifySequenceAction(actions.get(0), SequenceActionType.CLICK, locator);
        verifySequenceAction(actions.get(1), SequenceActionType.DOUBLE_CLICK, locator);
        verify(stringToLocatorConverter, times(2)).convertValue(by, null);
        verifyNoMoreInteractions(stringToLocatorConverter, pointConverter);
    }

    @Test
    void testConvertNullableClick()
    {
        String by = "By.xpath(//button)";
        Locator locator = mock(Locator.class);
        when(stringToLocatorConverter.convertValue(by, null)).thenReturn(locator);
        String value = "|type   |argument          |\n"
                     + "|MOVE_TO|By.xpath(//button)|\n"
                     + "|CLICK  |                  |";
        List<SequenceAction> actions = asActions(value);
        assertThat(actions, hasSize(2));
        verifySequenceAction(actions.get(0), SequenceActionType.MOVE_TO, locator);
        verifySequenceAction(actions.get(1), SequenceActionType.CLICK, null);
        verify(stringToLocatorConverter).convertValue(by, null);
        verifyNoMoreInteractions(stringToLocatorConverter, pointConverter);
    }

    @Test
    void testConvertValuePoint()
    {
        String pointAsString = "(100, 100)";
        Point point = mock(Point.class);
        when(pointConverter.convertValue(pointAsString, null)).thenReturn(point);
        String value = "|type          |argument  |\n"
                     + "|MOVE_BY_OFFSET|(100, 100)|";
        List<SequenceAction> actions = asActions(value);
        assertThat(actions, hasSize(1));
        verifySequenceAction(actions.get(0), SequenceActionType.MOVE_BY_OFFSET, point);
        verify(pointConverter).convertValue(pointAsString, null);
        verifyNoMoreInteractions(stringToLocatorConverter, pointConverter);
    }

    @Test
    void testConvertValueString()
    {
        String value = "|type      |argument|\n"
                     + "|ENTER_TEXT|text    |";
        List<SequenceAction> actions = asActions(value);
        assertThat(actions, hasSize(1));
        verifySequenceAction(actions.get(0), SequenceActionType.ENTER_TEXT, TEXT);
        verifyNoMoreInteractions(stringToLocatorConverter, pointConverter);
    }

    @Test
    void testConvertKeys()
    {
        String value = "|type      |argument     |\n"
                     + "|PRESS_KEYS|value1,value2|";
        List<SequenceAction> actions = asActions(value);
        assertThat(actions, hasSize(1));
        verifySequenceAction(actions.get(0), SequenceActionType.PRESS_KEYS, List.of("value1", "value2"));
        verifyNoMoreInteractions(stringToLocatorConverter, pointConverter);
    }

    private List<SequenceAction> asActions(String table)
    {
        return new ExamplesTable(table).getRowsAsParameters()
                                       .stream()
                                       .map(p -> converter.convertValue(p, null))
                                       .collect(Collectors.toList());
    }

    private static void verifySequenceAction(SequenceAction action, SequenceActionType expectedType,
            Object expectedArgument)
    {
        assertEquals(expectedType, action.getType());
        assertEquals(expectedArgument, action.getArgument());
    }
}
