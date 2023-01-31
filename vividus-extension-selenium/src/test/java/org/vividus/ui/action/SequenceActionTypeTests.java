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
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.function.Consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openqa.selenium.WebElement;
import org.vividus.testdouble.TestSequenceActionType;

@SuppressWarnings("unchecked")
class SequenceActionTypeTests
{
    @Test
    void testPerformActionArgumentIsNull()
    {
        Consumer<WebElement> consumer = mock(Consumer.class);
        var runnable = mock(Runnable.class);
        TestSequenceActionType.DOUBLE_CLICK.performOnWebElement(null, consumer, runnable);
        verify(runnable).run();
        verifyNoMoreInteractions(consumer);
    }

    @Test
    void testPerformActionOnWebElement()
    {
        var object = mock(WebElement.class);
        Consumer<WebElement> consumer = mock(Consumer.class);
        var runnable = mock(Runnable.class);
        TestSequenceActionType.DOUBLE_CLICK.performOnWebElement(object, consumer, runnable);
        verify(consumer).accept(object);
        verifyNoMoreInteractions(object, runnable);
    }

    @Test
    void testPerformAction()
    {
        var object = mock(WebElement.class);
        Consumer<WebElement> consumer = mock(Consumer.class);
        TestSequenceActionType.DOUBLE_CLICK.perform(object, consumer);
        verify(consumer).accept(object);
        verifyNoMoreInteractions(object);
    }

    @EnumSource(TestSequenceActionType.class)
    @ParameterizedTest
    void testPerformActionWrongArgumentType(TestSequenceActionType type)
    {
        var dummy = mock(Object.class);
        var consumer = mock(Consumer.class);
        var runnable = mock(Runnable.class);
        var exception = assertThrows(IllegalArgumentException.class, () -> type.perform(dummy, consumer, runnable));
        assertEquals(String.format("Argument for %s action must be of type %s", type.name(), type.getArgumentType()),
                exception.getMessage());
        verifyNoMoreInteractions(consumer, runnable);
    }
}
