/*
 * Copyright 2021 the original author or authors.
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

package org.vividus.azure.storage.queue;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import com.azure.storage.queue.models.PeekedMessageItem;
import com.azure.storage.queue.models.SendMessageResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.storage.queue.model.Queue;
import org.vividus.bdd.context.BddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.util.property.PropertyMappedCollection;

@ExtendWith(MockitoExtension.class)
class StorageQueueStepsTests
{
    private static final String MESSAGE = "{\"name\" : \"azure\"}";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.SCENARIO);
    private static final String QUEUE = "queue";
    private static final String VARIABLE_NAME = "variableName";

    @Mock private StorageQueueService storageQueueService;
    @Mock private BddVariableContext bddVariableContext;
    @Mock private PropertyMappedCollection<Queue> queues;
    @Mock private Queue mockedQueue;

    @InjectMocks private StorageQueueSteps storageQueueSteps;

    @BeforeEach
    void beforeEach()
    {
        when(queues.get(QUEUE, "No connection details provided for the queue: %s", QUEUE)).thenReturn(mockedQueue);
    }

    @Test
    void shouldPeekMessagesFromTheQueue()
    {
        List<PeekedMessageItem> messages = List.of(mock(PeekedMessageItem.class));
        when(storageQueueService.peekMessages(mockedQueue, 1)).thenReturn(messages);
        storageQueueSteps.peekMessages(1, QUEUE, SCOPES, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(SCOPES, VARIABLE_NAME, messages);
    }

    @Test
    void shouldSendAMessageToTheQueue()
    {
        SendMessageResult sendMessageResult = mock(SendMessageResult.class);
        when(storageQueueService.sendMessage(mockedQueue, MESSAGE)).thenReturn(sendMessageResult);
        storageQueueSteps.sendMessage(MESSAGE, QUEUE, SCOPES, VARIABLE_NAME);
        verify(bddVariableContext).putVariable(SCOPES, VARIABLE_NAME, sendMessageResult);
    }
}
