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

import com.microsoft.playwright.Locator;

import org.vividus.ui.web.action.JavascriptActions;
import org.vividus.ui.web.action.ScrollActions;
import org.vividus.util.ResourceUtils;

public class PlaywrightScrollActions implements ScrollActions<Locator>
{
    private static final String IS_ELEMENT_IN_VIEWPORT = "(element) => {const arguments = [element];%s}"
            .formatted(ResourceUtils.loadResource("check-element-in-viewport.js").trim());
    private static final String SCROLL_ELEMENT_INTO_VIEWPORT_CENTER =
            loadScript("scroll-element-into-viewport-center.js");
    private static final String SCROLL_TO_END_OF_PAGE = loadScript("scroll-to-end-of-page.js");

    private final JavascriptActions javascriptActions;

    private int stickyHeaderSizePercentage;

    public PlaywrightScrollActions(JavascriptActions javascriptActions)
    {
        this.javascriptActions = javascriptActions;
    }

    /**
     * Scrolls page to the top (0, 0)
     */
    public void scrollToStartOfPage()
    {
        javascriptActions.executeScript(
                "if (window.PageYOffset || document.documentElement.scrollTop > 0){ window.scrollTo(0,0);}");
    }

    public void scrollToEndOf(Locator element)
    {
        element.evaluate("(el) => el.scrollTop = el.scrollHeight");
    }

    /**
     * Scrolls to the end of the page with dynamically loading content upon scrolling
     */
    public void scrollToEndOfPage()
    {
        javascriptActions.executeScript(SCROLL_TO_END_OF_PAGE);
    }

    public void scrollToStartOf(Locator element)
    {
        element.evaluate("(el) => el.scrollTop = 0");
    }

    public void scrollToLeftOf(Locator element)
    {
        element.evaluate("(el) => el.scrollLeft=0");
    }

    public void scrollToRightOf(Locator element)
    {
        element.evaluate("(el) => el.scrollLeft=el.scrollWidth");
    }

    public void scrollElementIntoViewportCenter(Locator element)
    {
        element.evaluate(SCROLL_ELEMENT_INTO_VIEWPORT_CENTER, stickyHeaderSizePercentage);
    }

    @Override
    public boolean isElementInViewport(Locator element)
    {
        return (boolean) element.evaluate(IS_ELEMENT_IN_VIEWPORT);
    }

    private static String loadScript(String jsResourceName)
    {
        return ResourceUtils.loadResource(PlaywrightScrollActions.class, jsResourceName);
    }

    public void setStickyHeaderSizePercentage(int stickyHeaderSizePercentage)
    {
        this.stickyHeaderSizePercentage = stickyHeaderSizePercentage;
    }
}
