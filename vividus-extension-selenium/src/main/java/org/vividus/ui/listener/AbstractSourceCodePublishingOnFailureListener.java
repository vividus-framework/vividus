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

import java.util.Map;
import java.util.Optional;

import com.google.common.eventbus.Subscribe;

import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.event.AssertionFailedEvent;

public abstract class AbstractSourceCodePublishingOnFailureListener
{
    private final IAttachmentPublisher attachmentPublisher;
    private final IWebDriverProvider webDriverProvider;
    private final String format;

    private boolean publishSourceOnFailure;

    protected AbstractSourceCodePublishingOnFailureListener(IAttachmentPublisher attachmentPublisher,
            IWebDriverProvider webDriverProvider, String format)
    {
        this.webDriverProvider = webDriverProvider;
        this.attachmentPublisher = attachmentPublisher;
        this.format = format;
    }

    @Subscribe
    public void onAssertionFailure(AssertionFailedEvent event)
    {
        if (publishSourceOnFailure && webDriverProvider.isWebDriverInitialized())
        {
            getSourceCode().ifPresent(this::publishSource);
        }
    }

    private void publishSource(String source)
    {
        attachmentPublisher.publishAttachment("/templates/source-code.ftl",
                Map.of("sourceCode", source, "format", format), "Application source code");
    }

    protected abstract Optional<String> getSourceCode();

    public void setPublishSourceOnFailure(boolean publishSourceOnFailure)
    {
        this.publishSourceOnFailure = publishSourceOnFailure;
    }

    protected IWebDriverProvider getWebDriverProvider()
    {
        return webDriverProvider;
    }
}
