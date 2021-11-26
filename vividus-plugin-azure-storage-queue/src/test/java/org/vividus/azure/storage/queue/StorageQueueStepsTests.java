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

package org.vividus.azure.storage.queue;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.models.PeekedMessageItem;
import com.azure.storage.queue.models.SendMessageResult;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.context.VariableContext;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.property.PropertyMappedCollection;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class StorageQueueStepsTests
{
    private static final Duration RECEIVE_TIMEOUT = Duration.ofSeconds(1);
    private static final String MESSAGE = "{\"name\" : \"azure\"}";
    private static final String KEY = "queue";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.SCENARIO);
    private static final String VARIABLE_NAME = "variableName";

    @Mock private PropertyMappedCollection<String> storageQueueEndpoints;
    @Mock private VariableContext variableContext;
    @Mock private JsonUtils jsonUtils;
    @Mock private TokenCredential tokenCredential;

    @Test
    void shouldPeekMessagesFromTheQueue()
    {
        runWithClient((steps, client) ->
        {
            @SuppressWarnings("unchecked")
            PagedIterable<PeekedMessageItem> result = mock(PagedIterable.class);
            var message = mock(PeekedMessageItem.class);
            when(message.getBody()).thenReturn(BinaryData.fromString(MESSAGE));
            var messages = List.of(message);
            when(client.peekMessages(1, RECEIVE_TIMEOUT, Context.NONE)).thenReturn(result);
            when(result.stream()).thenReturn(messages.stream());
            steps.peekMessages(1, KEY, SCOPES, VARIABLE_NAME);
            verify(variableContext).putVariable(SCOPES, VARIABLE_NAME, List.of(MESSAGE));
        });
    }

    @Test
    void shouldSendAMessageToTheQueue()
    {
        runWithClient((steps, client) ->
        {
            var result = mock(SendMessageResult.class);
            when(client.sendMessage(MESSAGE)).thenReturn(result);
            var resultAsJson = "result";
            when(jsonUtils.toJson(result)).thenReturn(resultAsJson);
            steps.sendMessage(MESSAGE, KEY, SCOPES, VARIABLE_NAME);
            verify(variableContext).putVariable(SCOPES, VARIABLE_NAME, resultAsJson);
        });
    }

    @Test
    void shouldDeleteAllMessagesFromTheQueue()
    {
        runWithClient((steps, client) ->
        {
            steps.clearMessages(KEY);
            verify(client).clearMessages();
        });
    }

    private void runWithClient(BiConsumer<StorageQueueSteps, QueueClient> testToRun)
    {
        var queueClient = mock(QueueClient.class);
        var endpoint = "https://accountName.queue.core.windows.net/queueName";
        when(storageQueueEndpoints.get(KEY, "Storage queue with key '%s' is not configured in properties", KEY))
                .thenReturn(endpoint);
        try (MockedConstruction<QueueClientBuilder> queueClientBuilder =
                mockConstruction(QueueClientBuilder.class, (mock, context) -> {
                    when(mock.credential(tokenCredential)).thenReturn(mock);
                    when(mock.endpoint(endpoint)).thenReturn(mock);
                    when(mock.buildClient()).thenReturn(queueClient);
                }))
        {
            StorageQueueSteps steps = new StorageQueueSteps(storageQueueEndpoints, RECEIVE_TIMEOUT, tokenCredential,
                    variableContext, jsonUtils);
            testToRun.accept(steps, queueClient);
            assertThat(queueClientBuilder.constructed(), hasSize(1));
            QueueClientBuilder builder = queueClientBuilder.constructed().get(0);
            var ordered = inOrder(builder);
            ordered.verify(builder).credential(tokenCredential);
            ordered.verify(builder).endpoint(endpoint);
            ordered.verify(builder).buildClient();
        }
    }
}
