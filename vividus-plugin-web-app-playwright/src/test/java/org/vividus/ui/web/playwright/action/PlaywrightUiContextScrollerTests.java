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

package org.vividus.ui.web.playwright.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Locator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.playwright.UiContext;

@ExtendWith(MockitoExtension.class)
class PlaywrightUiContextScrollerTests
{
    @Mock private UiContext uiContext;
    @Mock private PlaywrightScrollActions scrollActions;
    @Mock private Locator locator;
    @InjectMocks private PlaywrightUiContextScroller scroller;

    @Test
    void shouldScrollToTopOfElement()
    {
        when(uiContext.getContext()).thenReturn(locator);
        scroller.scrollToTop();
        verify(scrollActions).scrollToStartOf(locator);
    }

    @Test
    void shouldScrollToBottomOfElement()
    {
        when(uiContext.getContext()).thenReturn(locator);
        scroller.scrollToBottom();
        verify(scrollActions).scrollToEndOf(locator);
    }

    @Test
    void shouldScrollToLeftOfElement()
    {
        when(uiContext.getContext()).thenReturn(locator);
        scroller.scrollToLeft();
        verify(scrollActions).scrollToLeftOf(locator);
    }

    @Test
    void shouldScrollToRightOfElement()
    {
        when(uiContext.getContext()).thenReturn(locator);
        scroller.scrollToRight();
        verify(scrollActions).scrollToRightOf(locator);
    }

    @Test
    void shouldScrollToStartOfPage()
    {
        when(uiContext.getContext()).thenReturn(null);
        scroller.scrollToTop();
        verify(scrollActions).scrollToStartOfPage();
    }

    @Test
    void shouldScrollToEndOfPage()
    {
        when(uiContext.getContext()).thenReturn(null);
        scroller.scrollToBottom();
        verify(scrollActions).scrollToEndOfPage();
    }

    @Test
    void shouldThrowExceptionWhenScrollLeftOnPage()
    {
        shouldThrowExceptionWhenHorizontalPageScroll(scroller::scrollToLeft);
    }

    @Test
    void shouldThrowExceptionWhenScrollRightOnPage()
    {
        shouldThrowExceptionWhenHorizontalPageScroll(scroller::scrollToRight);
    }

    private void shouldThrowExceptionWhenHorizontalPageScroll(Executable executable)
    {
        when(uiContext.getContext()).thenReturn(null);
        var exception = assertThrows(UnsupportedOperationException.class, executable);
        assertEquals("Horizontal scroll of the page is not supported", exception.getMessage());
    }
}
