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
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.empty;
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

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.AzureException;
import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusErrorContext;
import com.azure.messaging.servicebus.ServiceBusErrorSource;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceivedMessageContext;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.servicebus.ServiceBusSendMessageException;
import org.vividus.azure.servicebus.model.ChannelType;
import org.vividus.azure.servicebus.model.ServiceBusConnectionParameters;
import org.vividus.testcontext.TestContext;
import org.vividus.util.property.PropertyMappedCollection;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
public class ServiceBusServiceTests
{
    private static final String CLIENT_KEY = "clientKey";
    private static final String NAMESPACE_NAME = "namespaceName";
    private static final String QUEUE_NAME = "queueName";
    private static final String MESSAGE = "msg";
    private static final String TOPIC_NAME = "topicName";
    private static final String SUBSCRIPTION_NAME = "subName";
    private static final String SUCCESS_LOG_MSG = "The message was successfully sent to {} in {} namespace";
    private static final String GET_CLIENT_CONFIG_ERROR = "No Service Bus connection with key `%s` is configured";
    private static final String CLIENTS_FIELD_NAME = "clients";
    private static final String CONSUMER_STARTED_LOG_MESSAGE = "'{}' Azure Service Bus consumer of messages from the {}"
            + " '{}' in namespace {} is started";
    private static final String PROCESSING_MESSAGE_LOG_MESSAGE = "Processing message. Session: {}, Sequence #: {}."
            + " Contents: {}";
    private static final String CONSUMER_IS_STOPPED_LOG_MSG = "'{}' Azure Service Bus consumer is stopped";
    private static final Class<?> MESSAGES_KEY = ServiceBusReceivedMessage.class;
    private static final Map<String, Object> CUSTOM_PROPERTIES = Map.of("key", "value");

    private final TestLogger logger = TestLoggerFactory.getTestLogger(ServiceBusService.class);

    @Mock
    private TokenCredential tokenCredential;
    @Mock
    private TestContext testContext;
    @Mock
    private PropertyMappedCollection<ServiceBusConnectionParameters> clientConfigs;
    @Mock
    private ServiceBusSenderClient senderClient;
    @Mock
    private ServiceBusProcessorClient processorClient;
    @Mock
    private ServiceBusClientBuilder.ServiceBusSenderClientBuilder clientSenderBuilder;
    @Mock
    private ServiceBusClientBuilder.ServiceBusProcessorClientBuilder clientProcessorBuilder;

    private MockedConstruction<AmqpRetryOptions> amqpRetryOptions;
    private MockedConstruction<ServiceBusClientBuilder> clientBuilder;
    private ServiceBusService serviceBusService;

    private ServiceBusConnectionParameters queueClient = createDefaultClient(ChannelType.QUEUE);
    private ServiceBusConnectionParameters topicClient = createDefaultClient(ChannelType.TOPIC);

    static Stream<Arguments> messageSendingProvider()
    {
        return Stream.of(
                Arguments.of((Consumer<ServiceBusService>) s -> s.send(CLIENT_KEY, MESSAGE), Map.of()),
                Arguments.of((Consumer<ServiceBusService>) s -> s.send(CLIENT_KEY, MESSAGE, CUSTOM_PROPERTIES),
                        CUSTOM_PROPERTIES));
    }

    @AfterEach
    void afterEach()
    {
        Optional.ofNullable(clientBuilder).ifPresent(MockedConstruction::close);
        Optional.ofNullable(amqpRetryOptions).ifPresent(MockedConstruction::close);
    }

    @ParameterizedTest
    @MethodSource("messageSendingProvider")
    void testSendQueue(Consumer<ServiceBusService> action, Map<String, Object> expectedApplicationProperties)
    {
        mockConstructorForServiceBusClient(ClientType.SENDER);
        mockClientEntity(ChannelType.QUEUE);
        when(clientSenderBuilder.queueName(eq(QUEUE_NAME))).thenReturn(clientSenderBuilder);
        when(clientSenderBuilder.buildClient()).thenReturn(senderClient);

        action.accept(serviceBusService);

        ArgumentCaptor<ServiceBusMessage> messageCaptor = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(senderClient).sendMessage(messageCaptor.capture());
        assertEquals(MESSAGE, messageCaptor.getValue().getBody().toString());
        assertEquals(expectedApplicationProperties, messageCaptor.getValue().getApplicationProperties());
        assertThat(logger.getLoggingEvents(), is(List.of(info(SUCCESS_LOG_MSG, QUEUE_NAME, NAMESPACE_NAME))));
    }

    @ParameterizedTest
    @MethodSource("messageSendingProvider")
    void testSendTopic(Consumer<ServiceBusService> action, Map<String, Object> expectedApplicationProperties)
    {
        mockConstructorForServiceBusClient(ClientType.SENDER);
        mockClientEntity(ChannelType.TOPIC);
        when(clientSenderBuilder.topicName(eq(TOPIC_NAME))).thenReturn(clientSenderBuilder);
        when(clientSenderBuilder.buildClient()).thenReturn(senderClient);

        action.accept(serviceBusService);

        ArgumentCaptor<ServiceBusMessage> messageCaptor = ArgumentCaptor.forClass(ServiceBusMessage.class);
        verify(senderClient).sendMessage(messageCaptor.capture());
        assertEquals(MESSAGE, messageCaptor.getValue().getBody().toString());
        assertEquals(expectedApplicationProperties, messageCaptor.getValue().getApplicationProperties());
        assertThat(logger.getLoggingEvents(), is(List.of(info(SUCCESS_LOG_MSG, TOPIC_NAME, NAMESPACE_NAME))));
    }

    @Test
    void testException()
    {
        mockConstructorForServiceBusClient(ClientType.SENDER);
        mockClientEntity(ChannelType.QUEUE);
        when(clientSenderBuilder.queueName(eq(QUEUE_NAME))).thenReturn(clientSenderBuilder);
        when(clientSenderBuilder.buildClient()).thenReturn(senderClient);

        ServiceBusException sbe = new ServiceBusException(new RuntimeException("exception"),
                ServiceBusErrorSource.SEND);
        doThrow(sbe).when(senderClient).sendMessage(any(ServiceBusMessage.class));

        ServiceBusSendMessageException actualException = assertThrows(ServiceBusSendMessageException.class,
                () -> serviceBusService.send(CLIENT_KEY, MESSAGE));
        assertEquals("Unable to send message to Azure Service Bus", actualException.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testStartConsumingQueue() throws NoSuchFieldException, IllegalAccessException
    {
        HashMap<String, BlockingQueue<ServiceBusReceivedMessage>> messagesMap = new HashMap<>();
        when(testContext.get(eq(ServiceBusReceivedMessage.class), any(Supplier.class))).thenReturn(messagesMap);
        mockConstructorForServiceBusClient(ClientType.PROCESSOR);
        mockClientEntity(ChannelType.QUEUE);

        when(clientProcessorBuilder.queueName(eq(QUEUE_NAME))).thenReturn(clientProcessorBuilder);
        when(clientProcessorBuilder.processError(any(Consumer.class))).thenReturn(clientProcessorBuilder);
        when(clientProcessorBuilder.processMessage(any(Consumer.class))).thenReturn(clientProcessorBuilder);
        when(clientProcessorBuilder.buildProcessorClient()).thenReturn(processorClient);

        serviceBusService.startConsuming(CLIENT_KEY);

        // verify error processing consumer
        ArgumentCaptor<Consumer<ServiceBusErrorContext>> errorCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(clientProcessorBuilder).processError(errorCaptor.capture());
        ServiceBusErrorContext serviceBusErrorContext = mock(ServiceBusErrorContext.class);
        when(serviceBusErrorContext.getException()).thenReturn(new ServiceBusException(new AzureException(),
                ServiceBusErrorSource.ABANDON));
        Consumer<ServiceBusErrorContext> errorProcessingConsumer = errorCaptor.getValue();
        assertThrows(ServiceBusException.class, () -> errorProcessingConsumer.accept(serviceBusErrorContext));

        // verify messages processing consumer
        ArgumentCaptor<Consumer<ServiceBusReceivedMessageContext>> messageCaptor =
                ArgumentCaptor.forClass(Consumer.class);
        verify(clientProcessorBuilder).processMessage(messageCaptor.capture());
        ServiceBusReceivedMessageContext messageContext = mock(ServiceBusReceivedMessageContext.class);
        ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);
        when(messageContext.getMessage()).thenReturn(receivedMessage);
        BinaryData messageBinaryData = mock(BinaryData.class);
        when(receivedMessage.getBody()).thenReturn(messageBinaryData);
        String sessionId = "sessionId";
        when(receivedMessage.getMessageId()).thenReturn(sessionId);
        long sequenceNumber = 1;
        when(receivedMessage.getSequenceNumber()).thenReturn(sequenceNumber);
        messageCaptor.getValue().accept(messageContext);
        assertEquals(1, messagesMap.size());
        BlockingQueue<ServiceBusReceivedMessage> serviceBusReceivedMessages = messagesMap.get(CLIENT_KEY);
        assertEquals(1, serviceBusReceivedMessages.size());
        assertEquals(messageBinaryData, serviceBusReceivedMessages.peek().getBody());

        //verify new client was added to the clients list
        Map<String, ServiceBusProcessorClient> clients = getActiveServiceBusClients();
        assertEquals(1, clients.size());
        assertEquals(processorClient, clients.get(CLIENT_KEY));

        verify(processorClient).start();
        assertThat(logger.getLoggingEvents(), is(List.of(info(
                        CONSUMER_STARTED_LOG_MESSAGE,
                        CLIENT_KEY, ChannelType.QUEUE, QUEUE_NAME, NAMESPACE_NAME),
                info(PROCESSING_MESSAGE_LOG_MESSAGE,
                        sessionId, sequenceNumber, messageBinaryData))));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testStartConsumingTopic() throws NoSuchFieldException, IllegalAccessException
    {
        HashMap<String, BlockingQueue<ServiceBusReceivedMessage>> messagesMap = new HashMap<>();
        when(testContext.get(eq(ServiceBusReceivedMessage.class), any(Supplier.class))).thenReturn(messagesMap);
        mockConstructorForServiceBusClient(ClientType.PROCESSOR);
        mockClientEntity(ChannelType.TOPIC);

        when(clientProcessorBuilder.topicName(eq(TOPIC_NAME))).thenReturn(clientProcessorBuilder);
        when(clientProcessorBuilder.subscriptionName(eq(SUBSCRIPTION_NAME))).thenReturn(clientProcessorBuilder);
        when(clientProcessorBuilder.processError(any(Consumer.class))).thenReturn(clientProcessorBuilder);
        when(clientProcessorBuilder.processMessage(any(Consumer.class))).thenReturn(clientProcessorBuilder);
        when(clientProcessorBuilder.buildProcessorClient()).thenReturn(processorClient);

        serviceBusService.startConsuming(CLIENT_KEY);

        // verify error processing consumer
        ArgumentCaptor<Consumer<ServiceBusErrorContext>> errorCaptor = ArgumentCaptor.forClass(Consumer.class);
        verify(clientProcessorBuilder).processError(errorCaptor.capture());
        ServiceBusErrorContext serviceBusErrorContext = mock(ServiceBusErrorContext.class);
        when(serviceBusErrorContext.getException()).thenReturn(new ServiceBusException(new AzureException(),
                ServiceBusErrorSource.ABANDON));
        Consumer<ServiceBusErrorContext> errorProcessingConsumer = errorCaptor.getValue();
        assertThrows(ServiceBusException.class, () -> errorProcessingConsumer.accept(serviceBusErrorContext));

        // verify messages processing consumer
        ArgumentCaptor<Consumer<ServiceBusReceivedMessageContext>> messageCaptor =
                ArgumentCaptor.forClass(Consumer.class);
        verify(clientProcessorBuilder).processMessage(messageCaptor.capture());
        ServiceBusReceivedMessageContext messageContext = mock(ServiceBusReceivedMessageContext.class);
        ServiceBusReceivedMessage receivedMessage = mock(ServiceBusReceivedMessage.class);
        when(messageContext.getMessage()).thenReturn(receivedMessage);
        BinaryData messageBinaryData = mock(BinaryData.class);
        when(receivedMessage.getBody()).thenReturn(messageBinaryData);
        String sessionId = "sessionId2";
        when(receivedMessage.getMessageId()).thenReturn(sessionId);
        long sequenceNumber = 1;
        when(receivedMessage.getSequenceNumber()).thenReturn(sequenceNumber);
        messageCaptor.getValue().accept(messageContext);
        assertEquals(1, messagesMap.size());
        BlockingQueue<ServiceBusReceivedMessage> serviceBusReceivedMessages = messagesMap.get(CLIENT_KEY);
        assertEquals(1, serviceBusReceivedMessages.size());
        assertEquals(messageBinaryData, serviceBusReceivedMessages.peek().getBody());

        //verify new client was added to the clients list
        Map<String, ServiceBusProcessorClient> clients = getActiveServiceBusClients();
        assertEquals(1, clients.size());
        assertEquals(processorClient, clients.get(CLIENT_KEY));

        verify(processorClient).start();
        assertThat(logger.getLoggingEvents(), is(List.of(info(CONSUMER_STARTED_LOG_MESSAGE, CLIENT_KEY,
                        ChannelType.TOPIC, TOPIC_NAME, NAMESPACE_NAME),
                info(PROCESSING_MESSAGE_LOG_MESSAGE, sessionId, sequenceNumber, messageBinaryData))));
    }

    @Test
    void testStartConsumingDuplicateKey() throws NoSuchFieldException, IllegalAccessException
    {
        serviceBusService = new ServiceBusService(tokenCredential, testContext, clientConfigs);
        Map<String, ServiceBusProcessorClient> clients = getActiveServiceBusClients();
        clients.put(CLIENT_KEY, processorClient);

        IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class,
                () -> serviceBusService.startConsuming(CLIENT_KEY));
        assertEquals("Azure Service Bus consumer associated with the key '" + CLIENT_KEY
                        + "' is already running", actualException.getMessage());
    }

    @Test
    void testStopConsuming() throws NoSuchFieldException, IllegalAccessException
    {
        serviceBusService = new ServiceBusService(tokenCredential, testContext, clientConfigs);

        Map<String, ServiceBusProcessorClient> clients = getActiveServiceBusClients();
        clients.put(CLIENT_KEY, processorClient);

        serviceBusService.stopConsuming(CLIENT_KEY);
        verify(processorClient).close();
        assertThat(logger.getLoggingEvents(), is(List.of(info(CONSUMER_IS_STOPPED_LOG_MSG, CLIENT_KEY))));
    }

    @Test
    void testStopConsumingNoClientError()
    {
        serviceBusService = new ServiceBusService(tokenCredential, testContext, clientConfigs);

        String clientId = "noClient";
        IllegalArgumentException actualException = assertThrows(IllegalArgumentException.class,
                () -> serviceBusService.stopConsuming(clientId));
        assertEquals("There are no running Azure Service Bus consumers associated with the key:" + clientId,
                actualException.getMessage());
    }

    @Test
    void testStopAllClients() throws NoSuchFieldException, IllegalAccessException
    {
        serviceBusService = new ServiceBusService(tokenCredential, testContext, clientConfigs);

        Map<String, ServiceBusProcessorClient> clients = getActiveServiceBusClients();

        String clientKey1 = "clientA";
        ServiceBusProcessorClient client1 = mock(ServiceBusProcessorClient.class);
        clients.put(clientKey1, client1);
        String clientKey2 = "clientB";
        ServiceBusProcessorClient client2 = mock(ServiceBusProcessorClient.class);
        clients.put(clientKey2, client2);

        serviceBusService.stopAll();
        verify(client1).close();
        verify(client2).close();
        assertThat(logger.getLoggingEvents(), containsInAnyOrder(info(CONSUMER_IS_STOPPED_LOG_MSG, clientKey1),
                info(CONSUMER_IS_STOPPED_LOG_MSG, clientKey2)));
    }

    @Test
    void testGetMessagesForClient()
    {
        serviceBusService = new ServiceBusService(tokenCredential, testContext, clientConfigs);

        BlockingQueue<ServiceBusReceivedMessage> receivedMessages = initSimpleTestContextWithOneMessage();

        BlockingQueue<ServiceBusReceivedMessage> actualReceivedMessages = serviceBusService
                .getMessagesForClient(CLIENT_KEY);
        assertEquals(actualReceivedMessages, receivedMessages);
    }

    @Test
    void testGetMessagesForClientNoMessages()
    {
        serviceBusService = new ServiceBusService(tokenCredential, testContext, clientConfigs);

        initSimpleTestContextWithOneMessage();

        BlockingQueue<ServiceBusReceivedMessage> actualReceivedMessages = serviceBusService
                .getMessagesForClient("client2");
        assertThat(actualReceivedMessages, empty());
    }

    private BlockingQueue<ServiceBusReceivedMessage> initSimpleTestContextWithOneMessage()
    {
        HashMap<String, BlockingQueue<ServiceBusReceivedMessage>> messagesMap = new HashMap<>();
        ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        BlockingQueue<ServiceBusReceivedMessage> receivedMessages = new LinkedBlockingDeque<>();
        receivedMessages.add(message);
        messagesMap.put(CLIENT_KEY, receivedMessages);
        when(testContext.get(MESSAGES_KEY)).thenReturn(messagesMap);
        return receivedMessages;
    }

    @SuppressWarnings("unchecked")
    private Map<String, ServiceBusProcessorClient> getActiveServiceBusClients() throws NoSuchFieldException,
            IllegalAccessException
    {
        Field clientsField = ServiceBusService.class.getDeclaredField(CLIENTS_FIELD_NAME);
        clientsField.setAccessible(true);
        return (Map<String, ServiceBusProcessorClient>) clientsField.get(serviceBusService);
    }

    private ServiceBusConnectionParameters createDefaultClient(ChannelType channelType)
    {
        ServiceBusConnectionParameters clientEntity = new ServiceBusConnectionParameters();
        clientEntity.setNamespace(NAMESPACE_NAME);
        clientEntity.setChannelType(channelType);
        if (channelType == ChannelType.QUEUE)
        {
            clientEntity.setName(QUEUE_NAME);
        }
        else
        {
            clientEntity.setName(TOPIC_NAME);
            clientEntity.setSubscriptionName(SUBSCRIPTION_NAME);
        }
        return clientEntity;
    }

    private void mockClientEntity(ChannelType channelType)
    {
        if (channelType == ChannelType.QUEUE)
        {
            when(clientConfigs.get(CLIENT_KEY, GET_CLIENT_CONFIG_ERROR, CLIENT_KEY)).thenReturn(queueClient);
        }
        else
        {
            when(clientConfigs.get(CLIENT_KEY, GET_CLIENT_CONFIG_ERROR, CLIENT_KEY)).thenReturn(topicClient);
        }
    }

    private void mockConstructorForServiceBusClient(ClientType clientType)
    {
        AmqpRetryOptions retryOptions = mock(AmqpRetryOptions.class);
        amqpRetryOptions = mockConstruction(AmqpRetryOptions.class, (mock, context) ->
        {
            when(mock.setMaxRetries(1)).thenReturn(mock);
            when(mock.setTryTimeout(Duration.ofSeconds(10))).thenReturn(retryOptions);
        });

        clientBuilder =
                mockConstruction(ServiceBusClientBuilder.class, (mock, context) ->
                {
                    when(mock.fullyQualifiedNamespace(NAMESPACE_NAME + ".servicebus.windows.net")).thenReturn(mock);
                    when(mock.credential(tokenCredential)).thenReturn(mock);
                    when(mock.retryOptions(retryOptions)).thenReturn(mock);
                    if (clientType == ClientType.SENDER)
                    {
                        when(mock.sender()).thenReturn(clientSenderBuilder);
                    }
                    else
                    {
                        when(mock.processor()).thenReturn(clientProcessorBuilder);
                    }
                });
        serviceBusService = new ServiceBusService(tokenCredential, testContext, clientConfigs);
    }

    protected enum ClientType
    {
        PROCESSOR, SENDER;
    }
}
