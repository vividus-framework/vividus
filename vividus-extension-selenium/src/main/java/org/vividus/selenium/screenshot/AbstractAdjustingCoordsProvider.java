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

package org.vividus.selenium.screenshot;

import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebElement;
import org.vividus.ui.context.IUiContext;

import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.coordinates.WebDriverCoordsProvider;

public abstract class AbstractAdjustingCoordsProvider extends WebDriverCoordsProvider
{
    private static final long serialVersionUID = -7145089672020971479L;
    private final transient IUiContext uiContext;

    protected AbstractAdjustingCoordsProvider(IUiContext uiContext)
    {
        this.uiContext = uiContext;
    }

    protected Coords adjustToSearchContext(Coords coords)
    {
        SearchContext searchContext = uiContext.getSearchContext();
        if (searchContext instanceof WebElement)
        {
            Coords searchContextCoords = getCoords((WebElement) searchContext);
            Coords intersected = coords.intersection(searchContextCoords);
            intersected.x = intersected.x - searchContextCoords.x;
            intersected.y = intersected.y - searchContextCoords.y;
            return intersected;
        }
        return coords;
    }

    protected abstract Coords getCoords(WebElement element);

    protected IUiContext getUiContext()
    {
        return uiContext;
    }
}
