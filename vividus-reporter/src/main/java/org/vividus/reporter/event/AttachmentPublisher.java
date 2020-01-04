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

package org.vividus.reporter.event;

import java.io.IOException;

import com.google.common.eventbus.EventBus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.reporter.model.Attachment;
import org.vividus.util.freemarker.FreemarkerProcessor;

import freemarker.template.TemplateException;

public class AttachmentPublisher implements IAttachmentPublisher
{
    private static final Logger LOGGER = LoggerFactory.getLogger(AttachmentPublisher.class);

    private FreemarkerProcessor freemarkerProcessor;
    private EventBus eventBus;

    @Override
    public void publishAttachment(String templateName, Object dataModel, String title)
    {
        try
        {
            byte[] attachmentContent = freemarkerProcessor.process(templateName, dataModel);
            publishAttachment(new Attachment(attachmentContent, title, "text/html"));
        }
        catch (IOException | TemplateException e)
        {
            LOGGER.error("Unable to generate attachment", e);
        }
    }

    @Override
    public void publishAttachment(byte[] attachmentContent, String fileName)
    {
        publishAttachment(new Attachment(attachmentContent, fileName));
    }

    private void publishAttachment(Attachment attachment)
    {
        eventBus.post(new AttachmentPublishEvent(attachment));
    }

    public void setFreemarkerProcessor(FreemarkerProcessor freemarkerProcessor)
    {
        this.freemarkerProcessor = freemarkerProcessor;
    }

    public void setEventBus(EventBus eventBus)
    {
        this.eventBus = eventBus;
    }
}
