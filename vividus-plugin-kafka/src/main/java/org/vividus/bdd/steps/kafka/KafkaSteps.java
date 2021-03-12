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

package org.vividus.bdd.steps.kafka;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.hamcrest.Matcher;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.GenericMessageListenerContainer;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.softassert.SoftAssert;
import org.vividus.testcontext.TestContext;
import org.vividus.util.property.IPropertyParser;
import org.vividus.util.wait.DurationBasedWaiter;

public class KafkaSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSteps.class);

    private static final int WAIT_TIMEOUT_IN_MINUTES = 10;

    private static final Class<?> LISTENER_KEY = GenericMessageListenerContainer.class;
    private static final Class<?> MESSAGES_KEY = ConsumerRecord.class;

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final DefaultKafkaConsumerFactory<Object, Object> consumerFactory;

    private final TestContext testContext;
    private final IBddVariableContext bddVariableContext;
    private final SoftAssert softAssert;

    public KafkaSteps(IPropertyParser propertyParser, TestContext testContext, IBddVariableContext bddVariableContext,
            SoftAssert softAssert)
    {
        Map<String, Object> producerConfig = new HashMap<>();
        producerConfig.putAll(propertyParser.getPropertyValuesByPrefix("kafka.producer."));
        producerConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        DefaultKafkaProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(producerConfig);
        this.kafkaTemplate = new KafkaTemplate<>(producerFactory);

        Map<String, Object> consumerConfig = new HashMap<>();
        consumerConfig.putAll(propertyParser.getPropertyValuesByPrefix("kafka.consumer."));
        consumerConfig.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        this.consumerFactory = new DefaultKafkaConsumerFactory<>(consumerConfig);

        this.testContext = testContext;
        this.bddVariableContext = bddVariableContext;
        this.softAssert = softAssert;
    }

    /**
     * Send the data to the provided topic with no key or partition.
     * @param data the data to send
     * @param topic the topic name
     * @throws InterruptedException if the current thread was interrupted while waiting
     * @throws ExecutionException if the computation threw an exception
     * @throws TimeoutException if the wait timed out
     */
    @When("I send data `$data` to Kafka topic `$topic`")
    public void sendData(String data, String topic) throws InterruptedException, ExecutionException, TimeoutException
    {
        kafkaTemplate.send(topic, data).get(WAIT_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * Starts the Kafka consumer with the provided configuration to listen the specified topics. The consumer must be
     * stopped when it's not needed.
     *
     * @param topics the comma-separated set of topics to listen
     */
    @When("I start consuming messages from Kafka topics `$topics`")
    public void startKafkaListener(Set<String> topics)
    {
        stopListener(false);
        BlockingQueue<String> messageQueue = new LinkedBlockingDeque<>();
        testContext.put(MESSAGES_KEY, messageQueue);
        ContainerProperties containerProperties = new ContainerProperties(topics.toArray(new String[0]));
        containerProperties.setMessageListener(
                (MessageListener<String, String>) data -> messageQueue.add(data.value()));
        GenericMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(consumerFactory,
                containerProperties);
        container.start();
        testContext.put(LISTENER_KEY, container);
        LOGGER.info("Kafka message listener is started");
    }

    /**
     * Waits until the count of the consumed messaged (from the consumer start or after the last draining operation)
     * matches to the rule or until the timeout is exceeded.
     *
     * @param timeout        The maximum time to wait for the messages in ISO-8601 format
     * @param comparisonRule The rule to match the quantity of messages. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param expectedCount  The expected count of the messages to be matched by the rule
     */
    @When("I wait with `$timeout` timeout until count of consumed Kafka messages is $comparisonRule `$expectedCount`")
    public void waitForKafkaMessages(Duration timeout, ComparisonRule comparisonRule, int expectedCount)
    {
        Matcher<Integer> countMatcher = comparisonRule.getComparisonRule(expectedCount);
        Integer result = new DurationBasedWaiter(timeout, Duration.ofSeconds(1)).wait(
                () -> testContext.<BlockingQueue<String>>get(MESSAGES_KEY).size(), countMatcher::matches);
        softAssert.assertThat("Total count of consumed Kafka messages", result, countMatcher);
    }

    /**
     * Stops the Kafka consumer started by the corresponding step before. All recorded messages are kept and can be
     * drained into the variable using the step described above.
     */
    @When("I stop consuming messages from Kafka")
    public void stopKafkaListener()
    {
        stopListener(true);
    }

    /**
     * Drains the consumed messaged to the specified variable. If the consumer is not stopped, the new messages might
     * arrive after the draining. If the consumer is stopped, all the messages received from the consumer start or
     * after the last draining operation are stored to the variable.
     *
     * @param scopes       The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variable's scope<br>
     *                     <i>Available scopes:</i>
     *                     <ul>
     *                     <li><b>STEP</b> - the variable will be available only within the step,
     *                     <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                     <li><b>STORY</b> - the variable will be available within the whole story,
     *                     <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                     </ul>
     * @param variableName the variable name to store the messages. The messages are accessible via zero-based index,
     *                     e.g. `${my-var[0]}` will return the first received message.
     */
    @When("I drain consumed Kafka messages to $scopes variable `$variableName`")
    public void drainKafkaMessagesToVariable(Set<VariableScope> scopes, String variableName)
    {
        BlockingQueue<String> messagesQueue = testContext.get(MESSAGES_KEY);
        List<String> messages = new ArrayList<>();
        messagesQueue.drainTo(messages);
        bddVariableContext.putVariable(scopes, variableName, messages);
    }

    @AfterStory
    public void cleanUp()
    {
        stopListener(false);
    }

    private void stopListener(boolean throwExceptionIfNoListener)
    {
        GenericMessageListenerContainer<String, String> container = testContext.get(LISTENER_KEY);
        if (container != null)
        {
            container.stop();
            LOGGER.info("Kafka message listener is stopped");
            testContext.remove(LISTENER_KEY);
        }
        else if (throwExceptionIfNoListener)
        {
            throw new IllegalStateException(
                    "No Kafka message listener is running, did you forget to start consuming messages?");
        }
    }
}
