/*
 * Copyright 2019-2024 the original author or authors.
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

package org.vividus.azure.servicebus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

import com.azure.core.util.BinaryData;
import com.azure.messaging.servicebus.ServiceBusReceivedMessage;

import org.hamcrest.Matcher;
import org.jbehave.core.model.ExamplesTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.servicebus.service.ServiceBusService;
import org.vividus.context.VariableContext;
import org.vividus.softassert.SoftAssert;
import org.vividus.steps.ComparisonRule;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
public class ServiceBusStepsTests
{
    private static final String SERVICE_BUS_KEY = "key";
    private static final String MESSAGE = "msg";

    @Mock private ServiceBusService serviceBusService;
    @Mock private VariableContext variableContext;
    @Mock private SoftAssert softAssert;

    @InjectMocks
    private ServiceBusSteps serviceBusSteps;

    @Test
    void testSendMessageToServiceBus()
    {
        serviceBusSteps.sendMessageToServiceBus(SERVICE_BUS_KEY, MESSAGE);
        verify(serviceBusService).send(SERVICE_BUS_KEY, MESSAGE);
    }

    @Test
    void testSendMessageWithCustomPropertiesToServiceBus()
    {
        var customPropertiesTable = new ExamplesTable("""
                |key       |type   |value|
                |keyString |STRING |test |
                |keyNumber |NUMBER |5    |
                |keyBoolean|BOOLEAN|true |""");
        Map<String, Object> customProperties = Map.of("keyString", "test", "keyNumber", 5L, "keyBoolean", true);
        serviceBusSteps.sendMessageToServiceBus(SERVICE_BUS_KEY, MESSAGE, customPropertiesTable);
        verify(serviceBusService).send(SERVICE_BUS_KEY, MESSAGE, customProperties);
    }

    @Test
    void testStartConsumingMessages()
    {
        serviceBusSteps.startConsumingMessagesFromServiceBus(SERVICE_BUS_KEY);
        verify(serviceBusService).startConsuming(SERVICE_BUS_KEY);
    }

    @Test
    void testStopConsumingMessages()
    {
        serviceBusSteps.stopConsumingMessagesFromServiceBus(SERVICE_BUS_KEY);
        verify(serviceBusService).stopConsuming(SERVICE_BUS_KEY);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    void testWaitForServiceBusMessages()
    {
        ComparisonRule equalTo = mock(ComparisonRule.EQUAL_TO.getClass());
        Matcher matcher = mock(Matcher.class);
        int expectedCount = 1;
        when(equalTo.getComparisonRule(expectedCount)).thenReturn(matcher);

        ServiceBusReceivedMessage mockedMessage = mock(ServiceBusReceivedMessage.class);
        BlockingQueue<ServiceBusReceivedMessage> returnedMessages = new LinkedBlockingDeque<>();
        returnedMessages.add(mockedMessage);
        when(serviceBusService.getMessagesForClient(SERVICE_BUS_KEY))
                .thenReturn(new LinkedBlockingDeque<>())
                .thenReturn(returnedMessages);

        serviceBusSteps.waitForServiceBusMessages(Duration.ofMillis(1700), SERVICE_BUS_KEY, equalTo, expectedCount);

        verify(serviceBusService, atLeast(1)).getMessagesForClient(SERVICE_BUS_KEY);
        verify(softAssert).assertThat("Total count of messages for Service Bus with key: "
                        + SERVICE_BUS_KEY, 1, matcher);
    }

    @Test
    void testPeekServiceBusMessages()
    {
        Set<VariableScope> scopes = Set.of(VariableScope.SCENARIO);
        String varName = "varName1";
        String message = "abc";

        mockServiceBusMessage(message);

        serviceBusSteps.processServiceBusMessages(ServiceBusSteps.QueueOperation.PEEK, SERVICE_BUS_KEY, scopes,
                varName);
        verify(variableContext).putVariable(scopes, varName, List.of(message));
    }

    @Test
    void testDrainServiceBusMessages()
    {
        Set<VariableScope> scopes = Set.of(VariableScope.STORY);
        String varName = "varName2";
        String message = "cba";

        BlockingQueue<ServiceBusReceivedMessage> returnedMessages = mockServiceBusMessage(message);

        serviceBusSteps.processServiceBusMessages(ServiceBusSteps.QueueOperation.DRAIN, SERVICE_BUS_KEY, scopes,
                varName);
        verify(variableContext).putVariable(scopes, varName, List.of(message));
        assertEquals(0, returnedMessages.size());
    }

    private BlockingQueue<ServiceBusReceivedMessage> mockServiceBusMessage(String message)
    {
        ServiceBusReceivedMessage mockedMessage = mock(ServiceBusReceivedMessage.class);
        BinaryData binaryData = mock(BinaryData.class);
        when(mockedMessage.getBody()).thenReturn(binaryData);
        when(binaryData.toString()).thenReturn(message);

        BlockingQueue<ServiceBusReceivedMessage> returnedMessages = new LinkedBlockingDeque<>();
        returnedMessages.add(mockedMessage);
        when(serviceBusService.getMessagesForClient(SERVICE_BUS_KEY)).thenReturn(returnedMessages);
        return returnedMessages;
    }

    @Test
    void shouldStopInAfterStory()
    {
        serviceBusSteps.cleanUp();
        verify(serviceBusService).stopAll();
    }
}
