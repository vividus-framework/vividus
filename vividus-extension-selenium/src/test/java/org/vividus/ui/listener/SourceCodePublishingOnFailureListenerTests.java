/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.ui.listener;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.vividus.ui.ContextSourceCodeProvider.APPLICATION_SOURCE_CODE;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.ContextSourceCodeProvider;

@ExtendWith(MockitoExtension.class)
class SourceCodePublishingOnFailureListenerTests
{
    private static final String SOURCES = "<html/>";

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private ContextSourceCodeProvider contextSourceCodeProvider;
    @Mock private IAttachmentPublisher attachmentPublisher;

    @InjectMocks private SourceCodePublishingOnFailureListener listener;

    @Test
    void shouldNoPublishSourceCodeWhenWebDriverIsNotInitialized()
    {
        listener.onAssertionFailure(null);
        verify(webDriverProvider).isWebDriverInitialized();
        verifyNoInteractions(attachmentPublisher);
        verifyNoMoreInteractions(webDriverProvider);
    }

    @Test
    void shouldPublishSourceCode()
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(contextSourceCodeProvider.getSourceCode()).thenReturn(Map.of(APPLICATION_SOURCE_CODE, SOURCES));
        String html = "html";
        listener.setSourceCodeAttachmentFormat(html);
        listener.onAssertionFailure(null);
        verify(webDriverProvider).isWebDriverInitialized();
        verify(attachmentPublisher).publishAttachment("/templates/source-code.ftl",
                Map.of("sourceCode", SOURCES, "format", html), "Application source code.html");
        verifyNoMoreInteractions(webDriverProvider, contextSourceCodeProvider, attachmentPublisher);
    }

    @Test
    void shouldNotPublishMissingSource()
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(contextSourceCodeProvider.getSourceCode()).thenReturn(Map.of());
        listener.onAssertionFailure(null);
        verify(webDriverProvider).isWebDriverInitialized();
        verifyNoInteractions(attachmentPublisher);
        verifyNoMoreInteractions(webDriverProvider, contextSourceCodeProvider);
    }
}
