/*
 * Copyright 2019-2025 the original author or authors.
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

package org.vividus.steps.kafka;

import static java.util.Map.entry;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

import java.nio.charset.StandardCharsets;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.hamcrest.Matcher;
import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.GenericMessageListenerContainer;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.testcontext.TestContext;
import org.vividus.util.property.IPropertyParser;
import org.vividus.util.wait.DurationBasedWaiter;
import org.vividus.variable.VariableScope;

public class KafkaSteps
{
    private static final String DOT = ".";
    private static final String NAME = "name";
    private static final String VALUE = "value";

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaSteps.class);

    private static final int WAIT_TIMEOUT_IN_MINUTES = 10;

    private static final Class<?> LISTENER_KEY = GenericMessageListenerContainer.class;
    private static final Class<?> EVENTS_KEY = ConsumerRecord.class;
    private static final Class<?> HEADERS_KEY = Header.class;

    private final Map<String, KafkaTemplate<String, String>> kafkaTemplates;
    private final Map<String, DefaultKafkaConsumerFactory<Object, Object>> consumerFactories;

    private final TestContext testContext;
    private final VariableContext variableContext;
    private final ISoftAssert softAssert;

    public KafkaSteps(IPropertyParser propertyParser, TestContext testContext, VariableContext variableContext,
            ISoftAssert softAssert)
    {
        this.kafkaTemplates = convert("kafka.producer.", propertyParser, config -> {
            config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            DefaultKafkaProducerFactory<String, String> producerFactory =
                new DefaultKafkaProducerFactory<>(config);
            return new KafkaTemplate<>(producerFactory);
        });
        this.consumerFactories = convert("kafka.consumer.", propertyParser, config -> {
            config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
            return new DefaultKafkaConsumerFactory<>(config);
        });
        this.testContext = testContext;
        this.variableContext = variableContext;
        this.softAssert = softAssert;
    }

    private <T> Map<String, T> convert(String propertiesPrefix, IPropertyParser propertyParser,
            Function<Map<String, Object>, T> factoryCreator)
    {
        Map<String, Object> properties = new HashMap<>(propertyParser.getPropertyValuesByPrefix(propertiesPrefix));
        return properties.entrySet()
                         .stream()
                         .collect(groupingBy(e -> substringBefore(e.getKey(), DOT),
                                      mapping(e -> entry(substringAfter(e.getKey(), DOT), e.getValue()),
                                          collectingAndThen(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue),
                                          factoryCreator::apply))));
    }

    /**
     * Set Kafka event headers
     * @param headers ExamplesTable representing list of headers with columns "name" and "value" specifying kafka header
     * names and values respectively
     */
    @When("I set Kafka event headers:$headers")
    public void setEventHeaders(ExamplesTable headers)
    {
        List<Header> kafkaHeaders = headers.getRowsAsParameters(true).stream()
                .map(row ->
                {
                    String name = row.valueAs(NAME, String.class);
                    String value = row.valueAs(VALUE, String.class);
                    return new RecordHeader(name, value.getBytes(StandardCharsets.UTF_8));
                })
                .collect(toList());
        testContext.put(HEADERS_KEY, kafkaHeaders);
    }

    /**
     * Send the event with the specified value to the provided topic with no key or partition.
     * @param value                 The event value
     * @param producerKey           The key of the producer configuration
     * @param topic                 The topic name
     * @throws InterruptedException If the current thread was interrupted while waiting
     * @throws ExecutionException   If the computation threw an exception
     * @throws TimeoutException     If the wait timed out
     */
    @When("I send event with value `$value` to `$producerKey` Kafka topic `$topic`")
    public void sendEvent(String value, String producerKey, String topic)
            throws InterruptedException, ExecutionException, TimeoutException
    {
        sendEventWithKey(null, value, producerKey, topic);
    }

    /**
     * Sends the event with the key and the value to the provided topic. Events with the same event key are written to
     * the <a href="https://kafka.apache.org/documentation/#intro_concepts_and_terms">same partition</a>, and Kafka
     * guarantees that any consumer of a given topic-partition will always read that partition's events in exactly the
     * same order as they were written.
     *
     * @param key                   The event key, all the events with the same event key are written to the same
     * partition
     * @param value                 The event value
     * @param producerKey           The key of the producer configuration
     * @param topic                 The topic name
     * @throws InterruptedException If the current thread was interrupted while waiting
     * @throws ExecutionException   If the computation threw an exception
     * @throws TimeoutException     If the wait timed out
     */
    @When("I send event with key `$key` and value `$value` to `$producerKey` Kafka topic `$topic`")
    public void sendEventWithKey(String key, String value, String producerKey, String topic)
            throws InterruptedException, ExecutionException, TimeoutException
    {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, key, value,
                testContext.remove(HEADERS_KEY));
        kafkaTemplates.get(producerKey).send(record).get(WAIT_TIMEOUT_IN_MINUTES, TimeUnit.MINUTES);
    }

    /**
     * Starts the Kafka consumer with the provided configuration to listen the specified topics. The consumer must be
     * stopped when it's not needed.
     *
     * @param consumerKey The key of the producer configuration
     * @param topics      The comma-separated set of topics to listen
     */
    @SuppressWarnings("PreferMethodReference")
    @When("I start consuming events from `$consumerKey` Kafka topics `$topics`")
    public void startKafkaListener(String consumerKey, Set<String> topics)
    {
        stopListener(getListeners().remove(consumerKey), false);
        BlockingQueue<ConsumerRecord<String, String>> eventValueQueue = new LinkedBlockingDeque<>();
        testContext.get(EVENTS_KEY, HashMap::new).put(consumerKey, eventValueQueue);
        ContainerProperties containerProperties = new ContainerProperties(topics.toArray(new String[0]));
        containerProperties.setMessageListener((MessageListener<String, String>) data -> eventValueQueue.add(data));
        GenericMessageListenerContainer<String, String> container = new KafkaMessageListenerContainer<>(
                consumerFactories.get(consumerKey), containerProperties);
        container.start();
        getListeners().put(consumerKey, container);

        LOGGER.info("Kafka event listener is started");
    }

    /**
     * Waits until the count of the consumed events (from the consumer start or after the last draining operation)
     * matches to the rule or until the timeout is exceeded.
     *
     * @param timeout        The maximum time to wait for the event in ISO-8601 format
     * @param consumerKey    The key of the producer configuration
     * @param comparisonRule The rule to match the quantity of events. The supported rules:
     *                       <ul>
     *                       <li>less than (&lt;)</li>
     *                       <li>less than or equal to (&lt;=)</li>
     *                       <li>greater than (&gt;)</li>
     *                       <li>greater than or equal to (&gt;=)</li>
     *                       <li>equal to (=)</li>
     *                       <li>not equal to (!=)</li>
     *                       </ul>
     * @param expectedCount  The expected count of the events to be matched by the rule
     */
    @When("I wait with `$timeout` timeout until count of consumed `$consumerKey` Kafka events is $comparisonRule"
            + " `$expectedCount`")
    public void waitForKafkaEvents(Duration timeout, String consumerKey, ComparisonRule comparisonRule,
            int expectedCount)
    {
        Matcher<Integer> countMatcher = comparisonRule.getComparisonRule(expectedCount);
        Integer result = new DurationBasedWaiter(timeout, Duration.ofSeconds(1)).wait(
                () -> getEventsBy(consumerKey).size(), countMatcher::matches);
        softAssert.assertThat("Total count of consumed Kafka events", result, countMatcher);
    }

    private BlockingQueue<ConsumerRecord<String, String>> getEventsBy(String key)
    {
        return testContext.<Map<String, BlockingQueue<ConsumerRecord<String, String>>>>get(EVENTS_KEY).get(key);
    }

    /**
     * Stops the Kafka consumer started by the corresponding step before. All recorded events are kept and can be
     * drained into the variable using the step described above.
     * @param consumerKey The key of the producer configuration
     */
    @When("I stop consuming events from `$consumerKey` Kafka")
    public void stopKafkaListener(String consumerKey)
    {
        stopListener(getListeners().remove(consumerKey), true);
    }

    /**
     * Drains the consumed events to the specified variable. If the consumer is not stopped, the new events might
     * arrive after the draining. If the consumer is stopped, all the events received from the consumer start or
     * after the last draining operation are stored to the variable.
     * @param queueOperation The one of: <br>
     *                       <ul>
     *                       <li><b>PEEK</b> - saves the events consumed since the last drain or from the
     *                       consumption start and doesn't change the consumer cursor position
     *                       <li><b>DRAIN</b> - saves the events consumed since the last drain or from the
     *                       consumption start and moves the consumer cursor
     *                       to the position after the last consumed event
     *                       </ul>
     * @param consumerKey    The key of the producer configuration
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
    @When("I $queueOperation consumed `$consumerKey` Kafka events to $scopes variable `$variableName`")
    public void processKafkaEvents(QueueOperation queueOperation, String consumerKey, Set<VariableScope> scopes,
            String variableName)
    {
        List<ConsumerRecord<String, String>> events = queueOperation.performOn(getEventsBy(consumerKey));
        LOGGER.atInfo().addArgument(() -> events.stream().map(e -> {
            String key = e.key() == null ? "<no key>" : e.key();
            String headerNames = StreamSupport.stream(e.headers().spliterator(), false)
                    .map(Header::key)
                    .collect(Collectors.joining(", "));
            return headerNames.isEmpty() ? key : "{" + key + "; " + headerNames + "}";
        }).toList()).log("Saving events with the keys and headers: {}");
        List<String> eventValues = events.stream().map(ConsumerRecord::value).toList();
        variableContext.putVariable(scopes, variableName, eventValues);
    }

    @AfterStory
    public void cleanUp()
    {
        Map<String, GenericMessageListenerContainer<String, String>> listeners = getListeners();
        listeners.values().forEach(k -> stopListener(k, false));
        listeners.clear();
    }

    private void stopListener(GenericMessageListenerContainer<String, String> container,
            boolean throwExceptionIfNoListener)
    {
        if (container != null)
        {
            container.stop();
            LOGGER.info("Kafka event listener is stopped");
        }
        else if (throwExceptionIfNoListener)
        {
            throw new IllegalStateException(
                    "No Kafka event listener is running, did you forget to start consuming events?");
        }
    }

    private Map<String, GenericMessageListenerContainer<String, String>> getListeners()
    {
        return testContext.get(LISTENER_KEY, HashMap::new);
    }

    protected enum QueueOperation
    {
        PEEK
        {
            @Override
            List<ConsumerRecord<String, String>> performOn(BlockingQueue<ConsumerRecord<String, String>> eventsQueue)
            {
                return new ArrayList<>(eventsQueue);
            }
        },
        DRAIN
        {
            @Override
            List<ConsumerRecord<String, String>> performOn(BlockingQueue<ConsumerRecord<String, String>> eventsQueue)
            {
                List<ConsumerRecord<String, String>> events = new ArrayList<>();
                eventsQueue.drainTo(events);
                return events;
            }
        };

        abstract List<ConsumerRecord<String, String>>
                performOn(BlockingQueue<ConsumerRecord<String, String>> blockingQueue);
    }
}
