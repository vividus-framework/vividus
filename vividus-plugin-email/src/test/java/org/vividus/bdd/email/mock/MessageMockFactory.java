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

package org.vividus.bdd.email.mock;

import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;

public final class MessageMockFactory
{
    private static final String FROM = "from";
    private static final String CC = "cc";
    private static final String BCC = "bcc";
    private static final String TO = "to";
    private static final String REPLY_TO = "replyTo";

    private MessageMockFactory()
    {
    }

    public static MessageMock create() throws MessagingException
    {
        Message mock = mock(Message.class);

        String subject = "subject";
        when(mock.getSubject()).thenReturn(subject);

        Date sentDate = new Date();
        when(mock.getSentDate()).thenReturn(sentDate);

        Date receivedDate = new Date();
        when(mock.getReceivedDate()).thenReturn(receivedDate);

        Address[] from = mockAddress(FROM);
        when(mock.getFrom()).thenReturn(from);

        Address[] cc = mockAddress(CC);
        when(mock.getRecipients(RecipientType.CC)).thenReturn(cc);

        Address[] bcc = mockAddress(BCC);
        when(mock.getRecipients(RecipientType.BCC)).thenReturn(bcc);

        Address[] to = mockAddress(TO);
        when(mock.getRecipients(RecipientType.TO)).thenReturn(to);

        Address[] replyTo = mockAddress(REPLY_TO);
        when(mock.getReplyTo()).thenReturn(replyTo);

        return new MessageMock(mock, subject, sentDate, receivedDate, from, cc, bcc, to, replyTo);
    }

    private static Address[] mockAddress(String asString)
    {
        Address address = mock(Address.class);
        lenient().when(address.toString()).thenReturn(asString);
        return new Address[] { address };
    }

    public static final class MessageMock
    {
        private final Message mock;

        private final String mockSubject;
        private final Date mockSentDate;
        private final Date mockReceivedDate;
        private final Address[] mockFrom;
        private final Address[] mockCc;
        private final Address[] mockBcc;
        private final Address[] mockTo;
        private final Address[] mockReplyTo;

        public MessageMock(Message mock, String mockSubject, Date mockSentDate, Date mockReceivedDate,
                Address[] mockFrom, Address[] mockCc, Address[] mockBcc, Address[] mockTo, Address[] mockReplyTo)
        {
            this.mock = mock;
            this.mockSubject = mockSubject;
            this.mockSentDate = Date.from(mockSentDate.toInstant());
            this.mockReceivedDate = Date.from(mockReceivedDate.toInstant());
            this.mockFrom = Arrays.copyOf(mockFrom, mockFrom.length);
            this.mockCc = Arrays.copyOf(mockCc, mockCc.length);
            this.mockBcc = Arrays.copyOf(mockBcc, mockBcc.length);
            this.mockTo = Arrays.copyOf(mockTo, mockTo.length);
            this.mockReplyTo = Arrays.copyOf(mockReplyTo, mockReplyTo.length);
        }

        public Message getMock()
        {
            return mock;
        }

        public String getMockSubject()
        {
            return mockSubject;
        }

        public Date getMockSentDate()
        {
            return Date.from(mockSentDate.toInstant());
        }

        public Date getMockReceivedDate()
        {
            return Date.from(mockReceivedDate.toInstant());
        }

        public Address[] getMockFrom()
        {
            return Arrays.copyOf(mockFrom, mockFrom.length);
        }

        public String fromAsStr()
        {
            return FROM;
        }

        public Address[] getMockCc()
        {
            return Arrays.copyOf(mockCc, mockCc.length);
        }

        public String ccAsStr()
        {
            return CC;
        }

        public Address[] getMockBcc()
        {
            return Arrays.copyOf(mockBcc, mockBcc.length);
        }

        public String bccAsStr()
        {
            return BCC;
        }

        public Address[] getMockTo()
        {
            return Arrays.copyOf(mockTo, mockTo.length);
        }

        public String toAsStr()
        {
            return TO;
        }

        public Address[] getMockReplyTo()
        {
            return Arrays.copyOf(mockReplyTo, mockReplyTo.length);
        }

        public String replyToAsStr()
        {
            return REPLY_TO;
        }
    }
}
