/*
 * Copyright 2019-2023 the original author or authors.
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
import org.vividus.selenium.mobileapp.screenshot.util.CoordsUtils;
import org.vividus.ui.context.IUiContext;

import pazone.ashot.coordinates.Coords;
import pazone.ashot.coordinates.WebDriverCoordsProvider;

public class MobileAppCoordsProvider extends WebDriverCoordsProvider
{
    private static final long serialVersionUID = 2966521618709606533L;

    private final transient MobileAppWebDriverManager mobileAppWebDriverManager;
    private final transient IUiContext uiContext;

    public MobileAppCoordsProvider(MobileAppWebDriverManager mobileAppWebDriverManager, IUiContext uiContext)
    {
        this.mobileAppWebDriverManager = mobileAppWebDriverManager;
        this.uiContext = uiContext;
    }

    @Override
    public Coords ofElement(WebDriver driver, WebElement element)
    {
        Coords coords = super.ofElement(null, element);
        Coords barSizeAdjustedCoords = cutBarSize(coords);

        return uiContext.getOptionalSearchContext()
                .filter(context -> !context.equals(element) && context instanceof WebElement)
                .map(WebElement.class::cast)
                .map(context -> super.ofElement(null, context))
                .map(contextCoords ->
                {
                    Coords adjustedContext = adjustToDpr(cutBarSize(contextCoords));

                    coords.width = adjustToDpr(coords.width);
                    coords.height = adjustToDpr(coords.height);
                    coords.x = adjustedContext.x + adjustToDpr(coords.x - contextCoords.x);
                    coords.y = adjustedContext.y + adjustToDpr(coords.y - contextCoords.y);

                    return coords;
                })
                .orElseGet(() -> adjustToDpr(barSizeAdjustedCoords));
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
