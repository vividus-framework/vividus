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

package org.vividus.azure.servicebus;

import org.jbehave.core.annotations.When;
import org.vividus.azure.servicebus.model.ChannelType;
import org.vividus.azure.servicebus.service.ServiceBusService;

public class ServiceBusSteps
{
    private ServiceBusService serviceBusService;

    public ServiceBusSteps(ServiceBusService serviceBusService)
    {
        this.serviceBusService = serviceBusService;
    }

    /**
     * Send messages in <a href="https://learn.microsoft.com/en-us/azure/service-bus-messaging">Azure Service Bus</a>
     *
     * @param type              The type of service bus messaging components: either `QUEUE` or `TOPIC`.
     * @param name              The queue or topic name.
     * @param namespaceName     The name of the namespace the service bus belongs to.
     * @param payload           Message to send to the service bus.
     */
    @When("I send message to service bus $type with name `$name` in namespace `$namespaceName` and payload:`$payload`")
    public void sendMessageToServiceBus(ChannelType type, String name, String namespaceName, String payload)
    {
        serviceBusService.send(type, name, namespaceName, payload);
    }
}
