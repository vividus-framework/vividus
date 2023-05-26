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

package org.vividus.selenium.screenshot.strategies;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.screenshot.DebuggingViewportPastingDecorator;
import org.vividus.ui.web.action.WebJavascriptActions;
import org.vividus.ui.web.screenshot.WebCutOptions;

import pazone.ashot.PageDimensions;
import pazone.ashot.ShootingStrategy;

public class AdjustingScrollableElementAwareViewportPastingDecorator extends DebuggingViewportPastingDecorator
{
    private static final long serialVersionUID = 278174510416744242L;

    private final transient WebElement scrollableElement;
    private final transient WebJavascriptActions javascriptActions;
    private final transient WebCutOptions webCutOptions;

    public AdjustingScrollableElementAwareViewportPastingDecorator(ShootingStrategy strategy,
            WebElement scrollableElement, WebJavascriptActions javascriptActions,
            WebCutOptions webCutOptions)
    {
        super(strategy, "arguments[1]", webCutOptions.getWebHeaderToCut(), webCutOptions.getWebFooterToCut());
        this.javascriptActions = javascriptActions;
        this.scrollableElement = scrollableElement;
        this.webCutOptions = webCutOptions;
    }

    @Override
    protected PageDimensions getPageDimensions(WebDriver driver)
    {
        PageDimensions pageDimension = super.getPageDimensions(driver);
        int fullHeight = ((Number) javascriptActions.executeScript(
                "return Math.max(document.body.scrollHeight,"
              + "document.body.offsetHeight,"
              + "document.documentElement.clientHeight,"
              + "document.documentElement.scrollHeight,"
              + "document.documentElement.offsetHeight,"
              + "arguments[0].scrollHeight);", scrollableElement)).intValue()
              + webCutOptions.getWebHeaderToCut() + webCutOptions.getWebFooterToCut();
        return new PageDimensions(fullHeight, pageDimension.getViewportWidth(),
                pageDimension.getViewportHeight());
    }

    @Override
    protected int getScrollY(JavascriptExecutor js, int currentChunkIndex)
    {
        int scrollY = ((Number) javascriptActions.executeScript(
                "var scrollTop = arguments[0].scrollTop;" + "if(scrollTop){return scrollTop;} else {return 0;}",
                scrollableElement)).intValue();
        if (currentChunkIndex > 0)
        {
            scrollY += webCutOptions.getWebHeaderToCut();
        }
        return scrollY;
    }

    @Override
    protected void scrollVertically(JavascriptExecutor js, int scrollY)
    {
        javascriptActions
            .executeScript(getScrollVerticallyScript(), scrollY, scrollableElement);
    }
}
