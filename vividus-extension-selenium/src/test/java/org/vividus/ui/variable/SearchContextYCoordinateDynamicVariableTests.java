/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.ui.variable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebElement;
import org.vividus.ui.context.UiContext;
import org.vividus.variable.DynamicVariableCalculationResult;

@ExtendWith(MockitoExtension.class)
class SearchContextYCoordinateDynamicVariableTests
{
    @Mock private UiContext uiContext;
    @InjectMocks private SearchContextYCoordinateDynamicVariable dynamicVariable;

    @Test
    void shouldReturnElementHeight()
    {
        var webElement = mock(WebElement.class);
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(webElement);
        var rectangle = mock(Rectangle.class);
        when(webElement.getRect()).thenReturn(rectangle);
        when(rectangle.getY()).thenReturn(1870);
        assertEquals(DynamicVariableCalculationResult.withValue("1870"), dynamicVariable.calculateValue());
    }

    @Test
    void shouldReturnErrorInCaseOfMissingContext()
    {
        assertEquals(DynamicVariableCalculationResult.withError("the search context is not set"),
                dynamicVariable.calculateValue());
    }
}
