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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;

import javax.imageio.ImageIO;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.ui.screenshot.ScreenshotConfiguration;
import org.vividus.ui.screenshot.ScreenshotParametersFactory;
import org.vividus.util.ResourceUtils;
import org.vividus.visual.VisualCheckFactory;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.model.VisualCheck;
import org.vividus.visual.model.VisualCheckResult;
import org.vividus.visual.screenshot.ScreenshotProvider;

import ru.yandex.qatools.ashot.Screenshot;

@ExtendWith({MockitoExtension.class, TestLoggerFactoryExtension.class})
class VisualTestingEngineTests
{
    private static final String ACCEPTABLE = "acceptable";

    private static final String BASELINE = "baseline";

    private static final String BASELINE_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAIAAAACDbGyAAAAD0lEQVR4XmPg5uZmoCIA"
                                                + "AApeACIUkokZAAAAAElFTkSuQmCC";

    private static final String CHECKPOINT_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAIAAAACDbGyAAAAL0lEQVR4XmNgZ2dnQA"
                                                  + "bz589HYb9///78+fNADpAEskHCQOrbt29QDgT8/v0bwgAAXNsZsacVEHkAAAAASUVO"
                                                  + "RK5CYII=";

    private static final String DIFF_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAIAAAACDbGyAAAAHklEQVR4XmPg5uZmQAbv8t+h"
                                            + "sIEYIgRnQFnIChG6ADGrFxBXlUutAAAAAElFTkSuQmCC";

    private static final String LOG_MESSAGE = "The {} visual difference percentage is {}% , but actual was {}%";

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(VisualTestingEngine.class);

    @Mock private ScreenshotParametersFactory<ScreenshotConfiguration> screenshotParametersFactory;
    @Mock private IBaselineRepository baselineRepository;
    @Mock private ScreenshotProvider screenshotProvider;
    @InjectMocks private VisualTestingEngine visualTestingEngine;

    private VisualCheckFactory factory;

    @BeforeEach
    void init()
    {
        factory = new VisualCheckFactory(screenshotParametersFactory);
        when(screenshotParametersFactory.create(Optional.empty())).thenReturn(Optional.empty());
        factory.setScreenshotIndexer(Optional.empty());
        factory.setIndexers(Map.of());
        visualTestingEngine.setAcceptableDiffPercentage(0);
    }

    @Test
    void shouldReturnOnlyCheckpointForEstablishAction() throws IOException
    {
        VisualCheck visualCheck = createVisualCheck(VisualActionType.ESTABLISH);
        BufferedImage finalImage = mockGetCheckpointScreenshot(visualCheck);
        VisualCheckResult checkResult = visualTestingEngine.establish(visualCheck);
        verify(baselineRepository).saveBaseline(argThat(s -> finalImage.equals(s.getImage())), eq(BASELINE));
        Assertions.assertAll(
            () -> assertNull(checkResult.getBaseline()),
            () -> assertNull(checkResult.getDiff()),
            () -> assertEquals(BASELINE, checkResult.getBaselineName()),
            () -> assertEquals(VisualActionType.ESTABLISH, checkResult.getActionType()),
            () -> assertEquals(CHECKPOINT_BASE64, checkResult.getCheckpoint()),
            () -> assertFalse(checkResult.isPassed()));
        assertThat(testLogger.getLoggingEvents(), empty());
    }

    private VisualCheck createVisualCheck(VisualActionType actionType)
    {
        return factory.create(BASELINE, actionType);
    }

    @ParameterizedTest
    @CsvSource({"0, false", "25, false", "50, true", "100, true"})
    void shouldReturnVisualCheckResultWithDiffBaselineAndCheckpointDiffSensitivity(
        double acceptableDiffPercentage, boolean status) throws IOException
    {
        when(baselineRepository.getBaseline(BASELINE)).thenReturn(Optional.of(new Screenshot(loadImage(BASELINE))));
        VisualCheck visualCheck = createVisualCheck(VisualActionType.COMPARE_AGAINST);
        visualCheck.setAcceptableDiffPercentage(OptionalDouble.of(acceptableDiffPercentage));
        mockGetCheckpointScreenshot(visualCheck);
        VisualCheckResult checkResult = visualTestingEngine.compareAgainst(visualCheck);
        Assertions.assertAll(
            () -> assertEquals(BASELINE_BASE64, checkResult.getBaseline()),
            () -> assertEquals(BASELINE, checkResult.getBaselineName()),
            () -> assertEquals(CHECKPOINT_BASE64, checkResult.getCheckpoint()),
            () -> assertEquals(VisualActionType.COMPARE_AGAINST, checkResult.getActionType()),
            () -> assertEquals(DIFF_BASE64, checkResult.getDiff()),
            () -> assertEquals(status, checkResult.isPassed()));
        verify(baselineRepository, never()).saveBaseline(any(), any());
        assertThat(testLogger.getLoggingEvents(), is(List.of(info(LOG_MESSAGE, ACCEPTABLE,
                BigDecimal.valueOf(acceptableDiffPercentage), 40.0))));
    }

    @ParameterizedTest
    @CsvSource({"0,      false",
                "0.0002, false",
                "25,     false",
                "50,     true",
                "100,    true"})
    void shouldCompareImagesUsingRequiredDiffPercentageForTheInequalComparison(
        double requiredDiffPercentage, boolean status) throws IOException
    {
        when(baselineRepository.getBaseline(BASELINE)).thenReturn(Optional.of(new Screenshot(loadImage(BASELINE))));
        VisualCheck visualCheck = createVisualCheck(VisualActionType.CHECK_INEQUALITY_AGAINST);
        visualCheck.setRequiredDiffPercentage(OptionalDouble.of(requiredDiffPercentage));
        mockGetCheckpointScreenshot(visualCheck);
        VisualCheckResult checkResult = visualTestingEngine.compareAgainst(visualCheck);
        Assertions.assertAll(
            () -> assertEquals(BASELINE_BASE64, checkResult.getBaseline()),
            () -> assertEquals(BASELINE, checkResult.getBaselineName()),
            () -> assertEquals(CHECKPOINT_BASE64, checkResult.getCheckpoint()),
            () -> assertEquals(VisualActionType.CHECK_INEQUALITY_AGAINST, checkResult.getActionType()),
            () -> assertEquals(DIFF_BASE64, checkResult.getDiff()),
            () -> assertEquals(status, checkResult.isPassed()));
        verify(baselineRepository, never()).saveBaseline(any(), any());
        assertThat(testLogger.getLoggingEvents(), is(List.of(info(LOG_MESSAGE, "required",
            BigDecimal.valueOf(requiredDiffPercentage), 40.0))));
    }

    @Test
    void shouldReturnVisualCheckResultWithBaselineAndCheckpointUsingAcceptableDiffPercentage() throws IOException
    {
        when(baselineRepository.getBaseline(BASELINE)).thenReturn(Optional.of(new Screenshot(loadImage(BASELINE))));
        VisualCheck visualCheck = factory.create(BASELINE, VisualActionType.COMPARE_AGAINST);
        visualCheck.setAcceptableDiffPercentage(OptionalDouble.of(50));
        mockGetCheckpointScreenshot(visualCheck);
        VisualCheckResult checkResult = visualTestingEngine.compareAgainst(visualCheck);
        Assertions.assertAll(
            () -> assertEquals(BASELINE_BASE64, checkResult.getBaseline()),
            () -> assertEquals(BASELINE, checkResult.getBaselineName()),
            () -> assertEquals(CHECKPOINT_BASE64, checkResult.getCheckpoint()),
            () -> assertEquals(VisualActionType.COMPARE_AGAINST, checkResult.getActionType()),
            () -> assertEquals(DIFF_BASE64, checkResult.getDiff()),
            () -> assertTrue(checkResult.isPassed()));
        verify(baselineRepository, never()).saveBaseline(any(), any());
        assertThat(testLogger.getLoggingEvents(), is(List.of(info(LOG_MESSAGE, ACCEPTABLE, BigDecimal.valueOf(50.0),
                40.0))));
    }

    @Test
    void shouldReturnVisualCheckResultWithBaselineAndCheckpoint() throws IOException
    {
        when(baselineRepository.getBaseline(BASELINE)).thenReturn(Optional.of(new Screenshot(loadImage(BASELINE))));
        VisualCheck visualCheck = createVisualCheck(VisualActionType.COMPARE_AGAINST);
        mockGetCheckpointScreenshot(visualCheck, BASELINE);
        VisualCheckResult checkResult = visualTestingEngine.compareAgainst(visualCheck);
        Assertions.assertAll(
            () -> assertEquals(BASELINE_BASE64, checkResult.getBaseline()),
            () -> assertEquals(BASELINE, checkResult.getBaselineName()),
            () -> assertEquals(BASELINE_BASE64, checkResult.getCheckpoint()),
            () -> assertEquals(VisualActionType.COMPARE_AGAINST, checkResult.getActionType()),
            () -> assertEquals(BASELINE_BASE64, checkResult.getDiff()),
            () -> assertTrue(checkResult.isPassed()));
        verify(baselineRepository, never()).saveBaseline(any(), any());
        assertThat(testLogger.getLoggingEvents(), is(List.of(info(LOG_MESSAGE, ACCEPTABLE, BigDecimal.valueOf(0d),
            0.0))));
    }

    @Test
    void shouldOverrideBaselinesIfPropertySet() throws IOException
    {
        visualTestingEngine.setOverrideBaselines(true);
        when(baselineRepository.getBaseline(BASELINE)).thenReturn(Optional.of(new Screenshot(loadImage(BASELINE))));
        VisualCheck visualCheck = createVisualCheck(VisualActionType.COMPARE_AGAINST);
        BufferedImage finalImage = mockGetCheckpointScreenshot(visualCheck);
        visualTestingEngine.compareAgainst(visualCheck);
        verify(baselineRepository).saveBaseline(argThat(s -> finalImage.equals(s.getImage())), eq(BASELINE));
        assertThat(testLogger.getLoggingEvents(), is(List.of(info(LOG_MESSAGE, ACCEPTABLE, BigDecimal.valueOf(0d),
            40.0))));
    }

    @Test
    void shouldReturnVisualCheckResultWithDiffAgainstEmptyImageAndCheckpoint() throws IOException
    {
        when(baselineRepository.getBaseline(BASELINE)).thenReturn(Optional.empty());
        VisualCheck visualCheck = createVisualCheck(VisualActionType.COMPARE_AGAINST);
        mockGetCheckpointScreenshot(visualCheck);
        VisualCheckResult checkResult = visualTestingEngine.compareAgainst(visualCheck);
        Assertions.assertAll(
            () -> assertNull(checkResult.getBaseline()),
            () -> assertEquals(BASELINE, checkResult.getBaselineName()),
            () -> assertEquals(CHECKPOINT_BASE64, checkResult.getCheckpoint()),
            () -> assertEquals(VisualActionType.COMPARE_AGAINST, checkResult.getActionType()),
            () -> assertNull(checkResult.getDiff()),
            () -> assertFalse(checkResult.isPassed()));
        assertThat(testLogger.getLoggingEvents(), is(empty()));
    }

    private BufferedImage mockGetCheckpointScreenshot(VisualCheck visualCheck, String imageName) throws IOException
    {
        BufferedImage image = loadImage(imageName);
        Screenshot screenshot = new Screenshot(image);
        when(screenshotProvider.take(visualCheck)).thenReturn(screenshot);
        return image;
    }

    private BufferedImage mockGetCheckpointScreenshot(VisualCheck visualCheck) throws IOException
    {
        return mockGetCheckpointScreenshot(visualCheck, "checkpoint");
    }

    private BufferedImage loadImage(String fileName) throws IOException
    {
        return ImageIO.read(
                ResourceUtils.loadFile(FileSystemBaselineRepository.class, "/baselines/" + fileName + ".png"));
    }
}
