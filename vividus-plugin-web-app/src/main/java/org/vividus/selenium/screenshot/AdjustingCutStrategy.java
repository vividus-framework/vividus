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

import org.openqa.selenium.WebDriver;

import ru.yandex.qatools.ashot.shooting.cutter.CutStrategy;

class AdjustingCutStrategy implements CutStrategy
{
    private static final long serialVersionUID = 2124902370489242441L;

    private final CutStrategy cutStrategy;
    private final int headerAdjustment;

    private boolean beforeScrolling = true;

    AdjustingCutStrategy(CutStrategy cutStrategy, int headerAdjustment)
    {
        this.cutStrategy = cutStrategy;
        this.headerAdjustment = headerAdjustment;
    }

    @Override
    public int getHeaderHeight(WebDriver driver)
    {
        int headerHeight = cutStrategy.getHeaderHeight(driver);
        if (beforeScrolling)
        {
            beforeScrolling = false;
            return headerHeight;
        }
        return headerHeight + headerAdjustment;
    }

    @Override
    public int getFooterHeight(WebDriver driver)
    {
        return cutStrategy.getFooterHeight(driver);
    }
}
