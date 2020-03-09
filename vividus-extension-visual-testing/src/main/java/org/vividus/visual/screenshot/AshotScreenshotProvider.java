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

package org.vividus.visual.screenshot;

import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openqa.selenium.By;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.screenshot.IScreenshotTaker;
import org.vividus.selenium.screenshot.ScreenshotDebugger;
import org.vividus.ui.web.action.ISearchActions;
import org.vividus.visual.model.VisualCheck;

import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.coordinates.CoordsProvider;

public class AshotScreenshotProvider implements ScreenshotProvider
{
    private final IScreenshotTaker screenshotTaker;
    private final ISearchActions searchActions;
    private final ScreenshotDebugger screenshotDebugger;
    private final CoordsProvider coordsProvider;
    private final IWebDriverProvider webDriverProvider;

    private Map<IgnoreStrategy, Set<By>> ignoreStrategies;

    public AshotScreenshotProvider(IScreenshotTaker screenshotTaker, ISearchActions searchActions,
            ScreenshotDebugger screenshotDebugger, CoordsProvider coordsProvider, IWebDriverProvider webDrvierProvider)
    {
        this.screenshotTaker = screenshotTaker;
        this.searchActions = searchActions;
        this.screenshotDebugger = screenshotDebugger;
        this.coordsProvider = coordsProvider;
        this.webDriverProvider = webDrvierProvider;
    }

    @Override
    public Screenshot take(VisualCheck visualCheck)
    {
        Screenshot screenshot = screenshotTaker.takeAshotScreenshot(
                visualCheck.getSearchContext(), visualCheck.getScreenshotConfiguration());
        BufferedImage original = screenshot.getImage();
        Map<IgnoreStrategy, Set<By>> stepLevelElementsToIgnore = visualCheck.getElementsToIgnore();
        for (Map.Entry<IgnoreStrategy, Set<By>> strategy : ignoreStrategies.entrySet())
        {
            IgnoreStrategy cropStrategy = strategy.getKey();
            Set<Coords> ignore = Stream.concat(
                    getLocatorsStream(strategy.getValue()),
                    getLocatorsStream(stepLevelElementsToIgnore.get(cropStrategy)))
                    .distinct()
                    .map(searchActions::findElements)
                    .flatMap(Collection::stream)
                    .map(e -> coordsProvider.ofElement(webDriverProvider.get(), e))
                    .collect(Collectors.toSet());
            if (ignore.isEmpty())
            {
                continue;
            }
            original = cropStrategy.crop(original, ignore);
            screenshotDebugger.debug(this.getClass(), "cropped_by_" + cropStrategy, original);
        }
        screenshot.setImage(original);
        return screenshot;
    }

    private Stream<By> getLocatorsStream(Set<By> locatorsSet)
    {
        return Optional.ofNullable(locatorsSet).stream().flatMap(Collection::stream);
    }

    public void setIgnoreStrategies(Map<IgnoreStrategy, Set<By>> ignoreStrategies)
    {
        this.ignoreStrategies = ignoreStrategies;
    }
}
