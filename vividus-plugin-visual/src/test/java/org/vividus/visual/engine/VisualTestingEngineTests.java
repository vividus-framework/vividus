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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

import javax.imageio.ImageIO;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.selenium.screenshot.AshotScreenshotTaker;
import org.vividus.ui.screenshot.ScreenshotParameters;
import org.vividus.util.ResourceUtils;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.model.VisualCheck;
import org.vividus.visual.model.VisualCheckResult;
import org.vividus.visual.storage.BaselineStorage;
import org.vividus.visual.storage.BaselineStorageProvider;
import org.vividus.visual.storage.FileSystemBaselineStorage;

import pazone.ashot.Screenshot;

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
    private static final BigDecimal DIFF = BigDecimal.valueOf(40).setScale(3);
    private static final String FILESYSTEM = "filesystem";
    private static final String MEMORY = "MEMORY";

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(VisualTestingEngine.class);

    @Mock private BaselineStorage baselineStorage;
    @Mock private AshotScreenshotTaker<ScreenshotParameters> ashotScreenshotTaker;
    @Mock private BaselineStorageProvider baselineStorageProvider;
    @Spy private DiffMarkupPolicyFactory diffMarkupPolicyFactory;

    private VisualTestingEngine visualTestingEngine;

    void initObjectUnderTest()
    {
        visualTestingEngine = new VisualTestingEngine(ashotScreenshotTaker, diffMarkupPolicyFactory,
                baselineStorageProvider);
        visualTestingEngine.setAcceptableDiffPercentage(0.0d);
        visualTestingEngine.setBaselineStorage(FILESYSTEM);
    }

    @Test
    void shouldReturnOnlyCheckpointForEstablishAction() throws IOException
    {
        initObjectUnderTest();
        when(baselineStorageProvider.getBaselineStorage(FILESYSTEM)).thenReturn(baselineStorage);
        VisualCheck visualCheck = createVisualCheck(VisualActionType.ESTABLISH);
        BufferedImage finalImage = mockGetCheckpointScreenshot(visualCheck);
        VisualCheckResult checkResult = visualTestingEngine.establish(visualCheck);
        verify(baselineStorage).saveBaseline(argThat(s -> finalImage.equals(s.getImage())), eq(BASELINE));
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
        VisualCheck check = new VisualCheck(BASELINE, actionType);
        check.setScreenshotParameters(Optional.empty());
        return check;
    }

    @ParameterizedTest
    @CsvSource({"0, false", "25, false", "50, true", "100, true"})
    void shouldReturnVisualCheckResultWithDiffBaselineAndCheckpointDiffSensitivity(
        double acceptableDiffPercentage, boolean status) throws IOException
    {
        initObjectUnderTest();
        when(baselineStorageProvider.getBaselineStorage(FILESYSTEM)).thenReturn(baselineStorage);
        when(baselineStorage.getBaseline(BASELINE)).thenReturn(Optional.of(new Screenshot(loadImage(BASELINE))));
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
        verify(baselineStorage, never()).saveBaseline(any(), any());
        assertThat(testLogger.getLoggingEvents(), is(List.of(info(LOG_MESSAGE, ACCEPTABLE,
                BigDecimal.valueOf(acceptableDiffPercentage), DIFF))));
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
        initObjectUnderTest();
        when(baselineStorageProvider.getBaselineStorage(FILESYSTEM)).thenReturn(baselineStorage);
        visualTestingEngine.setRequiredDiffPercentage(0.000001);
        when(baselineStorage.getBaseline(BASELINE)).thenReturn(Optional.of(new Screenshot(loadImage(BASELINE))));
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
        verify(baselineStorage, never()).saveBaseline(any(), any());
        assertThat(testLogger.getLoggingEvents(), is(List.of(info(LOG_MESSAGE, "required",
            BigDecimal.valueOf(requiredDiffPercentage), DIFF))));
    }

    @Test
    void shouldReturnVisualCheckResultWithBaselineAndCheckpointUsingAcceptableDiffPercentage() throws IOException
    {
        initObjectUnderTest();
        when(baselineStorageProvider.getBaselineStorage(FILESYSTEM)).thenReturn(baselineStorage);
        when(baselineStorage.getBaseline(BASELINE)).thenReturn(Optional.of(new Screenshot(loadImage(BASELINE))));
        VisualCheck visualCheck = createVisualCheck(VisualActionType.COMPARE_AGAINST);
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
        verify(baselineStorage, never()).saveBaseline(any(), any());
        assertThat(testLogger.getLoggingEvents(), is(List.of(info(LOG_MESSAGE, ACCEPTABLE, BigDecimal.valueOf(50.0),
                DIFF))));
    }

    @Test
    void shouldReturnVisualCheckResultWithBaselineAndCheckpoint() throws IOException
    {
        initObjectUnderTest();
        when(baselineStorageProvider.getBaselineStorage(FILESYSTEM)).thenReturn(baselineStorage);
        when(baselineStorage.getBaseline(BASELINE)).thenReturn(Optional.of(new Screenshot(loadImage(BASELINE))));
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
        verify(baselineStorage, never()).saveBaseline(any(), any());
        assertThat(testLogger.getLoggingEvents(), is(List.of(info(LOG_MESSAGE, ACCEPTABLE, BigDecimal.valueOf(0d),
                new BigDecimal(0).setScale(3)))));
    }

    @Test
    void shouldOverrideBaselineDuringComparisonAction() throws IOException
    {
        initObjectUnderTest();
        when(baselineStorageProvider.getBaselineStorage(FILESYSTEM)).thenReturn(baselineStorage);
        visualTestingEngine.setOverrideBaselines(true);
        when(baselineStorage.getBaseline(BASELINE)).thenReturn(Optional.of(new Screenshot(loadImage(BASELINE))));
        VisualCheck visualCheck = createVisualCheck(VisualActionType.COMPARE_AGAINST);
        var finalImage = mockGetCheckpointScreenshot(visualCheck, BASELINE);
        VisualCheckResult checkResult = visualTestingEngine.compareAgainst(visualCheck);
        Assertions.assertAll(
                () -> assertEquals(BASELINE_BASE64, checkResult.getBaseline()),
                () -> assertEquals(BASELINE, checkResult.getBaselineName()),
                () -> assertEquals(BASELINE_BASE64, checkResult.getCheckpoint()),
                () -> assertEquals(VisualActionType.COMPARE_AGAINST, checkResult.getActionType()),
                () -> assertEquals(BASELINE_BASE64, checkResult.getDiff()),
                () -> assertTrue(checkResult.isPassed()));
        verify(baselineStorage).saveBaseline(argThat(s -> finalImage.equals(s.getImage())), eq(BASELINE));
        assertThat(testLogger.getLoggingEvents(), is(List.of(info(LOG_MESSAGE, ACCEPTABLE, BigDecimal.valueOf(0d),
                new BigDecimal(0).setScale(3)))));
    }

    @Test
    void shouldReturnVisualCheckResultWithDiffAgainstEmptyImageAndCheckpoint() throws IOException
    {
        initObjectUnderTest();
        when(baselineStorageProvider.getBaselineStorage(FILESYSTEM)).thenReturn(baselineStorage);
        when(baselineStorage.getBaseline(BASELINE)).thenReturn(Optional.empty());
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

    @Test
    void shouldOverrideBaselineProviderViaCheckSettings() throws IOException
    {
        initObjectUnderTest();
        when(baselineStorageProvider.getBaselineStorage(MEMORY)).thenReturn(baselineStorage);
        when(baselineStorage.getBaseline(BASELINE)).thenReturn(Optional.empty());
        VisualCheck visualCheck = createVisualCheck(VisualActionType.COMPARE_AGAINST);
        visualCheck.setBaselineStorage(Optional.of(MEMORY));
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

    @Test
    void shouldExecuteVisualCheckWithImage() throws IOException
    {
        initObjectUnderTest();
        when(baselineStorageProvider.getBaselineStorage(FILESYSTEM)).thenReturn(baselineStorage);
        when(baselineStorage.getBaseline(BASELINE)).thenReturn(Optional.of(new Screenshot(loadImage(BASELINE))));
        VisualCheck visualCheck = createVisualCheck(VisualActionType.COMPARE_AGAINST);
        BufferedImage image = loadImage(BASELINE);
        Screenshot screenshot = new Screenshot(image);
        visualCheck.setScreenshot(Optional.of(screenshot));
        VisualCheckResult checkResult = visualTestingEngine.compareAgainst(visualCheck);
        Assertions.assertAll(
                () -> assertEquals(BASELINE_BASE64, checkResult.getBaseline()),
                () -> assertEquals(BASELINE, checkResult.getBaselineName()),
                () -> assertEquals(BASELINE_BASE64, checkResult.getCheckpoint()),
                () -> assertEquals(VisualActionType.COMPARE_AGAINST, checkResult.getActionType()),
                () -> assertEquals(BASELINE_BASE64, checkResult.getDiff()),
                () -> assertTrue(checkResult.isPassed()));
        verify(baselineStorage, never()).saveBaseline(any(), any());
        verifyNoInteractions(ashotScreenshotTaker);
        assertThat(testLogger.getLoggingEvents(), is(List.of(info(LOG_MESSAGE, ACCEPTABLE, BigDecimal.valueOf(0d),
                new BigDecimal(0).setScale(3)))));
    }

    private BufferedImage mockGetCheckpointScreenshot(VisualCheck visualCheck, String imageName) throws IOException
    {
        BufferedImage image = loadImage(imageName);
        Screenshot screenshot = new Screenshot(image);
        when(ashotScreenshotTaker.takeAshotScreenshot(visualCheck.getSearchContext(),
                visualCheck.getScreenshotParameters())).thenReturn(screenshot);
        return image;
    }

    private BufferedImage mockGetCheckpointScreenshot(VisualCheck visualCheck) throws IOException
    {
        return mockGetCheckpointScreenshot(visualCheck, "checkpoint");
    }

    private BufferedImage loadImage(String fileName) throws IOException
    {
        return ImageIO.read(
                ResourceUtils.loadFile(FileSystemBaselineStorage.class, "/baselines/" + fileName + ".png"));
    }
}
