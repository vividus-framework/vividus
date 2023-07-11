/*
 * Copyright 2019-2023 the original author or authors.
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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.valfirst.slf4jtest.LoggingEvent;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.event.Level;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.steps.kafka.KafkaSteps.QueueOperation;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.util.property.IPropertyParser;
import org.vividus.variable.VariableScope;

@EmbeddedKafka(topics = KafkaStepsIntegrationTests.TOPIC)
@ExtendWith({ MockitoExtension.class, SpringExtension.class, TestLoggerFactoryExtension.class })
class KafkaStepsIntegrationTests
{
    static final String TOPIC = "test-topic";

    private static final String DOT = ".";

    private static final String CONSUMER = "keyConsumer";

    private static final String PRODUCER = "keyProducer";

    private static final String KEY = "key";

    private static final String ANY_DATA = "any-data";

    private static final String VARIABLE_NAME = "var";

    private static final String EQUAL_TO_ONE_MATCHER = "a value equal to <1>";

    private static final String LISTENER_STARTED = "Kafka event listener is started";

    private static final String LISTENER_STOPPED = "Kafka event listener is stopped";

    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.SCENARIO);

    private final TestLogger logger = TestLoggerFactory.getTestLogger(KafkaSteps.class);

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Mock private IPropertyParser propertyParser;
    @Mock private VariableContext variableContext;
    @Mock private ISoftAssert softAssert;
    private KafkaSteps kafkaSteps;

    @BeforeEach
    void beforeEach()
    {
        Map<String, String> producerConfigs = KafkaTestUtils.producerProps(embeddedKafkaBroker)
                .entrySet().stream()
                .filter(e -> e.getValue() instanceof String)
                .collect(toMap(e -> PRODUCER + DOT + e.getKey(), e -> (String) e.getValue()));

        Map<String, String> consumerConfigs = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker)
                .entrySet().stream()
                .filter(e -> e.getValue() instanceof String)
                .collect(toMap(e -> CONSUMER + DOT + e.getKey(), e -> (String) e.getValue()));
        consumerConfigs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        when(propertyParser.getPropertyValuesByPrefix("kafka.producer.")).thenReturn(producerConfigs);
        when(propertyParser.getPropertyValuesByPrefix("kafka.consumer.")).thenReturn(consumerConfigs);
        kafkaSteps = new KafkaSteps(propertyParser, new SimpleTestContext(), variableContext, softAssert);
    }

    static Stream<Arguments> kafkaOperations()
    {
        return Stream.of(
                Arguments.of((BiConsumer<KafkaSteps, VariableContext>) (steps, context) -> {
                    steps.processKafkaEvents(QueueOperation.DRAIN, CONSUMER, SCOPES, VARIABLE_NAME);
                    steps.processKafkaEvents(QueueOperation.DRAIN, CONSUMER, SCOPES, VARIABLE_NAME);
                    InOrder ordered = Mockito.inOrder(context);
                    ordered.verify(context).putVariable(SCOPES, VARIABLE_NAME, List.of(ANY_DATA));
                    ordered.verify(context).putVariable(SCOPES, VARIABLE_NAME, List.of());
                }),
                Arguments.of((BiConsumer<KafkaSteps, VariableContext>) (steps, context) -> {
                    steps.processKafkaEvents(QueueOperation.PEEK, CONSUMER, SCOPES, VARIABLE_NAME);
                    steps.processKafkaEvents(QueueOperation.PEEK, CONSUMER, SCOPES, VARIABLE_NAME);
                    verify(context, times(2)).putVariable(SCOPES, VARIABLE_NAME, List.of(ANY_DATA));
                }));
    }

    @ParameterizedTest
    @MethodSource("kafkaOperations")
    void shouldProduceMessagesToAndConsumeMessagesFromKafka(BiConsumer<KafkaSteps, VariableContext> test)
            throws InterruptedException, ExecutionException, TimeoutException
    {
        kafkaSteps.startKafkaListener(CONSUMER, Set.of(TOPIC));

        kafkaSteps.sendEvent(ANY_DATA, PRODUCER, TOPIC);

        ComparisonRule comparisonRule = ComparisonRule.EQUAL_TO;
        kafkaSteps.waitForKafkaMessages(Duration.ofSeconds(10), CONSUMER, comparisonRule, 1);
        verify(softAssert).assertThat(eq("Total count of consumed Kafka messages"), eq(1),
                argThat(matcher -> EQUAL_TO_ONE_MATCHER.equals(matcher.toString())));

        kafkaSteps.stopKafkaListener(CONSUMER);

        assertThat(logger.getLoggingEvents(), is(List.of(info(LISTENER_STARTED), info(LISTENER_STOPPED))));

        test.accept(kafkaSteps, variableContext);
    }

    @ParameterizedTest
    @MethodSource("kafkaOperations")
    void shouldProduceEventsToAndConsumeEventsFromKafka(BiConsumer<KafkaSteps, VariableContext> test)
            throws InterruptedException, ExecutionException, TimeoutException
    {
        kafkaSteps.startKafkaListener(CONSUMER, Set.of(TOPIC));

        kafkaSteps.sendEvent(ANY_DATA, PRODUCER, TOPIC);

        ComparisonRule comparisonRule = ComparisonRule.EQUAL_TO;
        kafkaSteps.waitForKafkaEvents(Duration.ofSeconds(10), CONSUMER, comparisonRule, 1);
        verify(softAssert).assertThat(eq("Total count of consumed Kafka events"), eq(1),
                argThat(matcher -> EQUAL_TO_ONE_MATCHER.equals(matcher.toString())));

        kafkaSteps.stopKafkaListener(CONSUMER);
        assertThat(logger.getLoggingEvents(), is(List.of(info(LISTENER_STARTED), info(LISTENER_STOPPED))));

        test.accept(kafkaSteps, variableContext);
    }

    @Test
    void shouldProduceEventWithKey() throws InterruptedException, ExecutionException, TimeoutException
    {
        kafkaSteps.startKafkaListener(CONSUMER, Set.of(TOPIC));

        kafkaSteps.sendEventWithKey(KEY + 1, ANY_DATA, PRODUCER, TOPIC);
        kafkaSteps.sendEvent(ANY_DATA, PRODUCER, TOPIC);
        kafkaSteps.sendEvent(ANY_DATA, PRODUCER, TOPIC);
        kafkaSteps.sendEventWithKey(KEY + 2, ANY_DATA, PRODUCER, TOPIC);
        kafkaSteps.sendEventWithKey(KEY + 3, ANY_DATA, PRODUCER, TOPIC);
        kafkaSteps.sendEvent(ANY_DATA, PRODUCER, TOPIC);

        kafkaSteps.waitForKafkaEvents(Duration.ofSeconds(10), CONSUMER, ComparisonRule.EQUAL_TO, 6);
        kafkaSteps.stopKafkaListener(CONSUMER);
        kafkaSteps.processKafkaEvents(QueueOperation.PEEK, CONSUMER, SCOPES, VARIABLE_NAME);

        List<LoggingEvent> events = logger.getLoggingEvents();
        assertThat(events, hasSize(3));
        assertEquals(info(LISTENER_STARTED), events.get(0));
        assertEquals(info(LISTENER_STOPPED), events.get(1));
        LoggingEvent keysLogEvent = events.get(2);
        assertEquals(Level.INFO, keysLogEvent.getLevel());
        assertEquals("Saving events with the keys: {}", keysLogEvent.getMessage());
        List<Object> arguments = keysLogEvent.getArguments();
        assertThat(arguments, hasSize(1));
        List<String> keys = Stream.of(arguments.get(0).toString().split(",")).map(String::strip).sorted()
                .collect(Collectors.toList());
        String noKey = "<no key>";
        assertEquals(List.of(noKey, noKey, noKey, KEY + 1, KEY + 2, KEY + 3), keys);

        verify(variableContext).putVariable(SCOPES, VARIABLE_NAME,
                List.of(ANY_DATA, ANY_DATA, ANY_DATA, ANY_DATA, ANY_DATA, ANY_DATA));
    }
}
