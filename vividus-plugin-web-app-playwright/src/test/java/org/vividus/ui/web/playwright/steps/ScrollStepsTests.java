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

package org.vividus.ui.web.playwright.steps;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Locator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ui.web.ScrollDirection;
import org.vividus.ui.web.playwright.UiContext;
import org.vividus.ui.web.playwright.action.ScrollActions;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

@ExtendWith(MockitoExtension.class)
class ScrollStepsTests
{
    private static final PlaywrightLocator PLAYWRIGHT_LOCATOR = new PlaywrightLocator("css", ".example");

    @Mock
    private ISoftAssert softAssert;

    @Mock
    private UiContext uiContext;

    @Mock
    private ScrollActions scrollActions;

    @Mock
    private Locator locator;

    @InjectMocks
    private ScrollSteps scrollSteps;

    @Test
    void shouldScrollContextInDownDirectionWhenContextIsPage()
    {
        when(uiContext.getContext()).thenReturn(null);
        scrollSteps.scrollContextIn(ScrollDirection.BOTTOM);
        verify(scrollActions).scrollToEndOfPage();
    }

    @Test
    void shouldScrollContextInDownDirectionWhenContextIsElement()
    {
        when(uiContext.getContext()).thenReturn(locator);
        scrollSteps.scrollContextIn(ScrollDirection.BOTTOM);
        verify(scrollActions).scrollToEndOf(locator);
    }

    @Test
    void shouldScrollContextInUpDirectionWhenContextIsPage()
    {
        when(uiContext.getContext()).thenReturn(null);
        scrollSteps.scrollContextIn(ScrollDirection.TOP);
        verify(scrollActions).scrollToStartOfPage();
    }

    @Test
    void shouldScrollContextInUpDirectionWhenContextIsElement()
    {
        when(uiContext.getContext()).thenReturn(locator);
        scrollSteps.scrollContextIn(ScrollDirection.TOP);
        verify(scrollActions).scrollToStartOf(locator);
    }

    @Test
    void shouldScrollContextInLeftDirectionWhenContextIsElement()
    {
        when(uiContext.getContext()).thenReturn(locator);
        scrollSteps.scrollContextIn(ScrollDirection.LEFT);
        verify(scrollActions).scrollToLeftOf(locator);
    }

    @Test
    void shouldScrollContextInRightDirectionWhenContextIsElement()
    {
        when(uiContext.getContext()).thenReturn(locator);
        scrollSteps.scrollContextIn(ScrollDirection.RIGHT);
        verify(scrollActions).scrollToRightOf(locator);
    }

    @Test
    void shouldScrollElementIntoView()
    {
        when(uiContext.locateElement(PLAYWRIGHT_LOCATOR)).thenReturn(locator);
        scrollSteps.scrollIntoView(PLAYWRIGHT_LOCATOR);
        verify(scrollActions).scrollElementIntoViewportCenter(locator);
    }

    @Test
    void shouldCheckIfPageIsScrolledToElement()
    {
        when(uiContext.locateElement(PLAYWRIGHT_LOCATOR)).thenReturn(locator);
        when(scrollActions.isScrolledToElement(locator)).thenReturn(true);
        scrollSteps.isPageScrolledToElement(PLAYWRIGHT_LOCATOR);
        verify(softAssert).assertTrue(
                "The page is scrolled to an element with located by css(.example) with visibility: visible", true);
    }
}
