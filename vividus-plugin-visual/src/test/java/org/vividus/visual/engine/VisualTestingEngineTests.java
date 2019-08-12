/*
 * Copyright 2019 the original author or authors.
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

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.selenium.screenshot.IScreenshotTaker;
import org.vividus.selenium.screenshot.ScreenshotConfiguration;
import org.vividus.selenium.screenshot.ScreenshotDebugger;
import org.vividus.ui.web.action.ISearchActions;
import org.vividus.util.ResourceUtils;
import org.vividus.visual.engine.VisualCheckFactory.VisualCheck;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.model.VisualCheckResult;

import ru.yandex.qatools.ashot.Screenshot;
import ru.yandex.qatools.ashot.coordinates.Coords;
import ru.yandex.qatools.ashot.coordinates.CoordsProvider;

@ExtendWith(MockitoExtension.class)
class VisualTestingEngineTests
{
    private static final By B_XPATH = By.xpath(".//b");
    private static final By A_XPATH = By.xpath(".//a");
    private static final By FOOTER_XPATH = By.xpath(".//footer");
    private static final By HEADER_XPATH = By.xpath(".//header");
    private static final IgnoreStrategy ELEMENT = mock(IgnoreStrategy.class);
    private static final IgnoreStrategy AREA = mock(IgnoreStrategy.class);

    private static final String BASELINE = "baseline";
    private static final Map<IgnoreStrategy, Set<By>> STRATEGIES = Map.of(
            ELEMENT, Set.of(HEADER_XPATH, A_XPATH),
            AREA, Set.of(FOOTER_XPATH));
    private static final Map<IgnoreStrategy, Set<By>> STEP_LEVEL_STRATEGIES = Map.of(
            ELEMENT, Set.of(A_XPATH),
            AREA, Set.of(B_XPATH, FOOTER_XPATH));

    private static final String BASELINE_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAIAAAACDbGyAAAAD0lEQVR4XmPg5uZmoCIA"
                                                + "AApeACIUkokZAAAAAElFTkSuQmCC";

    private static final String CHECKPOINT_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAIAAAACDbGyAAAAL0lEQVR4XmNgZ2dnQA"
                                                  + "bz589HYb9///78+fNADpAEskHCQOrbt29QDgT8/v0bwgAAXNsZsacVEHkAAAAASUVO"
                                                  + "RK5CYII=";

    private static final String DIFF_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAIAAAACDbGyAAAAHklEQVR4XmPg5uZmQAbv8t+h"
                                            + "sIEYIgRnQFnIChG6ADGrFxBXlUutAAAAAElFTkSuQmCC";

    private static final String DIFF_VS_EMPTY_BASE64 = "iVBORw0KGgoAAAANSUhEUgAAAAUAAAAFCAIAAAACDbGyAAAAFElEQVR4XmNgYGB"
                                            + "4l/8OjlA4ZPABQuY3CRSTNYoAAAAASUVORK5CYII=";

    private static final VisualCheckFactory FACTORY = new VisualCheckFactory();

    @Mock
    private IWebDriverProvider webDriverProvider;

    @Mock
    private IScreenshotTaker screenshotTaker;

    @Mock
    private ISearchActions searchActions;

    @Mock
    private CoordsProvider coordsProvider;

    @Mock
    private IBaselineRepository baselineRepository;

    @Mock
    private SearchContext searchContext;

    @Mock
    private ScreenshotDebugger screenshotDebugger;

    @InjectMocks
    private VisualTestingEngine visualTestingEngine;

    private final BufferedImage original = mock(BufferedImage.class);

    @BeforeAll
    static void beforeAll()
    {
        FACTORY.setScreenshotIndexer(Optional.empty());
    }

    @Test
    void shouldReturnOnlyCheckpointForEstablishAction() throws IOException
    {
        BufferedImage finalImage = mockGetCheckpointScreenshot();
        visualTestingEngine.setIgnoreStrategies(STRATEGIES);
        VisualCheck visualCheck = createVisualCheck(VisualActionType.ESTABLISH);
        VisualCheckResult checkResult = visualTestingEngine.establish(visualCheck);
        verify(baselineRepository).saveBaseline(argThat(s -> finalImage.equals(s.getImage())), eq(BASELINE));
        verifyScreenshotDebugging(finalImage);
        Assertions.assertAll(
            () -> assertNull(checkResult.getBaseline()),
            () -> assertNull(checkResult.getDiff()),
            () -> assertEquals(BASELINE, checkResult.getBaselineName()),
            () -> assertEquals(VisualActionType.ESTABLISH, checkResult.getActionType()),
            () -> assertEquals(CHECKPOINT_BASE64, checkResult.getCheckpoint()),
            () -> assertFalse(checkResult.isPassed()));
    }

    private VisualCheck createVisualCheck(VisualActionType actionType)
    {
        VisualCheck visualCheck = FACTORY.create(BASELINE, actionType);
        visualCheck.setElementsToIgnore(STEP_LEVEL_STRATEGIES);
        visualCheck.setSearchContext(searchContext);
        return visualCheck;
    }

    private void verifyScreenshotDebugging(BufferedImage finalImage)
    {
        verify(screenshotDebugger).debug(visualTestingEngine.getClass(), "cropped_by_AREA", finalImage);
    }

    @Test
    void shouldPerformVisualCheckWithCustomConfiguration() throws IOException
    {
        visualTestingEngine.setIgnoreStrategies(STRATEGIES);
        VisualCheck visualCheck = createVisualCheck(VisualActionType.ESTABLISH);
        Optional<ScreenshotConfiguration> screenshotConfiguration = Optional.of(mock(ScreenshotConfiguration.class));
        visualCheck.setScreenshotConfiguration(screenshotConfiguration);
        BufferedImage finalImage = mockGetCheckpointScreenshot(screenshot ->
            when(screenshotTaker.takeAshotScreenshot(searchContext, screenshotConfiguration)).thenReturn(screenshot));
        VisualCheckResult checkResult = visualTestingEngine.establish(visualCheck);
        verify(baselineRepository).saveBaseline(argThat(s -> finalImage.equals(s.getImage())), eq(BASELINE));
        verifyScreenshotDebugging(finalImage);
        Assertions.assertAll(
            () -> assertNull(checkResult.getBaseline()),
            () -> assertNull(checkResult.getDiff()),
            () -> assertEquals(BASELINE, checkResult.getBaselineName()),
            () -> assertEquals(VisualActionType.ESTABLISH, checkResult.getActionType()),
            () -> assertEquals(CHECKPOINT_BASE64, checkResult.getCheckpoint()),
            () -> assertFalse(checkResult.isPassed()));
    }

    @Test
    void shouldReturnVisualCheckResultWithDiffBaselineAndCheckpoint() throws IOException
    {
        visualTestingEngine.setIgnoreStrategies(STRATEGIES);
        BufferedImage finalImage = mockGetCheckpointScreenshot();
        when(baselineRepository.getBaseline(BASELINE)).thenReturn(Optional.of(new Screenshot(loadImage(BASELINE))));
        VisualCheck visualCheck = createVisualCheck(VisualActionType.COMPARE_AGAINST);
        VisualCheckResult checkResult = visualTestingEngine.compareAgainst(visualCheck);
        Assertions.assertAll(
            () -> assertEquals(BASELINE_BASE64, checkResult.getBaseline()),
            () -> assertEquals(BASELINE, checkResult.getBaselineName()),
            () -> assertEquals(CHECKPOINT_BASE64, checkResult.getCheckpoint()),
            () -> assertEquals(VisualActionType.COMPARE_AGAINST, checkResult.getActionType()),
            () -> assertEquals(DIFF_BASE64, checkResult.getDiff()),
            () -> assertFalse(checkResult.isPassed()));
        verifyScreenshotDebugging(finalImage);
        verify(baselineRepository, never()).saveBaseline(any(), any());
    }

    @Test
    void shouldOverrideBaselinesIfPropertySet() throws IOException
    {
        visualTestingEngine.setIgnoreStrategies(STRATEGIES);
        visualTestingEngine.setOverrideBaselines(true);
        when(baselineRepository.getBaseline(BASELINE)).thenReturn(Optional.of(new Screenshot(loadImage(BASELINE))));
        VisualCheck visualCheck = createVisualCheck(VisualActionType.COMPARE_AGAINST);
        BufferedImage finalImage = mockGetCheckpointScreenshot();
        visualTestingEngine.compareAgainst(visualCheck);
        verify(baselineRepository).saveBaseline(argThat(s -> finalImage.equals(s.getImage())), eq(BASELINE));
        verifyScreenshotDebugging(finalImage);
    }

    @Test
    void shouldReturnVisualCheckResultWithDiffAgainstEmptyImageAndCheckpoint() throws IOException
    {
        visualTestingEngine.setIgnoreStrategies(STRATEGIES);
        BufferedImage finalImage = mockGetCheckpointScreenshot();
        when(baselineRepository.getBaseline(BASELINE)).thenReturn(Optional.empty());
        VisualCheck visualCheck = createVisualCheck(VisualActionType.COMPARE_AGAINST);
        VisualCheckResult checkResult = visualTestingEngine.compareAgainst(visualCheck);
        Assertions.assertAll(
            () -> assertNull(checkResult.getBaseline()),
            () -> assertEquals(BASELINE, checkResult.getBaselineName()),
            () -> assertEquals(CHECKPOINT_BASE64, checkResult.getCheckpoint()),
            () -> assertEquals(VisualActionType.COMPARE_AGAINST, checkResult.getActionType()),
            () -> assertEquals(DIFF_VS_EMPTY_BASE64, checkResult.getDiff()),
            () -> assertFalse(checkResult.isPassed()));
        verifyScreenshotDebugging(finalImage);
    }

    private BufferedImage mockGetCheckpointScreenshot() throws IOException
    {
        return mockGetCheckpointScreenshot(screenshot ->
            when(screenshotTaker.takeAshotScreenshot(searchContext, Optional.empty())).thenReturn(screenshot));
    }

    private BufferedImage mockGetCheckpointScreenshot(Consumer<Screenshot> screenshotTakerMocker) throws IOException
    {
        Screenshot screenshot = new Screenshot(original);
        screenshotTakerMocker.accept(screenshot);
        WebElement webElement = mock(WebElement.class);
        when(searchActions.findElements(HEADER_XPATH)).thenReturn(List.of());
        when(searchActions.findElements(A_XPATH)).thenReturn(List.of());
        when(searchActions.findElements(FOOTER_XPATH)).thenReturn(List.of(webElement));
        when(searchActions.findElements(B_XPATH)).thenReturn(List.of());
        WebDriver webDriver = mock(WebDriver.class);
        when(webDriverProvider.get()).thenReturn(webDriver);
        Coords coords = mock(Coords.class);
        when(coordsProvider.ofElement(webDriver, webElement)).thenReturn(coords);
        BufferedImage cropped = loadImage("checkpoint");
        when(AREA.crop(original, Set.of(coords))).thenReturn(cropped);
        return cropped;
    }

    private BufferedImage loadImage(String fileName) throws IOException
    {
        return ImageIO.read(
                ResourceUtils.loadFile(FileSystemBaselineRepository.class, "/baselines/" + fileName + ".png"));
    }
}
