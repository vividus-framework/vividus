/*
 * Copyright 2019-2026 the original author or authors.
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

package org.vividus.steps.rabbitmq;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.substringAfter;
import static org.apache.commons.lang3.StringUtils.substringBefore;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jbehave.core.annotations.When;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.property.IPropertyParser;
import org.vividus.variable.VariableScope;

public class RabbitMqSteps
{
    private static final String DOT = ".";

    private final Map<String, RabbitTemplate> rabbitTemplates;
    private final VariableContext variableContext;
    private final ISoftAssert softAssert;

    public RabbitMqSteps(IPropertyParser propertyParser, VariableContext variableContext, ISoftAssert softAssert)
    {
        this.rabbitTemplates = buildTemplates(propertyParser);
        this.variableContext = variableContext;
        this.softAssert = softAssert;
    }

    private static Map<String, RabbitTemplate> buildTemplates(IPropertyParser propertyParser)
    {
        Map<String, String> properties = new HashMap<>(
                propertyParser.getPropertyValuesByPrefix("rabbitmq."));
        return properties.entrySet()
                .stream()
                .collect(groupingBy(e -> substringBefore(e.getKey(), DOT),
                        mapping(e -> Map.entry(substringAfter(e.getKey(), DOT), e.getValue()),
                                collectingAndThen(toMap(Map.Entry::getKey, Map.Entry::getValue),
                                        RabbitMqSteps::createRabbitTemplate))));
    }

    private static RabbitTemplate createRabbitTemplate(Map<String, String> connectionProperties)
    {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();
        connectionFactory.setHost(connectionProperties.getOrDefault("host", "localhost"));
        getOptional(connectionProperties, "port").map(Integer::parseInt).ifPresent(connectionFactory::setPort);
        getOptional(connectionProperties, "username").ifPresent(connectionFactory::setUsername);
        getOptional(connectionProperties, "password").ifPresent(connectionFactory::setPassword);
        getOptional(connectionProperties, "virtual-host").ifPresent(connectionFactory::setVirtualHost);
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMandatory(false);
        return template;
    }

    private static Optional<String> getOptional(Map<String, String> properties, String key)
    {
        return Optional.ofNullable(properties.get(key)).filter(value -> !value.isEmpty());
    }

    /**
     * Publishes a text message to the default exchange with the given routing key (typically the queue name).
     *
     * @param message    The message body
     * @param routingKey The routing key
     * @param brokerKey  The key of the RabbitMQ connection configuration
     */
    @When("I publish message `$message` with routing key `$routingKey` to RabbitMQ broker `$brokerKey`")
    public void publishMessage(String message, String routingKey, String brokerKey)
    {
        getTemplate(brokerKey).convertAndSend(routingKey, message);
    }

    /**
     * Waits up to the given timeout for a message on the queue and saves the result to a variable when a message is
     * received.
     *
     * @param queue         The queue name
     * @param brokerKey     The key of the RabbitMQ connection configuration
     * @param timeout       The maximum time to wait (ISO-8601 duration)
     * @param scopes        The variable scopes
     * @param variableName  The variable name
     */
    @When("I retrieve message from queue `$queue` of RabbitMQ broker `$brokerKey` with `$timeout` timeout and save it "
            + "to $scopes variable `$variableName`")
    public void retrieveMessage(String queue, String brokerKey, Duration timeout, Set<VariableScope> scopes,
            String variableName)
    {
        Object body = getTemplate(brokerKey).receiveAndConvert(queue, timeout.toMillis());
        if (softAssert.assertNotNull("Received a message from %s queue within %s".formatted(queue, timeout), body))
        {
            variableContext.putVariable(scopes, variableName, body.toString());
        }
    }

    private RabbitTemplate getTemplate(String brokerKey)
    {
        RabbitTemplate template = rabbitTemplates.get(brokerKey);
        if (template == null)
        {
            throw new IllegalStateException(
                    "RabbitMQ connection with key '%s' is not configured in properties".formatted(brokerKey));
        }
        return template;
    }
}
