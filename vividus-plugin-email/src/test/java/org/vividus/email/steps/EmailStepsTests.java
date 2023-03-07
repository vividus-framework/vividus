/*
 * Copyright 2019-2023 the original author or authors.
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

package org.vividus.email.steps;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.commons.lang3.function.FailablePredicate;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.email.mock.MessageMockFactory;
import org.vividus.email.mock.MessageMockFactory.MessageMock;
import org.vividus.email.model.EmailMessage;
import org.vividus.email.model.EmailMessageTextContent;
import org.vividus.email.model.EmailServerConfiguration;
import org.vividus.email.service.EmailFetchService;
import org.vividus.email.service.ImapFetchService.EmailFetchServiceException;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.property.PropertyMappedCollection;
import org.vividus.variable.VariableScope;

import jakarta.mail.Message;
import jakarta.mail.MessagingException;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class EmailStepsTests
{
    private static final String CONFIG_KEY = "config_key";
    private static final String VAR_KEY = "var_key";
    private static final String TEXT_ENTRIES_MSG = "Email text content to save";
    private static final String COLLECTION_MATCHER_MSG = "a collection with size a value greater than <0>";

    @Mock
    private EmailFetchService messageFetchService;
    @Mock
    private VariableContext variableContext;
    @Mock
    private ISoftAssert softAssert;
    @Mock
    private EmailServerConfiguration configuration;
    @Mock
    private FailablePredicate<Message, MessagingException> filter;

    private EmailSteps steps;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(EmailSteps.class);

    @BeforeEach
    void init()
    {
        PropertyMappedCollection<EmailServerConfiguration> configs = new PropertyMappedCollection<>(
                Map.of(CONFIG_KEY, configuration));
        steps = new EmailSteps(configs, messageFetchService, variableContext, softAssert);
    }

    @AfterEach
    void verifyAfter()
    {
        verifyNoMoreInteractions(configuration);
    }

    @Test
    void testSaveMessageContent() throws MessagingException, EmailFetchServiceException
    {
        String text = "text";
        String contentType = "TEXT/PLAIN; charset=utf-8";
        EmailMessageTextContent content = new EmailMessageTextContent(contentType, text);
        EmailMessage message = new EmailMessage(null, List.of(content));
        when(messageFetchService.fetch(List.of(filter), configuration)).thenReturn(List.of(message));
        when(softAssert.assertThat(eq(TEXT_ENTRIES_MSG), eq(List.of(content)),
                argThat(arg -> COLLECTION_MATCHER_MSG.equals(arg.toString())))).thenReturn(true);

        steps.saveMessageContent(CONFIG_KEY, List.of(filter), Set.of(VariableScope.STORY), VAR_KEY);

        assertThat(logger.getLoggingEvents(), is(List.of(info("Content {} by index '{}'", contentType, 0))));
        verify(variableContext).putVariable(Set.of(VariableScope.STORY), VAR_KEY + "[0]", text);
        verifyNoMoreInteractions(softAssert, messageFetchService, variableContext);
    }

    @Nested
    class NegativeTests
    {
        @Test
        void testNoMessagesFoundByGivenFilters() throws MessagingException, EmailFetchServiceException
        {
            when(messageFetchService.fetch(List.of(filter), configuration)).thenReturn(List.of());

            steps.saveMessageContent(CONFIG_KEY, List.of(filter), Set.of(VariableScope.STORY), VAR_KEY);

            verify(softAssert).recordFailedAssertion("No messages were found by the specified filters");
            verifyNoMoreInteractions(softAssert, messageFetchService, variableContext);
            assertThat(logger.getLoggingEvents(), empty());
        }

        @Test
        void testMoreThanOneMessageFoundByGivenFilters() throws MessagingException, EmailFetchServiceException
        {
            MessageMock messageMock = MessageMockFactory.create();
            EmailMessage emailMessage = new EmailMessage(messageMock.getMock(), List.of());
            when(messageFetchService.fetch(List.of(filter), configuration))
                    .thenReturn(List.of(emailMessage, emailMessage));

            steps.saveMessageContent(CONFIG_KEY, List.of(filter), Set.of(VariableScope.STORY), VAR_KEY);

            String messagesFormat = "Message #%d%nSubject:\t%s%nSent date:\t%s%nReceived date:\t%s%nFrom:\t\t%s%n"
                    + "CC Recipients:\t%s%nBCC Recipients:\t%s%nTO Recipients:\t%s%nReply to:\t%s%n";

            String sentDate = messageMock.getMockSentDate().toInstant().toString();
            String recDate = messageMock.getMockReceivedDate().toInstant().toString();

            String assertionFirst = String.format(messagesFormat, 1, messageMock.getMockSubject(), sentDate, recDate,
                    messageMock.fromAsStr(), messageMock.ccAsStr(), messageMock.bccAsStr(),
                    messageMock.toAsStr(), messageMock.replyToAsStr());
            String assertionSecond = String.format(messagesFormat, 2, messageMock.getMockSubject(), sentDate, recDate,
                    messageMock.fromAsStr(), messageMock.ccAsStr(), messageMock.bccAsStr(),
                    messageMock.toAsStr(), messageMock.replyToAsStr());

            verify(softAssert).recordFailedAssertion(
                    String.format("Expected one message, but found 2:%n%s%s", assertionFirst, assertionSecond));
            verifyNoMoreInteractions(softAssert, messageFetchService, variableContext);
            assertThat(logger.getLoggingEvents(), empty());
        }

        @Test
        void testOneMessageFoundByGivenFilters() throws MessagingException, EmailFetchServiceException
        {
            EmailMessage emailMessage = new EmailMessage(null, List.of());
            when(messageFetchService.fetch(List.of(filter), configuration)).thenReturn(List.of(emailMessage));

            steps.saveMessageContent(CONFIG_KEY, List.of(filter), Set.of(VariableScope.STORY), VAR_KEY);

            verify(softAssert).assertThat(eq(TEXT_ENTRIES_MSG), eq(List.of()),
                    argThat(arg -> COLLECTION_MATCHER_MSG.equals(arg.toString())));
            verifyNoMoreInteractions(softAssert, messageFetchService, variableContext);
            assertThat(logger.getLoggingEvents(), empty());
        }
    }
}
