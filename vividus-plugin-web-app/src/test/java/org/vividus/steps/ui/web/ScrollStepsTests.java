/*
 * Copyright 2019-2024 the original author or authors.
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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.locator.Locator;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ui.validation.IBaseValidations;
import org.vividus.ui.action.search.SearchParameters;
import org.vividus.ui.action.search.Visibility;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.action.search.WebLocatorType;

@ExtendWith(MockitoExtension.class)
class ScrollStepsTests
{
    private static final String ELEMENT_TO_SCROLL_INTO_VIEW = "Element to scroll into view";
    private static final String ELEMENT_TO_VERIFY_POSITION = "Element to verify position";

    @Mock private IUiContext uiContext;
    @Mock private WebJavascriptActions javascriptActions;
    @Mock private ISoftAssert softAssert;
    @Mock private IBaseValidations baseValidations;
    @InjectMocks private ScrollSteps scrollSteps;

    @Test
    void shouldScrollContextInDownDirectionWhenContextIsPage()
    {
        var driver = mock(WebDriver.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(driver));
        scrollSteps.scrollContextIn(ScrollDirection.BOTTOM);
        verify(javascriptActions).scrollToEndOfPage();
    }

    @Test
    void shouldScrollContextInDownDirectionWhenContextIsElement()
    {
        var webElement = mock(WebElement.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(webElement));
        scrollSteps.scrollContextIn(ScrollDirection.BOTTOM);
        verify(javascriptActions).scrollToEndOf(webElement);
    }

    @Test
    void shouldScrollContextInUpDirectionWhenContextIsPage()
    {
        var driver = mock(WebDriver.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(driver));
        scrollSteps.scrollContextIn(ScrollDirection.TOP);
        verify(javascriptActions).scrollToStartOfPage();
    }

    @Test
    void shouldScrollContextInUpDirectionWhenContextIsElement()
    {
        var webElement = mock(WebElement.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(webElement));
        scrollSteps.scrollContextIn(ScrollDirection.TOP);
        verify(javascriptActions).scrollToStartOf(webElement);
    }

    @Test
    void shouldScrollContextInLeftDirectionWhenContextIsElement()
    {
        var webElement = mock(WebElement.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(webElement));
        scrollSteps.scrollContextIn(ScrollDirection.LEFT);
        verify(javascriptActions).scrollToLeftOf(webElement);
    }

    @Test
    void shouldScrollContextInRightDirectionWhenContextIsElement()
    {
        var webElement = mock(WebElement.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(webElement));
        scrollSteps.scrollContextIn(ScrollDirection.RIGHT);
        verify(javascriptActions).scrollToRightOf(webElement);
    }

    @Test
    void shouldScrollElementIntoView()
    {
        var webElement = mock(WebElement.class);
        var locator = mock(Locator.class);
        when(baseValidations.assertIfElementsExist(ELEMENT_TO_SCROLL_INTO_VIEW, locator)).thenReturn(
                List.of(webElement));
        scrollSteps.scrollIntoView(locator);
        verify(javascriptActions).scrollElementIntoViewportCenter(webElement);
    }

    @Test
    void shouldNotCallJavaScriptActionsIfNoElementFound()
    {
        var locator = mock(Locator.class);
        when(baseValidations.assertIfElementsExist(ELEMENT_TO_SCROLL_INTO_VIEW, locator)).thenReturn(List.of());
        scrollSteps.scrollIntoView(locator);
        verifyNoInteractions(javascriptActions);
    }

    @Test
    void testIsElementAtTheTop()
    {
        var webElement = mock(WebElement.class);
        when(webElement.getLocation()).thenReturn(new Point(50, 100));
        var locator = new Locator(WebLocatorType.ID, new SearchParameters("all", Visibility.ALL));
        when(baseValidations.assertIfElementExists(ELEMENT_TO_VERIFY_POSITION, locator)).thenReturn(webElement);
        when(javascriptActions.executeScript(
                "var windowScrollY = Math.floor(window.scrollY);"
                + "return windowScrollY <= 100 && 100 <= (windowScrollY + window.innerHeight)")).thenReturn(true);
        scrollSteps.isPageScrolledToElement(locator);
        verify(softAssert).assertTrue("The page is scrolled to an element with located by  Id: 'all'; Visibility: ALL;",
                true);
    }

    @Test
    void shouldNotCheckPageScrollPositionIfTargetElementIsMissing()
    {
        var locator = new Locator(WebLocatorType.ID, "visible");
        when(baseValidations.assertIfElementExists(ELEMENT_TO_VERIFY_POSITION, locator)).thenReturn(null);
        scrollSteps.isPageScrolledToElement(locator);
        verifyNoInteractions(softAssert);
    }
}
