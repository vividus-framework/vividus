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

package org.vividus.bdd.steps.ui.web;

import java.util.function.Consumer;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.web.action.WebJavascriptActions;

public enum ScrollDirection
{
    TOP
    {
        @Override
        public void scroll(IUiContext uiContext, WebJavascriptActions javascriptActions)
        {
            scrollContext(javascriptActions::scrollToStartOfPage, javascriptActions::scrollToStartOf, uiContext);
        }
    },
    BOTTOM
    {
        @Override
        public void scroll(IUiContext uiContext, WebJavascriptActions javascriptActions)
        {
            scrollContext(javascriptActions::scrollToEndOfPage, javascriptActions::scrollToEndOf, uiContext);
        }
    },
    LEFT
    {
        @Override
        public void scroll(IUiContext uiContext, WebJavascriptActions javascriptActions)
        {
            scrollContext(ScrollDirection::unsupportedScroll, javascriptActions::scrollToLeftOf,
                    uiContext);
        }
    },
    RIGHT
    {
        @Override
        public void scroll(IUiContext uiContext, WebJavascriptActions javascriptActions)
        {
            scrollContext(ScrollDirection::unsupportedScroll, javascriptActions::scrollToRightOf,
                    uiContext);
        }
    };

    public abstract void scroll(IUiContext uiContext, WebJavascriptActions javascriptActions);

    private static void scrollContext(Runnable pageScroller, Consumer<WebElement> elementScroller,
            IUiContext uiContext)
    {
        SearchContext context = uiContext.getSearchContext();
        if (context instanceof WebDriver)
        {
            pageScroller.run();
            return;
        }
        elementScroller.accept((WebElement) context);
    }

    private static void unsupportedScroll()
    {
        throw new UnsupportedOperationException("Horizontal scroll of the page not supported");
    }
}
