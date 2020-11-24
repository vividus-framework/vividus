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

package org.vividus.bdd.steps.kafka;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.steps.ComparisonRule;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.softassert.SoftAssert;
import org.vividus.testcontext.SimpleTestContext;
import org.vividus.util.property.IPropertyParser;

@EmbeddedKafka(topics = KafkaStepsIntegrationTests.TOPIC)
@ExtendWith({ MockitoExtension.class, SpringExtension.class, TestLoggerFactoryExtension.class })
class KafkaStepsIntegrationTests
{
    static final String TOPIC = "test-topic";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(KafkaSteps.class);

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Mock private IPropertyParser propertyParser;
    @Mock private IBddVariableContext bddVariableContext;
    @Mock private SoftAssert softAssert;
    private KafkaSteps kafkaSteps;

    @BeforeEach
    void beforeEach()
    {
        Map<String, String> producerConfigs = KafkaTestUtils.producerProps(embeddedKafkaBroker)
                .entrySet().stream()
                .filter(e -> e.getValue() instanceof String)
                .collect(toMap(Entry::getKey, e -> (String) e.getValue()));

        Map<String, String> consumerConfigs = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker)
                .entrySet().stream()
                .filter(e -> e.getValue() instanceof String)
                .collect(toMap(Entry::getKey, e -> (String) e.getValue()));
        consumerConfigs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        when(propertyParser.getPropertyValuesByPrefix("kafka.producer.")).thenReturn(producerConfigs);
        when(propertyParser.getPropertyValuesByPrefix("kafka.consumer.")).thenReturn(consumerConfigs);
        kafkaSteps = new KafkaSteps(propertyParser, new SimpleTestContext(), bddVariableContext, softAssert);
    }

    @Test
    void shouldProduceToAndConsumerFromKafka() throws InterruptedException, ExecutionException, TimeoutException
    {
        kafkaSteps.startKafkaListener(Set.of(TOPIC));

        String data = "any-data";
        kafkaSteps.sendData(data, TOPIC);

        ComparisonRule comparisonRule = ComparisonRule.EQUAL_TO;
        kafkaSteps.waitForKafkaMessages(Duration.ofSeconds(10), comparisonRule, 1);
        verify(softAssert).assertThat(eq("Total count of consumed Kafka messages"), eq(1),
                argThat(matcher -> "a value equal to <1>".equals(matcher.toString())));

        kafkaSteps.stopKafkaListener();
        assertThat(logger.getLoggingEvents(),
                is(List.of(info("Kafka message listener is started"), info("Kafka message listener is stopped"))));

        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        String variableName = "var";
        kafkaSteps.drainKafkaMessagesToVariable(scopes, variableName);
        verify(bddVariableContext).putVariable(scopes, variableName, List.of(data));
    }
}
