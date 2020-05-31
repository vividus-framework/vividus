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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.applitools.eyes.StepInfo;
import com.applitools.eyes.StepInfo.ApiUrls;
import com.applitools.eyes.StepInfo.AppUrls;
import com.applitools.eyes.TestResults;
import com.applitools.eyes.images.Eyes;
import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.http.client.HttpResponse;
import org.vividus.http.client.IHttpClient;
import org.vividus.visual.eyes.factory.ImageEyesFactory;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheck;
import org.vividus.visual.eyes.model.ApplitoolsVisualCheckResult;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.screenshot.ScreenshotProvider;

import ru.yandex.qatools.ashot.Screenshot;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class ImageVisualTestingServiceTests
{
    private static final String DIFF = "diff";
    private static final TestLogger LOGGER = TestLoggerFactory.getTestLogger(ImageVisualTestingService.class);
    private static final String DIFF_AS_B64 = "ZGlmZg==";
    private static final String CHECKPOINT_AS_B64 = "Y2hlY2twb2ludA==";
    private static final String BASELINE_AS_B64 = "YmFzZWxpbmU=";
    private static final VisualActionType ACTION = VisualActionType.COMPARE_AGAINST;
    private static final String API_KEY = "?apiKey=";
    private static final String READ_KEY = "readKey";
    private static final String CHECKPOINT_IMAGE_URL = "https://applitools.com/checkpointImage";
    private static final String DIFF_IMAGE_URL = "https://applitools.com/diffImage";
    private static final String BASELINE_IMAGE_URL = "https://applitools.com/baselineImage";
    private static final String BATCH_URL = "https://appitools.com/batch";
    private static final String STEP_EDITOR_URL = "https://apllitools.com/stepEditor";
    private static final URI CHECKPOINT_IMAGE_URI = URI.create(CHECKPOINT_IMAGE_URL + API_KEY + READ_KEY);
    private static final URI DIFF_IMAGE_URI = URI.create(DIFF_IMAGE_URL + API_KEY + READ_KEY);
    private static final URI BASELINE_IMAGE_URI = URI.create(BASELINE_IMAGE_URL + API_KEY + READ_KEY);
    private static final String APP_NAME = "Dune 2";
    private static final String BASELINE_NAME = "baselineName";
    private static final String BATCH_NAME = "batchName";

    private final ImageEyesFactory eyesFactory = mock(ImageEyesFactory.class);
    private final ScreenshotProvider screenshotProvider = mock(ScreenshotProvider.class);
    private final IHttpClient httpClient = mock(IHttpClient.class);

    @InjectMocks private final ImageVisualTestingService imageVisualTestingService
        = new ImageVisualTestingService(eyesFactory, screenshotProvider, httpClient);

    @Test
    void shouldRunVisualTestAndPublishResults() throws IOException
    {
        ApplitoolsVisualCheck applitoolsVisualCheck = createCheck();
        Eyes eyes = mockEyes(applitoolsVisualCheck);
        BufferedImage image = mockScreenshot(applitoolsVisualCheck);
        mockTestResult(eyes);
        mockImagesRetrieval();

        ApplitoolsVisualCheckResult result = imageVisualTestingService.run(applitoolsVisualCheck);

        InOrder ordered = inOrder(eyes);
        ordered.verify(eyes).open(APP_NAME, BASELINE_NAME);
        ordered.verify(eyes).checkImage(image);
        ordered.verify(eyes).close(false);
        Assertions.assertAll(
            () -> assertEquals(ACTION, result.getActionType()),
            () -> assertEquals(BASELINE_AS_B64, result.getBaseline()),
            () -> assertEquals(CHECKPOINT_AS_B64, result.getCheckpoint()),
            () -> assertEquals(DIFF_AS_B64, result.getDiff()),
            () -> assertEquals(BATCH_URL, result.getBatchUrl()),
            () -> assertEquals(STEP_EDITOR_URL, result.getStepUrl()),
            () -> assertTrue(result.isPassed())
        );
    }

    @Test
    void shouldRunVisualTestAndPublishResultsAvoidMissingImagesIssues() throws IOException
    {
        ApplitoolsVisualCheck applitoolsVisualCheck = createCheck();
        Eyes eyes = mockEyes(applitoolsVisualCheck);
        mockTestResult(eyes);
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        when(httpClient.doHttpGet(BASELINE_IMAGE_URI)).thenReturn(response);
        when(httpClient.doHttpGet(CHECKPOINT_IMAGE_URI)).thenThrow(new IOException("API is broken"));
        HttpResponse emptyResponse = mock(HttpResponse.class);
        when(emptyResponse.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(httpClient.doHttpGet(DIFF_IMAGE_URI)).thenReturn(emptyResponse);
        when(emptyResponse.getResponseBody()).thenReturn(null);
        BufferedImage image = mockScreenshot(applitoolsVisualCheck);
        InOrder ordered = inOrder(eyes);

        ApplitoolsVisualCheckResult result = imageVisualTestingService.run(applitoolsVisualCheck);

        ordered.verify(eyes).open(APP_NAME, BASELINE_NAME);
        ordered.verify(eyes).checkImage(image);
        ordered.verify(eyes).close(false);
        Assertions.assertAll(
            () -> assertEquals(ACTION, result.getActionType()),
            () -> assertNull(result.getBaseline()),
            () -> assertNull(result.getCheckpoint()),
            () -> assertNull(result.getDiff()),
            () -> assertEquals(BATCH_URL, result.getBatchUrl()),
            () -> assertEquals(STEP_EDITOR_URL, result.getStepUrl()),
            () -> assertTrue(result.isPassed())
        );
        assertThat(LOGGER.getLoggingEvents(),
                is(List.of(LoggingEvent.warn("Unable to get image from {}", CHECKPOINT_IMAGE_URI))));
    }

    @Test
    void shouldNotRetrieveImagesWhenUrlNotAvailable()
    {
        ApplitoolsVisualCheck applitoolsVisualCheck = createCheck();
        Eyes eyes = mockEyes(applitoolsVisualCheck);
        TestResults results = mockTestResults(eyes);
        StepInfo stepInfo = mockStepInfo(results);
        ApiUrls apiUrls = mock(ApiUrls.class);
        when(stepInfo.getApiUrls()).thenReturn(apiUrls);
        BufferedImage image = mockScreenshot(applitoolsVisualCheck);
        InOrder ordered = inOrder(eyes);

        ApplitoolsVisualCheckResult result = imageVisualTestingService.run(applitoolsVisualCheck);

        ordered.verify(eyes).open(APP_NAME, BASELINE_NAME);
        ordered.verify(eyes).checkImage(image);
        ordered.verify(eyes).close(false);
        Assertions.assertAll(
            () -> assertEquals(ACTION, result.getActionType()),
            () -> assertNull(result.getBaseline()),
            () -> assertNull(result.getCheckpoint()),
            () -> assertNull(result.getDiff()),
            () -> assertEquals(BATCH_URL, result.getBatchUrl()),
            () -> assertEquals(STEP_EDITOR_URL, result.getStepUrl()),
            () -> assertTrue(result.isPassed())
        );
        verifyNoInteractions(httpClient);
    }

    private BufferedImage mockScreenshot(ApplitoolsVisualCheck applitoolsVisualCheck)
    {
        Screenshot screenshot = mock(Screenshot.class);
        when(screenshotProvider.take(applitoolsVisualCheck)).thenReturn(screenshot);
        BufferedImage image = mock(BufferedImage.class);
        when(screenshot.getImage()).thenReturn(image);
        return image;
    }

    private Eyes mockEyes(ApplitoolsVisualCheck applitoolsVisualCheck)
    {
        Eyes eyes = mock(Eyes.class);
        when(eyesFactory.createEyes(applitoolsVisualCheck)).thenReturn(eyes);
        return eyes;
    }

    private void mockTestResult(Eyes eyes)
    {
        TestResults results = mockTestResults(eyes);

        StepInfo stepInfo = mockStepInfo(results);

        ApiUrls apiUrls = mock(ApiUrls.class);
        when(stepInfo.getApiUrls()).thenReturn(apiUrls);
        when(apiUrls.getBaselineImage()).thenReturn(BASELINE_IMAGE_URL);
        when(apiUrls.getDiffImage()).thenReturn(DIFF_IMAGE_URL);
        when(apiUrls.getCheckpointImage()).thenReturn(CHECKPOINT_IMAGE_URL);
    }

    private StepInfo mockStepInfo(TestResults results)
    {
        StepInfo stepInfo = mock(StepInfo.class);
        when(results.getStepsInfo()).thenReturn(new StepInfo[] { stepInfo });
        AppUrls appUrls = mock(AppUrls.class);
        when(stepInfo.getAppUrls()).thenReturn(appUrls);
        when(appUrls.getStepEditor()).thenReturn(STEP_EDITOR_URL);
        return stepInfo;
    }

    private TestResults mockTestResults(Eyes eyes)
    {
        TestResults results = mock(TestResults.class);
        when(results.getUrl()).thenReturn(BATCH_URL);
        when(results.isPassed()).thenReturn(true);
        when(eyes.close(false)).thenReturn(results);
        return results;
    }

    private void mockImagesRetrieval() throws IOException
    {
        mockImageRetrieval(BASELINE_IMAGE_URI, "baseline");
        mockImageRetrieval(CHECKPOINT_IMAGE_URI, "checkpoint");
        mockImageRetrieval(DIFF_IMAGE_URI, DIFF);
    }

    private void mockImageRetrieval(URI imageUri, String responseString) throws IOException
    {
        HttpResponse response = mock(HttpResponse.class);
        when(response.getStatusCode()).thenReturn(HttpStatus.SC_OK);
        when(httpClient.doHttpGet(imageUri)).thenReturn(response);
        when(response.getResponseBody()).thenReturn(responseString.getBytes(StandardCharsets.UTF_8));
    }

    private ApplitoolsVisualCheck createCheck()
    {
        ApplitoolsVisualCheck applitoolsVisualCheck = new ApplitoolsVisualCheck(BATCH_NAME, BASELINE_NAME,
                ACTION);
        applitoolsVisualCheck.setAppName(APP_NAME);
        applitoolsVisualCheck.setReadApiKey(READ_KEY);
        return applitoolsVisualCheck;
    }
}
