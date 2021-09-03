/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.visual.screenshot;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.selenium.screenshot.AbstractAdjustingCoordsProvider;
import org.vividus.selenium.screenshot.IScrollbarHandler;
import org.vividus.ui.context.IUiContext;

import ru.yandex.qatools.ashot.coordinates.Coords;

public class WebAdjustingCoordsProvider extends AbstractAdjustingCoordsProvider
{
    private static final long serialVersionUID = 3963826455192835938L;

    private final transient IWebDriverManager webDriverManager;

    private final transient IScrollbarHandler scrollbarHandler;

    public WebAdjustingCoordsProvider(IWebDriverManager webDriverManager, IScrollbarHandler scrollbarHandler,
            IUiContext uiContext)
    {
        super(uiContext);
        this.webDriverManager = webDriverManager;
        this.scrollbarHandler = scrollbarHandler;
    }

    @Override
    public Coords ofElement(WebDriver driver, WebElement element)
    {
        return scrollbarHandler.performActionWithHiddenScrollbars(() ->
        {
            Coords coords = super.ofElement(driver, element);
            if (webDriverManager.isIOS())
            {
                coords.y += Math.toIntExact(getYOffset(driver));
            }
            return adjustToSearchContext(coords);
        });
    }

    @Override
    protected Coords getCoords(WebElement webElement)
    {
        return super.ofElement(null, webElement);
    }

    private long getYOffset(WebDriver webDriver)
    {
        return executeScript(webDriver, "return pageYOffset");
    }

    private long executeScript(WebDriver webDriver, String script)
    {
        return (long) ((JavascriptExecutor) webDriver).executeScript(script);
    }
}
