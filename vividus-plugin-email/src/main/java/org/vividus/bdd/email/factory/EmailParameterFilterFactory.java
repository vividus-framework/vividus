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

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.function.FailableFunction;
import org.apache.commons.lang3.function.FailablePredicate;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.steps.StringComparisonRule;

public enum EmailParameterFilterFactory
{
    SUBJECT
    {
        @Override
        public FailablePredicate<Message, MessagingException> createFilter(String rule, String variable)
        {
            return m -> apply(m.getSubject(), rule, variable);
        }
    },
    SENT_DATE
    {
        @Override
        public FailablePredicate<Message, MessagingException> createFilter(String rule, String variable)
        {
            return checkDates(rule, variable, Message::getSentDate);
        }
    },
    RECEIVED_DATE
    {
        @Override
        public FailablePredicate<Message, MessagingException> createFilter(String rule, String variable)
        {
            return checkDates(rule, variable, Message::getReceivedDate);
        }
    },
    FROM
    {
        @Override
        public FailablePredicate<Message, MessagingException> createFilter(String rule, String variable)
        {
            return checkAddressees(rule, variable, Message::getFrom);
        }
    },
    CC_RECIPIENTS
    {
        @Override
        public FailablePredicate<Message, MessagingException> createFilter(String rule, String variable)
        {
            return checkRecipients(rule, variable, RecipientType.CC);
        }
    },
    BCC_RECIPIENTS
    {
        @Override
        public FailablePredicate<Message, MessagingException> createFilter(String rule, String variable)
        {
            return checkRecipients(rule, variable, RecipientType.BCC);
        }
    },
    TO_RECIPIENTS
    {
        @Override
        public FailablePredicate<Message, MessagingException> createFilter(String rule, String variable)
        {
            return checkRecipients(rule, variable, RecipientType.TO);
        }
    },
    REPLY_TO
    {
        @Override
        public FailablePredicate<Message, MessagingException> createFilter(String rule, String variable)
        {
            return checkAddressees(rule, variable, Message::getReplyTo);
        }
    };

    public abstract FailablePredicate<Message, MessagingException> createFilter(String rule, String variable);

    FailablePredicate<Message, MessagingException> checkDates(String rule, String inputDate,
            FailableFunction<Message, Date, MessagingException> getter)
    {
        return m -> apply(getter.apply(m).toInstant(), rule, asISODateTime(inputDate));
    }

    FailablePredicate<Message, MessagingException> checkRecipients(String rule, String addressesAsString,
            RecipientType type)
    {
        return checkAddressees(rule, addressesAsString, msg -> msg.getRecipients(type));
    }

    FailablePredicate<Message, MessagingException> checkAddressees(String rule, String addressesAsString,
            FailableFunction<Message, Address[], MessagingException> getter)
    {
        return m ->
        {
            Address[] actualAddresses = getter.apply(m);
            List<String> addresses = split(addressesAsString);
            int size = actualAddresses.length;
            return addresses.size() == size && IntStream.range(0, size)
                    .allMatch(idx -> apply(actualAddresses[idx].toString(), rule, addresses.get(idx)));
        };
    }

    private static List<String> split(String value)
    {
        return Stream.of(value.split(",")).map(String::strip).collect(Collectors.toList());
    }

    <T extends Comparable<T>> boolean apply(Object messageData, String rule, T input)
    {
        if (input == null)
        {
            return false;
        }
        ComparisonRule comparableRule = EnumUtils.getEnumIgnoreCase(ComparisonRule.class, rule);
        if (comparableRule != null)
        {
            return comparableRule.getComparisonRule(input).matches(messageData);
        }
        StringComparisonRule stringRule = EnumUtils.getEnumIgnoreCase(StringComparisonRule.class, rule);
        if (stringRule != null)
        {
            Validate.isTrue(input instanceof String, "%s filter is not applicable for %s parameter", stringRule,
                    this.name());
            return stringRule.createMatcher((String) input).matches(messageData);
        }
        throw new IllegalArgumentException(getUnknownRuleMessage(rule));
    }

    private static String getUnknownRuleMessage(String unknown)
    {
        Set<String> rules = Stream.concat(Stream.of(ComparisonRule.values()), Stream.of(StringComparisonRule.values()))
                                  .map(Enum::name)
                                  .collect(Collectors.toSet());

        return String.format("Unknown rule %s, please choose among the following rules: %s", unknown, rules);
    }

    private static Instant asISODateTime(String date)
    {
        try
        {
            return Instant.parse(date);
        }
        catch (DateTimeParseException e)
        {
            String dateAsString = Instant.now().truncatedTo(ChronoUnit.MILLIS).toString();
            throw new IllegalArgumentException(
                    String.format("Please use ISO 8601 zone date time format like '%s'", dateAsString),
                    e);
        }
    }

    public static final class EmailParameterFilterFactoryException extends RuntimeException
    {
        private static final long serialVersionUID = -7497616361654913089L;

        public EmailParameterFilterFactoryException(Throwable cause)
        {
            super(cause);
        }
    }
}
