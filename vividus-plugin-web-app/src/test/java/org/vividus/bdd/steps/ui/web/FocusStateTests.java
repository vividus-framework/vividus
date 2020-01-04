/*
 * Copyright 2019-2020 the original author or authors.
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

package org.vividus.bdd.steps.ui.web;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.ui.web.action.IJavascriptActions;

@ExtendWith(MockitoExtension.class)
class FocusStateTests
{
    @Mock
    private WebElement webElement;

    @Mock
    private IJavascriptActions javascriptActions;

    @Test
    void testIsInStatePositive()
    {
        mockIsInFocus(true);
        assertTrue(FocusState.IN_FOCUS.isInState(javascriptActions, webElement));
    }

    @Test
    void testIsInStateNegative()
    {
        mockIsInFocus(false);
        assertFalse(FocusState.IN_FOCUS.isInState(javascriptActions, webElement));
    }

    @Test
    void testIsNotInStatePositive()
    {
        mockIsInFocus(false);
        assertTrue(FocusState.NOT_IN_FOCUS.isInState(javascriptActions, webElement));
    }

    @Test
    void testIsNotInStateNegative()
    {
        mockIsInFocus(true);
        assertFalse(FocusState.NOT_IN_FOCUS.isInState(javascriptActions, webElement));
    }

    private void mockIsInFocus(boolean isInState)
    {
        when(javascriptActions.executeScript("return arguments[0]==document.activeElement", webElement))
                .thenReturn(isInState);
    }
}
