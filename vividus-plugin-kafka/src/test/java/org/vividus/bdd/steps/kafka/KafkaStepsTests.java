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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

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
        Map<String, String> producerConfigs = Map.of();
        Map<String, String> consumerConfigs = Map.of();

        when(propertyParser.getPropertyValuesByPrefix("kafka.producer.")).thenReturn(producerConfigs);
        when(propertyParser.getPropertyValuesByPrefix("kafka.consumer.")).thenReturn(consumerConfigs);
        kafkaSteps = new KafkaSteps(propertyParser, testContext, bddVariableContext, softAssert);
    }

    @Test
    void shouldThrowExceptionWhenTryingToStopNotRunningListener()
    {
        when(testContext.get(GenericMessageListenerContainer.class)).thenReturn(null);
        IllegalStateException exception = assertThrows(IllegalStateException.class,
                kafkaSteps::stopKafkaListener);
        assertEquals("No Kafka message listener is running, did you forget to start consuming messages?",
                exception.getMessage());
    }

    @Test
    void shouldStopListenerInAfterStory()
    {
        @SuppressWarnings("rawtypes")
        GenericMessageListenerContainer container = mock(GenericMessageListenerContainer.class);
        Class<?> key = GenericMessageListenerContainer.class;
        when(testContext.get(key)).thenReturn(container);
        kafkaSteps.cleanUp();
        verify(container).stop();
        verify(testContext).remove(key);
        assertThat(logger.getLoggingEvents(), is(List.of(info(LISTENER_IS_STOPPED))));
    }

    @Test
    void shouldDoNothingInAfterStoryWhenListenerIsStopped()
    {
        when(testContext.get(GenericMessageListenerContainer.class)).thenReturn(null);
        kafkaSteps.cleanUp();
        verifyNoMoreInteractions(testContext);
        assertThat(logger.getLoggingEvents(), is(List.of()));
    }

    @SuppressWarnings("rawtypes")
    @Test
    void shouldStopStartedKafkaListenerIfNewKafkaListenerIsCreated()
    {
        try (MockedConstruction<KafkaMessageListenerContainer> construction = Mockito.mockConstruction(
                KafkaMessageListenerContainer.class,
                withSettings().extraInterfaces(GenericMessageListenerContainer.class)))
        {
            String topic = "topic";
            kafkaSteps.startKafkaListener(Set.of(topic));
            KafkaMessageListenerContainer container = construction.constructed().get(0);

            when(testContext.get(GenericMessageListenerContainer.class)).thenReturn(container);

            kafkaSteps.startKafkaListener(Set.of(topic));

            verify(testContext).put(GenericMessageListenerContainer.class, container);
            verify(testContext).put(GenericMessageListenerContainer.class, construction.constructed().get(1));
            assertThat(construction.constructed(), hasSize(2));

            verify(testContext).remove(GenericMessageListenerContainer.class);

            String listenerIsStarted = "Kafka message listener is started";
            assertThat(logger.getLoggingEvents(),
                    is(List.of(info(listenerIsStarted),
                               info(LISTENER_IS_STOPPED),
                               info(listenerIsStarted))));
        }
    }
}
