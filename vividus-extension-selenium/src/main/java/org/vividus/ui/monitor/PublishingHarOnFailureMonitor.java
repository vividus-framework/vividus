/*
 * Copyright 2019-2022 the original author or authors.
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Optional;

import com.browserup.harreader.model.Har;
import com.google.common.eventbus.EventBus;

import org.vividus.context.RunContext;
import org.vividus.proxy.har.HarOnFailureManager;
import org.vividus.reporter.model.Attachment;
import org.vividus.selenium.IWebDriverProvider;

public class PublishingHarOnFailureMonitor extends AbstractPublishingAttachmentOnFailureMonitor
{
    private final HarOnFailureManager harOnFailureManager;

    private final boolean publishHarOnFailure;

    public PublishingHarOnFailureMonitor(boolean publishHarOnFailure, HarOnFailureManager harOnFailureManager,
            EventBus eventBus, RunContext runContext, IWebDriverProvider webDriverProvider)
    {
        super(runContext, webDriverProvider, eventBus, "noHarOnFailure", "Unable to capture HAR");
        this.harOnFailureManager = harOnFailureManager;
        this.publishHarOnFailure = publishHarOnFailure;
    }

    @Override
    protected Optional<Attachment> createAttachment() throws IOException
    {
        Optional<Har> har = harOnFailureManager.takeHar();
        if (har.isPresent())
        {
            try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream())
            {
                har.get().writeTo(byteArrayOutputStream);
                Attachment attachment = new Attachment(byteArrayOutputStream.toByteArray(), "har-on-failure.har");
                return Optional.of(attachment);
            }
        }
        return Optional.empty();
    }

    @Override
    protected boolean isPublishingEnabled(Method method)
    {
        return publishHarOnFailure || getAnnotation(method, PublishHarOnFailure.class).isPresent();
    }
}
