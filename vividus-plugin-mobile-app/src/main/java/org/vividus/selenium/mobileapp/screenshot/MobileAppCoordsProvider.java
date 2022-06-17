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

import java.util.Optional;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.mobileapp.MobileAppWebDriverManager;
import org.vividus.selenium.mobileapp.screenshot.util.CoordsUtils;
import org.vividus.ui.context.IUiContext;

import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;

public class MobileAppCoordsProvider extends WebDriverCoordsProvider
{
    private static final long serialVersionUID = 2966521618709606533L;

    private final transient MobileAppWebDriverManager mobileAppWebDriverManager;
    private final transient IUiContext uiContext;
    private final boolean downscale;

    public MobileAppCoordsProvider(boolean downscale, MobileAppWebDriverManager mobileAppWebDriverManager,
            IUiContext uiContext)
    {
        this.mobileAppWebDriverManager = mobileAppWebDriverManager;
        this.uiContext = uiContext;
        this.downscale = downscale;
    }

    @Override
    public Coords ofElement(WebDriver driver, WebElement element)
    {
        Coords coords = super.ofElement(null, element);
        Coords barSizeAdjustedCoords = cutBarSize(coords);

        if (downscale)
        {
            return barSizeAdjustedCoords;
        }

        Optional<SearchContext> searchContext = uiContext.getOptionalSearchContext();
        if (searchContext.isPresent())
        {
            SearchContext context = searchContext.get();
            if (!context.equals(element) && context instanceof WebElement)
            {
                Coords contextCoords = super.ofElement(null, (WebElement) context);
                Coords adjustedContext = adjustToDpr(cutBarSize(contextCoords));

                coords.width = adjustToDpr(coords.width);
                coords.height = adjustToDpr(coords.height);
                coords.x = adjustedContext.x + adjustToDpr(coords.x - contextCoords.x);
                coords.y = adjustedContext.y + adjustToDpr(coords.y - contextCoords.y);

                return coords;
            }
        }

        return adjustToDpr(barSizeAdjustedCoords);
    }

    private Coords cutBarSize(Coords coords)
    {
        return new Coords(coords.x, coords.y - mobileAppWebDriverManager.getStatusBarSize(), coords.width,
                coords.height);
    }

    private Coords adjustToDpr(Coords coords)
    {
        double dpr = mobileAppWebDriverManager.getDpr();
        return CoordsUtils.scale(coords, dpr);
    }

    private int adjustToDpr(int value)
    {
        double dpr = mobileAppWebDriverManager.getDpr();
        return CoordsUtils.scale(value, dpr);
    }
}
