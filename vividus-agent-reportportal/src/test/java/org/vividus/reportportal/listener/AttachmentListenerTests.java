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

package org.vividus.reportportal.listener;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import java.nio.charset.StandardCharsets;
import java.util.List;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.reporter.event.AttachmentPublishEvent;
import org.vividus.reporter.model.Attachment;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class AttachmentListenerTests
{
    @InjectMocks private AttachmentListener attachmentListener;

    private final TestLogger testLogger = TestLoggerFactory.getTestLogger(AttachmentListener.class);

    @Test
    void shouldPublishEncodedAttachment()
    {
        String title = "date";
        Attachment attachment = new Attachment("11.11.22".getBytes(StandardCharsets.UTF_8), title);
        AttachmentPublishEvent event = new AttachmentPublishEvent(attachment);
        attachmentListener.onAttachmentPublish(event);
        assertThat(testLogger.getLoggingEvents(), is(List.of(info("RP_MESSAGE#BASE64#{}#{}", "MTEuMTEuMjI=", title))));
    }
}
