/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.azure.servicebus.service;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorSource;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.servicebus.ServiceBusSendMessageException;
import org.vividus.azure.servicebus.model.ChannelType;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
public class ServiceBusServiceTests
{
    private static final String NAMESPACE_NAME = "namespaceName";
    private static final String QUEUE_NAME = "queueName";
    private static final String MESSAGE = "msg";
    private static final String TOPIC_NAME = "topicName";
    private static final String SUCCESS_LOG_MSG = "The message was successfully sent to {} in {} namespace";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(ServiceBusService.class);

    @Mock private TokenCredential tokenCredential;
    @Mock private ServiceBusSenderClient client;
    @Mock private ServiceBusClientBuilder.ServiceBusSenderClientBuilder clientSenderBuilder;

    private MockedConstruction<AmqpRetryOptions> amqpRetryOptions;
    private MockedConstruction<ServiceBusClientBuilder> clientBuilder;
    private ServiceBusService serviceBusService;

    @BeforeEach
    void beforeEach()
    {
        when(clientSenderBuilder.buildClient()).thenReturn(client);

        AmqpRetryOptions retryOptions = mock(AmqpRetryOptions.class);
        amqpRetryOptions = mockConstruction(AmqpRetryOptions.class, (mock, context) -> {
            when(mock.setMaxRetries(1)).thenReturn(mock);
            when(mock.setTryTimeout(Duration.ofSeconds(1))).thenReturn(retryOptions);
        });

        clientBuilder =
                mockConstruction(ServiceBusClientBuilder.class, (mock, context) -> {
                    when(mock.fullyQualifiedNamespace(NAMESPACE_NAME + ".servicebus.windows.net")).thenReturn(mock);
                    when(mock.credential(tokenCredential)).thenReturn(mock);
                    when(mock.retryOptions(retryOptions)).thenReturn(mock);
                    when(mock.sender()).thenReturn(clientSenderBuilder);
                });

        serviceBusService = new ServiceBusService(tokenCredential);
    }

    @AfterEach
    void afterEach()
    {
        clientBuilder.close();
        amqpRetryOptions.close();
    }

    @Test
    void testSendQueue()
    {
        when(clientSenderBuilder.queueName(eq(QUEUE_NAME))).thenReturn(clientSenderBuilder);

        serviceBusService.send(ChannelType.QUEUE, QUEUE_NAME, NAMESPACE_NAME, MESSAGE);

        ArgumentCaptor<ServiceBusMessage> messageCaptor = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(client).sendMessage(messageCaptor.capture());
        assertEquals(MESSAGE, messageCaptor.getValue().getBody().toString());
        assertThat(logger.getLoggingEvents(), is(List.of(info(SUCCESS_LOG_MSG, QUEUE_NAME, NAMESPACE_NAME))));
    }

    @Test
    void testSendTopic()
    {
        when(clientSenderBuilder.topicName(eq(TOPIC_NAME))).thenReturn(clientSenderBuilder);

        serviceBusService.send(ChannelType.TOPIC, TOPIC_NAME, NAMESPACE_NAME, MESSAGE);

        ArgumentCaptor<ServiceBusMessage> messageCaptor = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(client).sendMessage(messageCaptor.capture());
        assertEquals(MESSAGE, messageCaptor.getValue().getBody().toString());
        assertThat(logger.getLoggingEvents(), is(List.of(info(SUCCESS_LOG_MSG, TOPIC_NAME, NAMESPACE_NAME))));
    }

    @Test
    void testException()
    {
        when(clientSenderBuilder.queueName(eq(QUEUE_NAME))).thenReturn(clientSenderBuilder);

        ServiceBusException sbe = new ServiceBusException(new RuntimeException("exception"),
                ServiceBusErrorSource.SEND);
        doThrow(sbe).when(client).sendMessage(any(ServiceBusMessage.class));

        ServiceBusSendMessageException actualException = assertThrows(ServiceBusSendMessageException.class,
                () -> serviceBusService.send(ChannelType.QUEUE, QUEUE_NAME, NAMESPACE_NAME, MESSAGE));
        assertEquals("Unable to send message to Azure Service Bus", actualException.getMessage());
    }
}
