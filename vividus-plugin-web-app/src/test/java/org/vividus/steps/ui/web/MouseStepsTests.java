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

package org.vividus.steps.ui.web;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.vividus.ui.web.action.search.WebLocatorType.CASE_SENSITIVE_TEXT;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.locator.Locator;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.web.action.ClickResult;
import org.vividus.ui.web.action.IMouseActions;
import org.vividus.ui.web.action.search.WebLocatorType;

@ExtendWith(MockitoExtension.class)
class MouseStepsTests
{
    private static final String AN_ELEMENT_TO_CLICK = "An element to click";
    private static final String ELEMENT_TO_CLICK = "Element to click";
    private static final String XPATH = ".//xpath";
    private static final String TEXT = "text";
    private static final String ELEMENT_NAME = "elementName";
    private static final String PAGE_NOT_REFRESH_AFTER_CLICK_DESCRIPTION = "Page has not been refreshed after "
            + "clicking on the element located by Case sensitive text: 'text'; Visibility: VISIBLE;";

    @Mock private IMouseActions mouseActions;
    @Mock private ISoftAssert softAssert;
    @Mock private IBaseValidations baseValidations;
    @InjectMocks private MouseSteps mouseSteps;

    @Test
    void shouldClickOnElement()
    {
        var locator = new Locator(WebLocatorType.XPATH, XPATH);
        WebElement webElement = mock();
        when(baseValidations.assertElementExists(ELEMENT_TO_CLICK, locator)).thenReturn(Optional.of(webElement));

        mouseSteps.clickOnElement(locator);

        verify(mouseActions).click(webElement);
    }

    @Test
    void shouldRetryClickOnElementIfStaleElementReferenceExceptionIsThrown()
    {
        var exception = mock(StaleElementReferenceException.class);
        var locator = new Locator(WebLocatorType.XPATH, XPATH);
        WebElement webElement = mock();
        when(baseValidations.assertElementExists(ELEMENT_TO_CLICK, locator)).thenReturn(Optional.of(webElement));
        doThrow(exception).doReturn(new ClickResult()).when(mouseActions).click(webElement);

        mouseSteps.clickOnElement(locator);

        verify(mouseActions, times(2)).click(webElement);
        verify(baseValidations, times(2)).assertElementExists(ELEMENT_TO_CLICK, locator);
    }

    @Test
    void testContextClickElementByLocator()
    {
        var locator = new Locator(WebLocatorType.XPATH, XPATH);
        WebElement webElement = mock();
        when(baseValidations.assertIfElementExists(AN_ELEMENT_TO_CLICK, locator)).thenReturn(webElement);
        mouseSteps.contextClickElementByLocator(locator);
        verify(mouseActions).contextClick(webElement);
    }

    @Test
    void testRightClickElementByLocator()
    {
        var locator = new Locator(WebLocatorType.XPATH, XPATH);
        WebElement webElement = mock();
        when(baseValidations.assertElementExists(AN_ELEMENT_TO_CLICK, locator)).thenReturn(Optional.of(webElement));
        mouseSteps.rightClickOnElement(locator);
        verify(mouseActions).contextClick(webElement);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testClickElementWithTextPageNotRefresh(boolean newPageLoaded)
    {
        var clickResult = new ClickResult();
        clickResult.setNewPageLoaded(newPageLoaded);
        var locator = new Locator(CASE_SENSITIVE_TEXT, TEXT);
        WebElement webElement = mock();
        when(baseValidations.assertIfElementExists(AN_ELEMENT_TO_CLICK, locator)).thenReturn(webElement);
        when(mouseActions.click(webElement)).thenReturn(clickResult);
        mouseSteps.clickElementPageNotRefresh(locator);
        verify(softAssert).assertTrue(PAGE_NOT_REFRESH_AFTER_CLICK_DESCRIPTION, !newPageLoaded);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void testClickElementWithTextAndPageNotRefresh(boolean newPageLoaded)
    {
        var clickResult = new ClickResult();
        clickResult.setNewPageLoaded(newPageLoaded);
        var locator = new Locator(CASE_SENSITIVE_TEXT, TEXT);
        WebElement webElement = mock();
        when(baseValidations.assertElementExists(AN_ELEMENT_TO_CLICK, locator)).thenReturn(Optional.of(webElement));
        when(mouseActions.click(webElement)).thenReturn(clickResult);
        mouseSteps.clickElementAndPageNotRefresh(locator);
        verify(softAssert).assertTrue(PAGE_NOT_REFRESH_AFTER_CLICK_DESCRIPTION, !newPageLoaded);
    }

    @Test
    void testHoverMouseOverAnElementByLocator()
    {
        var locator = new Locator(WebLocatorType.ELEMENT_NAME, ELEMENT_NAME);
        WebElement webElement = mock();
        when(baseValidations.assertIfElementExists(
                "An element with attributes Element name: 'elementName'; Visibility: VISIBLE;", locator))
                .thenReturn(webElement);
        mouseSteps.hoverMouseOverElementByLocator(locator);
        verify(mouseActions).moveToElement(webElement);
    }

    @Test
    void testHoverMouseOverElementByLocator()
    {
        var locator = new Locator(WebLocatorType.ELEMENT_NAME, ELEMENT_NAME);
        WebElement webElement = mock();
        when(baseValidations.assertElementExists("An element to hover mouse over "
                + "Element name: 'elementName'; Visibility: VISIBLE;", locator))
                .thenReturn(Optional.of(webElement));
        mouseSteps.hoverMouseOverElement(locator);
        verify(mouseActions).moveToElement(webElement);
    }
}
