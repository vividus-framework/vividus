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

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.stream.Collectors;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;

import org.hamcrest.Matcher;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.vividus.azure.servicebus.service.ServiceBusService;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.util.wait.DurationBasedWaiter;
import org.vividus.variable.VariableScope;

public class ServiceBusSteps
{
    private ServiceBusService serviceBusService;
    private VariableContext variableContext;
    private ISoftAssert softAssert;

    public ServiceBusSteps(ServiceBusService serviceBusService, VariableContext variableContext, ISoftAssert softAssert)
    {
        this.serviceBusService = serviceBusService;
        this.variableContext = variableContext;
        this.softAssert = softAssert;
    }

    /**
     * Send message to <a href="https://learn.microsoft.com/en-us/azure/service-bus-messaging">Azure Service Bus</a>
     * <br>
     * <br>Service Bus <b>connection configuration</b> should be defined via properties with a following format:
     * <br><code>azure.service-bus.{service-bus-key}.{property-name}</code>
     * <br>Example:
     * <br><code>
     * <br>azure.service-bus.test-topic.channel-type=TOPIC
     * <br>azure.service-bus.test-topic.namespace=testNamespace
     * <br>azure.service-bus.test-topic.name=testTopicName
     * <br>azure.service-bus.test-topic.subscription-name=testSubscriptionName
     * </code>
     * <br>
     * <br> Where:
     * <ul>
     *     <li><b>channel-type</b> - the type of service bus messaging components: either `QUEUE` or `TOPIC`</li>
     *     <li><b>namespace</b> - the name of the namespace the service bus belongs to</li>
     *     <li><b>name</b> - the queue or topic name</li>
     *     <li><b>subscription-name</b> - the name of the topic subscription. Only for TOPIC channel-type</li>
     * </ul>
     * @param serviceBusKey The key associated with Azure Service Bus connection configuration.
     * @param payload       Message to send to the service bus.
     */
    @When("I send message to `$serviceBusKey` service bus with payload:`$payload`")
    public void sendMessageToServiceBus(String serviceBusKey, String payload)
    {
        serviceBusService.send(serviceBusKey, payload);
    }

    /**
     * Send message with custom properties to
     * <a href="https://learn.microsoft.com/en-us/azure/service-bus-messaging">Azure Service Bus</a>
     * <br>
     * <br>Service Bus <b>connection configuration</b> should be defined via properties with a following format:
     * <br><code>azure.service-bus.{service-bus-key}.{property-name}</code>
     * <br>Example:
     * <br><code>
     * <br>azure.service-bus.test-topic.channel-type=TOPIC
     * <br>azure.service-bus.test-topic.namespace=testNamespace
     * <br>azure.service-bus.test-topic.name=testTopicName
     * <br>azure.service-bus.test-topic.subscription-name=testSubscriptionName
     * </code>
     * <br>
     * <br> Where:
     * <ul>
     *     <li><b>channel-type</b> - the type of service bus messaging components: either `QUEUE` or `TOPIC`</li>
     *     <li><b>namespace</b> - the name of the namespace the service bus belongs to</li>
     *     <li><b>name</b> - the queue or topic name</li>
     *     <li><b>subscription-name</b> - the name of the topic subscription. Only for TOPIC channel-type</li>
     * </ul>
     * @param serviceBusKey    The key associated with Azure Service Bus connection configuration.
     * @param payload          Message to send to the service bus.
     * @param customProperties ExamplesTable representing the list of custom properties with columns "key", "type"
     *                         and "value":
     *                         <ul>
     *                              <li><b>key</b> - the property name</li>
     *                              <li><b>type</b> - the data type of the property (case-insensitive).
     *                                                Options include 'string', 'boolean', 'number'</li>
     *                              <li><b>value</b> - the property value</li>
     *                         </ul>
     *                         <p>Example:</p>
     *                         <code>
     *                         |key     |type   |value|<br>
     *                         |purpose |string |test |<br>
     *                         |flag    |boolean|true |<br>
     *                         |mode    |number |5    |<br>
     *                         </code>
     */
    @When("I send message to `$serviceBusKey` service bus with payload:`$payload` and custom properties:"
            + "$customProperties")
    public void sendMessageToServiceBus(String serviceBusKey, String payload, ExamplesTable customProperties)
    {
        Map<String, Object> properties = customProperties.getRowsAsParameters(true).stream()
                .collect(Collectors.toMap(row -> row.valueAs("key", String.class), row -> {
                    ApplicationPropertyType type = row.valueAs("type", ApplicationPropertyType.class);
                    return row.valueAs("value", type.getValueType());
                }));
        serviceBusService.send(serviceBusKey, payload, properties);
    }

    /**
     * Starts the <a href="https://learn.microsoft.com/en-us/azure/service-bus-messaging">Azure Service Bus</a>
     * consumer with the provided configuration to listen the specified topics or queues.
     * The consumer must be stopped when it's not needed.
     * <br>
     * <br>Service Bus <b>connection configuration</b> should be defined via properties with a following format:
     * <br><code>azure.service-bus.{service-bus-key}.{property-name}</code>
     * <br>Example:
     * <br><code>
     * <br>azure.service-bus.test-topic.channel-type=TOPIC
     * <br>azure.service-bus.test-topic.namespace=testNamespace
     * <br>azure.service-bus.test-topic.name=testTopicName
     * <br>azure.service-bus.test-topic.subscription-name=testSubscriptionName
     * </code>
     * <br>
     * <br> Where:
     * <ul>
     *     <li><b>channel-type</b> - the type of service bus messaging components: either `QUEUE` or `TOPIC`</li>
     *     <li><b>namespace</b> - the name of the namespace the service bus belongs to</li>
     *     <li><b>name</b> - the queue or topic name</li>
     *     <li><b>subscription-name</b> - the name of the topic subscription. Only for TOPIC channel-type</li>
     * </ul>
     * @param serviceBusKey The key associated with Azure Service Bus connection configuration.
     */
    @When("I start consuming messages from `$serviceBusKey` service bus")
    public void startConsumingMessagesFromServiceBus(String serviceBusKey)
    {
        serviceBusService.startConsuming(serviceBusKey);
    }

    /**
     * Stops the <a href="https://learn.microsoft.com/en-us/azure/service-bus-messaging">Azure Service Bus</a>
     * consumer started by the corresponding step before.
     * <br>All recorded messages are kept and can be drained into the variable using the step:
     * "When I $queueOperation consumed `$serviceBusKey` service bus messages to $scopes variable `$variableName`"
     * <br>
     * <br>Service Bus <b>connection configuration</b> should be defined via properties with a following format:
     * <br><code>azure.service-bus.{service-bus-key}.{property-name}</code>
     * <br>Example:
     * <br><code>
     * <br>azure.service-bus.test-topic.channel-type=TOPIC
     * <br>azure.service-bus.test-topic.namespace=testNamespace
     * <br>azure.service-bus.test-topic.name=testTopicName
     * <br>azure.service-bus.test-topic.subscription-name=testSubscriptionName
     * </code>
     * <br>
     * <br> Where:
     * <ul>
     *     <li><b>channel-type</b> - the type of service bus messaging components: either `QUEUE` or `TOPIC`</li>
     *     <li><b>namespace</b> - the name of the namespace the service bus belongs to</li>
     *     <li><b>name</b> - the queue or topic name</li>
     *     <li><b>subscription-name</b> - the name of the topic subscription. Only for TOPIC channel-type</li>
     * </ul>
     * @param serviceBusKey The key associated with Azure Service Bus connection configuration.
     */
    @When("I stop consuming messages from `$serviceBusKey` service bus")
    public void stopConsumingMessagesFromServiceBus(String serviceBusKey)
    {
        serviceBusService.stopConsuming(serviceBusKey);
    }

    /**
     * Waits until the count of the consumed messages (from the consumer start or after the last draining operation)
     * matches to the rule or until the timeout is exceeded.
     * <br>
     * <br>Service Bus <b>connection configuration</b> should be defined via properties with a following format:
     * <br><code>azure.service-bus.{service-bus-key}.{property-name}</code>
     * <br>Example:
     * <br><code>
     * <br>azure.service-bus.test-topic.channel-type=TOPIC
     * <br>azure.service-bus.test-topic.namespace=testNamespace
     * <br>azure.service-bus.test-topic.name=testTopicName
     * <br>azure.service-bus.test-topic.subscription-name=testSubscriptionName
     * </code>
     * <br>
     * <br> Where:
     * <ul>
     *     <li><b>channel-type</b> - the type of service bus messaging components: either `QUEUE` or `TOPIC`</li>
     *     <li><b>namespace</b> - the name of the namespace the service bus belongs to</li>
     *     <li><b>name</b> - the queue or topic name</li>
     *     <li><b>subscription-name</b> - the name of the topic subscription. Only for TOPIC channel-type</li>
     * </ul>
     * @param timeout        The maximum time to wait for the event in ISO-8601 format.
     * @param serviceBusKey  The key associated with Azure Service Bus connection configuration.
     * @param comparisonRule The rule to match the quantity of events. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param expectedCount  The expected count of the events to be matched by the rule.
     */
    @When("I wait with `$timeout` timeout until count of consumed `$serviceBusKey` service bus messages is"
            + " $comparisonRule `$expectedCount`")
    public void waitForServiceBusMessages(Duration timeout, String serviceBusKey, ComparisonRule comparisonRule,
                                          int expectedCount)
    {
        Matcher<Integer> countMatcher = comparisonRule.getComparisonRule(expectedCount);
        Integer result = new DurationBasedWaiter(timeout, Duration.ofSeconds(1)).wait(
                () -> serviceBusService.getMessagesForClient(serviceBusKey).size(), countMatcher::matches);
        softAssert.assertThat("Total count of messages for Service Bus with key: " + serviceBusKey,
                result, countMatcher);
    }

    /**
     * Drains/Peeks the consumed messages to the specified variable. If the consumer is not stopped, the new events
     * might arrive after the draining. If the consumer is stopped, all the events received from the consumer start or
     * after the last draining operation are stored to the variable.
     * @param operation      The operation under the consumed messages, one of: <br>
     *                       <ul>
     *                       <li><b>PEEK</b> - saves the messages consumed since the last drain or from the
     *                       consumption start and doesn't change the consumer cursor position
     *                       <li><b>DRAIN</b> - saves the messages consumed since the last drain or from the
     *                       consumption start and moves the consumer cursor
     *                       to the position after the last consumed message
     *                       </ul>
     * @param serviceBusKey  The key associated with Azure Service Bus connection configuration.
     * @param scopes         The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     *                       <i>Available scopes:</i>
     *                       <ul>
     *                       <li><b>STEP</b> - the variable will be available only within the step,
     *                       <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                       <li><b>STORY</b> - the variable will be available within the whole story,
     *                       <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                       </ul>
     * @param variableName   the variable name to store the events. The events are accessible via zero-based index,
     *                       e.g. `${my-var[0]}` will return the first received event.
     */
    @When("I $operation consumed `$serviceBusKey` service bus messages to $scopes variable `$variableName`")
    public void processServiceBusMessages(QueueOperation operation, String serviceBusKey,
                                          Set<VariableScope> scopes, String variableName)
    {
        List<ServiceBusReceivedMessage> messages = operation.performOn(serviceBusService
                .getMessagesForClient(serviceBusKey));
        List<String> messagesAsText = messages.stream().map(m -> m.getBody().toString()).toList();
        variableContext.putVariable(scopes, variableName, messagesAsText);
    }

    @AfterStory
    public void cleanUp()
    {
        serviceBusService.stopAll();
    }

    protected enum QueueOperation
    {
        PEEK
        {
            @Override
            List<ServiceBusReceivedMessage> performOn(BlockingQueue<ServiceBusReceivedMessage> messagesQueue)
            {
                return new ArrayList<>(messagesQueue);
            }
        },
        DRAIN
        {
            @Override
            List<ServiceBusReceivedMessage> performOn(BlockingQueue<ServiceBusReceivedMessage> eventsQueue)
            {
                List<ServiceBusReceivedMessage> events = new ArrayList<>();
                eventsQueue.drainTo(events);
                return events;
            }
        };

        abstract List<ServiceBusReceivedMessage> performOn(BlockingQueue<ServiceBusReceivedMessage> blockingQueue);
    }

    private enum ApplicationPropertyType
    {
        STRING(String.class),
        BOOLEAN(Boolean.class),
        NUMBER(Number.class);

        private final Class<?> valueType;

        ApplicationPropertyType(Class<?> valueType)
        {
            this.valueType = valueType;
        }

        public Class<?> getValueType()
        {
            return valueType;
        }
    }
}
