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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jbehave.core.annotations.When;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.vividus.util.property.IPropertyParser;

public class KafkaSteps
{
    private static final int WAIT_TIMEOUT_IN_MINUTES = 10;

    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaSteps(IPropertyParser propertyParser)
    {
        Map<String, Object> config = new HashMap<>();
        config.putAll(propertyParser.getPropertyValuesByPrefix("kafka."));
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        DefaultKafkaProducerFactory<String, String> producerFactory = new DefaultKafkaProducerFactory<>(config);
        this.kafkaTemplate = new KafkaTemplate<>(producerFactory);
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
}
