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

package org.vividus.ui.web.playwright;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;

@SuppressWarnings("PMD.CloseResource")
@ExtendWith(MockitoExtension.class)
class UiContextTests
{
    private static final PlaywrightLocator PLAYWRIGHT_LOCATOR = new PlaywrightLocator("xpath", "//div");
    private final UiContext uiContext = new UiContext(new SimpleTestContext());

    @Test
    void shouldSetAndGetPage()
    {
        Page page = mock();

        uiContext.setCurrentPage(page);
        var currentPage = uiContext.getCurrentPage();

        assertSame(page, currentPage);
    }

    @Test
    void shouldReturnNullWhenPageNotSet()
    {
        var currentPage = uiContext.getCurrentPage();

        assertNull(currentPage);
    }

    @Test
    void shouldLocateElementOnPage()
    {
        Page page = mock();
        uiContext.setCurrentPage(page);
        Locator locator = mock();
        when(page.locator(PLAYWRIGHT_LOCATOR.getLocator())).thenReturn(locator);
        Locator actual = uiContext.locateElement(PLAYWRIGHT_LOCATOR);
        assertSame(locator, actual);
    }

    @Test
    void shouldLocateElementWithinContext()
    {
        Page page = mock();
        uiContext.setCurrentPage(page);
        Locator context = mock();
        uiContext.setContext(context);
        Locator locator = mock();
        when(context.locator(PLAYWRIGHT_LOCATOR.getLocator())).thenReturn(locator);
        Locator actual = uiContext.locateElement(PLAYWRIGHT_LOCATOR);
        assertSame(locator, actual);
    }

    @Test
    void shouldLocateElementOnPageAfterContextReset()
    {
        Page page = mock();
        uiContext.setCurrentPage(page);
        Locator context = mock();
        uiContext.setContext(context);
        uiContext.reset();
        Locator locator = mock();
        when(page.locator(PLAYWRIGHT_LOCATOR.getLocator())).thenReturn(locator);
        Locator actual = uiContext.locateElement(PLAYWRIGHT_LOCATOR);
        assertSame(locator, actual);
        verifyNoInteractions(context);
    }

    @Test
    void shouldReturnCurrentContext()
    {
        Page page = mock();
        uiContext.setCurrentPage(page);
        Locator context = mock();
        uiContext.setContext(context);
        Locator actual = uiContext.getCurrentContexOrPageRoot();
        assertSame(context, actual);
        verifyNoInteractions(page);
    }

    @Test
    void shouldReturnPageRootWhenNoContextIsSet()
    {
        Page page = mock();
        uiContext.setCurrentPage(page);
        Locator root = mock();
        when(page.locator("//html/body")).thenReturn(root);
        Locator actual = uiContext.getCurrentContexOrPageRoot();
        assertSame(root, actual);
    }
}
