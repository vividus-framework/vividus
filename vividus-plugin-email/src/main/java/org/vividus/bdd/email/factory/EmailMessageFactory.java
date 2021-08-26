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

package org.vividus.bdd.email.factory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Named;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.bdd.email.model.EmailMessage;
import org.vividus.bdd.email.model.EmailMessageTextContent;

@Named
public class EmailMessageFactory
{
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailMessageFactory.class);

    public EmailMessage create(Message message) throws EmailMessageCreationException
    {
        try
        {
            List<EmailMessageTextContent> contents = new ArrayList<>();
            Object content = message.getContent();
            if (isMultipart(content))
            {
                unwrapMultipart((Multipart) content, contents);
            }
            else
            {
                contents.add(new EmailMessageTextContent(message.getContentType(), (String) content));
            }
            return new EmailMessage(message, contents);
        }
        catch (MessagingException | IOException e)
        {
            throw new EmailMessageCreationException(e);
        }
    }

    private void unwrapMultipart(Multipart multipart, List<EmailMessageTextContent> container)
            throws MessagingException, IOException
    {
        for (int partIndex = 0; partIndex < multipart.getCount(); partIndex++)
        {
            BodyPart part = multipart.getBodyPart(partIndex);
            Object content = part.getContent();
            if (isMultipart(content))
            {
                unwrapMultipart((Multipart) content, container);
                continue;
            }
            if (Part.ATTACHMENT.equalsIgnoreCase(part.getDisposition()))
            {
                LOGGER.atInfo().log(() ->
                {
                    try
                    {
                        return "Skip saving of attachment with the name " + part.getFileName();
                    }
                    catch (MessagingException e)
                    {
                        return "Got an error while identifying attachment name being skipped: " + e.getMessage();
                    }
                });
                continue;
            }
            String contentType = part.getContentType();
            String type = StringUtils.substringBefore(contentType, '/');
            if (!"text".equalsIgnoreCase(type))
            {
                LOGGER.info("Skip saving of content with the content type '{}'", contentType);
                continue;
            }
            container.add(new EmailMessageTextContent(contentType, (String) content));
        }
    }

    private boolean isMultipart(Object content)
    {
        return content instanceof Multipart;
    }

    public static final class EmailMessageCreationException extends Exception
    {
        private static final long serialVersionUID = -195769916068213868L;

        public EmailMessageCreationException(Throwable cause)
        {
            super(cause);
        }
    }
}
