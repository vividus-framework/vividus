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

package org.vividus.mobileapp.listener;

import java.nio.charset.StandardCharsets;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;

import org.vividus.reporter.event.AttachmentPublishEvent;
import org.vividus.reporter.model.Attachment;
import org.vividus.selenium.IWebDriverProvider;
import org.vividus.softassert.event.AssertionFailedEvent;

public class PublishingSourceOnFailureListener
{
    private final IWebDriverProvider webDriverProvider;
    private final EventBus eventBus;

    public PublishingSourceOnFailureListener(IWebDriverProvider webDriverProvider, EventBus eventBus)
    {
        this.webDriverProvider = webDriverProvider;
        this.eventBus = eventBus;
    }

    @Subscribe
    public void publishApplicationSource(AssertionFailedEvent event)
    {
        if (webDriverProvider.isWebDriverInitialized())
        {
            String pageSource = webDriverProvider.get().getPageSource();
            Attachment attachment = new Attachment(pageSource.getBytes(StandardCharsets.UTF_8),
                    "ApplicationSource.xml");
            eventBus.post(new AttachmentPublishEvent(attachment));
        }
    }
}
