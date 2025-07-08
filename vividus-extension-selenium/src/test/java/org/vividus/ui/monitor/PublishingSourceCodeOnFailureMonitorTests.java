/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.ui.monitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.RunContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.ui.ContextSourceCodeProvider;

@ExtendWith(MockitoExtension.class)
class PublishingSourceCodeOnFailureMonitorTests
{
    private static final String FORMAT = "html";

    @Mock private ContextSourceCodeProvider contextSourceCodeProvider;
    @Mock private RunContext runContext;
    @Mock private IWebDriverProvider webDriverProvider;
    @Mock private IAttachmentPublisher attachmentPublisher;

    private PublishingSourceCodeOnFailureMonitor createMonitor(boolean enabled)
    {
        return new PublishingSourceCodeOnFailureMonitor(enabled, FORMAT, contextSourceCodeProvider, runContext,
                webDriverProvider, attachmentPublisher);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void shouldReturnIfPublishingIfEnabled(boolean enabled)
    {
        assertEquals(enabled, createMonitor(enabled).isPublishingEnabled(null));
    }

    @Test
    void shouldPublishAttachment()
    {
        var sourceCode = "<html/>";
        when(contextSourceCodeProvider.getSourceCode()).thenReturn(Optional.of(sourceCode));
        createMonitor(true).publishAttachment();
        verify(attachmentPublisher).publishAttachment("/templates/source-code.ftl",
                Map.of("sourceCode", sourceCode, "format", FORMAT), "Application source code.html");
        verifyNoMoreInteractions(webDriverProvider, contextSourceCodeProvider, attachmentPublisher);
    }

    @Test
    void shouldNotPublishAttachmentIfSourceCodeIsEmpty()
    {
        when(contextSourceCodeProvider.getSourceCode()).thenReturn(Optional.empty());
        createMonitor(true).publishAttachment();
        verifyNoInteractions(attachmentPublisher);
    }
}
