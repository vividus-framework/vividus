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

package org.vividus.visual.screenshot;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.manager.IWebDriverManager;
import org.vividus.selenium.screenshot.IScrollbarHandler;
import org.vividus.ui.context.IUiContext;

import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;

public class AdjustingCoordsProvider extends WebDriverCoordsProvider
{
    private static final long serialVersionUID = 3963826455192835938L;

    private final transient IWebDriverManager webDriverManager;

    private final transient IScrollbarHandler scrollbarHandler;

    private final transient IUiContext uiContext;

    public AdjustingCoordsProvider(IWebDriverManager webDriverManager, IScrollbarHandler scrollbarHandler,
            IUiContext uiContext)
    {
        this.webDriverManager = webDriverManager;
        this.scrollbarHandler = scrollbarHandler;
        this.uiContext = uiContext;
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
            return adjustToSearchContext(driver, coords);
        });
    }

    private Coords adjustToSearchContext(WebDriver driver, Coords coords)
    {
        SearchContext searchContext = uiContext.getSearchContext();
        if (searchContext instanceof WebElement)
        {
            Coords searchContextCoords = super.ofElement(driver, (WebElement) searchContext);
            Coords intersected = coords.intersection(searchContextCoords);
            intersected.x = intersected.x - searchContextCoords.x;
            intersected.y = intersected.y - searchContextCoords.y;
            return intersected;
        }
        return coords;
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
