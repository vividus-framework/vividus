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

package org.vividus.visual.engine;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Base64;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.inject.Inject;

import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.screenshot.IScreenshotTaker;
import org.vividus.selenium.screenshot.ScreenshotDebugger;
import org.vividus.ui.web.action.ISearchActions;
import org.vividus.visual.engine.VisualCheckFactory.VisualCheck;
import org.vividus.visual.model.VisualCheckResult;

import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;
import ru.yandex.qatools.ashot.comparison.PointsMarkupPolicy;
import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.coordinates.CoordsProvider;
import ru.yandex.qatools.ashot.util.ImageTool;

public class VisualTestingEngine implements IVisualTestingEngine
{
    private static final Color DIFF_COLOR = new Color(238, 111, 238);
    private static final Screenshot EMPTY_SCREENSHOT =
            new Screenshot(new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR));

    @Inject private IScreenshotTaker screenshotTaker;
    @Inject private CoordsProvider coordsProvider;
    @Inject private ISearchActions searchActions;
    @Inject private IWebDriverProvider webDriverProvider;
    @Inject private IBaselineRepository baselineRepository;
    @Inject private ScreenshotDebugger screenshotDebugger;

    private Map<IgnoreStrategy, Set<By>> ignoreStrategies;
    private boolean overrideBaselines;

    @Override
    public VisualCheckResult establish(VisualCheck visualCheck) throws IOException
    {
        VisualCheckResult comparisonResult = new VisualCheckResult(visualCheck);
        Screenshot checkpoint = getCheckpointScreenshot(visualCheck);
        comparisonResult.setCheckpoint(imageToBase64(checkpoint.getImage()));
        baselineRepository.saveBaseline(checkpoint, visualCheck.getBaselineName());
        return comparisonResult;
    }

    @Override
    public VisualCheckResult compareAgainst(VisualCheck visualCheck) throws IOException
    {
        VisualCheckResult comparisonResult = new VisualCheckResult(visualCheck);
        Screenshot checkpoint = getCheckpointScreenshot(visualCheck);
        comparisonResult.setCheckpoint(imageToBase64(checkpoint.getImage()));
        Optional<Screenshot> baseline = baselineRepository.getBaseline(visualCheck.getBaselineName());
        Screenshot baselineScreenshot;
        if (baseline.isPresent())
        {
            baselineScreenshot = baseline.get();
            comparisonResult.setBaseline(imageToBase64(baselineScreenshot.getImage()));
        }
        else
        {
            baselineScreenshot = EMPTY_SCREENSHOT;
        }
        ImageDiffer differ = new ImageDiffer().withDiffMarkupPolicy(new PointsMarkupPolicy().withDiffColor(DIFF_COLOR));
        ImageDiff diff = differ.makeDiff(baselineScreenshot, checkpoint);

        comparisonResult.setPassed(!diff.hasDiff());
        comparisonResult.setDiff(imageToBase64(diff.getMarkedImage()));
        if (overrideBaselines)
        {
            baselineRepository.saveBaseline(checkpoint, visualCheck.getBaselineName());
        }
        return comparisonResult;
    }

    private String imageToBase64(BufferedImage image) throws IOException
    {
        return Base64.getEncoder().encodeToString(ImageTool.toByteArray(image));
    }

    private Screenshot getCheckpointScreenshot(VisualCheck visualCheck)
    {
        SearchContext searchContext = visualCheck.getSearchContext();
        Screenshot takeAshotScreenshot = screenshotTaker.takeAshotScreenshot(searchContext,
                visualCheck.getScreenshotConfiguration());
        BufferedImage original = takeAshotScreenshot.getImage();
        Map<IgnoreStrategy, Set<By>> stepLevelElementsToIgnore = visualCheck.getElementsToIgnore();
        for (Map.Entry<IgnoreStrategy, Set<By>> s : ignoreStrategies.entrySet())
        {
            IgnoreStrategy cropStrategy = s.getKey();
            Set<Coords> ignore = Stream.concat(
                    getLocatorsStream(s.getValue()),
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
        takeAshotScreenshot.setImage(original);
        return takeAshotScreenshot;
    }

    private Stream<By> getLocatorsStream(Set<By> locatorsSet)
    {
        return Optional.ofNullable(locatorsSet).stream().flatMap(Collection::stream);
    }

    public void setIgnoreStrategies(Map<IgnoreStrategy, Set<By>> ignoreStrategies)
    {
        this.ignoreStrategies = ignoreStrategies;
    }

    public void setOverrideBaselines(boolean overrideBaselines)
    {
        this.overrideBaselines = overrideBaselines;
    }
}
