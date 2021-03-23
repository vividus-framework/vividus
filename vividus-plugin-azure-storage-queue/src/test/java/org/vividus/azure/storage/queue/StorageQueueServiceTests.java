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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.function.BiConsumer;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.Context;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.models.PeekedMessageItem;
import com.azure.storage.queue.models.SendMessageResult;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.storage.queue.model.Queue;

@ExtendWith(MockitoExtension.class)
class StorageQueueServiceTests
{
    private static final Duration TIMEOUT = Duration.ofSeconds(1);
    private static final StorageQueueService SERVICE = new StorageQueueService();
    private static final String ENDPOINT = "https://azure.portal.com";
    private static final String SAS_TOKEN = "sastoken";
    private static final String QUEUE_NAME = "queue";

    @BeforeEach
    void beforeEach()
    {
        SERVICE.setReceiveTimeout(TIMEOUT);
    }

    @Test
    void shouldPeekMessages()
    {
        runWithClient((queue, client) -> {
            @SuppressWarnings("unchecked")
            PagedIterable<PeekedMessageItem> result = mock(PagedIterable.class);
            List<PeekedMessageItem> messages = List.of(mock(PeekedMessageItem.class));
            when(client.peekMessages(Integer.valueOf(1), TIMEOUT, Context.NONE)).thenReturn(result);
            when(result.spliterator()).thenReturn(messages.spliterator());
            assertEquals(messages, SERVICE.peekMessages(queue, 1));
        }, ENDPOINT);
    }

    @Test
    void shouldSendAMessage()
    {
        runWithClient((queue, client) -> {
            SendMessageResult result = mock(SendMessageResult.class);
            String messageText = "message";
            when(client.sendMessage(messageText)).thenReturn(result);
            assertEquals(result, SERVICE.sendMessage(queue, messageText));
        }, "https://portal.azure.com/send");
    }

    private void runWithClient(BiConsumer<Queue, QueueClient> testToRun, String endpoint)
    {
        @SuppressWarnings("unchecked")
        Queue queue = new Queue();
        queue.setEndpoint(endpoint);
        queue.setName(QUEUE_NAME);
        queue.setSasToken(SAS_TOKEN);
        QueueClient client = mock(QueueClient.class);
        try (MockedConstruction<QueueClientBuilder> builderConstructor =
                mockConstruction(QueueClientBuilder.class, (mock, context) -> {
                    when(mock.endpoint(endpoint)).thenReturn(mock);
                    when(mock.sasToken(SAS_TOKEN)).thenReturn(mock);
                    when(mock.queueName(QUEUE_NAME)).thenReturn(mock);
                    when(mock.buildClient()).thenReturn(client);
                }))
        {
            testToRun.accept(queue, client);
            assertThat(builderConstructor.constructed(), hasSize(1));
        }
    }
}
