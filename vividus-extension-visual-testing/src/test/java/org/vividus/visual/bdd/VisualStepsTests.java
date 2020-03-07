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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.SearchContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.ui.web.context.IWebUiContext;
import org.vividus.visual.model.VisualCheck;
import org.vividus.visual.model.VisualCheckResult;

@ExtendWith(MockitoExtension.class)
class VisualStepsTests
{
    private static final String TEMPLATE = "template";

    @Mock
    private IWebUiContext webUiContext;
    @Mock
    private IAttachmentPublisher attachmentPublisher;

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
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        Function<VisualCheck, VisualCheckResult> checkResultProvider = check -> null;
        Supplier<VisualCheck> visualCheckFactory = () -> visualCheck;
        assertNull(visualSteps.execute(checkResultProvider, visualCheckFactory, TEMPLATE));
        verifyNoInteractions(attachmentPublisher);
        verify(visualCheck).setSearchContext(searchContext);
    }

    @Test
    void shouldPublishAttachment()
    {
        SearchContext searchContext = mock(SearchContext.class);
        VisualCheck visualCheck = mock(VisualCheck.class);
        VisualCheckResult visualCheckResult = mock(VisualCheckResult.class);
        when(webUiContext.getSearchContext()).thenReturn(searchContext);
        Function<VisualCheck, VisualCheckResult> checkResultProvider = check -> visualCheckResult;
        Supplier<VisualCheck> visualCheckFactory = () -> visualCheck;
        assertSame(visualCheckResult, visualSteps.execute(checkResultProvider, visualCheckFactory, TEMPLATE));
        verify(attachmentPublisher).publishAttachment(TEMPLATE, Map.of("result", visualCheckResult),
                "Visual comparison");
        verify(visualCheck).setSearchContext(searchContext);
    }

    private static final class TestVisualSteps extends AbstractVisualSteps
    {
        private TestVisualSteps(IWebUiContext webUiContext, IAttachmentPublisher attachmentPublisher)
        {
            super(webUiContext, attachmentPublisher);
        }
    }
}
