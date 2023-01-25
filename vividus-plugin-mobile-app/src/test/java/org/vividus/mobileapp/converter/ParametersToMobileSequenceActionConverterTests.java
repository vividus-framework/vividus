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

package org.vividus.mobileapp.converter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.converter.ui.StringToLocatorConverter;
import org.vividus.mobileapp.action.MobileSequenceActionType;
import org.vividus.ui.action.SequenceAction;
import org.vividus.ui.action.search.Locator;

@ExtendWith(MockitoExtension.class)
class ParametersToMobileSequenceActionConverterTests
{
    private static final String TEXT = "text";

    @Mock private  StringToLocatorConverter stringToLocatorConverter;
    @InjectMocks private ParametersToMobileSequenceActionConverter converter;

    @Test
    void shouldConvertParametersToMobileSequenceActions()
    {
        String by = "By.id(" + TEXT + ")";
        Locator locator = mock(Locator.class);
        when(stringToLocatorConverter.convertValue(by, null)).thenReturn(locator);
        String value = "|type        |argument   |\n"
                     + "|TAP_AND_HOLD|By.id(text)|\n"
                     + "|MOVE_TO     |By.id(text)|\n"
                     + "|RELEASE     |           |\n"
                     + "|WAIT        |PT1S       |\n"
                     + "|TAP_AND_HOLD|           |\n";
        List<SequenceAction<MobileSequenceActionType>> actions = asActions(value);
        assertThat(actions, hasSize(5));
        verify(stringToLocatorConverter, times(2)).convertValue(by, null);
        verifyNoMoreInteractions(stringToLocatorConverter);
        verifySequenceAction(actions.get(0), MobileSequenceActionType.TAP_AND_HOLD, locator);
        verifySequenceAction(actions.get(1), MobileSequenceActionType.MOVE_TO, locator);
        verifySequenceAction(actions.get(2), MobileSequenceActionType.RELEASE, null);
        Duration duration = Duration.ofSeconds(1);
        verifySequenceAction(actions.get(3), MobileSequenceActionType.WAIT, duration);
        verifySequenceAction(actions.get(4), MobileSequenceActionType.TAP_AND_HOLD, null);
    }

    private static void verifySequenceAction(SequenceAction<MobileSequenceActionType> action,
            MobileSequenceActionType expectedType, Object expectedArgument)
    {
        assertEquals(expectedType, action.getType());
        assertEquals(expectedArgument, action.getArgument());
    }

    private List<SequenceAction<MobileSequenceActionType>> asActions(String table)
    {
        return new ExamplesTable(table).getRowsAsParameters()
                .stream()
                .map(p -> converter.convertValue(p, null))
                .collect(Collectors.toList());
    }
}
