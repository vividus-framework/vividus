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

package org.vividus.steps.ui;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.context.SearchContextSetter;

@ExtendWith(MockitoExtension.class)
class GenericSetContextStepsTests
{
    private static final String ELEMENT_TO_SET_CONTEXT = "Element to set context";

    @Mock private IUiContext uiContext;
    @Mock private IBaseValidations baseValidations;
    @InjectMocks private GenericSetContextSteps genericSetContextSteps;

    @AfterEach
    void afterEach()
    {
        verifyNoMoreInteractions(uiContext, baseValidations);
    }

    @Test
    void shouldResetContext()
    {
        genericSetContextSteps.resetContext();
        verify(uiContext).reset();
    }

    @Test
    void shouldResetAndChangeContextToElement()
    {
        Locator locator = mock(Locator.class);
        WebElement webElement = mock(WebElement.class);
        GenericSetContextSteps spy = Mockito.spy(genericSetContextSteps);
        ArgumentCaptor<SearchContextSetter> setterCaptor = ArgumentCaptor.forClass(SearchContextSetter.class);

        when(baseValidations.assertIfElementExists(ELEMENT_TO_SET_CONTEXT, locator)).thenReturn(webElement);
        doNothing().when(uiContext).putSearchContext(eq(webElement), setterCaptor.capture());

        spy.resetAndChangeContextToElement(locator);

        SearchContextSetter setter = setterCaptor.getValue();
        setter.setSearchContext();
        verify(spy, times(2)).resetAndChangeContextToElement(locator);
        verify(uiContext, times(2)).reset();
        verify(uiContext, times(2)).putSearchContext(eq(webElement), any(SearchContextSetter.class));
        verify(baseValidations, times(2)).assertIfElementExists(ELEMENT_TO_SET_CONTEXT, locator);
    }

    @Test
    void shouldChangeContextToElement()
    {
        Locator locator = mock(Locator.class);
        WebElement webElement = mock(WebElement.class);
        GenericSetContextSteps spy = Mockito.spy(genericSetContextSteps);
        when(baseValidations.assertElementExists(ELEMENT_TO_SET_CONTEXT, locator)).thenReturn(Optional.of(webElement));
        ArgumentCaptor<SearchContextSetter> setterCaptor = ArgumentCaptor.forClass(SearchContextSetter.class);
        doNothing().when(uiContext).putSearchContext(eq(webElement), setterCaptor.capture());

        spy.changeContextToElement(locator);

        SearchContextSetter setter = setterCaptor.getValue();
        setter.setSearchContext();
        verify(spy, times(2)).changeContextToElement(locator);
        verify(uiContext, never()).reset();
        verify(uiContext, times(2)).putSearchContext(eq(webElement), any(SearchContextSetter.class));
    }

    @Test
    void shouldSetNullInCaseOfMissingElement()
    {
        Locator locator = mock(Locator.class);
        when(baseValidations.assertElementExists(ELEMENT_TO_SET_CONTEXT, locator)).thenReturn(Optional.empty());

        genericSetContextSteps.changeContextToElement(locator);

        verify(uiContext, never()).reset();
        verify(uiContext).putSearchContext(eq(null), any(SearchContextSetter.class));
    }
}
