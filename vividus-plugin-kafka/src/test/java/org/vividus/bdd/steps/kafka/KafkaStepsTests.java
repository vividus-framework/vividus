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

import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.vividus.util.property.IPropertyParser;

@EmbeddedKafka(topics = KafkaStepsTests.TOPIC)
@ExtendWith({ MockitoExtension.class, SpringExtension.class })
class KafkaStepsTests
{
    static final String TOPIC = "test-topic";

    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Mock private IPropertyParser propertyParser;

    @Test
    void testSendData() throws InterruptedException, ExecutionException, TimeoutException
    {
        Map<String, String> configs = KafkaTestUtils.producerProps(embeddedKafkaBroker).entrySet().stream()
                .filter(e -> e.getValue() instanceof String)
                .collect(toMap(Entry::getKey, e -> (String) e.getValue()));

        when(propertyParser.getPropertyValuesByPrefix("kafka.")).thenReturn(configs);
        KafkaSteps kafkaSteps = new KafkaSteps(propertyParser);
        String data = "any-data";
        kafkaSteps.sendData(data, TOPIC);
        assertRecord(data);
    }

    private void assertRecord(String data)
    {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps("testGroup", "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        ConsumerFactory<Integer, String> cf = new DefaultKafkaConsumerFactory<>(consumerProps);
        Consumer<Integer, String> consumer = cf.createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, TOPIC);
        ConsumerRecords<Integer, String> records = KafkaTestUtils.getRecords(consumer);
        assertEquals(1, records.count());
        ConsumerRecord<Integer, String> record = records.iterator().next();
        assertEquals(data, record.value());
        assertEquals(TOPIC, record.topic());
    }
}
