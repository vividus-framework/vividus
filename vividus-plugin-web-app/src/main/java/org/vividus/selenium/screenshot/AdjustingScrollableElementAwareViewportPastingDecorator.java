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

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.ui.web.action.WebJavascriptActions;

import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

public class AdjustingScrollableElementAwareViewportPastingDecorator extends AdjustingViewportPastingDecorator
{
    private static final long serialVersionUID = 278174510416744242L;

    private final transient WebElement scrollableElement;
    private final transient WebJavascriptActions javascriptActions;

    public AdjustingScrollableElementAwareViewportPastingDecorator(ShootingStrategy strategy,
            WebElement scrollableElement, WebJavascriptActions javascriptActions, ScreenshotConfiguration configuration)
    {
        super(strategy, configuration.getWebHeaderToCut(), configuration.getWebFooterToCut(), "arguments[1]");
        this.javascriptActions = javascriptActions;
        this.scrollableElement = scrollableElement;
    }

    @Override
    public int getFullHeight(WebDriver driver)
    {
        return ((Number) javascriptActions.executeScript(
                "return Math.max(document.body.scrollHeight,"
              + "document.body.offsetHeight,"
              + "document.documentElement.clientHeight,"
              + "document.documentElement.scrollHeight,"
              + "document.documentElement.offsetHeight,"
              + "arguments[0].scrollHeight);", scrollableElement)).intValue()
              + getHeaderAdjustment() + getFooterAdjustment();
    }

    @Override
    protected int getScrollY(JavascriptExecutor js)
    {
        return ((Number) javascriptActions.executeScript("var scrollTop = arguments[0].scrollTop;"
                + "if(scrollTop){return scrollTop;} else {return 0;}", scrollableElement)).intValue();
    }

    @Override
    protected void scrollVertically(JavascriptExecutor js, int scrollY)
    {
        javascriptActions
            .executeScript(getScrollVerticallyScript(), scrollY, scrollableElement);
    }
}
