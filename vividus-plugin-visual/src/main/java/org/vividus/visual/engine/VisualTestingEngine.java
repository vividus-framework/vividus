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

package org.vividus.visual.engine;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Base64;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.visual.model.VisualCheck;
import org.vividus.visual.model.VisualCheckResult;
import org.vividus.visual.screenshot.ScreenshotProvider;

import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;
import ru.yandex.qatools.ashot.comparison.PointsMarkupPolicy;
import ru.yandex.qatools.ashot.util.ImageTool;

@SuppressWarnings("MagicNumber")
public class VisualTestingEngine implements IVisualTestingEngine
{
    private static final Logger LOGGER = LoggerFactory.getLogger(VisualTestingEngine.class);

    private static final Color DIFF_COLOR = new Color(238, 111, 238);
    private static final Screenshot EMPTY_SCREENSHOT =
            new Screenshot(new BufferedImage(1, 1, BufferedImage.TYPE_3BYTE_BGR));

    private final ScreenshotProvider screenshotProvider;
    private final IBaselineRepository baselineRepository;

    private int acceptableDiffPercentage;
    private boolean overrideBaselines;

    public VisualTestingEngine(ScreenshotProvider screenshotProvider,
            IBaselineRepository baselineRepository)
    {
        this.screenshotProvider = screenshotProvider;
        this.baselineRepository = baselineRepository;
    }

    @Override
    public VisualCheckResult establish(VisualCheck visualCheck) throws IOException
    {
        VisualCheckResult comparisonResult = new VisualCheckResult(visualCheck);
        Screenshot checkpoint = getCheckpointScreenshot(visualCheck);
        comparisonResult.setCheckpoint(imageToBase64(checkpoint.getImage()));
        baselineRepository.saveBaseline(checkpoint, visualCheck.getBaselineName());
        return comparisonResult;
    }

    private Screenshot getCheckpointScreenshot(VisualCheck visualCheck)
    {
        return screenshotProvider.take(visualCheck);
    }

    @Override
    public VisualCheckResult compareAgainst(VisualCheck visualCheck) throws IOException
    {
        visualCheck.getAcceptableDiffPercentage().ifPresent(e -> this.setAcceptableDiffPercentage(e));
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

        int comparisonImageSize = calculateComparisonImageSize(baselineScreenshot, checkpoint);
        ImageDiff diff = findImageDiff(baselineScreenshot, checkpoint, comparisonImageSize);
        comparisonResult.setPassed(!diff.hasDiff());
        comparisonResult.setDiff(imageToBase64(diff.getMarkedImage()));
        LOGGER.atInfo().addArgument((double) acceptableDiffPercentage)
                       .addArgument(() -> Math.ceil((double) (diff.getDiffSize() * 100) / (double) comparisonImageSize))
                       .log("The acceptable visual difference percentage is {}% , but actual was {}%");
        if (overrideBaselines)
        {
            baselineRepository.saveBaseline(checkpoint, visualCheck.getBaselineName());
        }
        return comparisonResult;
    }

    private ImageDiff findImageDiff(Screenshot expected, Screenshot actual, int comparisonImageSize)
    {
        PointsMarkupPolicy pointsMarkupPolicy = new PointsMarkupPolicy();
        pointsMarkupPolicy.setDiffSizeTrigger((int) (comparisonImageSize * acceptableDiffPercentage * 0.01));
        ImageDiffer differ = new ImageDiffer().withDiffMarkupPolicy(pointsMarkupPolicy.withDiffColor(DIFF_COLOR));
        return differ.makeDiff(expected, actual);
    }

    private int calculateComparisonImageSize(Screenshot expected, Screenshot actual)
    {
        int width = Math.max(expected.getImage().getWidth(), actual.getImage().getWidth());
        int height = Math.max(expected.getImage().getHeight(), actual.getImage().getHeight());
        return width * height;
    }

    private String imageToBase64(BufferedImage image) throws IOException
    {
        return Base64.getEncoder().encodeToString(ImageTool.toByteArray(image));
    }

    public void setOverrideBaselines(boolean overrideBaselines)
    {
        this.overrideBaselines = overrideBaselines;
    }

    public void setAcceptableDiffPercentage(int acceptableDiffPercentage)
    {
        this.acceptableDiffPercentage = acceptableDiffPercentage;
    }
}
