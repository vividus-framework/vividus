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

package org.vividus.email.steps;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;

import org.apache.commons.lang3.function.FailablePredicate;
import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.context.VariableContext;
import org.vividus.email.model.EmailMessage;
import org.vividus.email.model.EmailMessageTextContent;
import org.vividus.email.model.EmailServerConfiguration;
import org.vividus.email.service.EmailFetchService;
import org.vividus.email.service.ImapFetchService.EmailFetchServiceException;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.property.PropertyMappedCollection;
import org.vividus.variable.VariableScope;

public class EmailSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(EmailSteps.class);

    private final PropertyMappedCollection<EmailServerConfiguration> serverConfigurations;
    private final EmailFetchService messageFetchService;
    private final VariableContext variableContext;
    private final ISoftAssert softAssert;

    public EmailSteps(PropertyMappedCollection<EmailServerConfiguration> serverConfigurations,
            EmailFetchService messageFetchService, VariableContext variableContext, ISoftAssert softAssert)
    {
        this.messageFetchService = messageFetchService;
        this.variableContext = variableContext;
        this.serverConfigurations = serverConfigurations;
        this.softAssert = softAssert;
    }

    /**
     * Step fetches a mail message from a server specified by the <b>serverKey</b> and if message is found saves
     * its <i>text</i> content parts into indexed variables with the prefix <b>variableName</b> i.e. if the message
     * contains two text parts, the first part will be saved under <b>variableName[0]</b> and the second under
     * <b>variableName[1]</b>
     * <div><b>Example:</b></div>
     * <pre>
     * <code>
     * When I fetch email message from `gmail` server filtered by
     * <br> |parameter     |rule            |value                                                   |
     * <br> |SUBJECT       |EQUAL_TO        |Registration code                                       |
     * <br> |SENT_DATE     |GREATER_THAN    |1970-01-01T00:00:00.000Z                                |
     * <br> |RECEIVED_DATE |LESS_THAN       |2020-06-14T11:47:11.521Z                                |
     * <br> |FROM          |IS_EQUAL_TO     |service &lt;regservice@company.com&gt;                  |
     * <br> |CC_RECIPIENTS |CONTAINS        |admin                                                   |
     * <br> |BCC_RECIPIENTS|DOES_NOT_CONTAIN|gmail.com                                               |
     * <br> |TO_RECIPIENTS |EQUAL_TO        |me &lt;me@company.com&gt;, boss &lt;boss@company.com&gt;|
     * <br> |REPLY_TO      |MATCHES         |(?i)[0-9]@.*                                            |
     * <br>  and save message content to SCENARIO variable `textContent`
     * </code>
     * </pre>
     * Filtering <b>rule</b>s:
     * <ul>
     * <li>EQUAL_TO</li>
     * <li>GREATER_THAN_OR_EQUAL_TO</li>
     * <li>GREATER_THAN</li>
     * <li>LESS_THAN_OR_EQUAL_TO</li>
     * <li>LESS_THAN</li>
     * <li>NOT_EQUAL_TO</li>
     * <li>IS_EQUAL_TO</li>
     * <li>CONTAINS</li>
     * <li>DOES_NOT_CONTAIN</li>
     * <li>MATCHES</li>
     * </ul>
     * <b>Parameter</b>s:
     * <ul>
     * <li>SUBJECT</li>
     * <li>SENT_DATE</li>
     * <li>RECEIVED_DATE</li>
     * <li>FROM</li>
     * <li>CC_RECIPIENTS</li>
     * <li>BCC_RECIPIENTS</li>
     * <li>TO_RECIPIENTS</li>
     * <li>REPLY_TO</li>
     * </ul>
     * Notes:
     * <ul>
     * <li>CONTAINS, MATCHES, IS_EQUAL_TO, DOES_NOT_CONTAIN are not allowed to be used with <i>date</i>
     * parameters</li>
     * <li><b>rule</b> names are case insensitive</li>
     * <li>multiple <i>address</i> parameters can be checked by separating expected value by commas</li>
     * <li><i>date</i> parameters must have zoned ISO 8061 format i.e. 1970-01-01T00:00:00.000Z</li>
     * </ul>
     * @param serverKey key of a server to fetch a message from
     * @param messageFilters message filters
     * @param scopes The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     * <i>Available scopes:</i>
     * <ul>
     * <li><b>STEP</b> - the variable will be available only within the step,
     * <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     * <li><b>STORY</b> - the variable will be available within the whole story,
     * <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     * </ul>
     * @param variableName A name of variable to assign the values from command execution result
     * @throws MessagingException signals that an exception of some sort has occurred while communicating with
     * message service
     * @throws EmailFetchServiceException signals that an exception of some sort has occurred while fetching
     * message from email server
     */
    @When("I fetch email message from `$serverKey` server filtered by $filters and save message content to $scopes "
            + "variable `$variableName`")
    public void saveMessageContent(String serverKey,
            List<FailablePredicate<Message, MessagingException>> messageFilters, Set<VariableScope> scopes,
            String variableName) throws EmailFetchServiceException, MessagingException
    {
        EmailServerConfiguration config = serverConfigurations.get(serverKey,
                "Email server connection with key '%s' is not configured in properties", serverKey);
        List<EmailMessage> messages = messageFetchService.fetch(messageFilters, config);

        int size = messages.size();
        if (messages.size() != 1)
        {
            if (size == 0)
            {
                softAssert.recordFailedAssertion("No messages were found by the specified filters");
                return;
            }
            softAssert.recordFailedAssertion(
                    String.format("Expected one message, but found %d:%n%s", size, formatMessages(messages)));
            return;
        }

        EmailMessage message = messages.get(0);
        List<EmailMessageTextContent> contents = message.getContents();
        if (softAssert.assertThat("Email text content to save", contents, hasSize(greaterThan(0))))
        {
            IntStream.range(0, contents.size()).forEach(index ->
            {
                EmailMessageTextContent content = contents.get(index);
                LOGGER.atInfo()
                      .addArgument(content.getContentType())
                      .addArgument(index)
                      .log("Content {} by index '{}'");
                String key = String.format("%s[%d]", variableName, index);
                variableContext.putVariable(scopes, key, content.getContent());
            });
        }
    }

    private static String formatMessages(List<EmailMessage> messages) throws MessagingException
    {
        String messageFormat = "Message #%d%n"
                + "Subject:\t%s%n"
                + "Sent date:\t%s%n"
                + "Received date:\t%s%n"
                + "From:\t\t%s%n"
                + "CC Recipients:\t%s%n"
                + "BCC Recipients:\t%s%n"
                + "TO Recipients:\t%s%n"
                + "Reply to:\t%s%n";

        String emailMessage = "";
        for (int index = 0; index < messages.size(); index++)
        {
            EmailMessage message = messages.get(index);
            String messageAsString = String.format(messageFormat, index + 1, message.getSubject(),
                    dateAsIsoString(message.getSentDate()), dateAsIsoString(message.getReceivedDate()),
                    join(message.getFrom()), join(message.getRecipients(RecipientType.CC)),
                    join(message.getRecipients(RecipientType.BCC)), join(message.getRecipients(RecipientType.TO)),
                    join(message.getReplyTo()));
            emailMessage += messageAsString;
        }

        return emailMessage;
    }

    private static String join(Address[] addresses)
    {
        return Optional.ofNullable(addresses)
                       .map(a -> Stream.of(a)
                                       .map(Address::toString)
                                       .collect(Collectors.joining(",")))
                       .orElse("<empty>");
    }

    private static String dateAsIsoString(Date date)
    {
        return date.toInstant().toString();
    }
}
