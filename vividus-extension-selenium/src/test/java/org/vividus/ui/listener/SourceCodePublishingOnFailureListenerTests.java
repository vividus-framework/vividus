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

package org.vividus.ui.listener;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.IWebDriverProvider;

@ExtendWith(MockitoExtension.class)
class SourceCodePublishingOnFailureListenerTests
{
    private static final String SOURCES = "<html/>";
    private static final String HTML = "HTML";

    @Mock
    private IAttachmentPublisher attachmentPublisher;
    @Mock
    private IWebDriverProvider webDriverProvider;

    @InjectMocks
    private TestSorceCodePublishingOnFailureListener listener;

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
        listener.setPublishSourceOnFailure(true);
        listener.onAssertionFailure(null);
        verify(webDriverProvider).isWebDriverInitialized();
        verify(attachmentPublisher).publishAttachment("/templates/source-code.ftl",
                Map.of("sourceCode", SOURCES, "format", HTML), "Application source code");
        verifyNoMoreInteractions(webDriverProvider, attachmentPublisher);
    }

    @Test
    void shouldNotPublishMissingSource()
    {
        TestEmptySorceCodePublishingOnFailureListener listener = new TestEmptySorceCodePublishingOnFailureListener(
                attachmentPublisher, webDriverProvider);
        when(webDriverProvider.isWebDriverInitialized()).thenReturn(true);
        listener.setPublishSourceOnFailure(true);
        listener.onAssertionFailure(null);
        verify(webDriverProvider).isWebDriverInitialized();
        verifyNoInteractions(attachmentPublisher);
        verifyNoMoreInteractions(webDriverProvider, attachmentPublisher);
    }

    private static class TestSorceCodePublishingOnFailureListener extends AbstractSourceCodePublishingOnFailureListener
    {
        protected TestSorceCodePublishingOnFailureListener(IAttachmentPublisher attachmentPublisher,
                IWebDriverProvider webDriverProvider)
        {
            super(attachmentPublisher, webDriverProvider, HTML);
        }

        @Override
        protected Optional<String> getSourceCode()
        {
            return Optional.of(SOURCES);
        }
    }

    private static final class TestEmptySorceCodePublishingOnFailureListener
            extends TestSorceCodePublishingOnFailureListener
    {
        protected TestEmptySorceCodePublishingOnFailureListener(IAttachmentPublisher attachmentPublisher,
                IWebDriverProvider webDriverProvider)
        {
            super(attachmentPublisher, webDriverProvider);
        }

        @Override
        protected Optional<String> getSourceCode()
        {
            return Optional.empty();
        }
    }
}
