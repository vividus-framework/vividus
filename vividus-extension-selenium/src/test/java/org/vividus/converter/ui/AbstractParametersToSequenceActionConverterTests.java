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

package org.vividus.converter.ui;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.steps.ui.model.SequenceAction;
import org.vividus.steps.ui.model.SequenceActionType;
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
        String by = "By.caseSensitiveText(" + TEXT + ")";
        Locator locator = mock(Locator.class);
        when(stringToLocatorConverter.convertValue(by, null)).thenReturn(locator);
        String value = "|type        |argument                  |\n"
                     + "|CLICK       |By.caseSensitiveText(text)|\n"
                     + "|DOUBLE_CLICK|By.caseSensitiveText(text)|";
        List<SequenceAction<SequenceActionTypeImpl>> actions = asActions(value);
        assertThat(actions, hasSize(2));
        verifySequenceAction(actions.get(0), SequenceActionTypeImpl.CLICK, locator);
        verifySequenceAction(actions.get(1), SequenceActionTypeImpl.DOUBLE_CLICK, locator);
        verify(stringToLocatorConverter, times(2)).convertValue(by, null);
        verifyNoMoreInteractions(stringToLocatorConverter);
    }

    @Test
    void testConvertNullableAction()
    {
        String value = "|type   |argument|\n"
                     + "|CLICK  |        |\n"
                     + "|RELEASE|        |";
        List<SequenceAction<SequenceActionTypeImpl>> actions = asActions(value);
        assertThat(actions, hasSize(2));
        verifySequenceAction(actions.get(0), SequenceActionTypeImpl.CLICK, null);
        verifySequenceAction(actions.get(1), SequenceActionTypeImpl.RELEASE, null);
        verifyNoMoreInteractions(stringToLocatorConverter);
    }

    @Test
    void testConvertValueString()
    {
        String value = "|type      |argument|\n"
                     + "|ENTER_TEXT|text    |";
        List<SequenceAction<SequenceActionTypeImpl>> actions = asActions(value);
        assertThat(actions, hasSize(1));
        verifySequenceAction(actions.get(0), SequenceActionTypeImpl.ENTER_TEXT, TEXT);
        verifyNoMoreInteractions(stringToLocatorConverter);
    }

    private List<SequenceAction<SequenceActionTypeImpl>> asActions(String table)
    {
        return new ExamplesTable(table).getRowsAsParameters()
                                       .stream()
                                       .map(p -> converter.convertValue(p, null))
                                       .collect(Collectors.toList());
    }

    private static void verifySequenceAction(SequenceAction<SequenceActionTypeImpl> action,
            SequenceActionTypeImpl expectedType, Object expectedArgument)
    {
        assertEquals(expectedType, action.getType());
        assertEquals(expectedArgument, action.getArgument());
    }

    private static final class TestParametersToSequenceActionConverter
            extends AbstractParametersToSequenceActionConverter<SequenceActionTypeImpl>
    {
        protected TestParametersToSequenceActionConverter(StringToLocatorConverter stringToLocatorConverter)
        {
            super(stringToLocatorConverter, List.class);
        }
    }

    private enum SequenceActionTypeImpl implements SequenceActionType<Object>
    {
        ENTER_TEXT(false)
        {
            @Override
            public Type getArgumentType()
            {
                return String.class;
            }
        },
        RELEASE(true)
        {
            @Override
            public Type getArgumentType()
            {
                return null;
            }
        },
        DOUBLE_CLICK(false)
        {
            @Override
            public Type getArgumentType()
            {
                return WebElement.class;
            }
        },
        CLICK(true)
        {
            @Override
            public Type getArgumentType()
            {
                return WebElement.class;
            }
        };

        private final boolean nullable;

        SequenceActionTypeImpl(boolean nullable)
        {
            this.nullable = nullable;
        }

        @Override
        public void addAction(Object actions, Object argument)
        {
            // Do nothing
        }

        @Override
        public boolean isNullable()
        {
            return nullable;
        }
    }
}
