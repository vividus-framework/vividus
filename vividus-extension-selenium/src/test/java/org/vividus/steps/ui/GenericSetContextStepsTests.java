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

package org.vividus.steps.ui;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.context.SearchContextSetter;

@ExtendWith(MockitoExtension.class)
class GenericSetContextStepsTests
{
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
    void shouldChangeContextToElement()
    {
        Locator locator = mock(Locator.class);
        WebElement webElement = mock(WebElement.class);

        when(baseValidations.assertIfElementExists("Element to set context", locator)).thenReturn(webElement);

        genericSetContextSteps.changeContextToElement(locator);

        verify(uiContext).reset();
        verify(uiContext).putSearchContext(eq(webElement), any(SearchContextSetter.class));
    }
}
