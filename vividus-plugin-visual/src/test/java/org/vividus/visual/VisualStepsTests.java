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

package org.vividus.visual;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
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
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.steps.Parameters;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.By;
import org.openqa.selenium.SearchContext;
import org.vividus.bdd.resource.ResourceLoadException;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.screenshot.ScreenshotConfiguration;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.visual.engine.IVisualCheckFactory;
import org.vividus.visual.engine.IVisualTestingEngine;
import org.vividus.visual.engine.IgnoreStrategy;
import org.vividus.visual.engine.VisualCheckFactory;
import org.vividus.visual.engine.VisualCheckFactory.VisualCheck;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.model.VisualCheckResult;

@ExtendWith(MockitoExtension.class)
class VisualStepsTests
{
    private static final By DIV_LOCATOR = By.xpath("//div");

    private static final By A_LOCATOR = By.xpath(".//a");

    private static final String V = "v";

    private static final String K = "k";

    private static final String VISUAL_CHECK_PASSED = "Visual check passed";

    private static final String BASELINE = "baseline";

    private static final VisualCheckFactory FACTORY = new VisualCheckFactory();

    @Mock
    private IVisualTestingEngine visualTestingEngine;
    @Mock
    private ISoftAssert softAssert;
    @Mock
    private IAttachmentPublisher attachmentPublisher;
    @Mock
    private VisualCheckResult visualCheckResult;
    @Mock
    private IVisualCheckFactory visualCheckFactory;
    @Mock
    private IWebUiContext webUiContext;

    @InjectMocks
    private VisualSteps visualSteps;

    @BeforeAll
    static void beforeAll()
    {
        FACTORY.setScreenshotIndexer(Optional.empty());
    }

    @Test
    void shouldAssertCheckResultForCompareAgainstActionAndPublishAttachment() throws IOException
    {
        VisualCheck visualCheck = mockVisualCheckFactory(VisualActionType.COMPARE_AGAINST);
        mockWebUiContext();
        when(visualTestingEngine.compareAgainst(visualCheck)).thenReturn(visualCheckResult);
        mockCheckResult();
        visualSteps.runVisualTests(VisualActionType.COMPARE_AGAINST, BASELINE);
        verify(softAssert).assertTrue(VISUAL_CHECK_PASSED, false);
        assertEquals(Map.of(), visualCheck.getElementsToIgnore());
        verifyCheckResultPublish();
    }

    private void mockWebUiContext()
    {
        when(webUiContext.getSearchContext(SearchContext.class)).thenReturn(mock(SearchContext.class));
    }

    @Test
    void shouldPerformVisualCheckWithCustomConfiguration() throws IOException
    {
        VisualActionType compareAgainst = VisualActionType.COMPARE_AGAINST;
        mockWebUiContext();
        ScreenshotConfiguration screenshotConfiguration = mock(ScreenshotConfiguration.class);
        VisualCheck visualCheck = FACTORY.create(BASELINE, compareAgainst);
        when(visualCheckFactory.create(BASELINE, compareAgainst, screenshotConfiguration)).thenReturn(visualCheck);
        when(visualTestingEngine.compareAgainst(visualCheck)).thenReturn(visualCheckResult);
        mockCheckResult();
        visualSteps.runVisualTests(compareAgainst, BASELINE, screenshotConfiguration);
        verify(softAssert).assertTrue(VISUAL_CHECK_PASSED, false);
        assertEquals(Map.of(), visualCheck.getElementsToIgnore());
        verifyCheckResultPublish();
    }

    @Test
    void shouldRecordFailedAssertionInCaseOfMissingBaseline() throws IOException
    {
        VisualCheck visualCheck = mockVisualCheckFactory(VisualActionType.COMPARE_AGAINST);
        mockWebUiContext();
        when(visualTestingEngine.compareAgainst(visualCheck)).thenReturn(visualCheckResult);
        visualSteps.runVisualTests(VisualActionType.COMPARE_AGAINST, BASELINE);
        verify(softAssert, never()).assertTrue(VISUAL_CHECK_PASSED, false);
        verify(softAssert).recordFailedAssertion("Unable to find baseline with name: baseline");
        assertEquals(Map.of(), visualCheck.getElementsToIgnore());
        verifyCheckResultPublish();
    }

    @Test
    void shouldAssertCheckResultForCompareAgainstActionAndUseStepLevelExclusions() throws IOException
    {
        mockWebUiContext();
        ExamplesTable table = mock(ExamplesTable.class);
        Parameters row = mock(Parameters.class);
        when(table.getRows()).thenReturn(List.of(Map.of(K, V)));
        when(table.getRowAsParameters(0)).thenReturn(row);
        Set<By> elementsToIgnore = Set.of(A_LOCATOR);
        Set<By> areasToIgnore = Set.of(DIV_LOCATOR);
        mockRow(row, elementsToIgnore, areasToIgnore);
        VisualCheck visualCheck = mockVisualCheckFactory(VisualActionType.COMPARE_AGAINST);
        when(visualTestingEngine.compareAgainst(visualCheck)).thenReturn(visualCheckResult);
        mockCheckResult();
        visualSteps.runVisualTests(VisualActionType.COMPARE_AGAINST, BASELINE, table);
        verify(softAssert).assertTrue(VISUAL_CHECK_PASSED, false);
        assertEquals(Map.of(IgnoreStrategy.ELEMENT, elementsToIgnore, IgnoreStrategy.AREA, areasToIgnore),
                visualCheck.getElementsToIgnore());
        verifyCheckResultPublish();
    }

    @Test
    void shouldRunVisualTestWithStepLevelExclusionsAndCustomScreenshotConfiguration() throws IOException
    {
        mockWebUiContext();
        ExamplesTable table = mock(ExamplesTable.class);
        Parameters row = mock(Parameters.class);
        when(table.getRows()).thenReturn(List.of(Map.of(K, V)));
        when(table.getRowAsParameters(0)).thenReturn(row);
        Set<By> elementsToIgnore = Set.of(A_LOCATOR);
        Set<By> areasToIgnore = Set.of(DIV_LOCATOR);
        mockRow(row, elementsToIgnore, areasToIgnore);
        ScreenshotConfiguration screenshotConfiguration = mock(ScreenshotConfiguration.class);
        VisualActionType compareAgainst = VisualActionType.COMPARE_AGAINST;
        VisualCheck visualCheck = FACTORY.create(BASELINE, compareAgainst);
        when(visualCheckFactory.create(BASELINE, compareAgainst, screenshotConfiguration)).thenReturn(visualCheck);
        when(visualTestingEngine.compareAgainst(visualCheck)).thenReturn(visualCheckResult);
        mockCheckResult();
        visualSteps.runVisualTests(VisualActionType.COMPARE_AGAINST, BASELINE, table, screenshotConfiguration);
        verify(softAssert).assertTrue(VISUAL_CHECK_PASSED, false);
        assertEquals(Map.of(IgnoreStrategy.ELEMENT, elementsToIgnore, IgnoreStrategy.AREA, areasToIgnore),
                visualCheck.getElementsToIgnore());
        verifyCheckResultPublish();
    }

    private void mockCheckResult()
    {
        when(visualCheckResult.getBaseline()).thenReturn(StringUtils.EMPTY);
    }

    private VisualCheck mockVisualCheckFactory(VisualActionType actionType)
    {
        VisualCheck visualCheck = FACTORY.create(BASELINE, actionType);
        when(visualCheckFactory.create(BASELINE, actionType)).thenReturn(visualCheck);
        return visualCheck;
    }

    @Test
    void shouldThrowExceptionIfTableHasMoreThanOneRow()
    {
        ExamplesTable table = mock(ExamplesTable.class);
        when(table.getRows()).thenReturn(List.of(Map.of(K, V), Map.of(K, V)));
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            visualSteps.runVisualTests(VisualActionType.COMPARE_AGAINST, BASELINE, table));
        assertEquals("Only one row of locators to ignore supported, actual: 2", exception.getMessage());
        verify(table, never()).getRowAsParameters(0);
        verifyNoInteractions(softAssert, visualTestingEngine, attachmentPublisher);
    }

    private static void mockRow(Parameters row, Set<By> elementIgnore, Set<By> areaIgnore)
    {
        mockGettingValue(row, "ELEMENT", elementIgnore);
        mockGettingValue(row, "AREA", areaIgnore);
    }

    private static void mockGettingValue(Parameters row, String name, Set<By> result)
    {
        doReturn(result).when(row).valueAs(eq(name),
                argThat(t -> t instanceof ParameterizedType
                        && ((ParameterizedType) t).getRawType() == Set.class
                        && ((ParameterizedType) t).getActualTypeArguments()[0] == By.class),
                eq(Set.of()));
    }

    @Test
    void shouldNotAssertResultForEstablishAction() throws IOException
    {
        mockWebUiContext();
        VisualCheck visualCheck = mockVisualCheckFactory(VisualActionType.ESTABLISH);
        when(visualTestingEngine.establish(visualCheck)).thenReturn(visualCheckResult);
        visualSteps.runVisualTests(VisualActionType.ESTABLISH, BASELINE);
        verifyNoInteractions(softAssert);
        verifyCheckResultPublish();
        assertEquals(Map.of(), visualCheck.getElementsToIgnore());
    }

    @Test
    void shouldNotPerformVisualCheckIfSearchContextIsNull()
    {
        visualSteps.runVisualTests(VisualActionType.ESTABLISH, BASELINE);
        verifyNoInteractions(visualCheckFactory, visualTestingEngine, attachmentPublisher, softAssert);
    }

    static Stream<Arguments> exceptionsToCatch()
    {
        return Stream.of(Arguments.of(new IOException(), new ResourceLoadException("resource not loaded")));
    }

    @ParameterizedTest
    @MethodSource("exceptionsToCatch")
    void shouldRecordExceptions(Exception exception) throws IOException
    {
        mockWebUiContext();
        shouldRecordException(exception);
    }

    private void shouldRecordException(Exception exception) throws IOException
    {
        VisualCheck visualCheck = mockVisualCheckFactory(VisualActionType.ESTABLISH);
        when(visualTestingEngine.establish(visualCheck)).thenThrow(exception);
        visualSteps.runVisualTests(VisualActionType.ESTABLISH, BASELINE);
        verify(softAssert).recordFailedAssertion(exception);
        verifyNoInteractions(attachmentPublisher);
        verifyNoMoreInteractions(softAssert);
    }

    private void verifyCheckResultPublish()
    {
        verify(attachmentPublisher).publishAttachment("visual-comparison.ftl", Map.of("result", visualCheckResult),
                "Visual comparison");
    }
}
