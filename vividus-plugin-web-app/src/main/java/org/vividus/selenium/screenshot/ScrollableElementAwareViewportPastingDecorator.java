/*
 * Copyright 2019 the original author or authors.
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

import java.awt.image.BufferedImage;
import java.time.Duration;
import java.util.Set;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.ui.web.action.IJavascriptActions;
import org.vividus.util.Sleeper;

import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.shooting.DebuggingViewportPastingDecorator;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

public class ScrollableElementAwareViewportPastingDecorator extends DebuggingViewportPastingDecorator
{
    private static final long serialVersionUID = -6444288772256432686L;

    private final transient WebElement scrollableElement;
    private final transient IJavascriptActions javascriptActions;
    private final Duration scrollTimeout;

    public ScrollableElementAwareViewportPastingDecorator(ShootingStrategy strategy, WebElement scrollableElement,
            IJavascriptActions javascriptActions, ScreenshotConfiguration configuration)
    {
        super(strategy);
        this.javascriptActions = javascriptActions;
        this.scrollableElement = scrollableElement;
        this.scrollTimeout = configuration.getScrollTimeout();
    }

    @Override
    public BufferedImage getScreenshot(WebDriver wd, Set<Coords> coordsSet)
    {
        int startScrollPosition = getCurrentScrollY(null);
        BufferedImage image = super.getScreenshot(wd, coordsSet);
        scrollVertically(null, startScrollPosition);
        Sleeper.sleep(scrollTimeout);
        return image;
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
              + "arguments[0].scrollHeight);", scrollableElement)).intValue();
    }

    @Override
    protected void scrollVertically(JavascriptExecutor js, int scrollY)
    {
        javascriptActions
            .executeScript("arguments[0].scrollTo(0, arguments[1]); return [];", scrollableElement, scrollY);
    }

    @Override
    protected int getCurrentScrollY(JavascriptExecutor js)
    {
        return ((Number) javascriptActions.executeScript("var scrollTop = arguments[0].scrollTop;"
                + "if(scrollTop){return scrollTop;} else {return 0;}", scrollableElement)).intValue();
    }
}
