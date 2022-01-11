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

package org.vividus.email.model;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;

public class EmailMessage
{
    private final Message message;
    private final List<EmailMessageTextContent> contents;

    public EmailMessage(Message message, List<EmailMessageTextContent> contents)
    {
        this.message = message;
        this.contents = Collections.unmodifiableList(contents);
    }

    public String getSubject() throws MessagingException
    {
        return this.message.getSubject();
    }

    public Date getSentDate() throws MessagingException
    {
        return this.message.getSentDate();
    }

    public Date getReceivedDate() throws MessagingException
    {
        return this.message.getReceivedDate();
    }

    public Address[] getRecipients(RecipientType type) throws MessagingException
    {
        return this.message.getRecipients(type);
    }

    public Address[] getFrom() throws MessagingException
    {
        return this.message.getFrom();
    }

    public Address[] getReplyTo() throws MessagingException
    {
        return this.message.getReplyTo();
    }

    public List<EmailMessageTextContent> getContents()
    {
        return contents;
    }
}
