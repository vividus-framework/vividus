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

package org.vividus.bdd.steps.ui;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebElement;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.steps.ui.validation.IBaseValidations;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.ElementActions;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;

@ExtendWith(MockitoExtension.class)
class GenericSetVariableStepsTests
{
    private static final String TEXT = "text";
    private static final String ATTRIBUTE_NAME = "attribute";
    private static final String ATTRIBUTE_VALUE_MESSAGE = "The '" + ATTRIBUTE_NAME + "' attribute value was found";
    private static final String VARIABLE_NAME = "variableName";
    private static final Set<VariableScope> VARIABLE_SCOPE = Set.of(VariableScope.SCENARIO);

    @Mock private ISoftAssert softAssert;
    @Mock private IBaseValidations baseValidations;
    @Mock private IBddVariableContext bddVariableContext;
    @Mock private ElementActions elementActions;
    @Mock private IUiContext uiContext;
    @Mock private ISearchActions searchActions;
    @InjectMocks private GenericSetVariableSteps genericSetVariableSteps;

    @AfterEach
    void afterEach()
    {
        verifyNoMoreInteractions(softAssert, baseValidations, bddVariableContext, elementActions, uiContext);
    }

    @Test
    void shouldSaveContextElementTextToVariable()
    {
        WebElement webElement = mock(WebElement.class);
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(webElement);
        when(elementActions.getElementText(webElement)).thenReturn(TEXT);
        genericSetVariableSteps.saveContextElementTextToVariable(VARIABLE_SCOPE, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, TEXT);
    }

    @Test
    void shouldSaveContextElementTextToVariableNoContext()
    {
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(null);
        genericSetVariableSteps.saveContextElementTextToVariable(VARIABLE_SCOPE, VARIABLE_NAME);
        verifyNoInteractions(bddVariableContext);
    }

    @Test
    void shouldSaveContextElementAttributeValueToVariable()
    {
        WebElement webElement = mock(WebElement.class);
        when(uiContext.getSearchContext(WebElement.class)).thenReturn(webElement);
        when(webElement.getAttribute(ATTRIBUTE_NAME)).thenReturn(TEXT);
        when(softAssert.assertNotNull(ATTRIBUTE_VALUE_MESSAGE, TEXT)).thenReturn(Boolean.TRUE);
        genericSetVariableSteps.saveContextElementAttributeValueToVariable(ATTRIBUTE_NAME, VARIABLE_SCOPE,
                VARIABLE_NAME);
        verify(bddVariableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, TEXT);
    }

    @ParameterizedTest
    @CsvSource({
            ",     0",
            "text, 1"
    })
    void shouldSaveAttributeValueOfElementByLocatorToVariable(String value, int numberOfSaves)
    {
        Locator locator = mock(Locator.class);
        WebElement webElement = mock(WebElement.class);
        when(baseValidations.assertElementExists("The element to extract the attribute", locator))
                .thenReturn(Optional.of(webElement));
        when(webElement.getAttribute(ATTRIBUTE_NAME)).thenReturn(value);
        when(softAssert.assertNotNull(ATTRIBUTE_VALUE_MESSAGE, value)).thenReturn(Boolean.TRUE);
        genericSetVariableSteps.saveAttributeValueOfElementByLocatorToVariable(ATTRIBUTE_NAME, locator, VARIABLE_SCOPE,
                VARIABLE_NAME);
        verify(bddVariableContext, times(numberOfSaves)).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, value);
    }

    @Test
    void shouldSaveNumberOfElementsToVariable()
    {
        Locator locator = mock(Locator.class);
        WebElement webElement = mock(WebElement.class);
        when(searchActions.findElements(locator)).thenReturn(List.of(webElement));

        genericSetVariableSteps.saveNumberOfElementsToVariable(locator, VARIABLE_SCOPE, VARIABLE_NAME);

        verify(bddVariableContext).putVariable(VARIABLE_SCOPE, VARIABLE_NAME, 1);
    }
}
