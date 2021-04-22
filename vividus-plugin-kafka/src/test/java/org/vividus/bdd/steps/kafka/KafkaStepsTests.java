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

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.listener.GenericMessageListenerContainer;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.softassert.SoftAssert;
import org.vividus.testcontext.TestContext;
import org.vividus.util.property.IPropertyParser;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class KafkaStepsTests
{
    private static final String KEY2 = "key2";

    private static final Class<?> LISTENER_KEY = GenericMessageListenerContainer.class;

    private static final String LISTENER_IS_STOPPED = "Kafka message listener is stopped";

    private final TestLogger logger = TestLoggerFactory.getTestLogger(KafkaSteps.class);

    @Mock private IPropertyParser propertyParser;
    @Mock private TestContext testContext;
    @Mock private IBddVariableContext bddVariableContext;
    @Mock private SoftAssert softAssert;
    private KafkaSteps kafkaSteps;

    @BeforeEach
    void beforeEach()
    {
        Map<String, String> producerConfigs = Map.of("key.producer.propery", "value");
        Map<String, String> consumerConfigs = Map.of("key2.consumer.property", "value2");

        when(propertyParser.getPropertyValuesByPrefix("kafka.producer.")).thenReturn(producerConfigs);
        when(propertyParser.getPropertyValuesByPrefix("kafka.consumer.")).thenReturn(consumerConfigs);
        kafkaSteps = new KafkaSteps(propertyParser, testContext, bddVariableContext, softAssert);
    }

    @Test
    void shouldThrowExceptionWhenTryingToStopNotRunningListener()
    {
        mockListeners(new HashMap<>());
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> kafkaSteps.stopKafkaListener(KEY2));
        assertEquals("No Kafka message listener is running, did you forget to start consuming messages?",
                exception.getMessage());
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldStopListenerInAfterStory()
    {
        var container = mock(GenericMessageListenerContainer.class);
        Map<String, GenericMessageListenerContainer<String, String>> listeners = new HashMap<>();
        mockListeners(listeners);
        listeners.put(KEY2, container);
        kafkaSteps.cleanUp();
        verify(container).stop();
        assertTrue(listeners.isEmpty());
        assertThat(logger.getLoggingEvents(), is(List.of(info(LISTENER_IS_STOPPED))));
    }

    @SuppressWarnings("unchecked")
    private void mockListeners(
            Map<String, GenericMessageListenerContainer<String, String>> listeners)
    {
        when(testContext.get(eq(LISTENER_KEY), any(Supplier.class))).thenReturn(listeners);
    }

    @SuppressWarnings("unchecked")
    @Test
    void shouldDoNothingInAfterStoryWhenListenerIsStopped()
    {
        Map<String, GenericMessageListenerContainer<String, String>> listeners = new HashMap<>();
        mockListeners(listeners);
        kafkaSteps.cleanUp();
        verifyNoMoreInteractions(testContext);
        assertThat(logger.getLoggingEvents(), is(List.of()));
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    void shouldStopStartedKafkaListenerIfNewKafkaListenerIsCreated()
    {
        try (MockedConstruction<KafkaMessageListenerContainer> construction = Mockito.mockConstruction(
                KafkaMessageListenerContainer.class,
                withSettings().extraInterfaces(GenericMessageListenerContainer.class)))
        {
            String topic = "topic";
            String consumerKey = KEY2;
            Map<String, GenericMessageListenerContainer<String, String>> listeners = new HashMap<>();
            mockListeners(listeners);
            when(testContext.get(eq(ConsumerRecord.class), any(Supplier.class))).thenReturn(new HashMap<>());
            kafkaSteps.startKafkaListener(consumerKey, Set.of(topic));
            KafkaMessageListenerContainer container = construction.constructed().get(0);

            assertThat(listeners.values(), hasSize(1));
            assertEquals(container, listeners.get(consumerKey));

            kafkaSteps.startKafkaListener(consumerKey, Set.of(topic));

            assertThat(listeners.values(), hasSize(1));
            assertEquals(construction.constructed().get(1), listeners.get(consumerKey));
            assertThat(construction.constructed(), hasSize(2));

            String listenerIsStarted = "Kafka message listener is started";
            assertThat(logger.getLoggingEvents(),
                    is(List.of(info(listenerIsStarted),
                               info(LISTENER_IS_STOPPED),
                               info(listenerIsStarted))));
        }
    }
}
