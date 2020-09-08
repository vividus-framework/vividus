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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.testdouble.TestLocatorType;
import org.vividus.ui.State;
import org.vividus.ui.action.search.Locator;

@ExtendWith(MockitoExtension.class)
class GenericElementStepsTests
{
    private static final String THE_NUMBER_OF_FOUND_ELEMENTS = "The number of found elements";
    private static final String VALUE = "value";

    @Mock private IBaseValidations baseValidations;
    @InjectMocks private GenericElementSteps elementSteps;

    @Test
    void shouldAssertElementsNumber()
    {
        ComparisonRule comparisonRule = ComparisonRule.EQUAL_TO;
        int number = 1;
        Locator locator = new Locator(TestLocatorType.SEARCH, VALUE);
        elementSteps.assertElementsNumber(locator, comparisonRule, number);
        verify(baseValidations).assertIfNumberOfElementsFound(THE_NUMBER_OF_FOUND_ELEMENTS,
                locator, number, comparisonRule);
    }

    @Test
    void shouldAssertElementsNumberInState()
    {
        WebElement webElement = mock(WebElement.class);
        ComparisonRule comparisonRule = ComparisonRule.EQUAL_TO;
        int number = 1;
        Locator locator = new Locator(TestLocatorType.SEARCH, VALUE);
        when(baseValidations.assertIfNumberOfElementsFound(THE_NUMBER_OF_FOUND_ELEMENTS,
                locator, number, comparisonRule)).thenReturn(List.of(webElement, webElement, webElement));
        State state = State.ENABLED;
        InOrder ordered = Mockito.inOrder(baseValidations);
        elementSteps.assertElementsNumberInState(state, locator, comparisonRule, number);
        ordered.verify(baseValidations).assertIfNumberOfElementsFound(THE_NUMBER_OF_FOUND_ELEMENTS,
                locator, number, comparisonRule);
        ordered.verify(baseValidations, times(3)).assertElementState("Element is ENABLED", state, webElement);
    }
}
