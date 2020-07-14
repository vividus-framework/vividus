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

package org.vividus.bdd.email.factory;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;

import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.email.factory.EmailMessageFactory.EmailMessageCreationException;
import org.vividus.bdd.email.mock.MessageMockFactory;
import org.vividus.bdd.email.mock.MessageMockFactory.MessageMock;
import org.vividus.bdd.email.model.EmailMessage;
import org.vividus.bdd.email.model.EmailMessageTextContent;

@ExtendWith(MockitoExtension.class)
class EmailMessageFactoryTests
{
    private static final String TEXT = "TEXT";
    private static final String TEXT_TYPE = "TEXT/PLAIN; charset=UTF-8";

    @InjectMocks
    private EmailMessageFactory factory;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(EmailMessageFactory.class);

    @Test
    void testTextContent() throws EmailMessageCreationException, MessagingException, IOException
    {
        MessageMock messageMock = MessageMockFactory.create();
        Message message = messageMock.getMock();
        mockContent(message, TEXT_TYPE, TEXT);

        EmailMessage output = factory.create(message);
        verifyMessage(output, messageMock);
    }

    @Test
    void testNestedMultipartContent() throws IOException, MessagingException, EmailMessageCreationException
    {
        BodyPart bodyPart = mock(BodyPart.class);
        Multipart nested = mock(Multipart.class);
        when(bodyPart.getContent()).thenReturn(nested);
        BodyPart textBodyPart = mock(BodyPart.class);
        mockContent(textBodyPart, TEXT_TYPE, TEXT);
        when(nested.getCount()).thenReturn(1);
        when(nested.getBodyPart(0)).thenReturn(textBodyPart);

        Multipart multipart = mock(Multipart.class);
        when(multipart.getCount()).thenReturn(1);
        when(multipart.getBodyPart(0)).thenReturn(bodyPart);

        MessageMock messageMock = MessageMockFactory.create();
        Message message = messageMock.getMock();
        when(message.getContent()).thenReturn(multipart);
        EmailMessage output = factory.create(message);
        verifyMessage(output, messageMock);
    }

    @Test
    void testMultipartContent() throws IOException, MessagingException, EmailMessageCreationException
    {
        Multipart multipart = mock(Multipart.class);
        when(multipart.getCount()).thenReturn(3);

        BodyPart attachment = mock(BodyPart.class);
        String fileName = "file_name";
        when(attachment.getDisposition()).thenReturn(Part.ATTACHMENT);
        when(attachment.getFileName()).thenReturn(fileName);
        when(multipart.getBodyPart(0)).thenReturn(attachment);

        BodyPart textPart = mock(BodyPart.class);
        mockContent(textPart, TEXT_TYPE, TEXT);
        when(multipart.getBodyPart(1)).thenReturn(textPart);

        BodyPart zipPart = mock(BodyPart.class);
        String zipContentType = "application/zip";
        when(zipPart.getContentType()).thenReturn(zipContentType);
        when(multipart.getBodyPart(2)).thenReturn(zipPart);

        MessageMock messageMock = MessageMockFactory.create();
        Message message = messageMock.getMock();
        when(message.getContent()).thenReturn(multipart);

        EmailMessage output = factory.create(message);
        verifyMessage(output, messageMock);
        assertThat(logger.getLoggingEvents(), is(List.of(
                info("Skip saving of attachment with the name " + fileName),
                info("Skip saving of content with the content type '{}'", zipContentType))));
    }

    private void verifyMessage(EmailMessage output, MessageMock messageMock) throws MessagingException
    {
        assertMessage(output, messageMock);
        assertThat(output.getContents(), hasSize(1));
        EmailMessageTextContent content = output.getContents().get(0);
        assertEquals(TEXT, content.getContent());
        assertEquals(TEXT_TYPE, content.getContentType());
    }

    private void mockContent(Part part, String contentType, Object content) throws IOException, MessagingException
    {
        when(part.getContent()).thenReturn(content);
        when(part.getContentType()).thenReturn(contentType);
    }

    private void assertMessage(EmailMessage message, MessageMock messageMock) throws MessagingException
    {
        assertEquals(messageMock.getMockSubject(), message.getSubject());
        assertEquals(messageMock.getMockSentDate(), message.getSentDate());
        assertEquals(messageMock.getMockReceivedDate(), message.getReceivedDate());
        assertArrayEquals(messageMock.getMockReplyTo(), message.getReplyTo());
        assertArrayEquals(messageMock.getMockFrom(), message.getFrom());
        assertArrayEquals(messageMock.getMockCc(), message.getRecipients(RecipientType.CC));
        assertArrayEquals(messageMock.getMockBcc(), message.getRecipients(RecipientType.BCC));
        assertArrayEquals(messageMock.getMockTo(), message.getRecipients(RecipientType.TO));
    }
}
