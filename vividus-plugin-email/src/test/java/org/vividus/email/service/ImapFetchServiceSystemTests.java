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

package org.vividus.email.service;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTimeout;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.user.GreenMailUser;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;
import com.icegreen.greenmail.util.ServerSetupTest;

import org.apache.commons.lang3.function.FailablePredicate;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.vividus.email.factory.EmailMessageFactory;
import org.vividus.email.factory.EmailParameterFilterFactory;
import org.vividus.email.model.EmailMessage;
import org.vividus.email.model.EmailServerConfiguration;
import org.vividus.steps.ComparisonRule;
import org.vividus.util.Sleeper;

class ImapFetchServiceSystemTests
{
    private static final String USER_LOGIN = "username";
    private static final String USER_PASS = "password";
    private static final String USERNAME_ADDR = "username@localhost";
    private static final String SENDER = "sender@localhost";

    private static GreenMail mailServer;
    private static GreenMailUser mailUser;

    @BeforeAll
    static void initServer()
    {
        long timeout = 20000L;
        ServerSetup smtpsSetup = ServerSetupTest.SMTPS;
        smtpsSetup.setServerStartupTimeout(timeout);
        ServerSetup imapsSetup = ServerSetupTest.IMAPS;
        imapsSetup.setServerStartupTimeout(timeout);
        mailServer = new GreenMail(new ServerSetup[] { smtpsSetup, imapsSetup });
        mailServer.start();
        mailUser = mailServer.setUser(USERNAME_ADDR, USER_LOGIN, USER_PASS);
        mailUser.create();
    }

    @AfterAll
    static void shutdownServer()
    {
        mailServer.stop();
    }

    @AfterEach
    void cleanUp() throws FolderException
    {
        mailServer.purgeEmailFromAllMailboxes();
    }

    @ParameterizedTest
    @CsvSource({
        "0,  7",
        "10, 60"
    })
    void testFetch(long deliveryDelay, long testTimeout) throws MessagingException
    {
        ImapFetchService service = serviceWith(90, 15);
        String subject = GreenMailUtil.random();
        FailablePredicate<Message, MessagingException> subjectPredicate = EmailParameterFilterFactory.SUBJECT
                .createFilter(ComparisonRule.EQUAL_TO.name(), subject);

        Instant now = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        FailablePredicate<Message, MessagingException> sentDatePredicate = EmailParameterFilterFactory.SENT_DATE
                .createFilter(ComparisonRule.GREATER_THAN.name(), now.minus(1, ChronoUnit.MINUTES).toString());

        FailablePredicate<Message, MessagingException> receivedPredicate = EmailParameterFilterFactory.RECEIVED_DATE
                .createFilter(ComparisonRule.LESS_THAN.name(), now.plus(1, ChronoUnit.DAYS).toString());

        List<FailablePredicate<Message, MessagingException>> predicates = List.of(subjectPredicate, sentDatePredicate,
                receivedPredicate);

        long testMessageDelay = deliveryDelay > 0 ? deliveryDelay / 2 : 0;
        scheduleSendEmail(testMessageDelay, GreenMailUtil.random());
        scheduleSendEmail(deliveryDelay, subject);
        List<EmailMessage> receivedMessages = new ArrayList<>();
        assertTimeout(Duration.ofSeconds(testTimeout),
            () -> receivedMessages.addAll(service.fetch(predicates, getConfig())));

        assertThat(receivedMessages, hasSize(1));
        EmailMessage message = receivedMessages.get(0);
        assertEquals(subject, message.getSubject());
    }

    @Test
    void testFetchNoEmailReceived()
    {
        String subject = GreenMailUtil.random();
        FailablePredicate<Message, MessagingException> subjectPredicate = EmailParameterFilterFactory.SUBJECT
                .createFilter(ComparisonRule.EQUAL_TO.name(), subject);

        List<EmailMessage> receivedMessages = new ArrayList<>();
        ImapFetchService service = serviceWith(5, 1);
        assertTimeout(Duration.ofMinutes(1),
            () -> receivedMessages.addAll(service.fetch(List.of(subjectPredicate), getConfig())));

        assertThat(receivedMessages, hasSize(0));
    }

    private static EmailServerConfiguration getConfig()
    {
        return new EmailServerConfiguration(USER_LOGIN, USER_PASS, Map.of(
                "host", mailServer.getImaps().getBindTo(),
                "port", String.valueOf(mailServer.getImaps().getPort()),
                "ssl.trust", "127.0.0.1"
                ));
    }

    private ImapFetchService serviceWith(int waitTimeout, int retries)
    {
        return new ImapFetchService(Duration.ofSeconds(waitTimeout), retries, "INBOX", new EmailMessageFactory());
    }

    private static void scheduleSendEmail(long deliveryDelay, String subject)
    {
        try
        {
            MimeMessage message = new MimeMessage((Session) null);
            message.setFrom(new InternetAddress(SENDER));
            message.setSubject(subject);
            message.setText(GreenMailUtil.random());

            if (deliveryDelay == 0)
            {
                mailUser.deliver(message);
                return;
            }
            Thread sendMailThread = new Thread(() ->
            {
                Sleeper.sleep(deliveryDelay, TimeUnit.SECONDS);
                mailUser.deliver(message);
            });

            sendMailThread.setDaemon(true);
            sendMailThread.start();
        }
        catch (MessagingException e)
        {
            throw new IllegalStateException(e);
        }
    }
}
