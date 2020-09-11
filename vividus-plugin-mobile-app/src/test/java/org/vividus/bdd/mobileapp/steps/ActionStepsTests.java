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

package org.vividus.bdd.mobileapp.steps;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.mobileapp.action.KeyboardActions;
import org.vividus.mobileapp.action.TapActions;
import org.vividus.ui.action.search.Locator;

@ExtendWith(MockitoExtension.class)
class ActionStepsTests
{
    private static final String TEXT = "text";
    private static final String ELEMENT_TO_TAP = "The element to tap";
    private static final String ELEMENT_TO_TYPE_TEXT = "The element to type text";

    @Mock private IBaseValidations baseValidations;
    @Mock private TapActions tapActions;
    @Mock private KeyboardActions keyboardActions;
    @Mock private Locator locator;
    @InjectMocks private ActionSteps actionSteps;

    @AfterEach
    void afterEach()
    {
        verifyNoMoreInteractions(tapActions, keyboardActions, baseValidations, locator);
    }

    @Test
    void testTapByLocatorWithDuration()
    {
        WebElement element = mock(WebElement.class);
        when(baseValidations.assertElementExists(ELEMENT_TO_TAP, locator)).thenReturn(Optional.of(element));
        actionSteps.tapByLocatorWithDuration(locator, Duration.ZERO);
        verify(tapActions).tap(element, Duration.ZERO);
    }

    @Test
    void testTapByLocatorWithDurationElementIsEmpty()
    {
        when(baseValidations.assertElementExists(ELEMENT_TO_TAP, locator)).thenReturn(Optional.empty());
        actionSteps.tapByLocatorWithDuration(locator, Duration.ZERO);
    }

    @Test
    void testTapByLocator()
    {
        WebElement element = mock(WebElement.class);
        when(baseValidations.assertElementExists(ELEMENT_TO_TAP, locator)).thenReturn(Optional.of(element));
        actionSteps.tapByLocator(locator);
        verify(tapActions).tap(element);
    }

    @Test
    void testTapByLocatorElementIsEmpty()
    {
        when(baseValidations.assertElementExists(ELEMENT_TO_TAP, locator)).thenReturn(Optional.empty());
        actionSteps.tapByLocator(locator);
    }

    @Test
    void testTypeTextInField()
    {
        WebElement element = mock(WebElement.class);
        when(baseValidations.assertElementExists(ELEMENT_TO_TYPE_TEXT, locator)).thenReturn(Optional.of(element));
        actionSteps.typeTextInField(TEXT, locator);
        verify(keyboardActions).typeText(element, TEXT);
    }

    @Test
    void testTypeTextInFieldElementIsEmpty()
    {
        when(baseValidations.assertElementExists(ELEMENT_TO_TYPE_TEXT, locator)).thenReturn(Optional.empty());
        actionSteps.typeTextInField(TEXT, locator);
    }
}
