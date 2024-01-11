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

import java.util.Map;

import com.google.common.eventbus.Subscribe;

import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.event.AssertionFailedEvent;
import org.vividus.ui.ContextSourceCodeProvider;

public class SourceCodePublishingOnFailureListener
{
    private final IWebDriverProvider webDriverProvider;
    private final ContextSourceCodeProvider contextSourceCodeProvider;
    private final IAttachmentPublisher attachmentPublisher;

    private boolean publishSourceOnFailure;
    private String format;

    public SourceCodePublishingOnFailureListener(IWebDriverProvider webDriverProvider,
            ContextSourceCodeProvider contextSourceCodeProvider, IAttachmentPublisher attachmentPublisher)
    {
        this.webDriverProvider = webDriverProvider;
        this.contextSourceCodeProvider = contextSourceCodeProvider;
        this.attachmentPublisher = attachmentPublisher;
    }

    @Subscribe
    public void onAssertionFailure(AssertionFailedEvent event)
    {
        if (publishSourceOnFailure && webDriverProvider.isWebDriverInitialized())
        {
            contextSourceCodeProvider.getSourceCode().forEach(this::publishSource);
        }
    }

    private void publishSource(String title, String source)
    {
        attachmentPublisher.publishAttachment("/templates/source-code.ftl",
            Map.of("sourceCode", source, "format", format), title);
    }

    public void setPublishSourceOnFailure(boolean publishSourceOnFailure)
    {
        this.publishSourceOnFailure = publishSourceOnFailure;
    }

    public void setFormat(String format)
    {
        this.format = format;
    }
}
