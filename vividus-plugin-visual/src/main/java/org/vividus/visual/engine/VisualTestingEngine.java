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

package org.vividus.visual.engine;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Base64;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.model.VisualCheck;
import org.vividus.visual.model.VisualCheckResult;
import org.vividus.visual.screenshot.ScreenshotProvider;

import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.comparison.ImageDiff;
import ru.yandex.qatools.ashot.comparison.ImageDiffer;
import ru.yandex.qatools.ashot.util.ImageTool;

public class VisualTestingEngine implements IVisualTestingEngine
{
    private static final Logger LOGGER = LoggerFactory.getLogger(VisualTestingEngine.class);
    private static final int ONE_HUNDRED = 100;
    private static final int SCALE = 3;

    private final ScreenshotProvider screenshotProvider;
    private final IBaselineRepository baselineRepository;
    private final DiffMarkupPolicyFactory diffMarkupPolicyFactory;

    private double acceptableDiffPercentage;
    private double requiredDiffPercentage;
    private boolean overrideBaselines;

    public VisualTestingEngine(ScreenshotProvider screenshotProvider, IBaselineRepository baselineRepository,
            DiffMarkupPolicyFactory diffMarkupPolicyFactory)
    {
        this.screenshotProvider = screenshotProvider;
        this.baselineRepository = baselineRepository;
        this.diffMarkupPolicyFactory = diffMarkupPolicyFactory;
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
        VisualCheckResult comparisonResult = new VisualCheckResult(visualCheck);
        Screenshot checkpoint = getCheckpointScreenshot(visualCheck);
        comparisonResult.setCheckpoint(imageToBase64(checkpoint.getImage()));
        Optional<Screenshot> baseline = baselineRepository.getBaseline(visualCheck.getBaselineName());
        if (baseline.isPresent())
        {
            Screenshot baselineScreenshot = baseline.get();
            comparisonResult.setBaseline(imageToBase64(baselineScreenshot.getImage()));

            boolean inequalityCheck = visualCheck.getAction() == VisualActionType.CHECK_INEQUALITY_AGAINST;
            int height = Math.max(baselineScreenshot.getImage().getHeight(), checkpoint.getImage().getHeight());
            int width = Math.max(baselineScreenshot.getImage().getWidth(), checkpoint.getImage().getWidth());
            double diffPercentage = calculateDiffPercentage(visualCheck, inequalityCheck);
            ImageDiff diff = findImageDiff(baselineScreenshot, checkpoint, height, width, diffPercentage);
            comparisonResult.setPassed(!diff.hasDiff());
            comparisonResult.setDiff(imageToBase64(diff.getMarkedImage()));
            LOGGER.atInfo()
                  .addArgument(() -> inequalityCheck ? "required" : "acceptable")
                  .addArgument(BigDecimal.valueOf(diffPercentage))
                  .addArgument(() -> BigDecimal.valueOf(
                      (double) (diff.getDiffSize() * ONE_HUNDRED) / (width * height)).setScale(SCALE,
                          RoundingMode.CEILING))
                  .log("The {} visual difference percentage is {}% , but actual was {}%");
            if (overrideBaselines)
            {
                baselineRepository.saveBaseline(checkpoint, visualCheck.getBaselineName());
            }
        }
        else
        {
            comparisonResult.setPassed(false);
        }

        return comparisonResult;
    }

    private double calculateDiffPercentage(VisualCheck visualCheck, boolean inequalityCheck)
    {
        if (inequalityCheck)
        {
            return visualCheck.getRequiredDiffPercentage().orElse(this.requiredDiffPercentage);
        }
        return visualCheck.getAcceptableDiffPercentage().orElse(this.acceptableDiffPercentage);
    }

    private ImageDiff findImageDiff(Screenshot expected, Screenshot actual, int height, int width,
            double diffPercentage)
    {
        ImageDiffer differ = new ImageDiffer().withDiffMarkupPolicy(
            diffMarkupPolicyFactory.create(height, width, (int) diffPercentage));
        return differ.makeDiff(expected, actual);
    }

    private String imageToBase64(BufferedImage image) throws IOException
    {
        return Base64.getEncoder().encodeToString(ImageTool.toByteArray(image));
    }

    public void setOverrideBaselines(boolean overrideBaselines)
    {
        this.overrideBaselines = overrideBaselines;
    }

    public void setAcceptableDiffPercentage(double acceptableDiffPercentage)
    {
        this.acceptableDiffPercentage = acceptableDiffPercentage;
    }

    public void setRequiredDiffPercentage(double requiredDiffPercentage)
    {
        this.requiredDiffPercentage = requiredDiffPercentage;
    }
}
