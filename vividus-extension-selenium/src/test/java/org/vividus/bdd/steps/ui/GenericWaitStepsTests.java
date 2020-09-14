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

package org.vividus.bdd.steps.ui;

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
import org.vividus.testdouble.TestLocatorType;
import org.vividus.ui.action.IExpectedConditions;
import org.vividus.ui.action.IExpectedSearchContextCondition;
import org.vividus.ui.action.IWaitActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;

@ExtendWith(MockitoExtension.class)
class GenericWaitStepsTests
{
    private static final String VALUE = "value";

    @Mock private WebElement webElement;
    @Mock private IWaitActions waitActions;
    @Mock private IUiContext uiContext;
    @Mock private IExpectedConditions<Locator> expectedSearchActionsConditions;
    @InjectMocks private GenericWaitSteps waitSteps;

    @AfterEach
    void verifyMocks()
    {
        verifyNoMoreInteractions(waitActions, expectedSearchActionsConditions, uiContext);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testWaitTillElementAppears()
    {
        when(uiContext.getSearchContext()).thenReturn(webElement);
        Locator locator = new Locator(TestLocatorType.SEARCH, VALUE);
        IExpectedSearchContextCondition<WebElement> condition = mock(IExpectedSearchContextCondition.class);
        when(expectedSearchActionsConditions.visibilityOfElement(locator)).thenReturn(condition);
        waitSteps.waitForElementAppearance(locator);
        verify(waitActions).wait(webElement, condition);
    }

    @SuppressWarnings("unchecked")
    @Test
    void testWaitTillElementDisappears()
    {
        when(uiContext.getSearchContext()).thenReturn(webElement);
        Locator locator = new Locator(TestLocatorType.SEARCH, VALUE);
        IExpectedSearchContextCondition<Boolean> condition = mock(IExpectedSearchContextCondition.class);
        when(expectedSearchActionsConditions.invisibilityOfElement(locator)).thenReturn(condition);
        waitSteps.waitForElementDisappearance(locator);
        verify(waitActions).wait(webElement, condition);
    }
}
