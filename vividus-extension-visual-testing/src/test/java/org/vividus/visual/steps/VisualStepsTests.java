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

package org.vividus.visual.steps;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.context.IUiContext;
import org.vividus.ui.screenshot.ScreenshotPrecondtionMismatchException;
import org.vividus.visual.model.AbstractVisualCheck;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.model.VisualCheckResult;

@ExtendWith(MockitoExtension.class)
class VisualStepsTests
{
    private static final String TEMPLATE = "template";
    private static final String BASELINE_NAME = "baseline name";
    private static final String RESULT = "result";
    private static final String VISUAL_COMPARISON_ATTACHMENT_TITLE = "Visual comparison: " + BASELINE_NAME;
    private static final String VISUAL_CHECK_PASSED = "Visual check passed";
    private static final String COMPARE_AGAINST = "true, COMPARE_AGAINST";
    private static final String CHECK_INEQUALITY_AGAINST = "false, CHECK_INEQUALITY_AGAINST";

    @Mock private IUiContext uiContext;
    @Mock private IAttachmentPublisher attachmentPublisher;
    @Mock private ISoftAssert softAssert;
    @InjectMocks private TestVisualSteps visualSteps;

    @SuppressWarnings("unchecked")
    @Test
    void shouldRecordAssertionWhenContextIsNull()
    {
        Function<AbstractVisualCheck, VisualCheckResult> checkResultProvider = mock(Function.class);
        Supplier<AbstractVisualCheck> visualCheckFactory = mock(Supplier.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.empty());
        visualSteps.execute(visualCheckFactory, checkResultProvider);
        verifyNoInteractions(visualCheckFactory, checkResultProvider, attachmentPublisher);
    }

    @Test
    void shouldNotPublishAttachmentWhenResultIsNull()
    {
        var searchContext = mock(SearchContext.class);
        var visualCheck = mock(AbstractVisualCheck.class);
        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(searchContext));
        visualSteps.execute(() -> visualCheck, check -> null);
        verifyNoInteractions(attachmentPublisher);
        verify(visualCheck).setSearchContext(searchContext);
    }

    @ParameterizedTest
    @CsvSource({ COMPARE_AGAINST, CHECK_INEQUALITY_AGAINST })
    void shouldPublishAttachment(boolean passed, VisualActionType action)
    {
        var searchContext = mock(SearchContext.class);
        var visualCheck = new AbstractVisualCheck(BASELINE_NAME, action) { };
        var visualCheckResult = new VisualCheckResult(visualCheck);

        when(uiContext.getOptionalSearchContext()).thenReturn(Optional.of(searchContext));
        var checkResultProvider = (Function<AbstractVisualCheck, VisualCheckResult>) check -> {
            visualCheckResult.setPassed(passed);
            return visualCheckResult;
        };

        visualSteps.execute(() -> visualCheck, checkResultProvider);
        var ordered = inOrder(attachmentPublisher, softAssert);
        ordered.verify(attachmentPublisher).publishAttachment(TEMPLATE, Map.of(RESULT, visualCheckResult),
                VISUAL_COMPARISON_ATTACHMENT_TITLE);
        ordered.verify(softAssert).assertTrue(VISUAL_CHECK_PASSED, true);
        assertEquals(searchContext, visualCheck.getSearchContext());
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

        visualSteps.execute(visualCheckFactory, checkResultProvider);

        verify(softAssert).recordFailedAssertion(exception);
        verifyNoInteractions(attachmentPublisher, checkResultProvider);
    }

    @ParameterizedTest
    @CsvSource({ COMPARE_AGAINST, CHECK_INEQUALITY_AGAINST })
    void shouldPerformVisualCheckAndPublishAttachment(boolean passed, VisualActionType action)
    {
        var visualCheck = new AbstractVisualCheck(BASELINE_NAME, action) { };
        var visualCheckResult = new VisualCheckResult(visualCheck);

        var checkResultProvider = (Function<AbstractVisualCheck, VisualCheckResult>) check -> {
            visualCheckResult.setPassed(passed);
            return visualCheckResult;
        };

        visualSteps.executeWithoutContext(() -> visualCheck, checkResultProvider);
        var ordered = inOrder(attachmentPublisher, softAssert);
        ordered.verify(attachmentPublisher).publishAttachment(TEMPLATE, Map.of(RESULT, visualCheckResult),
                VISUAL_COMPARISON_ATTACHMENT_TITLE);
        ordered.verify(softAssert).assertTrue(VISUAL_CHECK_PASSED, true);
        assertNull(visualCheck.getSearchContext());
    }

    private static final class TestVisualSteps extends AbstractVisualSteps
    {
        private TestVisualSteps(IUiContext uiContext, IAttachmentPublisher attachmentPublisher, ISoftAssert softAssert)
        {
            super(uiContext, attachmentPublisher, softAssert);
        }

        @Override
        protected String getTemplateName()
        {
            return TEMPLATE;
        }
    }
}
