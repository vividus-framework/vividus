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

import java.util.function.Consumer;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.ui.context.IUiContext;

public class WebUiContextScroller implements DirectionScroller
{
    private final IUiContext uiContext;
    private final WebJavascriptActions javascriptActions;

    public WebUiContextScroller(IUiContext uiContext, WebJavascriptActions javascriptActions)
    {
        this.uiContext = uiContext;
        this.javascriptActions = javascriptActions;
    }

    @Override
    public void scrollToTop()
    {
        scrollContext(javascriptActions::scrollToStartOfPage, javascriptActions::scrollToStartOf, uiContext);
    }

    @Override
    public void scrollToBottom()
    {
        scrollContext(javascriptActions::scrollToEndOfPage, javascriptActions::scrollToEndOf, uiContext);
    }

    @Override
    public void scrollToLeft()
    {
        scrollContext(DirectionScroller::unsupportedHorizontalScroll, javascriptActions::scrollToLeftOf,
                uiContext);
    }

    @Override
    public void scrollToRight()
    {
        scrollContext(DirectionScroller::unsupportedHorizontalScroll, javascriptActions::scrollToRightOf,
                uiContext);
    }

    private static void scrollContext(Runnable pageScroller, Consumer<WebElement> elementScroller, IUiContext uiContext)
    {
        uiContext.getOptionalSearchContext().ifPresent(context -> {
            if (context instanceof WebDriver)
            {
                pageScroller.run();
                return;
            }
            elementScroller.accept((WebElement) context);
        });
    }
}
