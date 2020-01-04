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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.manager.IWebDriverManager;

@ExtendWith(MockitoExtension.class)
class ScrollbarHandlerTests
{
    private static final String RESTORE_STYLE_SCRIPT = "document.documentElement.style.overflow=arguments[1];";
    private static final String HIDE_SCROLLBAR_SCRIPT =
            "var originalStyleOverflow = document.documentElement.style.overflow;"
                    + "document.documentElement.style.overflow='hidden';return originalStyleOverflow;";

    @Mock(extraInterfaces = {JavascriptExecutor.class})
    private WebDriver webDriver;

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private IWebDriverManager webDriverManager;

    @Mock
    private Supplier<?> action;

    @InjectMocks
    private ScrollbarHandler scrollbarHandler;

    @Test
    void testPerformActionWithHiddenScrollbarsMobile()
    {
        when(webDriverManager.isMobile()).thenReturn(true);
        scrollbarHandler.performActionWithHiddenScrollbars(action);
        verifyNoInteractions(webDriver);
    }

    @Test
    void testPerformActionWithHiddenScrollbars()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        Object object = mock(Object.class);
        mockScrollbarHiding(object);
        scrollbarHandler.performActionWithHiddenScrollbars(action);
        verify((JavascriptExecutor) webDriver).executeScript(eq(HIDE_SCROLLBAR_SCRIPT), nullable(WebElement.class));
        verify((JavascriptExecutor) webDriver).executeScript(eq(RESTORE_STYLE_SCRIPT), nullable(WebElement.class),
                eq(object));
    }

    private void mockScrollbarHiding(Object object)
    {
        when(((JavascriptExecutor) webDriver).executeScript(eq(HIDE_SCROLLBAR_SCRIPT), nullable(WebElement.class)))
            .thenReturn(object);
    }

    @Test
    void shouldPerformActionWithHiddenScrollbarForScrollableElement()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        Object object = mock(Object.class);
        WebElement scrollableElement = mock(WebElement.class);
        String hideScript = "var originalStyleOverflow = arguments[0].style.overflow;"
                + "arguments[0].style.overflow='hidden';return originalStyleOverflow;";
        when(((JavascriptExecutor) webDriver).executeScript(hideScript, scrollableElement)).thenReturn(object);
        scrollbarHandler.performActionWithHiddenScrollbars(action, scrollableElement);
        verify((JavascriptExecutor) webDriver).executeScript(eq(hideScript), nullable(WebElement.class));
        verify((JavascriptExecutor) webDriver).executeScript("arguments[0].style.overflow=arguments[1];",
                scrollableElement, object);
    }

    @Test
    void testPerformActionWithHiddenScrollbarsNoOriginalStyle()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        mockScrollbarHiding(null);
        scrollbarHandler.performActionWithHiddenScrollbars(action);
        verify((JavascriptExecutor) webDriver, never()).executeScript(eq(RESTORE_STYLE_SCRIPT),
                nullable(WebElement.class), any());
    }

    @Test
    void testPerformActionWithHiddenScrollbarsExceptionStyleRestore()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        Supplier<?> action = mock(Supplier.class);
        Object object = mock(Object.class);
        mockScrollbarHiding(object);
        when(action.get()).thenThrow(new WebDriverException());
        assertThrows(WebDriverException.class, () -> scrollbarHandler.performActionWithHiddenScrollbars(action));
        verify((JavascriptExecutor) webDriver).executeScript(eq(RESTORE_STYLE_SCRIPT),
                nullable(WebElement.class), eq(object));
    }

    @Test
    void testPerformActionWithHiddenScrollbarsExceptionNoStyleRestore()
    {
        when(webDriverProvider.get()).thenReturn(webDriver);
        when(((JavascriptExecutor) webDriver).executeScript(eq(HIDE_SCROLLBAR_SCRIPT), nullable(WebElement.class)))
            .thenThrow(new WebDriverException());
        assertThrows(WebDriverException.class, () -> scrollbarHandler.performActionWithHiddenScrollbars(action));
        verify((JavascriptExecutor) webDriver, never()).executeScript(eq(RESTORE_STYLE_SCRIPT),
                nullable(WebElement.class), any());
    }
}
