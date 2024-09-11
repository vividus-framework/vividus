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

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusProcessorClient;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.azure.servicebus.ServiceBusSendMessageException;
import org.vividus.azure.servicebus.model.ChannelType;
import org.vividus.azure.servicebus.model.ServiceBusConnectionParameters;
import org.vividus.testcontext.TestContext;
import org.vividus.util.property.PropertyMappedCollection;

public class ServiceBusService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusService.class);

    private static final int CLIENT_MAX_RETRIES = 1;
    private static final Duration CLIENT_TRY_TIMEOUT = Duration.ofSeconds(10);
    private static final Class<?> MESSAGES_KEY = ServiceBusReceivedMessage.class;
    private static final String DEFAULT_NAMESPACE_POSTFIX = ".servicebus.windows.net";

    private ServiceBusClientBuilder clientBuilder;
    private Map<String, ServiceBusProcessorClient> clients = new HashMap<>();
    private TestContext testContext;
    private PropertyMappedCollection<ServiceBusConnectionParameters> clientConfigs;

    public ServiceBusService(TokenCredential tokenCredential, TestContext testContext,
                             PropertyMappedCollection<ServiceBusConnectionParameters> clientConfigs)
    {
        this.clientBuilder = new ServiceBusClientBuilder()
                .credential(tokenCredential)
                .retryOptions(new AmqpRetryOptions().setMaxRetries(CLIENT_MAX_RETRIES)
                        .setTryTimeout(CLIENT_TRY_TIMEOUT));
        this.testContext = testContext;
        this.clientConfigs = clientConfigs;
    }

    public void send(String clientKey, String message)
    {
        send(clientKey, message, Map.of());
    }

    public void send(String clientKey, String message, Map<String, Object> customProperties)
    {
        ServiceBusConnectionParameters serviceBusConnectionParameters = getServiceBusClientConfig(clientKey);

        String namespace = serviceBusConnectionParameters.getNamespace();
        ServiceBusClientBuilder.ServiceBusSenderClientBuilder senderBuilder = clientBuilder
                .fullyQualifiedNamespace(namespace + DEFAULT_NAMESPACE_POSTFIX)
                .sender();

        ChannelType channelType = serviceBusConnectionParameters.getChannelType();
        String name = serviceBusConnectionParameters.getName();
        if (channelType == ChannelType.QUEUE)
        {
            senderBuilder.queueName(name);
        }
        else
        {
            senderBuilder.topicName(name);
        }

        try (ServiceBusSenderClient client = senderBuilder.buildClient())
        {
            ServiceBusMessage busMessage = new ServiceBusMessage(message);
            busMessage.getApplicationProperties().putAll(customProperties);
            client.sendMessage(busMessage);
            LOGGER.info("The message was successfully sent to {} in {} namespace", name, namespace);
        }
        catch (ServiceBusException e)
        {
            throw new ServiceBusSendMessageException("Unable to send message to Azure Service Bus", e);
        }
    }

    public void startConsuming(String clientKey)
    {
        if (null != clients.get(clientKey))
        {
            throw new IllegalArgumentException("Azure Service Bus consumer associated with the key '" + clientKey
                    + "' is already running");
        }
        BlockingQueue<ServiceBusReceivedMessage> messages = new LinkedBlockingDeque<>();
        testContext.get(MESSAGES_KEY, HashMap::new).put(clientKey, messages);
        ServiceBusConnectionParameters serviceBusConnectionParameters = getServiceBusClientConfig(clientKey);

        String namespace = serviceBusConnectionParameters.getNamespace();
        ServiceBusClientBuilder.ServiceBusProcessorClientBuilder processorBuilder = clientBuilder
                .fullyQualifiedNamespace(namespace + DEFAULT_NAMESPACE_POSTFIX)
                .processor()
                .processMessage(context -> {
                    ServiceBusReceivedMessage message = context.getMessage();
                    BinaryData messageBody = message.getBody();
                    LOGGER.info("Processing message. Session: {}, Sequence #: {}. Contents: {}", message.getMessageId(),
                            message.getSequenceNumber(), messageBody);
                    messages.add(message);
                })
                .processError(context ->
                {
                    throw (ServiceBusException) context.getException();
                });
        ChannelType channelType = serviceBusConnectionParameters.getChannelType();
        String name = serviceBusConnectionParameters.getName();
        if (channelType == ChannelType.QUEUE)
        {
            processorBuilder.queueName(name);
        }
        else
        {
            processorBuilder.topicName(name);
            processorBuilder.subscriptionName(serviceBusConnectionParameters.getSubscriptionName());
        }
        ServiceBusProcessorClient client = processorBuilder.buildProcessorClient();

        clients.put(clientKey, client);
        LOGGER.info("'{}' Azure Service Bus consumer of messages from the {} '{}' in namespace {} is started",
                clientKey, channelType, name, namespace);
        client.start();
    }

    private ServiceBusConnectionParameters getServiceBusClientConfig(String clientKey)
    {
        return clientConfigs.get(clientKey, "No Service Bus connection with key `%s` is configured", clientKey);
    }

    public void stopConsuming(String clientKey)
    {
        ServiceBusProcessorClient client = clients.remove(clientKey);
        if (client != null)
        {
            LOGGER.info("'{}' Azure Service Bus consumer is stopped", clientKey);
            client.close();
        }
        else
        {
            throw new IllegalArgumentException(
                    "There are no running Azure Service Bus consumers associated with the key:" + clientKey);
        }
    }

    public void stopAll()
    {
        clients.keySet().stream().toList().forEach(this::stopConsuming);
    }

    public BlockingQueue<ServiceBusReceivedMessage> getMessagesForClient(String clientKey)
    {
        return testContext.<Map<String, BlockingQueue<ServiceBusReceivedMessage>>>get(MESSAGES_KEY)
                .getOrDefault(clientKey, new LinkedBlockingDeque<>());
    }
}
