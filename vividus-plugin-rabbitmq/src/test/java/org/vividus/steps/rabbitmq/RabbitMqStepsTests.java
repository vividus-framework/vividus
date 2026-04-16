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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

import javax.net.ssl.SSLContext;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.vividus.context.VariableContext;
import org.vividus.softassert.ISoftAssert;
import org.vividus.util.property.IPropertyParser;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class RabbitMqStepsTests
{
    private static final String BROKER = "brk";
    private static final String UNKNOWN_BROKER = "unknown-broker";
    private static final String RABBITMQ_PREFIX = "rabbitmq.";
    private static final String SUFFIX_HOST = ".host";
    private static final String SUFFIX_PORT = ".port";
    private static final String SUFFIX_USERNAME = ".username";
    private static final String SUFFIX_PASSWORD = ".password";
    private static final String SUFFIX_VIRTUAL_HOST = ".virtual-host";
    private static final String SUFFIX_USE_SSL = ".use-ssl";

    private static final String MESSAGE_BODY = "msg";
    private static final String ROUTING_KEY = "rk";
    private static final String QUEUE = "q";
    private static final String PAYLOAD_TEXT = "payload";
    private static final String VARIABLE_V = "v";
    private static final String VARIABLE_N = "n";
    private static final String HOST_REMOTE = "rabbit.example";
    private static final String HOST_LOCAL = "localhost";
    private static final String PORT_ALT = "5673";
    private static final String USERNAME = "user";
    private static final String PASSWORD_SECRET = "secret";
    private static final String VIRTUAL_HOST = "vh";

    @Mock private IPropertyParser propertyParser;
    @Mock private VariableContext variableContext;
    @Mock private ISoftAssert softAssert;

    @Test
    void shouldPublishMessage()
    {
        withMockedRabbit((factories, templates) ->
        {
            var steps = createSteps(Map.of(BROKER + SUFFIX_HOST, HOST_LOCAL));
            steps.publishMessage(MESSAGE_BODY, ROUTING_KEY, BROKER);
            verify(templates.constructed().get(0)).convertAndSend(ROUTING_KEY, MESSAGE_BODY);
            verify(factories.constructed().get(0), never()).setPort(anyInt());
        });
    }

    @Test
    void shouldRetrieveMessageAndSaveVariable()
    {
        var timeout = Duration.ofSeconds(5);
        var description = receivedMessageDescription(QUEUE, timeout);
        when(softAssert.assertNotNull(description, PAYLOAD_TEXT)).thenReturn(true);
        withMockedRabbit((factories, templates) ->
        {
            var steps = createSteps(Map.of(BROKER + SUFFIX_HOST, HOST_LOCAL));
            when(templates.constructed().get(0).receiveAndConvert(QUEUE, 5_000L)).thenReturn(PAYLOAD_TEXT);
            steps.retrieveMessage(QUEUE, BROKER, timeout, Set.of(VariableScope.STORY), VARIABLE_V);
            verify(variableContext).putVariable(Set.of(VariableScope.STORY), VARIABLE_V, PAYLOAD_TEXT);
            verify(softAssert).assertNotNull(description, PAYLOAD_TEXT);
            verify(factories.constructed().get(0), never()).setPort(anyInt());
        });
    }

    @Test
    void shouldNotPutVariableWhenQueueEmpty()
    {
        var timeout = Duration.ofSeconds(1);
        var description = receivedMessageDescription(QUEUE, timeout);
        when(softAssert.assertNotNull(description, null)).thenReturn(false);
        withMockedRabbit((factories, templates) ->
        {
            var steps = createSteps(Map.of(BROKER + SUFFIX_HOST, HOST_LOCAL));
            when(templates.constructed().get(0).receiveAndConvert(QUEUE, 1_000L)).thenReturn(null);
            steps.retrieveMessage(QUEUE, BROKER, timeout, Set.of(VariableScope.STORY), VARIABLE_V);
            verifyNoInteractions(variableContext);
            verify(softAssert).assertNotNull(description, null);
            verify(factories.constructed().get(0), never()).setPort(anyInt());
        });
    }

    @Test
    void shouldConvertNonStringBodyToString()
    {
        var timeout = Duration.ofMillis(100);
        var description = receivedMessageDescription(QUEUE, timeout);
        when(softAssert.assertNotNull(description, 42)).thenReturn(true);
        withMockedRabbit((factories, templates) ->
        {
            var steps = createSteps(Map.of(BROKER + SUFFIX_HOST, HOST_LOCAL));
            when(templates.constructed().get(0).receiveAndConvert(QUEUE, 100L)).thenReturn(42);
            steps.retrieveMessage(QUEUE, BROKER, timeout, Set.of(VariableScope.STEP), VARIABLE_N);
            verify(variableContext).putVariable(Set.of(VariableScope.STEP), VARIABLE_N, "42");
            verify(softAssert).assertNotNull(description, 42);
            verify(factories.constructed().get(0), never()).setPort(anyInt());
        });
    }

    @Test
    void shouldEnableSslUsingDefaultJvmContext() throws NoSuchAlgorithmException
    {
        var rabbitConnFactory = mock(com.rabbitmq.client.ConnectionFactory.class);
        try (var factories = mockConstruction(CachingConnectionFactory.class,
                     (mock, ctx) -> when(mock.getRabbitConnectionFactory()).thenReturn(rabbitConnFactory));
                var ignored = mockConstruction(RabbitTemplate.class))
        {
            createSteps(Map.of(BROKER + SUFFIX_HOST, HOST_LOCAL, BROKER + SUFFIX_USE_SSL, Boolean.TRUE.toString()));
            verify(rabbitConnFactory).useSslProtocol(SSLContext.getDefault());
        }
    }

    @Test
    void shouldWrapNoSuchAlgorithmExceptionFromSslContext()
    {
        var cause = new NoSuchAlgorithmException("no-tls");
        try (var sslContext = mockStatic(SSLContext.class);
                var ignored1 = mockConstruction(CachingConnectionFactory.class,
                        (mock, ctx) -> when(mock.getRabbitConnectionFactory())
                                .thenReturn(mock(com.rabbitmq.client.ConnectionFactory.class)));
                var ignored2 = mockConstruction(RabbitTemplate.class))
        {
            sslContext.when(SSLContext::getDefault).thenThrow(cause);
            var props = Map.of(BROKER + SUFFIX_HOST, HOST_LOCAL, BROKER + SUFFIX_USE_SSL, Boolean.TRUE.toString());
            var exception = assertThrows(IllegalStateException.class, () -> createSteps(props));
            assertEquals(cause, exception.getCause());
        }
    }

    @Test
    void shouldNotEnableSslWhenFlagIsFalse()
    {
        withMockedRabbit((factories, templates) ->
        {
            createSteps(Map.of(BROKER + SUFFIX_HOST, HOST_LOCAL, BROKER + SUFFIX_USE_SSL, "false"));
            verify(factories.constructed().get(0), never()).getRabbitConnectionFactory();
        });
    }

    @Test
    void shouldFailWhenUseSslValueIsNotBoolean()
    {
        withMockedRabbit((factories, templates) ->
        {
            var props = Map.of(BROKER + SUFFIX_HOST, HOST_LOCAL, BROKER + SUFFIX_USE_SSL, "yes");
            var exception = assertThrows(IllegalArgumentException.class, () -> createSteps(props));
            assertEquals(
                    "Invalid value 'yes' for rabbitmq.<broker-key>.use-ssl, expected true/false",
                    exception.getMessage());
        });
    }

    @Test
    void shouldFailWhenBrokerKeyUnknown()
    {
        var steps = createSteps(Map.of());
        var exception = assertThrows(IllegalStateException.class,
                () -> steps.publishMessage(MESSAGE_BODY, ROUTING_KEY, UNKNOWN_BROKER));
        assertEquals(noConfigurationMessage(UNKNOWN_BROKER), exception.getMessage());
        verifyNoInteractions(variableContext, softAssert);
    }

    @Test
    void shouldApplyConnectionPropertiesFromConfiguration()
    {
        withMockedRabbit((factories, templates) ->
        {
            createSteps(Map.of(
                BROKER + SUFFIX_HOST, HOST_REMOTE,
                BROKER + SUFFIX_PORT, PORT_ALT,
                BROKER + SUFFIX_USERNAME, USERNAME,
                BROKER + SUFFIX_PASSWORD, PASSWORD_SECRET,
                BROKER + SUFFIX_VIRTUAL_HOST, VIRTUAL_HOST));
            var factory = factories.constructed().get(0);
            verify(factory).setHost(HOST_REMOTE);
            verify(factory).setPort(Integer.parseInt(PORT_ALT));
            verify(factory).setUsername(USERNAME);
            verify(factory).setPassword(PASSWORD_SECRET);
            verify(factory).setVirtualHost(VIRTUAL_HOST);
            verify(templates.constructed().get(0)).setMandatory(false);
        });
    }

    @Test
    void shouldSkipEmptyOptionalConnectionProperties()
    {
        withMockedRabbit((factories, templates) ->
        {
            createSteps(Map.of(BROKER + SUFFIX_HOST, HOST_LOCAL, BROKER + SUFFIX_PASSWORD, ""));
            var factory = factories.constructed().get(0);
            verify(factory, never()).setPassword(anyString());
            verify(factory, never()).setPort(anyInt());
            verify(factory, never()).setUsername(anyString());
            verify(factory, never()).setVirtualHost(anyString());
            verify(templates.constructed().get(0)).setMandatory(false);
        });
    }

    private void withMockedRabbit(
            BiConsumer<MockedConstruction<CachingConnectionFactory>, MockedConstruction<RabbitTemplate>> consumer)
    {
        try (var factories = mockConstruction(CachingConnectionFactory.class);
                var templates = mockConstruction(RabbitTemplate.class))
        {
            consumer.accept(factories, templates);
        }
    }

    private RabbitMqSteps createSteps(Map<String, String> templateConfig)
    {
        when(propertyParser.getPropertyValuesByPrefix(RABBITMQ_PREFIX)).thenReturn(templateConfig);
        return new RabbitMqSteps(propertyParser, variableContext, softAssert);
    }

    private static String noConfigurationMessage(String brokerKey)
    {
        return "RabbitMQ connection with key '%s' is not configured in properties".formatted(brokerKey);
    }

    private static String receivedMessageDescription(String queue, Duration timeout)
    {
        return "Received a message from %s queue within %s".formatted(queue, timeout);
    }
}
