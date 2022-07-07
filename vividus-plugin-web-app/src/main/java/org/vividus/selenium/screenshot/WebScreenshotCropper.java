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

import java.awt.Point;
import java.util.Optional;

import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.context.IUiContext;

import pazone.ashot.coordinates.Coords;
import pazone.ashot.coordinates.CoordsProvider;

public class WebScreenshotCropper extends ScreenshotCropper
{
    private final IUiContext uiContext;

    public WebScreenshotCropper(ScreenshotDebugger screenshotDebugger, ISearchActions searchActions,
            CoordsProvider coordsProvider, IWebDriverProvider webDriverProvider, IUiContext uiContext)
    {
        super(screenshotDebugger, searchActions, coordsProvider, webDriverProvider);
        this.uiContext = uiContext;
    }

    @Override
    protected Point calculateAdjustment(Optional<Coords> contextCoords, int topAdjustment)
    {
        if (contextCoords.isEmpty())
        {
            return super.calculateAdjustment(contextCoords, topAdjustment);
        }

        /**
         * This shift in x and y coords should be removed after the following issue is resolved:
         * https://github.com/vividus-framework/vividus/issues/2883
         */
        WebElement context = uiContext.getSearchContext(WebElement.class).get();
        Coords currentContextCoords = getCoordsProvider().ofElement(getWebDriverProvider().get(), context);
        Coords targetContextCoords = contextCoords.get();

        int yShift = targetContextCoords.y - currentContextCoords.y;
        int xShift = targetContextCoords.x - currentContextCoords.x;

        return new Point(xShift, yShift - topAdjustment);
    }
}
