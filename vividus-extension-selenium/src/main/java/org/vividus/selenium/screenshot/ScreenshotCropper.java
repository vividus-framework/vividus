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
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.openqa.selenium.WebDriver;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.action.ISearchActions;
import org.vividus.ui.action.search.Locator;

import pazone.ashot.coordinates.Coords;
import pazone.ashot.coordinates.CoordsProvider;

public class ScreenshotCropper
{
    private final ScreenshotDebugger screenshotDebugger;
    private final ISearchActions searchActions;
    private final CoordsProvider coordsProvider;
    private final IWebDriverProvider webDriverProvider;

    public ScreenshotCropper(ScreenshotDebugger screenshotDebugger, ISearchActions searchActions,
            CoordsProvider coordsProvider, IWebDriverProvider webDriverProvider)
    {
        this.screenshotDebugger = screenshotDebugger;
        this.searchActions = searchActions;
        this.coordsProvider = coordsProvider;
        this.webDriverProvider = webDriverProvider;
    }

    public BufferedImage crop(BufferedImage image, Optional<Coords> contextCoords,
            Map<IgnoreStrategy, Set<Locator>> ignoreStrategies, int topAdjustment)
    {
        BufferedImage outputImage = image;
        WebDriver webDriver = webDriverProvider.get();

        for (IgnoreStrategy strategy : List.of(IgnoreStrategy.ELEMENT, IgnoreStrategy.AREA))
        {
            Set<Coords> ignore = Optional.ofNullable(ignoreStrategies.get(strategy)).orElse(Set.of()).stream()
                    .map(searchActions::findElements)
                    .flatMap(Collection::stream)
                    .collect(Collectors.collectingAndThen(
                            Collectors.toList(),
                            webElementsToIgnore -> coordsProvider.ofElements(webDriver, webElementsToIgnore)
                    ));
            if (!ignore.isEmpty())
            {
                Point adjustment = calculateAdjustment(contextCoords, topAdjustment);
                ignore.forEach(c ->
                {
                    c.x += adjustment.x;
                    c.y += adjustment.y;
                });

                outputImage = strategy.crop(outputImage, ignore);
                screenshotDebugger.debug(this.getClass(), "cropped_by_" + strategy, outputImage);
            }
        }

        return outputImage;
    }

    protected Point calculateAdjustment(Optional<Coords> contextCoords, int topAdjustment)
    {
        return new Point(0, -topAdjustment);
    }

    protected CoordsProvider getCoordsProvider()
    {
        return coordsProvider;
    }

    protected IWebDriverProvider getWebDriverProvider()
    {
        return webDriverProvider;
    }
}
