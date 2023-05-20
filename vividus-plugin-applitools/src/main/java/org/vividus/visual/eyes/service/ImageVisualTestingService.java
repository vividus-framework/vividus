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

package org.vividus.visual.eyes.service;

import java.io.IOException;
import java.net.URI;

import com.applitools.eyes.StepInfo;
import com.applitools.eyes.StepInfo.ApiUrls;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.images.Eyes;

import org.apache.commons.lang3.mutable.MutableBoolean;
import org.apache.hc.core5.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.selenium.screenshot.AshotScreenshotTaker;
import org.vividus.ui.screenshot.ScreenshotParameters;
import org.vividus.visual.eyes.factory.ImageEyesFactory;
import org.vividus.visual.eyes.model.ApplitoolsTestResults;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheckResult;

import pazone.ashot.Screenshot;

public class ImageVisualTestingService implements VisualTestingService<ApplitoolsVisualCheckResult>
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageVisualTestingService.class);

    private final ImageEyesFactory eyesFactory;
    private final AshotScreenshotTaker<ScreenshotParameters> ashotScreenshotTaker;
    private final IHttpClient httpClient;

    public ImageVisualTestingService(ImageEyesFactory eyesFactory,
            AshotScreenshotTaker<ScreenshotParameters> ashotScreenshotTaker, IHttpClient httpClient)
    {
        this.eyesFactory = eyesFactory;
        this.ashotScreenshotTaker = ashotScreenshotTaker;
        this.httpClient = httpClient;
    }

    @Override
    public ApplitoolsVisualCheckResult run(ApplitoolsVisualCheck applitoolsVisualCheck)
    {
        Eyes eyes = eyesFactory.createEyes(applitoolsVisualCheck);
        TestResults testResults;
        try
        {
            eyes.open(applitoolsVisualCheck.getConfiguration().getAppName(), applitoolsVisualCheck.getBaselineName());
            Screenshot screenshot = ashotScreenshotTaker.takeAshotScreenshot(applitoolsVisualCheck.getSearchContext(),
                    applitoolsVisualCheck.getScreenshotParameters());
            eyes.checkImage(screenshot.getImage());
        }
        finally
        {
            testResults = eyes.close(false);
        }
        return createVisualCheckResult(testResults, applitoolsVisualCheck);
    }

    private ApplitoolsVisualCheckResult createVisualCheckResult(TestResults testResults,
            ApplitoolsVisualCheck applitoolsVisualCheck)
    {
        ApplitoolsVisualCheckResult visualCheckResult = new ApplitoolsVisualCheckResult(applitoolsVisualCheck);
        visualCheckResult.setBatchUrl(testResults.getUrl());
        visualCheckResult.setPassed(testResults.isPassed());
        StepInfo stepInfo = testResults.getStepsInfo()[0];
        visualCheckResult.setStepUrl(stepInfo.getAppUrls().getStepEditor());
        setImages(applitoolsVisualCheck.getReadApiKey(), visualCheckResult, stepInfo);
        visualCheckResult.setApplitoolsTestResults(new ApplitoolsTestResults(testResults));
        return visualCheckResult;
    }

    private void setImages(String readKey, ApplitoolsVisualCheckResult visualCheckResult, StepInfo stepInfo)
    {
        MutableBoolean apiUnauthorized = new MutableBoolean(false);
        ApiUrls apiUrls = stepInfo.getApiUrls();
        visualCheckResult.setBaseline(getImage(apiUrls.getBaselineImage(), readKey, apiUnauthorized));
        visualCheckResult.setCheckpoint(getImage(apiUrls.getCheckpointImage(), readKey, apiUnauthorized));
        visualCheckResult.setDiff(getImage(apiUrls.getDiffImage(), readKey, apiUnauthorized));
    }

    @SuppressWarnings("NoNullForCollectionReturn")
    private byte[] getImage(String url, String readKey, MutableBoolean apiUnauthorized)
    {
        if (url != null && apiUnauthorized.isFalse())
        {
            URI imageUrl = URI.create(addKeyToUrl(url, readKey));
            try
            {
                HttpResponse response = httpClient.doHttpGet(imageUrl);
                byte[] body = response.getResponseBody();
                int statusCode = response.getStatusCode();
                if (statusCode == HttpStatus.SC_UNAUTHORIZED)
                {
                    apiUnauthorized.setValue(true);
                    LOGGER.warn("The \"readApiKey\" property is not set or incorrect. "
                            + "Checkpoint and baseline images are not available in the attachment. "
                            + "You can use the \"Step editor\" button to view them on Applitools.");
                }
                else if (statusCode == HttpStatus.SC_OK && body != null)
                {
                    return body;
                }
            }
            catch (IOException e)
            {
                LOGGER.atWarn().addArgument(imageUrl).log("Unable to get image from {}");
            }
        }
        return null;
    }

    private String addKeyToUrl(String url, String readKey)
    {
        return url + "?apiKey=" + readKey;
    }
}
