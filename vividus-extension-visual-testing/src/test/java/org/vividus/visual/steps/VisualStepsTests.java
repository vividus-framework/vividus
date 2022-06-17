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

package org.vividus.visual.steps;

import static com.github.valfirst.slf4jtest.LoggingEvent.warn;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.screenshot.IgnoreStrategy;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.action.search.Locator;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.screenshot.ScreenshotConfiguration;
import org.vividus.ui.screenshot.ScreenshotPrecondtionMismatchException;
import org.vividus.visual.model.AbstractVisualCheck;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.model.VisualCheckResult;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class VisualStepsTests
{
    private static final String TEMPLATE = "template";
    private static final String SOURCE_KEY = "source-key";

    private static final LoggingEvent WARNING_MESSAGE = warn("The passing of elements and areas to ignore through {}"
            + " is deprecated, please use screenshot configuration instead", SOURCE_KEY);

    @Mock private IUiContext uiContext;
    @Mock private IAttachmentPublisher attachmentPublisher;
    @Mock private ISoftAssert softAssert;
    @InjectMocks private TestVisualSteps visualSteps;

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(AbstractVisualSteps.class);

    @SuppressWarnings("unchecked")
    @Test
    void shouldRecordAssertionWhenContextIsNull()
    {
        Function<AbstractVisualCheck, VisualCheckResult> checkResultProvider = mock(Function.class);
        Supplier<AbstractVisualCheck> visualCheckFactory = mock(Supplier.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.empty());
        visualSteps.execute(visualCheckFactory, checkResultProvider, TEMPLATE);
        verifyNoInteractions(visualCheckFactory, checkResultProvider, attachmentPublisher);
    }

    @Test
    void shouldNotPublishAttachmentWhenResultIsNull()
    {
        var searchContext = mock(SearchContext.class);
        var visualCheck = mock(AbstractVisualCheck.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(searchContext));
        var checkResultProvider = (Function<AbstractVisualCheck, VisualCheckResult>) check -> null;
        var visualCheckFactory = (Supplier<AbstractVisualCheck>) () -> visualCheck;
        visualSteps.execute(visualCheckFactory, checkResultProvider, TEMPLATE);
        verifyNoInteractions(attachmentPublisher);
        verify(visualCheck).setSearchContext(searchContext);
    }

    @ParameterizedTest
    @CsvSource({"true, COMPARE_AGAINST", "false, CHECK_INEQUALITY_AGAINST"})
    void shouldPublishAttachment(boolean passed, VisualActionType action)
    {
        var searchContext = mock(SearchContext.class);
        var visualCheck = mock(AbstractVisualCheck.class);
        var visualCheckResult = mock(VisualCheckResult.class);
        when(visualCheckResult.getActionType()).thenReturn(action);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(searchContext));
        var checkResultProvider = (Function<AbstractVisualCheck, VisualCheckResult>) check -> visualCheckResult;
        var visualCheckFactory = (Supplier<AbstractVisualCheck>) () -> visualCheck;
        when(visualCheckResult.isPassed()).thenReturn(passed);
        visualSteps.execute(visualCheckFactory, checkResultProvider, TEMPLATE);
        var ordered = Mockito.inOrder(attachmentPublisher, visualCheckResult, softAssert);
        ordered.verify(attachmentPublisher).publishAttachment(TEMPLATE, Map.of("result", visualCheckResult),
                "Visual comparison");
        ordered.verify(softAssert).assertTrue("Visual check passed", true);
        verify(visualCheck).setSearchContext(searchContext);
    }

    @Test
    void shouldReturnsSoftAssert()
    {
        assertSame(softAssert, visualSteps.getSoftAssert());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldRecordInvalidVisualCheckPreconditionException()
    {
        var searchContext = mock(SearchContext.class);
        var exception = mock(ScreenshotPrecondtionMismatchException.class);
        Supplier<AbstractVisualCheck> visualCheckFactory = mock(Supplier.class);
        doThrow(exception).when(visualCheckFactory).get();
        Function<AbstractVisualCheck, VisualCheckResult> checkResultProvider = mock(Function.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(searchContext));

        visualSteps.execute(visualCheckFactory, checkResultProvider, TEMPLATE);

        verify(softAssert).recordFailedAssertion(exception);
        verifyNoInteractions(attachmentPublisher, checkResultProvider);
    }

    @Test
    void shouldFailIfBothSourcesAreNotEmpty()
    {
        var locator = mock(Locator.class);
        var screenshotConfiguration = new ScreenshotConfiguration();
        screenshotConfiguration.setAreasToIgnore(Set.of(locator));
        screenshotConfiguration.setElementsToIgnore(Set.of(locator));
        Map<IgnoreStrategy, Set<Locator>> ignores = Map.of(
                IgnoreStrategy.AREA, Set.of(locator),
                IgnoreStrategy.ELEMENT, Set.of(locator)
        );
        var thrown = assertThrows(IllegalArgumentException.class,
                () -> visualSteps.patchIgnores(SOURCE_KEY, screenshotConfiguration, ignores));
        assertEquals("The elements and areas to ignore must be passed either through screenshot configuration or "
                + SOURCE_KEY, thrown.getMessage());
    }

    @Test
    void shouldNotPatchIgnores()
    {
        var locator = mock(Locator.class);
        var screenshotConfiguration = new ScreenshotConfiguration();
        screenshotConfiguration.setAreasToIgnore(Set.of(locator));
        screenshotConfiguration.setElementsToIgnore(Set.of(locator));
        visualSteps.patchIgnores(SOURCE_KEY, screenshotConfiguration, Map.of(
                IgnoreStrategy.AREA, Set.of(),
                IgnoreStrategy.ELEMENT, Set.of()
        ));
        assertEquals(screenshotConfiguration.getElementsToIgnore(), Set.of(locator));
        assertEquals(screenshotConfiguration.getAreasToIgnore(), Set.of(locator));
        assertThat(testLogger.getLoggingEvents(), equalTo(List.of()));
    }

    @Test
    void shouldPatchIgnores()
    {
        var locator = mock(Locator.class);
        var screenshotConfiguration = new ScreenshotConfiguration();
        visualSteps.patchIgnores(SOURCE_KEY, screenshotConfiguration, Map.of(
            IgnoreStrategy.AREA, Set.of(locator),
            IgnoreStrategy.ELEMENT, Set.of(locator)
        ));
        assertEquals(screenshotConfiguration.getElementsToIgnore(), Set.of(locator));
        assertEquals(screenshotConfiguration.getAreasToIgnore(), Set.of(locator));
        assertThat(testLogger.getLoggingEvents(), equalTo(List.of(WARNING_MESSAGE, WARNING_MESSAGE)));
    }

    private static final class TestVisualSteps extends AbstractVisualSteps
    {
        private TestVisualSteps(IUiContext uiContext, IAttachmentPublisher attachmentPublisher, ISoftAssert softAssert)
        {
            super(uiContext, attachmentPublisher, softAssert);
        }
    }
}
