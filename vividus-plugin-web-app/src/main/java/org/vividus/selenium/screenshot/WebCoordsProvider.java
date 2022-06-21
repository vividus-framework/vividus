/*
 * Copyright 2019-2022 the original author or authors.
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
import org.vividus.selenium.manager.IWebDriverManager;

import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;

public class WebCoordsProvider extends WebDriverCoordsProvider
{
    private static final long serialVersionUID = 3963826455192835938L;

    private final transient IWebDriverManager webDriverManager;
    private final transient IScrollbarHandler scrollbarHandler;

    public WebCoordsProvider(IWebDriverManager webDriverManager, IScrollbarHandler scrollbarHandler)
    {
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
            return coords;
        });
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
