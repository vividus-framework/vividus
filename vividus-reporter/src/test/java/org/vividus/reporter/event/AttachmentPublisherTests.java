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

import static com.github.valfirst.slf4jtest.LoggingEvent.error;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;
import com.google.common.eventbus.EventBus;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.reporter.model.Attachment;
import org.vividus.util.freemarker.FreemarkerProcessor;

import freemarker.template.TemplateException;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class AttachmentPublisherTests
{
    private static final String TEMPLATE_NAME = "templateName";
    private static final String TITLE = "title";
    private static final String TEXT_HTML = "text/html";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(AttachmentPublisher.class);

    @Mock
    private FreemarkerProcessor freemarkerProcessor;

    @Mock
    private EventBus eventBus;

    @InjectMocks
    private AttachmentPublisher attachmentPublisher;

    @Test
    void testPublishAttachment() throws IOException, TemplateException
    {
        String templateName = TEMPLATE_NAME;
        Object dataModel = new Object();
        byte[] attachmentContent = {};
        when(freemarkerProcessor.process(templateName, dataModel)).thenReturn(attachmentContent);
        String title = TITLE;
        attachmentPublisher.publishAttachment(templateName, dataModel, title);
        verify(eventBus).post(argThat(arg ->
        {
            if (arg instanceof AttachmentPublishEvent)
            {
                Attachment attachment = ((AttachmentPublishEvent) arg).getAttachment();
                return Arrays.equals(attachmentContent, attachment.getContent()) && title.equals(attachment.getTitle())
                        && TEXT_HTML.equals(attachment.getContentType());
            }
            return false;
        }));
        assertEquals(List.of(), logger.getLoggingEvents());
    }

    @Test
    void testPublishAttachmentWithTemplateProcessingException() throws IOException, TemplateException
    {
        String templateName = TEMPLATE_NAME;
        Object dataModel = new Object();
        IOException exception = new IOException("Template not found");
        when(freemarkerProcessor.process(templateName, dataModel)).thenThrow(exception);
        attachmentPublisher.publishAttachment(templateName, dataModel, TITLE);
        verifyNoInteractions(eventBus);
        assertEquals(List.of(error(exception, "Unable to generate attachment")), logger.getLoggingEvents());
    }

    @Test
    void testPublishPreparedAttachment()
    {
        byte[] attachmentContent = {};
        attachmentPublisher.publishAttachment(attachmentContent, TITLE + ".html");
        verify(eventBus).post(argThat(arg ->
        {
            if (arg instanceof AttachmentPublishEvent)
            {
                Attachment attachment = ((AttachmentPublishEvent) arg).getAttachment();
                return Arrays.equals(attachmentContent, attachment.getContent()) && TITLE.equals(attachment.getTitle())
                        && TEXT_HTML.equals(attachment.getContentType());
            }
            return false;
        }));
        assertEquals(List.of(), logger.getLoggingEvents());
    }
}
