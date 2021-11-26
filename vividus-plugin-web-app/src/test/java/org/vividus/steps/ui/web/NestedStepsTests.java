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

package org.vividus.steps.ui.web;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.SubSteps;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.context.SearchContextSetter;
import org.vividus.ui.web.action.ICssSelectorFactory;
import org.vividus.ui.web.action.search.WebLocatorType;

@ExtendWith(MockitoExtension.class)
class NestedStepsTests
{
    private static final String FIRST_XPATH = "//first";
    private static final String SECOND_XPATH = "//second";

    private static final String ELEMENTS_TO_PERFORM_STEPS = "Elements to iterate with steps";

    @Mock private IBaseValidations baseValidations;
    @Mock private IUiContext uiContext;
    @Mock private SubSteps subSteps;
    @Mock private ICssSelectorFactory cssSelectorFactory;
    @InjectMocks private NestedSteps nestedSteps;

    @Test
    void testPerformAllStepsForElementIfFound()
    {
        Locator locator = mock(Locator.class);
        doNothing().when(subSteps).execute(Optional.empty());
        WebElement first = mock(WebElement.class);
        WebElement second = mock(WebElement.class);
        when(baseValidations.assertIfNumberOfElementsFound(ELEMENTS_TO_PERFORM_STEPS, locator, 1,
                ComparisonRule.EQUAL_TO)).thenReturn(Arrays.asList(first, second));
        when(cssSelectorFactory.getCssSelectors(List.of(first, second)))
            .thenReturn(List.of(FIRST_XPATH, SECOND_XPATH).stream());
        Locator secondLocator = new Locator(WebLocatorType.CSS_SELECTOR,
                SECOND_XPATH);
        when(baseValidations.assertIfElementExists("An element for iteration 2",
                secondLocator)).thenReturn(second);
        SearchContextSetter searchContextSetter = mockSearchContextSetter();
        nestedSteps.performAllStepsForElementIfFound(ComparisonRule.EQUAL_TO, 1, locator, subSteps);
        verify(uiContext).putSearchContext(eq(first), any(SearchContextSetter.class));
        verify(uiContext).putSearchContext(eq(second), any(SearchContextSetter.class));
        verify(searchContextSetter, times(2)).setSearchContext();
    }

    @Test
    void testNotPerformStepsIfElementNotFound()
    {
        Locator locator = mock(Locator.class);
        when(baseValidations.assertIfNumberOfElementsFound(ELEMENTS_TO_PERFORM_STEPS, locator, 0,
                ComparisonRule.GREATER_THAN_OR_EQUAL_TO)).thenReturn(List.of());
        nestedSteps.performAllStepsForElementIfFound(ComparisonRule.GREATER_THAN_OR_EQUAL_TO, 0,
                locator, subSteps);
        verifyNoInteractions(cssSelectorFactory, uiContext, subSteps);
    }

    @Test
    void testPerformAllStepsForElementIfFoundExceptionDuringRun()
    {
        Locator locator = mock(Locator.class);
        WebElement element = mock(WebElement.class);
        when(baseValidations.assertIfNumberOfElementsFound(ELEMENTS_TO_PERFORM_STEPS, locator, 1,
                ComparisonRule.EQUAL_TO)).thenReturn(List.of(element));
        when(cssSelectorFactory.getCssSelectors(List.of(element))).thenReturn(List.of(FIRST_XPATH).stream());
        SearchContextSetter searchContextSetter = mockSearchContextSetter();
        doThrow(new StaleElementReferenceException("stale element")).when(uiContext).putSearchContext(eq(element),
                any(SearchContextSetter.class));
        assertThrows(StaleElementReferenceException.class, () -> nestedSteps
                .performAllStepsForElementIfFound(ComparisonRule.EQUAL_TO, 1, locator, subSteps));
        verify(searchContextSetter).setSearchContext();
    }

    private SearchContextSetter mockSearchContextSetter()
    {
        SearchContextSetter searchContextSetter = mock(SearchContextSetter.class);
        when(uiContext.getSearchContextSetter()).thenReturn(searchContextSetter);
        return searchContextSetter;
    }
}
