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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Locator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.web.action.JavascriptActions;
import org.vividus.util.ResourceUtils;

@ExtendWith(MockitoExtension.class)
class PlaywrightScrollActionsTests
{
    @Mock private Locator locator;
    @Mock private JavascriptActions javascriptActions;

    @InjectMocks private PlaywrightScrollActions scrollActions;

    @Test
    void shouldScrollToStartOfPage()
    {
        scrollActions.scrollToStartOfPage();
        verify(javascriptActions).executeScript(
                "if (window.PageYOffset || document.documentElement.scrollTop > 0){ window.scrollTo(0,0);}");
    }

    @Test
    void shouldScrollToEndOfElement()
    {
        scrollActions.scrollToEndOf(locator);
        verify(locator).evaluate("(el) => el.scrollTop = el.scrollHeight");
    }

    @Test
    void shouldScrollToEndOfPage()
    {
        scrollActions.scrollToEndOfPage();
        verify(javascriptActions).executeScript(
                ResourceUtils.loadResource(PlaywrightScrollActionsTests.class, "scroll-to-end-of-page.js"));
    }

    @Test
    void shouldScrollToStartOfElement()
    {
        scrollActions.scrollToStartOf(locator);
        verify(locator).evaluate("(el) => el.scrollTop = 0");
    }

    @Test
    void shouldScrollToLeftOfElement()
    {
        scrollActions.scrollToLeftOf(locator);
        verify(locator).evaluate("(el) => el.scrollLeft=0");
    }

    @Test
    void shouldScrollToRightOfElement()
    {
        scrollActions.scrollToRightOf(locator);
        verify(locator).evaluate("(el) => el.scrollLeft=el.scrollWidth");
    }

    @Test
    void shouldScrollElementIntoViewportCenter()
    {
        var stickyHeaderSize = 25;
        scrollActions.setStickyHeaderSizePercentage(stickyHeaderSize);
        scrollActions.scrollElementIntoViewportCenter(locator);
        verify(locator).evaluate(ResourceUtils.loadResource(PlaywrightScrollActionsTests.class,
                "scroll-element-into-viewport-center.js"), stickyHeaderSize);
    }

    @Test
    void shouldCheckIfElementIsInViewport()
    {
        String script = """
            (element) => {const arguments = [element];const rect = arguments[0].getBoundingClientRect();
            const windowHeight = window.innerHeight;

            return (rect.top >= 0 && rect.top <= windowHeight) || (rect.bottom > 0 && rect.bottom <= windowHeight);}""";
        when(locator.evaluate(script)).thenReturn(true);
        boolean actualValue = scrollActions.isElementInViewport(locator);
        Assertions.assertTrue(actualValue);
    }

    @Test
    void shouldSetStickyHeaderSizePercentage()
    {
        scrollActions.setStickyHeaderSizePercentage(50);
    }
}
