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

package org.vividus.ui.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.junit.jupiter.api.Test;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.vividus.testdouble.TestAtomicActionFactories;

class AtomicActionFactoryTests
{
    @Test
    void testPerformActionArgumentIsNull()
    {
        var actions = mock(Actions.class);
        new TestAtomicActionFactories.DoubleClick().addAction(actions, null);
        verify(actions).doubleClick();
    }

    @Test
    void testPerformActionOnWebElement()
    {
        var webElement = mock(WebElement.class);
        var actions = mock(Actions.class);
        new TestAtomicActionFactories.DoubleClick().addAction(actions, webElement);
        verify(actions).doubleClick(webElement);
    }

    @Test
    void testPerformActionWrongArgumentType()
    {
        var object = mock(Object.class);
        var actions = mock(Actions.class);
        AtomicActionFactory<Actions, ?> type = new TestAtomicActionFactories.Click();
        var exception = assertThrows(IllegalArgumentException.class, () -> type.addAction(actions, object));
        assertEquals("Argument for CLICK action must be of interface org.openqa.selenium.WebElement",
                exception.getMessage());
        verifyNoInteractions(actions);
    }
}
