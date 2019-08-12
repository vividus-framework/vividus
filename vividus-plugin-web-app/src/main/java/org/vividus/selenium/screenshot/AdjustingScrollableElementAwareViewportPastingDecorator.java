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

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.ui.web.action.IJavascriptActions;

import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

public class AdjustingScrollableElementAwareViewportPastingDecorator
        extends ScrollableElementAwareViewportPastingDecorator
{
    private static final long serialVersionUID = 278174510416744242L;

    private final int headerToCut;
    private final int footerToCut;
    private int scrolls = 2;

    public AdjustingScrollableElementAwareViewportPastingDecorator(ShootingStrategy strategy,
            WebElement scrollableElement, IJavascriptActions javascriptActions, ScreenshotConfiguration configuration)
    {
        super(strategy, scrollableElement, javascriptActions, configuration);
        this.footerToCut = configuration.getWebFooterToCut();
        this.headerToCut = configuration.getWebHeaderToCut();
    }

    @Override
    public int getWindowHeight(WebDriver driver)
    {
        return super.getWindowHeight(driver) - footerToCut - headerToCut;
    }

    @Override
    public int getFullHeight(WebDriver driver)
    {
        return super.getFullHeight(driver) + headerToCut + footerToCut;
    }

    @Override
    protected int getCurrentScrollY(JavascriptExecutor js)
    {
        int scrollY = super.getCurrentScrollY(js);
        if (scrolls != 0)
        {
            scrolls--;
            return scrollY;
        }
        return scrollY + headerToCut;
    }
}
