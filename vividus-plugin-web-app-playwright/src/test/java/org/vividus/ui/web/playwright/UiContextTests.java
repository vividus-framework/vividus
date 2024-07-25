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

package org.vividus.ui.web.playwright;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.microsoft.playwright.FrameLocator;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.ui.web.playwright.locator.PlaywrightLocator;
import org.vividus.ui.web.playwright.locator.Visibility;

@SuppressWarnings("PMD.CloseResource")
@ExtendWith(MockitoExtension.class)
class UiContextTests
{
    private static final PlaywrightLocator PLAYWRIGHT_LOCATOR = new PlaywrightLocator("xpath", "//div");
    private final SimpleTestContext testContext = new SimpleTestContext();
    private final UiContext uiContext = new UiContext(testContext);

    static
    {
        PLAYWRIGHT_LOCATOR.setVisibility(Visibility.ALL);
    }

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
    void shouldSetAndGetFrame()
    {
        FrameLocator frame = mock();
        uiContext.setCurrentFrame(frame);
        var currentFrame = uiContext.getCurrentFrame();
        assertSame(frame, currentFrame);
    }

    @Test
    void shouldSetAndGetContext()
    {
        Locator context = mock();
        uiContext.setContext(context);
        var currentContext = uiContext.getContext();
        assertSame(context, currentContext);
    }

    @Test
    void shouldReturnNullWhenFrameNotSet()
    {
        var currentFrame = uiContext.getCurrentFrame();
        assertNull(currentFrame);
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

    @Test
    void shouldReturnFrameWhenNoContextIsSet()
    {
        FrameLocator frame = mock();
        uiContext.setCurrentFrame(frame);
        Locator root = mock();
        when(frame.owner()).thenReturn(root);
        Locator actual = uiContext.getCurrentContexOrPageRoot();
        assertSame(root, actual);
    }

    @Test
    void shouldLocateVisibleElement()
    {
        var playwrightLocator = new PlaywrightLocator("css", "div");
        Page page = mock();
        uiContext.setCurrentPage(page);
        Locator locator = mock();
        Locator visibleLocator = mock();
        when(page.locator(playwrightLocator.getLocator())).thenReturn(locator);
        when(locator.locator("visible=true")).thenReturn(visibleLocator);
        Locator actual = uiContext.locateElement(playwrightLocator);
        assertSame(visibleLocator, actual);
    }

    @Test
    void shouldResetFrame()
    {
        setupFrameMock(false);
        uiContext.resetToActiveFrame();
        assertNull(uiContext.getCurrentFrame());
    }

    @Test
    void shouldNotResetFrameWhenFrameIsVisible()
    {
        FrameLocator frame = setupFrameMock(true);
        uiContext.resetToActiveFrame();
        assertEquals(frame, uiContext.getCurrentFrame());
    }

    @Test
    void shouldNotThrowExceptionWhenResetFrameWithNoFrames()
    {
        uiContext.resetToActiveFrame();
        assertNull(uiContext.getCurrentFrame());
    }

    @Test
    void shouldRemovePlaywrightContext()
    {
        uiContext.reset();
        assertEquals(1, testContext.size());
        uiContext.removePlaywrightContext();
        assertEquals(0, testContext.size());
    }

    private FrameLocator setupFrameMock(boolean visibility)
    {
        FrameLocator frame = mock();
        Locator frameOwner = mock();
        uiContext.setCurrentFrame(frame);
        when(frame.owner()).thenReturn(frameOwner);
        when(frameOwner.isVisible()).thenReturn(visibility);
        return frame;
    }
}
