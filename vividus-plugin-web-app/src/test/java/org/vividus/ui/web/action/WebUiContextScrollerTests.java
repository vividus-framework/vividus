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

package org.vividus.ui.web.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.ui.context.IUiContext;

@ExtendWith(MockitoExtension.class)
class WebUiContextScrollerTests
{
    @Mock private IUiContext uiContext;
    @Mock private WebJavascriptActions javascriptActions;
    @InjectMocks private WebUiContextScroller scroller;

    @Test
    void shouldScrollContextInDownDirectionWhenContextIsPage()
    {
        var driver = mock(WebDriver.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(driver));
        scroller.scrollToBottom();
        verify(javascriptActions).scrollToEndOfPage();
    }

    @Test
    void shouldScrollContextInDownDirectionWhenContextIsElement()
    {
        var webElement = mock(WebElement.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(webElement));
        scroller.scrollToBottom();
        verify(javascriptActions).scrollToEndOf(webElement);
    }

    @Test
    void shouldScrollContextInUpDirectionWhenContextIsPage()
    {
        var driver = mock(WebDriver.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(driver));
        scroller.scrollToTop();
        verify(javascriptActions).scrollToStartOfPage();
    }

    @Test
    void shouldScrollContextInUpDirectionWhenContextIsElement()
    {
        var webElement = mock(WebElement.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(webElement));
        scroller.scrollToTop();
        verify(javascriptActions).scrollToStartOf(webElement);
    }

    @Test
    void shouldScrollContextInLeftDirectionWhenContextIsElement()
    {
        var webElement = mock(WebElement.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(webElement));
        scroller.scrollToLeft();
        verify(javascriptActions).scrollToLeftOf(webElement);
    }

    @Test
    void shouldScrollContextInRightDirectionWhenContextIsElement()
    {
        var webElement = mock(WebElement.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(webElement));
        scroller.scrollToRight();
        verify(javascriptActions).scrollToRightOf(webElement);
    }

    @Test
    void shouldThrowExceptionForScrollContextInLeftDirectionWhenContextIsPage()
    {
        testThrowExceptionForHorizontalScrollWhenContextIsPage(scroller::scrollToLeft);
    }

    @Test
    void shouldThrowExceptionForScrollContextInRightDirectionWhenContextIsPage()
    {
        testThrowExceptionForHorizontalScrollWhenContextIsPage(scroller::scrollToRight);
    }

    private void testThrowExceptionForHorizontalScrollWhenContextIsPage(Executable toTest)
    {
        var driver = mock(WebDriver.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(driver));
        var exception = assertThrows(UnsupportedOperationException.class, toTest);
        assertEquals("Horizontal scroll of the page is not supported", exception.getMessage());
    }
}
