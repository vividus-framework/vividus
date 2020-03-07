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

package org.vividus.visual.eyes.service;

import java.io.IOException;
import java.net.URI;
import java.util.Base64;

import javax.inject.Named;

import com.applitools.eyes.StepInfo;
import com.applitools.eyes.StepInfo.ApiUrls;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.images.Eyes;

import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.visual.eyes.factory.ImageEyesFactory;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheckResult;
import org.vividus.visual.screenshot.ScreenshotProvider;

@Named
public class ImageVisualTestingService implements VisualTestingService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ImageVisualTestingService.class);

    private final ImageEyesFactory eyesFactory;
    private final ScreenshotProvider screenshotProvider;
    private final IHttpClient httpClient;

    public ImageVisualTestingService(ImageEyesFactory eyesFactory, ScreenshotProvider screenshotProvider,
            @Named("eyesHttpClient") IHttpClient httpClient)
    {
        this.eyesFactory = eyesFactory;
        this.screenshotProvider = screenshotProvider;
        this.httpClient = httpClient;
    }

    @Override
    public ApplitoolsVisualCheckResult run(ApplitoolsVisualCheck applitoolsVisualCheck)
    {
        Eyes eyes = eyesFactory.createEyes(applitoolsVisualCheck);
        TestResults testResults = null;
        try
        {
            eyes.open(applitoolsVisualCheck.getAppName(), applitoolsVisualCheck.getBaselineName());
            eyes.checkImage(screenshotProvider.take(applitoolsVisualCheck).getImage());
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
        return visualCheckResult;
    }

    private void setImages(String readKey, ApplitoolsVisualCheckResult visualCheckResult, StepInfo stepInfo)
    {
        ApiUrls apiUrls = stepInfo.getApiUrls();
        visualCheckResult.setBaseline(getImageAsBase64(apiUrls.getBaselineImage(), readKey));
        visualCheckResult.setCheckpoint(getImageAsBase64(apiUrls.getCheckpointImage(), readKey));
        visualCheckResult.setDiff(getImageAsBase64(apiUrls.getDiffImage(), readKey));
    }

    private String getImageAsBase64(String url, String readKey)
    {
        if (url != null)
        {
            URI imageUrl = URI.create(addKeyToUrl(url, readKey));
            try
            {
                HttpResponse response = httpClient.doHttpGet(imageUrl);
                byte[] body = response.getResponseBody();
                if (response.getStatusCode() == HttpStatus.SC_OK && body != null)
                {
                    return Base64.getEncoder().encodeToString(body);
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
