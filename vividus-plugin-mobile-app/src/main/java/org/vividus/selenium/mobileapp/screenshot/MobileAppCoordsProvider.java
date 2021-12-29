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

package org.vividus.selenium.mobileapp.screenshot;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.mobileapp.MobileAppWebDriverManager;
import org.vividus.selenium.mobileapp.screenshot.util.CoordsUtil;
import org.vividus.selenium.screenshot.AbstractAdjustingCoordsProvider;
import org.vividus.ui.context.IUiContext;

import ru.yandex.qatools.ashot.coordinates.Coords;

public class MobileAppCoordsProvider extends AbstractAdjustingCoordsProvider
{
    private static final long serialVersionUID = 2966521618709606533L;

    private final transient MobileAppWebDriverManager mobileAppWebDriverManager;
    private final boolean downscale;

    public MobileAppCoordsProvider(boolean downscale, MobileAppWebDriverManager mobileAppWebDriverManager,
        IUiContext uiContext)
    {
        super(uiContext);
        this.mobileAppWebDriverManager = mobileAppWebDriverManager;
        this.downscale = downscale;
    }

    @Override
    public Coords ofElement(WebDriver driver, WebElement element)
    {
        Coords coords = getCoords(element);
        coords = getUiContext().getSearchContext() == element ? coords : adjustToSearchContext(coords);
        return downscale ? coords : adjustToDpr(coords);
    }

    protected Coords getCoords(WebElement element)
    {
        Coords coords = super.ofElement(null, element);
        return new Coords(coords.x, coords.y - mobileAppWebDriverManager.getStatusBarSize(), coords.width,
                coords.height);
    }

    public MobileAppWebDriverManager getMobileAppWebDriverManager()
    {
        return mobileAppWebDriverManager;
    }

    private Coords adjustToDpr(Coords coords)
    {
        double dpr = getMobileAppWebDriverManager().getDpr();
        coords.x = CoordsUtil.multiply(coords.x, dpr);
        coords.y = CoordsUtil.multiply(coords.y, dpr);
        coords.width = CoordsUtil.multiply(coords.width, dpr);
        coords.height = CoordsUtil.multiply(coords.height, dpr);
        return coords;
    }
}
