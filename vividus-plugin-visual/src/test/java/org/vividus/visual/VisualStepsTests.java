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

package org.vividus.visual;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.resource.ResourceLoadException;
import org.vividus.selenium.screenshot.IgnoreStrategy;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.screenshot.ScreenshotConfiguration;
import org.vividus.ui.screenshot.ScreenshotParameters;
import org.vividus.ui.screenshot.ScreenshotParametersFactory;
import org.vividus.ui.web.action.search.WebLocatorType;
import org.vividus.visual.engine.IVisualTestingEngine;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.model.VisualCheck;
import org.vividus.visual.model.VisualCheckResult;
import org.vividus.visual.screenshot.BaselineIndexer;

import pazone.ashot.util.ImageTool;

@ExtendWith(MockitoExtension.class)
class VisualStepsTests
{
    private static final String ACCEPTABLE_DIFF_PERCENTAGE = "ACCEPTABLE_DIFF_PERCENTAGE";

    private static final String V = "v";

    private static final String K = "k";

    private static final String VISUAL_CHECK_PASSED = "Visual check passed";

    private static final String BASELINE = "baseline";

    private static final String FILESYSTEM = "filesystem";

    private static final String ELEMENT = "ELEMENT";
    private static final String AREA = "AREA";

    private static final String IGNORES_TABLE = "ignores table";

    private static final byte[] IMAGE = { -119, 80, 78, 71, 13, 10, 26, 10, 0, 0, 0, 13, 73, 72, 68, 82, 0, 0, 0, 1, 0,
            0, 0, 1, 8, 6, 0, 0, 0, 31, 21, -60, -119, 0, 0, 0, 13, 73, 68, 65, 84, 120, 94, 99, 96, -8, -1, -65, 30, 0,
            5, 127, 2, 126, 102, -25, 70, 104, 0, 0, 0, 0, 73, 69, 78, 68, -82, 66, 96, -126 };

    @Mock private ScreenshotParametersFactory<ScreenshotConfiguration> screenshotParametersFactory;
    @Mock private IVisualTestingEngine visualTestingEngine;
    @Mock private ISoftAssert softAssert;
    @Mock private IAttachmentPublisher attachmentPublisher;
    @Mock private VisualCheckResult visualCheckResult;
    @Mock private IUiContext uiContext;
    @Mock private BaselineIndexer baselineIndexer;
    @Captor private ArgumentCaptor<VisualCheck> visualCheckCaptor;
    @InjectMocks private VisualSteps visualSteps;

    @BeforeEach
    void init()
    {
        lenient().when(baselineIndexer.createIndexedBaseline(BASELINE)).thenReturn(BASELINE);
    }

    private Map<IgnoreStrategy, Set<Locator>> createEmptyIgnores()
    {
        return Map.of(IgnoreStrategy.ELEMENT, Set.of(), IgnoreStrategy.AREA, Set.of());
    }

    @ParameterizedTest
    @CsvSource({ "COMPARE_AGAINST", "CHECK_INEQUALITY_AGAINST" })
    void shouldAssertCheckResultForCompareAgainstActionAndPublishAttachment(VisualActionType action) throws IOException
    {
        mockUiContext();
        when(visualTestingEngine.compareAgainst(visualCheckCaptor.capture())).thenReturn(visualCheckResult);
        mockCheckResult();
        var screenshotParameters = mock(ScreenshotParameters.class);
        when(screenshotParametersFactory.create(Optional.empty(), IGNORES_TABLE, createEmptyIgnores())).thenReturn(
                screenshotParameters);
        visualSteps.runVisualTests(action, BASELINE);
        validateVisualCheck(visualCheckCaptor.getValue(), action, screenshotParameters);
        verify(softAssert).assertTrue(VISUAL_CHECK_PASSED, false);
        verifyCheckResultPublish();
    }

    @Test
    void shouldPerformVisualCheckWithBaselineStorage() throws IOException
    {
        mockUiContext();
        when(visualTestingEngine.establish(visualCheckCaptor.capture())).thenReturn(visualCheckResult);
        mockCheckResult();
        var screenshotParameters = mock(ScreenshotParameters.class);
        when(screenshotParametersFactory.create(Optional.empty(), IGNORES_TABLE, createEmptyIgnores())).thenReturn(
                screenshotParameters);
        visualSteps.runVisualTests(VisualActionType.ESTABLISH, BASELINE, FILESYSTEM);
        verify(softAssert).assertTrue(VISUAL_CHECK_PASSED, false);
        var visualCheck = visualCheckCaptor.getValue();
        assertEquals(Optional.of(FILESYSTEM), visualCheck.getBaselineStorage());
        validateVisualCheck(visualCheck, VisualActionType.ESTABLISH, screenshotParameters);
        verifyCheckResultPublish();
    }

    private void mockUiContext()
    {
        var searchContext = mock(SearchContext.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(searchContext));
    }

    @Test
    void shouldPerformVisualCheckWithCustomConfiguration() throws IOException
    {
        var compareAgainst = VisualActionType.COMPARE_AGAINST;
        mockUiContext();
        var screenshotConfiguration = mock(ScreenshotConfiguration.class);
        var screenshotParameters = mock(ScreenshotParameters.class);
        when(screenshotParametersFactory.create(Optional.of(screenshotConfiguration), IGNORES_TABLE,
                createEmptyIgnores())).thenReturn(screenshotParameters);
        when(visualTestingEngine.compareAgainst(visualCheckCaptor.capture())).thenReturn(visualCheckResult);
        mockCheckResult();
        visualSteps.runVisualTests(compareAgainst, BASELINE, screenshotConfiguration);
        validateVisualCheck(visualCheckCaptor.getValue(), compareAgainst, screenshotParameters);
        verify(softAssert).assertTrue(VISUAL_CHECK_PASSED, false);
        verifyCheckResultPublish();
    }

    @Test
    void shouldPerformVisualCheckWithCustomConfigurationAndBaselineStorage() throws IOException
    {
        var compareAgainst = VisualActionType.COMPARE_AGAINST;
        mockUiContext();
        var screenshotConfiguration = mock(ScreenshotConfiguration.class);
        var screenshotParameters = mock(ScreenshotParameters.class);
        when(screenshotParametersFactory.create(Optional.of(screenshotConfiguration), IGNORES_TABLE,
                createEmptyIgnores())).thenReturn(screenshotParameters);
        when(visualTestingEngine.compareAgainst(visualCheckCaptor.capture())).thenReturn(visualCheckResult);
        mockCheckResult();
        visualSteps.runVisualTests(compareAgainst, BASELINE, FILESYSTEM, screenshotConfiguration);
        verify(softAssert).assertTrue(VISUAL_CHECK_PASSED, false);
        var visualCheck = visualCheckCaptor.getValue();
        assertEquals(Optional.of(FILESYSTEM), visualCheck.getBaselineStorage());
        validateVisualCheck(visualCheck, compareAgainst, screenshotParameters);
        verifyCheckResultPublish();
    }

    @Test
    void shouldRecordFailedAssertionInCaseOfMissingBaseline() throws IOException
    {
        mockUiContext();
        when(visualTestingEngine.compareAgainst(visualCheckCaptor.capture())).thenReturn(visualCheckResult);
        when(visualCheckResult.getBaselineName()).thenReturn(BASELINE);
        var screenshotParameters = mock(ScreenshotParameters.class);
        when(screenshotParametersFactory.create(Optional.empty(), IGNORES_TABLE, createEmptyIgnores())).thenReturn(
                screenshotParameters);
        visualSteps.runVisualTests(VisualActionType.COMPARE_AGAINST, BASELINE);
        verify(softAssert, never()).assertTrue(VISUAL_CHECK_PASSED, false);
        verify(softAssert).recordFailedAssertion("Unable to find baseline with name: baseline");
        validateVisualCheck(visualCheckCaptor.getValue(), VisualActionType.COMPARE_AGAINST, screenshotParameters);
        verifyCheckResultPublish();
    }

    private static Stream<Arguments> diffMethodSource()
    {
        return Stream.of(
                arguments(
                        VisualActionType.COMPARE_AGAINST,
                        (Function<VisualCheck, OptionalDouble>) VisualCheck::getAcceptableDiffPercentage,
                        ACCEPTABLE_DIFF_PERCENTAGE
                ),
                arguments(
                        VisualActionType.CHECK_INEQUALITY_AGAINST,
                        (Function<VisualCheck, OptionalDouble>) VisualCheck::getRequiredDiffPercentage,
                        "REQUIRED_DIFF_PERCENTAGE"
                )
        );
    }

    @ParameterizedTest
    @MethodSource("diffMethodSource")
    void shouldAssertCheckResultAndUseStepLevelSettings(VisualActionType actionType,
            Function<VisualCheck, OptionalDouble> diffValueExtractor, String columnName) throws IOException
    {
        var table = mock(ExamplesTable.class);
        var screenshotParameters = prepareMocks(table, columnName, OptionalDouble.of(50d), Optional.empty());
        visualSteps.runVisualTests(actionType, BASELINE, table);
        verify(softAssert).assertTrue(VISUAL_CHECK_PASSED, false);
        var visualCheck = visualCheckCaptor.getValue();
        assertEquals(OptionalDouble.of(50), diffValueExtractor.apply(visualCheck));
        validateVisualCheck(visualCheck, actionType, screenshotParameters);
        verifyCheckResultPublish();
    }

    @Test
    void shouldAssertCheckResultUsingBaselineStorageAndUseStepLevelSettings() throws IOException
    {
        var table = mock(ExamplesTable.class);
        var screenshotParameters = prepareMocks(table, ACCEPTABLE_DIFF_PERCENTAGE, OptionalDouble.of(50d),
                Optional.empty());
        var actionType = VisualActionType.COMPARE_AGAINST;
        visualSteps.runVisualTests(actionType, BASELINE, FILESYSTEM, table);
        verify(softAssert).assertTrue(VISUAL_CHECK_PASSED, false);
        var visualCheck = visualCheckCaptor.getValue();
        assertEquals(OptionalDouble.of(50), visualCheck.getAcceptableDiffPercentage());
        assertEquals(Optional.of(FILESYSTEM), visualCheck.getBaselineStorage());
        validateVisualCheck(visualCheck, actionType, screenshotParameters);
        verifyCheckResultPublish();
    }

    @Test
    void shouldRunVisualTestWithStepLevelExclusionsAndCustomScreenshotConfiguration() throws IOException
    {
        var table = mock(ExamplesTable.class);
        var screenshotConfiguration = mock(ScreenshotConfiguration.class);
        var screenshotParameters = prepareMocks(table, ACCEPTABLE_DIFF_PERCENTAGE, OptionalDouble.empty(),
                Optional.of(screenshotConfiguration));
        var compareAgainst = VisualActionType.COMPARE_AGAINST;
        visualSteps.runVisualTests(compareAgainst, BASELINE, table, screenshotConfiguration);
        verify(softAssert).assertTrue(VISUAL_CHECK_PASSED, false);
        var visualCheck = visualCheckCaptor.getValue();
        assertEquals(OptionalDouble.empty(), visualCheck.getRequiredDiffPercentage());
        validateVisualCheck(visualCheck, compareAgainst, screenshotParameters);
        verifyCheckResultPublish();
    }

    @Test
    void shouldRunVisualTestWithBaselineStorageAndStepLevelExclusionsAndCustomScreenshotConfiguration()
            throws IOException
    {
        var table = mock(ExamplesTable.class);
        var screenshotConfiguration = mock(ScreenshotConfiguration.class);
        var screenshotParameters = prepareMocks(table, ACCEPTABLE_DIFF_PERCENTAGE, OptionalDouble.empty(),
                Optional.of(screenshotConfiguration));
        var compareAgainst = VisualActionType.COMPARE_AGAINST;
        visualSteps.runVisualTests(compareAgainst, BASELINE, FILESYSTEM, table, screenshotConfiguration);
        verify(softAssert).assertTrue(VISUAL_CHECK_PASSED, false);
        var visualCheck = visualCheckCaptor.getValue();
        assertEquals(OptionalDouble.empty(), visualCheck.getRequiredDiffPercentage());
        assertEquals(Optional.of(FILESYSTEM), visualCheck.getBaselineStorage());
        validateVisualCheck(visualCheck, compareAgainst, screenshotParameters);
        verifyCheckResultPublish();
    }

    private void mockCheckResult()
    {
        when(visualCheckResult.getBaselineName()).thenReturn(BASELINE);
        when(visualCheckResult.getBaseline()).thenReturn(new byte[] { });
    }

    @Test
    void shouldThrowExceptionIfTableHasMoreThanOneRow()
    {
        var table = mock(ExamplesTable.class);
        when(table.getRows()).thenReturn(List.of(Map.of(K, V), Map.of(K, V)));
        var exception = assertThrows(IllegalArgumentException.class,
                () -> visualSteps.runVisualTests(VisualActionType.COMPARE_AGAINST, BASELINE, table));
        assertEquals("Only one row of locators to ignore supported, actual: 2", exception.getMessage());
        verify(table, never()).getRowAsParameters(0);
        verifyNoInteractions(softAssert, visualTestingEngine, attachmentPublisher);
    }

    private ScreenshotParameters prepareMocks(ExamplesTable table, String columnName, OptionalDouble diffPercentage,
            Optional<ScreenshotConfiguration> screenshotConfiguration) throws IOException
    {
        mockUiContext();

        when(table.getRows()).thenReturn(List.of(Map.of(K, V)));
        var row = mock(Parameters.class);
        when(table.getRowAsParameters(0)).thenReturn(row);
        var elementsToIgnore = Set.of(new Locator(WebLocatorType.XPATH, ".//a"));
        var areasToIgnore = Set.of(new Locator(WebLocatorType.XPATH, "//div"));
        mockGettingValue(row, ELEMENT, elementsToIgnore);
        mockGettingValue(row, AREA, areasToIgnore);
        doReturn(diffPercentage).when(row).valueAs(columnName, OptionalDouble.class, OptionalDouble.empty());
        var ignores = Map.of(
                IgnoreStrategy.ELEMENT, elementsToIgnore,
                IgnoreStrategy.AREA, areasToIgnore
        );

        var screenshotParameters = mock(ScreenshotParameters.class);
        when(screenshotParametersFactory.create(screenshotConfiguration, IGNORES_TABLE, ignores)).thenReturn(
                screenshotParameters);

        when(visualTestingEngine.compareAgainst(visualCheckCaptor.capture())).thenReturn(visualCheckResult);
        mockCheckResult();

        return screenshotParameters;
    }

    private static void mockGettingValue(Parameters row, String name, Set<Locator> result)
    {
        doReturn(result).when(row).valueAs(eq(name),
                argThat(t -> t instanceof ParameterizedType && ((ParameterizedType) t).getRawType() == Set.class
                        && ((ParameterizedType) t).getActualTypeArguments()[0] == Locator.class), eq(Set.of()));
    }

    @Test
    void shouldNotAssertResultForEstablishAction() throws IOException
    {
        mockUiContext();
        when(visualTestingEngine.establish(visualCheckCaptor.capture())).thenReturn(visualCheckResult);
        when(visualCheckResult.getActionType()).thenReturn(VisualActionType.ESTABLISH);
        when(visualCheckResult.getBaselineName()).thenReturn(BASELINE);
        var screenshotParameters = mock(ScreenshotParameters.class);
        when(screenshotParametersFactory.create(Optional.empty(), IGNORES_TABLE, createEmptyIgnores())).thenReturn(
                screenshotParameters);
        visualSteps.runVisualTests(VisualActionType.ESTABLISH, BASELINE);
        verifyNoMoreInteractions(softAssert);
        verifyCheckResultPublish();
        validateVisualCheck(visualCheckCaptor.getValue(), VisualActionType.ESTABLISH, screenshotParameters);
    }

    @Test
    void shouldDoNothingOnMissingSearchContext()
    {
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.empty());
        visualSteps.runVisualTests(VisualActionType.ESTABLISH, BASELINE);
        verifyNoInteractions(visualTestingEngine, attachmentPublisher);
    }

    static List<Exception> exceptionsToCatch()
    {
        return List.of(
                new IOException(),
                new ResourceLoadException("resource not loaded")
        );
    }

    @ParameterizedTest
    @MethodSource("exceptionsToCatch")
    void shouldRecordExceptions(Exception exception) throws IOException
    {
        mockUiContext();
        when(visualTestingEngine.establish(visualCheckCaptor.capture())).thenThrow(exception);
        var screenshotParameters = mock(ScreenshotParameters.class);
        when(screenshotParametersFactory.create(Optional.empty(), IGNORES_TABLE, createEmptyIgnores())).thenReturn(
                screenshotParameters);
        visualSteps.runVisualTests(VisualActionType.ESTABLISH, BASELINE);
        verify(softAssert).recordFailedAssertion(exception);
        verifyNoInteractions(attachmentPublisher);
        verifyNoMoreInteractions(softAssert);
        validateVisualCheck(visualCheckCaptor.getValue(), VisualActionType.ESTABLISH, screenshotParameters);
    }

    @Test
    void shouldPerformVisualCheckUsingImage() throws IOException
    {
        when(visualTestingEngine.compareAgainst(visualCheckCaptor.capture())).thenReturn(visualCheckResult);
        mockCheckResult();
        visualSteps.runVisualTests(VisualActionType.COMPARE_AGAINST, BASELINE, IMAGE);
        var visualCheck = visualCheckCaptor.getValue();
        validateVisualCheck(visualCheck, VisualActionType.COMPARE_AGAINST, Optional.empty());
        verify(softAssert).assertTrue(VISUAL_CHECK_PASSED, false);
        assertArrayEquals(IMAGE, ImageTool.toByteArray(visualCheck.getScreenshot().get().getImage()));
        verifyNoInteractions(screenshotParametersFactory, uiContext);
        verifyCheckResultPublish();
    }

    @Test
    void shouldThrowAnUncheckedIOInCaseOfIOException()
    {
        try (MockedStatic<ImageTool> imageTool = Mockito.mockStatic(ImageTool.class))
        {
            var ioe = new IOException();
            imageTool.when(() -> ImageTool.toBufferedImage(IMAGE)).thenThrow(ioe);
            assertThrows(UncheckedIOException.class,
                    () -> visualSteps.runVisualTests(VisualActionType.COMPARE_AGAINST, BASELINE, IMAGE));
        }
    }

    @Test
    void shouldPerformVisualCheckUsingImageAndBaselineStorage() throws IOException
    {
        when(visualTestingEngine.establish(visualCheckCaptor.capture())).thenReturn(visualCheckResult);
        mockCheckResult();
        visualSteps.runVisualTests(VisualActionType.ESTABLISH, BASELINE, IMAGE, FILESYSTEM);
        verify(softAssert).assertTrue(VISUAL_CHECK_PASSED, false);
        var visualCheck = visualCheckCaptor.getValue();
        assertEquals(Optional.of(FILESYSTEM), visualCheck.getBaselineStorage());
        validateVisualCheck(visualCheck, VisualActionType.ESTABLISH, Optional.empty());
        verifyNoInteractions(screenshotParametersFactory, uiContext);
        assertArrayEquals(IMAGE, ImageTool.toByteArray(visualCheck.getScreenshot().get().getImage()));
        verifyCheckResultPublish();
    }

    @ParameterizedTest
    @MethodSource("diffMethodSource")
    void shouldAssertCheckResultAndUseStepLevelSettingsUsingImage(VisualActionType actionType,
            Function<VisualCheck, OptionalDouble> diffValueExtractor, String columnName) throws IOException
    {
        var table = mock(ExamplesTable.class);
        var row = mock(Parameters.class);
        when(table.getRows()).thenReturn(List.of(Map.of(K, V)));
        when(table.getRowAsParameters(0)).thenReturn(row);
        OptionalDouble diffPercentage = OptionalDouble.of(50d);
        doReturn(diffPercentage).when(row).valueAs(columnName, OptionalDouble.class, OptionalDouble.empty());
        when(visualTestingEngine.compareAgainst(visualCheckCaptor.capture())).thenReturn(visualCheckResult);
        mockCheckResult();
        visualSteps.runVisualTests(actionType, BASELINE, IMAGE, table);
        verify(softAssert).assertTrue(VISUAL_CHECK_PASSED, false);
        var visualCheck = visualCheckCaptor.getValue();
        assertEquals(diffPercentage, diffValueExtractor.apply(visualCheck));
        validateVisualCheck(visualCheck, actionType, Optional.empty());
        verifyNoInteractions(screenshotParametersFactory, uiContext);
        assertArrayEquals(IMAGE, ImageTool.toByteArray(visualCheck.getScreenshot().get().getImage()));
        verifyCheckResultPublish();
    }

    @ParameterizedTest
    @ValueSource(strings = { ELEMENT, AREA })
    void shouldThrowAnExceptionWhenIgnoresUsedForImageBaseChecks(String columnName)
    {
        var table = mock(ExamplesTable.class);
        var row = mock(Parameters.class);
        when(table.getRows()).thenReturn(List.of(Map.of(K, V)));
        when(table.getRowAsParameters(0)).thenReturn(row);
        doReturn(Map.of(columnName, "50")).when(row).values();
        var iae = assertThrows(IllegalArgumentException.class,
                () -> visualSteps.runVisualTests(VisualActionType.COMPARE_AGAINST, BASELINE, IMAGE, table));
        assertEquals("AREA and ELEMENT ignoring not supported for image based checks.", iae.getMessage());
        verifyNoInteractions(screenshotParametersFactory, uiContext, softAssert, visualTestingEngine);
    }

    @ParameterizedTest
    @MethodSource("diffMethodSource")
    void shouldAssertCheckResultAndUseStepLevelSettingsUsingImageAndBaselineStorage(VisualActionType actionType,
            Function<VisualCheck, OptionalDouble> diffValueExtractor, String columnName) throws IOException
    {
        var table = mock(ExamplesTable.class);
        var row = mock(Parameters.class);
        when(table.getRows()).thenReturn(List.of(Map.of(K, V)));
        when(table.getRowAsParameters(0)).thenReturn(row);
        OptionalDouble diffPercentage = OptionalDouble.of(50d);
        doReturn(diffPercentage).when(row).valueAs(columnName, OptionalDouble.class, OptionalDouble.empty());
        when(visualTestingEngine.compareAgainst(visualCheckCaptor.capture())).thenReturn(visualCheckResult);
        mockCheckResult();
        visualSteps.runVisualTests(actionType, BASELINE, IMAGE, FILESYSTEM, table);
        verify(softAssert).assertTrue(VISUAL_CHECK_PASSED, false);
        var visualCheck = visualCheckCaptor.getValue();
        assertEquals(diffPercentage, diffValueExtractor.apply(visualCheck));
        validateVisualCheck(visualCheck, actionType, Optional.empty());
        verifyNoInteractions(screenshotParametersFactory, uiContext);
        assertEquals(FILESYSTEM, visualCheck.getBaselineStorage().get());
        assertArrayEquals(IMAGE, ImageTool.toByteArray(visualCheck.getScreenshot().get().getImage()));
        verifyCheckResultPublish();
    }

    private void validateVisualCheck(VisualCheck visualCheck, VisualActionType type,
            ScreenshotParameters screenshotParameters)
    {
        validateVisualCheck(visualCheck, type, Optional.of(screenshotParameters));
    }

    private void validateVisualCheck(VisualCheck visualCheck, VisualActionType type,
            Optional<ScreenshotParameters> screenshotParameters)
    {
        assertEquals(BASELINE, visualCheck.getBaselineName());
        assertEquals(type, visualCheck.getAction());
        assertEquals(screenshotParameters, visualCheck.getScreenshotParameters());
    }

    private void verifyCheckResultPublish()
    {
        verify(attachmentPublisher).publishAttachment("visual-comparison.ftl", Map.of("result", visualCheckResult),
                "Visual comparison: baseline");
    }
}
