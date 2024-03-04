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

import com.azure.core.amqp.AmqpRetryOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusException;
import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.vividus.azure.servicebus.ServiceBusSendMessageException;
import org.vividus.azure.servicebus.model.ChannelType;

public class ServiceBusService
{
    private static final Logger LOGGER = LoggerFactory.getLogger(ServiceBusService.class);

    private static final int CLIENT_MAX_RETRIES = 1;
    private static final Duration CLIENT_TRY_TIMEOUT = Duration.ofSeconds(1);

    private ServiceBusClientBuilder clientBuilder;

    public ServiceBusService(TokenCredential tokenCredential)
    {
        this.clientBuilder = new ServiceBusClientBuilder()
                .credential(tokenCredential)
                .retryOptions(new AmqpRetryOptions().setMaxRetries(CLIENT_MAX_RETRIES)
                        .setTryTimeout(CLIENT_TRY_TIMEOUT));
    }

    public void send(ChannelType type, String name, String namespaceName, String message)
    {
        ServiceBusClientBuilder.ServiceBusSenderClientBuilder senderBuilder = clientBuilder
                .fullyQualifiedNamespace(namespaceName + ".servicebus.windows.net")
                .sender();
        if (type == ChannelType.QUEUE)
        {
            senderBuilder.queueName(name);
        }
        else
        {
            senderBuilder.topicName(name);
        }
        try (ServiceBusSenderClient client = senderBuilder.buildClient())
        {
            client.sendMessage(new ServiceBusMessage(message));
            LOGGER.info("The message was successfully sent to {} in {} namespace", name, namespaceName);
        }
        catch (ServiceBusException e)
        {
            throw new ServiceBusSendMessageException("Unable to send message to Azure Service Bus", e);
        }
    }
}
