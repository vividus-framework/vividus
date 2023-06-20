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

package org.vividus.visual.engine;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.vividus.selenium.screenshot.AshotScreenshotTaker;
import org.vividus.ui.screenshot.ScreenshotParameters;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.model.VisualCheck;
import org.vividus.visual.model.VisualCheckResult;
import org.vividus.visual.storage.BaselineStorage;

import pazone.ashot.Screenshot;
import pazone.ashot.comparison.ImageDiff;
import pazone.ashot.comparison.ImageDiffer;
import pazone.ashot.util.ImageTool;

public class VisualTestingEngine implements IVisualTestingEngine
{
    private static final Logger LOGGER = LoggerFactory.getLogger(VisualTestingEngine.class);
    private static final int ONE_HUNDRED = 100;
    private static final int SCALE = 3;

    private final AshotScreenshotTaker<ScreenshotParameters> ashotScreenshotTaker;
    private final DiffMarkupPolicyFactory diffMarkupPolicyFactory;
    private final Map<String, BaselineStorage> baselineStorages;

    private double acceptableDiffPercentage;
    private double requiredDiffPercentage;
    private boolean overrideBaselines;
    private String baselineStorage;

    public VisualTestingEngine(AshotScreenshotTaker<ScreenshotParameters> ashotScreenshotTaker,
            DiffMarkupPolicyFactory diffMarkupPolicyFactory, Map<String, BaselineStorage> baselineStorages)
    {
        this.ashotScreenshotTaker = ashotScreenshotTaker;
        this.diffMarkupPolicyFactory = diffMarkupPolicyFactory;
        this.baselineStorages = baselineStorages;
    }

    @Override
    public VisualCheckResult establish(VisualCheck visualCheck) throws IOException
    {
        VisualCheckResult comparisonResult = new VisualCheckResult(visualCheck);
        Screenshot checkpoint = getCheckpointScreenshot(visualCheck);
        comparisonResult.setCheckpoint(imageToBytes(checkpoint.getImage()));
        getBaselineStorage(visualCheck).saveBaseline(checkpoint, visualCheck.getBaselineName());
        return comparisonResult;
    }

    private Screenshot getCheckpointScreenshot(VisualCheck visualCheck)
    {
        return visualCheck.getScreenshot().orElseGet(
                () -> ashotScreenshotTaker.takeAshotScreenshot(visualCheck.getSearchContext(),
                        visualCheck.getScreenshotParameters()));
    }

    @Override
    public VisualCheckResult compareAgainst(VisualCheck visualCheck) throws IOException
    {
        VisualCheckResult comparisonResult = new VisualCheckResult(visualCheck);
        Screenshot checkpoint = getCheckpointScreenshot(visualCheck);
        comparisonResult.setCheckpoint(imageToBytes(checkpoint.getImage()));
        Optional<Screenshot> baseline = getBaselineStorage(visualCheck).getBaseline(visualCheck.getBaselineName());
        if (baseline.isPresent())
        {
            Screenshot baselineScreenshot = baseline.get();
            comparisonResult.setBaseline(imageToBytes(baselineScreenshot.getImage()));

            boolean inequalityCheck = visualCheck.getAction() == VisualActionType.CHECK_INEQUALITY_AGAINST;
            int height = Math.max(baselineScreenshot.getImage().getHeight(), checkpoint.getImage().getHeight());
            int width = Math.max(baselineScreenshot.getImage().getWidth(), checkpoint.getImage().getWidth());
            double diffPercentage = calculateDiffPercentage(visualCheck, inequalityCheck);
            ImageDiff diff = findImageDiff(baselineScreenshot, checkpoint, height, width, diffPercentage);
            boolean passed = !diff.hasDiff();
            comparisonResult.setPassed(passed);
            comparisonResult.setDiff(imageToBytes(diff.getMarkedImage()));
            LOGGER.atLevel(passed ? Level.INFO : Level.ERROR)
                  .addArgument(() -> inequalityCheck ? "required" : "acceptable")
                  .addArgument(BigDecimal.valueOf(diffPercentage))
                  .addArgument(() -> passed ? " and" : ", but")
                  .addArgument(() -> BigDecimal.valueOf(
                      (double) (diff.getDiffSize() * ONE_HUNDRED) / (width * height)).setScale(SCALE,
                          RoundingMode.CEILING))
                  .log("The {} visual difference percentage is {}%{} actual was {}%");
            if (overrideBaselines)
            {
                getBaselineStorage(visualCheck).saveBaseline(checkpoint, visualCheck.getBaselineName());
            }
        }
        else
        {
            comparisonResult.setPassed(false);
        }

        return comparisonResult;
    }

    private BaselineStorage getBaselineStorage(VisualCheck visualCheck)
    {
        String baselineStorageName = visualCheck.getBaselineStorage().orElse(baselineStorage);
        BaselineStorage baselineStorageToUse = baselineStorages.get(baselineStorageName);
        Validate.isTrue(baselineStorageToUse != null,
                "Unable to find baseline storage with name: %s. Available baseline storages: %s", baselineStorageName,
                baselineStorages.keySet());
        return baselineStorageToUse;
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
            diffMarkupPolicyFactory.create(height, width, diffPercentage));
        return differ.makeDiff(expected, actual);
    }

    private byte[] imageToBytes(BufferedImage image) throws IOException
    {
        return ImageTool.toByteArray(image);
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

    public void setBaselineStorage(String baselineStorage)
    {
        this.baselineStorage = baselineStorage;
    }
}
