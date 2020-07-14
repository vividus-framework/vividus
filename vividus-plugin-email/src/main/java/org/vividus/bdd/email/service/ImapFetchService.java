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

package org.vividus.bdd.email.service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import javax.mail.Authenticator;
import javax.mail.FetchProfile;
import javax.mail.FetchProfile.Item;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessageRemovedException;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.search.SearchTerm;

import org.vividus.bdd.email.factory.EmailMessageFactory;
import org.vividus.bdd.email.factory.EmailMessageFactory.EmailMessageCreationException;
import org.vividus.bdd.email.model.EmailMessage;
import org.vividus.bdd.email.model.EmailServerConfiguration;
import org.vividus.util.Sleeper;
import org.vividus.util.function.CheckedPredicate;
import org.vividus.util.function.CheckedSupplier;
import org.vividus.util.wait.WaitMode;
import org.vividus.util.wait.Waiter;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class ImapFetchService implements EmailFetchService
{
    private static final String PROTOCOL = "imaps";

    private static final float EVENT_ARRIVAL_FACTOR = 0.8f;
    private static final long POLL_LOWER_BOUND = 5;

    private final WaitMode waitMode;
    private final String folder;
    private final long messageEventWaitInMillis;

    private final EmailMessageFactory emailMessageFactory;

    public ImapFetchService(Duration duration, int retryTimes, String folder, EmailMessageFactory emailMessageFactory)
    {
        this.waitMode = new WaitMode(duration, retryTimes);
        long pollingTimeout = waitMode.calculatePollingTimeout(TimeUnit.SECONDS);
        if (pollingTimeout < POLL_LOWER_BOUND)
        {
            throw new IllegalArgumentException(
                    String.format("Polling timeout must be not less than %d seconds, but got %d milliseconds",
                            POLL_LOWER_BOUND, waitMode.calculatePollingTimeout(TimeUnit.MILLISECONDS)));
        }
        this.messageEventWaitInMillis = (long) (waitMode.calculatePollingTimeout(TimeUnit.MILLISECONDS)
                * EVENT_ARRIVAL_FACTOR);
        this.folder = folder;
        this.emailMessageFactory = emailMessageFactory;
    }

    @Override
    public List<EmailMessage> fetch(List<CheckedPredicate<Message, MessagingException>> messageFilters,
            EmailServerConfiguration configuration) throws EmailFetchServiceException
    {
        Authenticator authenticator = new PasswordAuthenticator(configuration.getUsername(),
                configuration.getPassword());

        Properties properties = new Properties();
        properties.putAll(asImapsProperties(configuration.getProperties()));

        Session session = Session.getInstance(properties, authenticator);

        try (Store store = session.getStore(PROTOCOL); Folder mailFolder = getFolder(store))
        {
            SearchTerm searchTerm = new PredicateSearchTerm(messageFilters);
            PollingMessageListener listener = new PollingMessageListener(mailFolder::isOpen, searchTerm);

            mailFolder.addMessageCountListener(listener);
            mailFolder.open(Folder.READ_ONLY);

            Message[] messages = fetchMessages(mailFolder, Set.of(Item.ENVELOPE));
            Message[] filtered = mailFolder.search(searchTerm, messages);

            if (filtered.length > 0)
            {
                fetchMessages(mailFolder, filtered, Set.of(Item.CONTENT_INFO));
                return asMailMessages(List.of(filtered));
            }
            else
            {
                Waiter waiter = new Waiter(waitMode);
                List<Message> output = interruptible(() -> waiter.wait(listener::getMessages, msgs -> !msgs.isEmpty()));
                return asMailMessages(output);
            }
        }
        catch (MessagingException | EmailMessageCreationException | InternalEmailFetchServiceException e)
        {
            throw new EmailFetchServiceException(e);
        }
    }

    private List<EmailMessage> asMailMessages(List<Message> messages) throws EmailMessageCreationException
    {
        List<EmailMessage> emailMessages = new ArrayList<>(messages.size());
        for (Message message : messages)
        {
            emailMessages.add(emailMessageFactory.create(message));
        }
        return emailMessages;
    }

    private Folder getFolder(Store store) throws MessagingException
    {
        store.connect();
        return store.getFolder(this.folder);
    }

    private Message[] fetchMessages(Folder folder, Set<Item> fetchSettings) throws MessagingException
    {
        return fetchMessages(folder, folder.getMessages(), fetchSettings);
    }

    private Message[] fetchMessages(Folder folder, Message[] messages, Set<Item> fetchSettings)
            throws MessagingException
    {
        FetchProfile profile = new FetchProfile();
        fetchSettings.forEach(profile::add);
        folder.fetch(messages, profile);
        return messages;
    }

    private Map<String, String> asImapsProperties(Map<String, String> properties)
    {
        return properties.entrySet().stream()
                .collect(Collectors.toMap(e -> "mail." + PROTOCOL + "." + e.getKey(), Map.Entry::getValue));
    }

    private <T> T interruptible(CheckedSupplier<T, InterruptedException> supplier)
    {
        try
        {
            return supplier.get();
        }
        catch (InterruptedException e)
        {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    private static final class PredicateSearchTerm extends SearchTerm
    {
        private static final long serialVersionUID = 1163386376061414046L;

        @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
        private final transient List<CheckedPredicate<Message, MessagingException>> messageFilters;

        private PredicateSearchTerm(List<CheckedPredicate<Message, MessagingException>> messageFilters)
        {
            this.messageFilters = messageFilters;
        }

        @Override
        public boolean match(Message msg)
        {
            for (CheckedPredicate<Message, MessagingException> filter : messageFilters)
            {
                try
                {
                    if (!filter.test(msg))
                    {
                        return false;
                    }
                }
                catch (MessageRemovedException e)
                {
                    return false;
                }
                catch (MessagingException e)
                {
                    throw new InternalEmailFetchServiceException(e);
                }
            }
            return true;
        }
    }

    private static final class PasswordAuthenticator extends Authenticator
    {
        private final String username;
        private final String password;

        private PasswordAuthenticator(String username, String password)
        {
            this.username = username;
            this.password = password;
        }

        @Override
        protected PasswordAuthentication getPasswordAuthentication()
        {
            return new PasswordAuthentication(username, password);
        }
    }

    private final class PollingMessageListener implements MessageCountListener
    {
        private final Runnable refresh;
        private final SearchTerm searchTerm;
        private final List<Message> messages;

        private final ReentrantLock lock = new ReentrantLock();
        private final Condition messageArrivedCondition = lock.newCondition();
        private final Condition messageHandleCondition = lock.newCondition();
        private final AtomicBoolean handleFlag = new AtomicBoolean(true);

        private PollingMessageListener(Runnable refresh, SearchTerm searchTerm)
        {
            this.refresh = refresh;
            this.searchTerm = searchTerm;
            this.messages = new CopyOnWriteArrayList<>();
        }

        @Override
        public void messagesAdded(MessageCountEvent event)
        {
            try
            {
                lock.lock();
                messageArrivedCondition.signal();
                interruptible(() ->
                {
                    while (handleFlag.get())
                    {
                        messageHandleCondition.await();
                    }
                    return null;
                });
                for (Message message : event.getMessages())
                {
                    if (searchTerm.match(message))
                    {
                        messages.add(message);
                    }
                }
            }
            finally
            {
                try
                {
                    handleFlag.set(true);
                    messageHandleCondition.signal();
                }
                finally
                {
                    lock.unlock();
                }
            }
        }

        @Override
        public void messagesRemoved(MessageCountEvent event)
        {
            // empty
        }

        public List<Message> getMessages() throws InterruptedException
        {
            while (lock.isLocked())
            {
                Sleeper.sleep(Duration.ofSeconds(1));
            }
            try
            {
                lock.lock();
                refresh.run();
                boolean arrived = false;
                while (true)
                {
                    arrived = messageArrivedCondition.await(messageEventWaitInMillis, TimeUnit.MILLISECONDS);
                    break;
                }

                if (arrived)
                {
                    handleFlag.set(false);
                    messageHandleCondition.signal();
                    while (!handleFlag.get())
                    {
                        messageHandleCondition.await();
                    }
                }
                return StreamSupport.stream(this.messages.spliterator(), false)
                                    .collect(Collectors.toList());
            }
            finally
            {
                try
                {
                    handleFlag.set(true);
                    messageHandleCondition.signal();
                }
                finally
                {
                    lock.unlock();
                }
            }
        }
    }

    public static final class EmailFetchServiceException extends Exception
    {
        private static final long serialVersionUID = -8997475793040858586L;

        public EmailFetchServiceException(Throwable cause)
        {
            super(cause);
        }
    }

    private static final class InternalEmailFetchServiceException extends RuntimeException
    {
        private static final long serialVersionUID = -5689187885543972098L;

        private InternalEmailFetchServiceException(Throwable cause)
        {
            super(cause);
        }
    }
}
