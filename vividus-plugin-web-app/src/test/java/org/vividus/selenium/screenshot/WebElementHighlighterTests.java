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

package org.vividus.selenium.screenshot;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebElement;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.web.action.IJavascriptActions;

@ExtendWith(MockitoExtension.class)
class WebElementHighlighterTests
{
    private static final String ENABLE_HIGHLIGHT_SCRIPT = "arguments[0].style.border=\"7px solid yellow\";"
            + "arguments[0].style.boxShadow=\"0px 0px 70px 20px red\"";
    private static final String DISABLE_HIGHLIGHT_SCRIPT = "arguments[0].style.border=\"\";"
            + "arguments[0].style.boxShadow=\"\"";

    @Mock
    private IUiContext uiContext;

    @Mock
    private IJavascriptActions javascriptActions;

    @Mock
    private WebElement webElement;

    @InjectMocks
    private WebElementHighlighter webElementHighlighter;

    @Test
    void testTakeScreenshotWithHighlights()
    {
        when(uiContext.getAssertingWebElements()).thenReturn(List.of(webElement));
        SearchContext searchContext = mock(SearchContext.class, withSettings().extraInterfaces(WebElement.class));
        when(uiContext.getSearchContext()).thenReturn(searchContext);
        Object expected = new Object();
        Object actual = webElementHighlighter.takeScreenshotWithHighlights(() -> expected);
        assertEquals(expected, actual);
        verify(javascriptActions).executeScript(ENABLE_HIGHLIGHT_SCRIPT, searchContext);
        verify(javascriptActions).executeScript(ENABLE_HIGHLIGHT_SCRIPT, webElement);
        verify(javascriptActions).executeScript(DISABLE_HIGHLIGHT_SCRIPT, searchContext);
        verify(javascriptActions).executeScript(DISABLE_HIGHLIGHT_SCRIPT, webElement);
    }

    @Test
    void testTakeScreenshotWithHighlightsNoContext()
    {
        when(uiContext.getAssertingWebElements()).thenReturn(List.of(webElement));
        SearchContext searchContext = mock(SearchContext.class);
        when(uiContext.getSearchContext()).thenReturn(searchContext);
        Object expected = new Object();
        Object actual = webElementHighlighter.takeScreenshotWithHighlights(() -> expected);
        assertEquals(expected, actual);
        verify(javascriptActions).executeScript(ENABLE_HIGHLIGHT_SCRIPT, webElement);
        verify(javascriptActions).executeScript(DISABLE_HIGHLIGHT_SCRIPT, webElement);
    }

    @Test
    void testEnableHighlightingStaleException()
    {
        when(uiContext.getAssertingWebElements()).thenReturn(List.of(webElement));
        SearchContext searchContext = mock(SearchContext.class);
        when(uiContext.getSearchContext()).thenReturn(searchContext);
        StaleElementReferenceException exception = new StaleElementReferenceException("StaleElementReferenceException");
        when(javascriptActions.executeScript(ENABLE_HIGHLIGHT_SCRIPT, webElement)).thenThrow(exception);
        Object expected = new Object();
        Object actual = webElementHighlighter.takeScreenshotWithHighlights(() -> expected);
        assertEquals(expected, actual);
        verify(javascriptActions).executeScript(DISABLE_HIGHLIGHT_SCRIPT, webElement);
    }

    @Test
    void testEnableHighlightingRuntimeException()
    {
        when(uiContext.getAssertingWebElements()).thenReturn(List.of(webElement));
        String exceptionMessage = "RuntimeException";
        SearchContext searchContext = mock(SearchContext.class);
        when(uiContext.getSearchContext()).thenReturn(searchContext);
        when(javascriptActions.executeScript(ENABLE_HIGHLIGHT_SCRIPT, webElement))
                .thenThrow(new RuntimeException(exceptionMessage));
        RuntimeException exception = assertThrows(RuntimeException.class,
            () -> webElementHighlighter.takeScreenshotWithHighlights(Object::new));
        assertEquals(exceptionMessage, exception.getMessage());
        verify(javascriptActions).executeScript(DISABLE_HIGHLIGHT_SCRIPT, webElement);
    }
}
