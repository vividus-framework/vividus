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
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.ContextSourceCodeProvider;

@ExtendWith(MockitoExtension.class)
class SourceCodePublishingOnFailureListenerTests
{
    private static final String SOURCES = "<html/>";
    private static final String HTML = "HTML";
    private static final String SOURCE_CODE_KEY = "sourceCode";
    private static final String FORMAT_KEY = "format";
    private static final String TEMPLATES_SOURCE_CODE_FTL = "/templates/source-code.ftl";
    private static final String TEMPLATES_SHADOW_CODE_FTL = "/templates/shadow-code.ftl";

    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private ContextSourceCodeProvider contextSourceCodeProvider;
    @Mock private IAttachmentPublisher attachmentPublisher;

    @InjectMocks private SourceCodePublishingOnFailureListener listener;

    @Test
    void shouldNoPublishSourceCodeWhenPublishingDisabled()
    {
        listener.setPublishSourceOnFailure(false);
        listener.onAssertionFailure(null);
        verifyNoInteractions(attachmentPublisher, webDriverProvider);
    }

    @Test
    void shouldNoPublishSourceCodeWhenWebDriverIsNotInitialized()
    {
        listener.setPublishSourceOnFailure(true);
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
        listener.setFormat(HTML);
        listener.setPublishSourceOnFailure(true);
        listener.onAssertionFailure(null);
        verify(webDriverProvider).isWebDriverInitialized();
        verify(attachmentPublisher).publishAttachment(TEMPLATES_SOURCE_CODE_FTL,
                Map.of(SOURCE_CODE_KEY, SOURCES, FORMAT_KEY, HTML), APPLICATION_SOURCE_CODE);
        verifyNoMoreInteractions(webDriverProvider, contextSourceCodeProvider, attachmentPublisher);
    }

    @Test
    void shouldPublishSourceCodeWithShadowDom()
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        Map<String, String> applicationSourceCode = Map.of(APPLICATION_SOURCE_CODE, SOURCES);
        when(contextSourceCodeProvider.getSourceCode()).thenReturn(applicationSourceCode);
        when(contextSourceCodeProvider.getShadowDomSourceCode()).thenReturn(applicationSourceCode);
        listener.setFormat(HTML);
        listener.setPublishSourceOnFailure(true);
        listener.setPublishShadowDomSourceOnFailure(true);
        listener.onAssertionFailure(null);
        verify(webDriverProvider).isWebDriverInitialized();
        InOrder inOrder = Mockito.inOrder(attachmentPublisher);
        inOrder.verify(attachmentPublisher).publishAttachment(TEMPLATES_SOURCE_CODE_FTL,
            Map.of(SOURCE_CODE_KEY, SOURCES, FORMAT_KEY, HTML), APPLICATION_SOURCE_CODE);
        inOrder.verify(attachmentPublisher).publishAttachment(TEMPLATES_SHADOW_CODE_FTL,
                Map.of("shadowDomSources", applicationSourceCode), "Shadow DOM sources");
        verifyNoMoreInteractions(webDriverProvider, contextSourceCodeProvider, attachmentPublisher);
    }

    @Test
    void shouldPublishSourceCodeWithShadowDomEmpty()
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(contextSourceCodeProvider.getSourceCode()).thenReturn(Map.of(APPLICATION_SOURCE_CODE, SOURCES));
        when(contextSourceCodeProvider.getShadowDomSourceCode()).thenReturn(Map.of());
        listener.setFormat(HTML);
        listener.setPublishSourceOnFailure(true);
        listener.setPublishShadowDomSourceOnFailure(true);
        listener.onAssertionFailure(null);
        verify(webDriverProvider).isWebDriverInitialized();
        verify(attachmentPublisher).publishAttachment(TEMPLATES_SOURCE_CODE_FTL,
                Map.of(SOURCE_CODE_KEY, SOURCES, FORMAT_KEY, HTML), APPLICATION_SOURCE_CODE);
        verifyNoMoreInteractions(webDriverProvider, contextSourceCodeProvider, attachmentPublisher);
    }

    @Test
    void shouldNotPublishMissingSource()
    {
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        when(contextSourceCodeProvider.getSourceCode()).thenReturn(Map.of());
        listener.setPublishSourceOnFailure(true);
        listener.onAssertionFailure(null);
        verify(webDriverProvider).isWebDriverInitialized();
        verifyNoInteractions(attachmentPublisher);
        verifyNoMoreInteractions(webDriverProvider, contextSourceCodeProvider);
    }
}
