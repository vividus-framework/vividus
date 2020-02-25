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

import ru.yandex.qatools.ashot.shooting.DebuggingViewportPastingDecorator;
import ru.yandex.qatools.ashot.shooting.ShootingStrategy;

class AdjustingViewportPastingDecorator extends DebuggingViewportPastingDecorator
{
    private static final long serialVersionUID = -18255457270069458L;
    private final int headerAdjustment;
    private final int footerAdjustment;
    private int scrolls = 2;

    AdjustingViewportPastingDecorator(ShootingStrategy strategy, int headerAdjustment, int footerAdjustment)
    {
        super(strategy);
        this.headerAdjustment = headerAdjustment;
        this.footerAdjustment = footerAdjustment;
    }

    AdjustingViewportPastingDecorator(ShootingStrategy strategy, int headerAdjustment)
    {
        this(strategy, headerAdjustment, 0);
    }

    @Override
    public int getWindowHeight(WebDriver driver)
    {
        return super.getWindowHeight(driver) - headerAdjustment - footerAdjustment;
    }

    @Override
    protected int getCurrentScrollY(JavascriptExecutor js)
    {
        int currentScrollY = getScrollY(js);
        if (scrolls != 0)
        {
            scrolls--;
            return currentScrollY;
        }
        return currentScrollY + headerAdjustment;
    }

    protected int getScrollY(JavascriptExecutor js)
    {
        return super.getCurrentScrollY(js);
    }

    public int getHeaderAdjustment()
    {
        return headerAdjustment;
    }

    public int getFooterAdjustment()
    {
        return footerAdjustment;
    }
}
