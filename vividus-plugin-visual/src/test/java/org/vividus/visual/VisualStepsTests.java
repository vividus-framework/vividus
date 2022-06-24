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

package org.vividus.visual;

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
import java.lang.reflect.ParameterizedType;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
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

@ExtendWith(MockitoExtension.class)
class VisualStepsTests
{
    private static final String ACCEPTABLE_DIFF_PERCENTAGE = "ACCEPTABLE_DIFF_PERCENTAGE";

    private static final Locator DIV_LOCATOR = new Locator(WebLocatorType.XPATH, "//div");

    private static final Locator A_LOCATOR = new Locator(WebLocatorType.XPATH, ".//a");

    private static final String V = "v";

    private static final String K = "k";

    private static final String VISUAL_CHECK_PASSED = "Visual check passed";

    private static final String BASELINE = "baseline";

    private static final String FILESYSTEM = "filesystem";
    private static final String IGNORES_TABLE = "ignores table";

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
        return Map.of(
                IgnoreStrategy.ELEMENT, Set.of(),
                IgnoreStrategy.AREA, Set.of()
        );
    }

    @ParameterizedTest
    @CsvSource({"COMPARE_AGAINST", "CHECK_INEQUALITY_AGAINST"})
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
        mockUiContext();
        var table = mock(ExamplesTable.class);
        var row = mock(Parameters.class);
        when(table.getRows()).thenReturn(List.of(Map.of(K, V)));
        when(table.getRowAsParameters(0)).thenReturn(row);
        var elementsToIgnore = Set.of(A_LOCATOR);
        var areasToIgnore = Set.of(DIV_LOCATOR);
        mockRow(row, elementsToIgnore, areasToIgnore, 50, columnName);
        var ignores = Map.of(IgnoreStrategy.ELEMENT, elementsToIgnore,
                IgnoreStrategy.AREA, areasToIgnore);
        var screenshotParameters = mock(ScreenshotParameters.class);
        when(screenshotParametersFactory.create(Optional.empty(), IGNORES_TABLE, ignores)).thenReturn(
                screenshotParameters);
        when(visualTestingEngine.compareAgainst(visualCheckCaptor.capture())).thenReturn(visualCheckResult);
        mockCheckResult();
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
        mockUiContext();
        var table = mock(ExamplesTable.class);
        var row = mock(Parameters.class);
        when(table.getRows()).thenReturn(List.of(Map.of(K, V)));
        when(table.getRowAsParameters(0)).thenReturn(row);
        var elementsToIgnore = Set.of(A_LOCATOR);
        var areasToIgnore = Set.of(DIV_LOCATOR);
        mockRow(row, elementsToIgnore, areasToIgnore, 50, ACCEPTABLE_DIFF_PERCENTAGE);
        var ignores = Map.of(IgnoreStrategy.ELEMENT, elementsToIgnore,
                IgnoreStrategy.AREA, areasToIgnore);
        var screenshotParameters = mock(ScreenshotParameters.class);
        when(screenshotParametersFactory.create(Optional.empty(), IGNORES_TABLE, ignores)).thenReturn(
                screenshotParameters);

        when(visualTestingEngine.compareAgainst(visualCheckCaptor.capture())).thenReturn(visualCheckResult);
        mockCheckResult();
        visualSteps.runVisualTests(VisualActionType.COMPARE_AGAINST, BASELINE, FILESYSTEM, table);
        verify(softAssert).assertTrue(VISUAL_CHECK_PASSED, false);
        var visualCheck = visualCheckCaptor.getValue();
        assertEquals(OptionalDouble.of(50), visualCheck.getAcceptableDiffPercentage());
        assertEquals(Optional.of(FILESYSTEM), visualCheck.getBaselineStorage());
        validateVisualCheck(visualCheck, VisualActionType.COMPARE_AGAINST, screenshotParameters);
        verifyCheckResultPublish();
    }

    @Test
    void shouldRunVisualTestWithStepLevelExclusionsAndCustomScreenshotConfiguration() throws IOException
    {
        mockUiContext();
        var table = mock(ExamplesTable.class);
        var row = mock(Parameters.class);
        when(table.getRows()).thenReturn(List.of(Map.of(K, V)));
        when(table.getRowAsParameters(0)).thenReturn(row);
        var elementsToIgnore = Set.of(A_LOCATOR);
        var areasToIgnore = Set.of(DIV_LOCATOR);
        mockRow(row, elementsToIgnore, areasToIgnore);
        var screenshotConfiguration = mock(ScreenshotConfiguration.class);
        var screenshotParameters = mock(ScreenshotParameters.class);
        var ignores = Map.of(
                IgnoreStrategy.ELEMENT, elementsToIgnore,
                IgnoreStrategy.AREA, areasToIgnore
        );
        when(screenshotParametersFactory.create(Optional.of(screenshotConfiguration), IGNORES_TABLE,
                ignores)).thenReturn(screenshotParameters);
        var compareAgainst = VisualActionType.COMPARE_AGAINST;
        when(visualTestingEngine.compareAgainst(visualCheckCaptor.capture())).thenReturn(visualCheckResult);
        mockCheckResult();
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
        mockUiContext();
        var table = mock(ExamplesTable.class);
        var row = mock(Parameters.class);
        when(table.getRows()).thenReturn(List.of(Map.of(K, V)));
        when(table.getRowAsParameters(0)).thenReturn(row);
        var elementsToIgnore = Set.of(A_LOCATOR);
        var areasToIgnore = Set.of(DIV_LOCATOR);
        mockRow(row, elementsToIgnore, areasToIgnore);
        var screenshotConfiguration = mock(ScreenshotConfiguration.class);
        var screenshotParameters = mock(ScreenshotParameters.class);
        when(screenshotParametersFactory.create(Optional.of(screenshotConfiguration), IGNORES_TABLE,
                Map.of(IgnoreStrategy.ELEMENT, elementsToIgnore, IgnoreStrategy.AREA, areasToIgnore))).thenReturn(
                screenshotParameters);
        var compareAgainst = VisualActionType.COMPARE_AGAINST;
        when(visualTestingEngine.compareAgainst(visualCheckCaptor.capture())).thenReturn(visualCheckResult);
        mockCheckResult();
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
        when(visualCheckResult.getBaseline()).thenReturn(StringUtils.EMPTY);
    }

    @Test
    void shouldThrowExceptionIfTableHasMoreThanOneRow()
    {
        var table = mock(ExamplesTable.class);
        when(table.getRows()).thenReturn(List.of(Map.of(K, V), Map.of(K, V)));
        var exception = assertThrows(IllegalArgumentException.class, () ->
            visualSteps.runVisualTests(VisualActionType.COMPARE_AGAINST, BASELINE, table));
        assertEquals("Only one row of locators to ignore supported, actual: 2", exception.getMessage());
        verify(table, never()).getRowAsParameters(0);
        verifyNoInteractions(softAssert, visualTestingEngine, attachmentPublisher);
    }

    private static void mockRow(Parameters row, Set<Locator> elementIgnore, Set<Locator> areaIgnore)
    {
        mockGettingValue(row, "ELEMENT", elementIgnore);
        mockGettingValue(row, "AREA", areaIgnore);
    }

    private static void mockRow(Parameters row, Set<Locator> elementIgnore, Set<Locator> areaIgnore,
            double acceptableDiffPercentage, String columnName)
    {
        mockRow(row, elementIgnore, areaIgnore);
        doReturn(Map.of(columnName, "50")).when(row).values();
        doReturn(acceptableDiffPercentage).when(row).valueAs(columnName, Double.TYPE);
    }

    private static void mockGettingValue(Parameters row, String name, Set<Locator> result)
    {
        doReturn(result).when(row).valueAs(eq(name),
                argThat(t -> t instanceof ParameterizedType
                        && ((ParameterizedType) t).getRawType() == Set.class
                        && ((ParameterizedType) t).getActualTypeArguments()[0] == Locator.class),
                eq(Set.of()));
    }

    @Test
    void shouldNotAssertResultForEstablishAction() throws IOException
    {
        mockUiContext();
        when(visualTestingEngine.establish(visualCheckCaptor.capture())).thenReturn(visualCheckResult);
        when(visualCheckResult.getActionType()).thenReturn(VisualActionType.ESTABLISH);
        var screenshotParameters = mock(ScreenshotParameters.class);
        when(screenshotParametersFactory.create(Optional.empty(), IGNORES_TABLE,
                createEmptyIgnores())).thenReturn(screenshotParameters);
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

    static Stream<Arguments> exceptionsToCatch()
    {
        return Stream.of(Arguments.of(new IOException(), new ResourceLoadException("resource not loaded")));
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

    private void validateVisualCheck(VisualCheck visualCheck, VisualActionType type,
            ScreenshotParameters screenshotParameters)
    {
        assertEquals(BASELINE, visualCheck.getBaselineName());
        assertEquals(type, visualCheck.getAction());
        assertEquals(Optional.of(screenshotParameters), visualCheck.getScreenshotParameters());
    }

    private void verifyCheckResultPublish()
    {
        verify(attachmentPublisher).publishAttachment("visual-comparison.ftl", Map.of("result", visualCheckResult),
                "Visual comparison");
    }
}
