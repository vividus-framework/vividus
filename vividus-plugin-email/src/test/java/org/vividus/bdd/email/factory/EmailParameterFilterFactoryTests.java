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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.stream.Stream;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.StringComparisonRule;
import org.vividus.util.function.CheckedPredicate;

class EmailParameterFilterFactoryTests
{
    @ParameterizedTest
    @CsvSource({
        "Test message, EQUAL_TO, Test message, true ",
        "Error       , EQUAL_TO, Test message, false"
    })
    void testSubject(String messageSubject, String rule, String subject, boolean passed) throws MessagingException
    {
        Message message = Mockito.mock(Message.class);
        when(message.getSubject()).thenReturn(messageSubject);
        performTest(message, EmailParameterFilterFactory.SUBJECT, rule, subject, passed);
    }

    @ParameterizedTest
    @CsvSource({
        "2020-06-14T11:47:11.521Z, GREATER_THAN, 1993-04-16T23:10:38.456Z, true",
        "1970-01-01T00:00:00.000Z, GREATER_THAN, 2003-10-23T01:20:38.113Z, false"
    })
    void testSentDate(ZonedDateTime messageDate, String rule, String date, boolean passed) throws MessagingException
    {
        Message message = Mockito.mock(Message.class);
        when(message.getSentDate()).thenReturn(asDate(messageDate));
        performTest(message, EmailParameterFilterFactory.SENT_DATE, rule, date, passed);
    }

    @ParameterizedTest
    @CsvSource({
        "1993-04-16T23:10:38.456Z, LESS_THAN, 2020-06-14T11:47:11.521Z, true",
        "2003-10-23T01:20:38.113Z, LESS_THAN, 1970-01-01T00:00:00.000Z, false"
    })
    void testReceivedDate(ZonedDateTime messageDate, String rule, String date, boolean passed) throws MessagingException
    {
        Message message = Mockito.mock(Message.class);
        when(message.getReceivedDate()).thenReturn(asDate(messageDate));
        performTest(message, EmailParameterFilterFactory.RECEIVED_DATE, rule, date, passed);
    }

    static Stream<Arguments> dataSetForFromTest()
    {
        return Stream.of(
            arguments(
                    new Address[] { asAddress("Bob Bob <bb@gmail.com>"), asAddress("John Nhoj <jn@gmail.com>")},
                    StringComparisonRule.MATCHES.name(),
                    "(?i).*bob.*,.*John.*", true),
            arguments(
                    new Address[] { asAddress("Pip Pip <pp@gmail.com>")},
                    StringComparisonRule.MATCHES.name(),
                    ".*,.*", false),
            arguments(
                    new Address[] { asAddress("Bill Llib <bl@gmail.com>"), asAddress("Yo Oy <yo@gmail.com>")},
                    StringComparisonRule.MATCHES.name(),
                    ".*", false)
            );
    }

    @ParameterizedTest
    @MethodSource("dataSetForFromTest")
    void testFrom(Address[] messageAddresses, String rule, String addresses, boolean passed) throws MessagingException
    {
        Message message = Mockito.mock(Message.class);
        when(message.getFrom()).thenReturn(messageAddresses);
        performTest(message, EmailParameterFilterFactory.FROM, rule, addresses, passed);
    }

    static Stream<Arguments> dataSetForRecipientsTest()
    {
        return Stream.of(
            arguments(
                    new Address[] { asAddress("Donald Dlanod <dd@tut.by>")},
                    StringComparisonRule.CONTAINS.name(),
                    "@tut.by", true),
            arguments(
                    new Address[] { asAddress("Chack Kcahc <ck@gmail.com>")},
                    StringComparisonRule.CONTAINS.name(),
                    "@yandex.ru", false)
            );
    }

    @ParameterizedTest
    @MethodSource("dataSetForRecipientsTest")
    void testCcRecipients(Address[] messageAddresses, String rule, String addresses, boolean passed)
            throws MessagingException
    {
        Message message = Mockito.mock(Message.class);
        when(message.getRecipients(RecipientType.CC)).thenReturn(messageAddresses);
        performTest(message, EmailParameterFilterFactory.CC_RECIPIENTS, rule, addresses, passed);
    }

    @ParameterizedTest
    @MethodSource("dataSetForRecipientsTest")
    void testBccRecipients(Address[] messageAddresses, String rule, String addresses, boolean passed)
            throws MessagingException
    {
        Message message = Mockito.mock(Message.class);
        when(message.getRecipients(RecipientType.BCC)).thenReturn(messageAddresses);
        performTest(message, EmailParameterFilterFactory.BCC_RECIPIENTS, rule, addresses, passed);
    }

    @ParameterizedTest
    @MethodSource("dataSetForRecipientsTest")
    void testToRecipients(Address[] messageAddresses, String rule, String addresses, boolean passed)
            throws MessagingException
    {
        Message message = Mockito.mock(Message.class);
        when(message.getRecipients(RecipientType.TO)).thenReturn(messageAddresses);
        performTest(message, EmailParameterFilterFactory.TO_RECIPIENTS, rule, addresses, passed);
    }

    static Stream<Arguments> dataSetForReplyToTest()
    {
        return Stream.of(
            arguments(
                    new Address[] { asAddress("Solomon Northup <anorthup@gmail.by>")},
                    StringComparisonRule.DOES_NOT_CONTAIN.name(),
                    "@dev.by", true),
            arguments(
                    new Address[] { asAddress("Luke Skywalker <jedi@gmail.com>")},
                    StringComparisonRule.DOES_NOT_CONTAIN.name(),
                    "jedi", false)
            );
    }

    @ParameterizedTest
    @MethodSource("dataSetForReplyToTest")
    void testReplyTo(Address[] messageAddresses, String rule, String addresses, boolean passed)
            throws MessagingException
    {
        Message message = Mockito.mock(Message.class);
        when(message.getReplyTo()).thenReturn(messageAddresses);
        performTest(message, EmailParameterFilterFactory.REPLY_TO, rule, addresses, passed);
    }

    @ParameterizedTest
    @CsvSource(value = {
        "MATCHES | MATCHES filter is not applicable for SENT_DATE parameter",
        "SIMILAR | Unknown rule SIMILAR, please choose among the following rules:"
            + " [DOES_NOT_CONTAIN, IS_EQUAL_TO, GREATER_THAN, LESS_THAN_OR_EQUAL_TO,"
            + " MATCHES, EQUAL_TO, LESS_THAN, GREATER_THAN_OR_EQUAL_TO, CONTAINS, NOT_EQUAL_TO]"
        }, delimiter = '|')
    void testErrors(String rule, String errorMessage) throws MessagingException
    {
        Message message = Mockito.mock(Message.class);
        when(message.getSentDate()).thenReturn(Date.from(Instant.now()));
        CheckedPredicate<Message, MessagingException> filter = EmailParameterFilterFactory.SENT_DATE.createFilter(rule,
                Instant.now().toString());
        Exception exception = assertThrows(IllegalArgumentException.class, () -> filter.test(message));
        assertEquals(errorMessage, exception.getMessage());
    }

    @Test
    void testInvalidDateFormat() throws MessagingException
    {
        Message message = Mockito.mock(Message.class);
        when(message.getSentDate()).thenReturn(Date.from(Instant.now()));
        CheckedPredicate<Message, MessagingException> filter = EmailParameterFilterFactory.SENT_DATE
                .createFilter(ComparisonRule.EQUAL_TO.name(), "11:11:11");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> filter.test(message));
        assertThat(exception.getMessage(), matchesRegex(
            "Please use ISO 8601 zone date time format like '\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}.\\d{3}Z'"));
    }

    private static Date asDate(ZonedDateTime dateTime)
    {
        return Date.from(dateTime.toInstant());
    }

    private static void performTest(Message message, EmailParameterFilterFactory filterFactory, String rule,
            String input, boolean passed) throws MessagingException
    {
        CheckedPredicate<Message, MessagingException> filter = filterFactory.createFilter(rule, input);
        assertEquals(passed, filter.test(message));
    }

    private static Address asAddress(String addr)
    {
        try
        {
            return new InternetAddress(addr);
        }
        catch (AddressException e)
        {
            throw new IllegalStateException(e);
        }
    }
}
