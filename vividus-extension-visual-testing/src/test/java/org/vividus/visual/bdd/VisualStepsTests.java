/*
 * Copyright 2019-2021 the original author or authors.
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

package org.vividus.visual.bdd;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.ui.context.IUiContext;
import org.vividus.visual.model.VisualActionType;
import org.vividus.visual.model.VisualCheck;
import org.vividus.visual.model.VisualCheckResult;

@ExtendWith(MockitoExtension.class)
class VisualStepsTests
{
    private static final String TEMPLATE = "template";

    @Mock
    private IUiContext uiContext;
    @Mock
    private IAttachmentPublisher attachmentPublisher;
    @Mock
    private ISoftAssert softAssert;

    @InjectMocks
    private TestVisualSteps visualSteps;

    @SuppressWarnings("unchecked")
    @Test
    void shouldThrowAnExceptionWhenContextIsNull()
    {
        Function<VisualCheck, VisualCheckResult> checkResultProvider = mock(Function.class);
        Supplier<VisualCheck> visualCheckFactory = mock(Supplier.class);
        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> visualSteps.execute(checkResultProvider, visualCheckFactory, TEMPLATE));
        assertEquals("Search context is null, please check is browser session started", exception.getMessage());
        verifyNoInteractions(visualCheckFactory, checkResultProvider, attachmentPublisher);
    }

    @Test
    void shouldNotPublishAttachmentWhenResultIsNull()
    {
        SearchContext searchContext = mock(SearchContext.class);
        VisualCheck visualCheck = mock(VisualCheck.class);
        when(uiContext.getSearchContext()).thenReturn(searchContext);
        Function<VisualCheck, VisualCheckResult> checkResultProvider = check -> null;
        Supplier<VisualCheck> visualCheckFactory = () -> visualCheck;
        assertNull(visualSteps.execute(checkResultProvider, visualCheckFactory, TEMPLATE));
        verifyNoInteractions(attachmentPublisher);
        verify(visualCheck).setSearchContext(searchContext);
    }

    @ParameterizedTest
    @CsvSource(value = {"true, COMPARE_AGAINST", "false, CHECK_INEQUALITY_AGAINST"})
    void shouldPublishAttachment(boolean passed, VisualActionType action)
    {
        SearchContext searchContext = mock(SearchContext.class);
        VisualCheck visualCheck = mock(VisualCheck.class);
        VisualCheckResult visualCheckResult = mock(VisualCheckResult.class);
        when(visualCheckResult.getActionType()).thenReturn(action);
        when(uiContext.getSearchContext()).thenReturn(searchContext);
        Function<VisualCheck, VisualCheckResult> checkResultProvider = check -> visualCheckResult;
        Supplier<VisualCheck> visualCheckFactory = () -> visualCheck;
        when(visualCheckResult.isPassed()).thenReturn(passed);
        assertSame(visualCheckResult, visualSteps.execute(checkResultProvider, visualCheckFactory, TEMPLATE));
        InOrder ordered = Mockito.inOrder(attachmentPublisher, visualCheckResult, softAssert);
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

    private static final class TestVisualSteps extends AbstractVisualSteps
    {
        private TestVisualSteps(IUiContext uiContext, IAttachmentPublisher attachmentPublisher, ISoftAssert softAssert)
        {
            super(uiContext, attachmentPublisher, softAssert);
        }
    }
}
