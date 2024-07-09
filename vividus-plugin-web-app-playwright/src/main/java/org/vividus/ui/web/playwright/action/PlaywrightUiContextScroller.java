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

import java.util.Optional;
import java.util.function.Consumer;

import com.microsoft.playwright.Locator;

import org.vividus.ui.web.action.DirectionScroller;
import org.vividus.ui.web.playwright.UiContext;

public class PlaywrightUiContextScroller implements DirectionScroller
{
    private final UiContext uiContext;
    private final ScrollActions scrollActions;

    public PlaywrightUiContextScroller(UiContext uiContext, ScrollActions scrollActions)
    {
        this.uiContext = uiContext;
        this.scrollActions = scrollActions;
    }

    @Override
    public void scrollToTop()
    {
        scrollContext(scrollActions::scrollToStartOfPage, scrollActions::scrollToStartOf, uiContext);
    }

    @Override
    public void scrollToBottom()
    {
        scrollContext(scrollActions::scrollToEndOfPage, scrollActions::scrollToEndOf, uiContext);
    }

    @Override
    public void scrollToLeft()
    {
        scrollContext(DirectionScroller::unsupportedHorizontalScroll, scrollActions::scrollToLeftOf,
                uiContext);
    }

    @Override
    public void scrollToRight()
    {
        scrollContext(DirectionScroller::unsupportedHorizontalScroll, scrollActions::scrollToRightOf,
                uiContext);
    }

    private static void scrollContext(Runnable pageScroller, Consumer<Locator> elementScroller,
            UiContext uiContext)
    {
        Optional.ofNullable(uiContext.getContext()).ifPresentOrElse(elementScroller, pageScroller);
    }
}
