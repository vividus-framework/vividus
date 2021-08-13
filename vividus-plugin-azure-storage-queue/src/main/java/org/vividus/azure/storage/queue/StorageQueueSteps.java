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

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.azure.core.credential.TokenCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.models.PeekedMessageItem;
import com.azure.storage.queue.models.SendMessageResult;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.jbehave.core.annotations.When;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.util.json.JsonUtils;
import org.vividus.util.property.PropertyMappedCollection;

public class StorageQueueSteps
{
    private final PropertyMappedCollection<String> storageQueueEndpoints;
    private final IBddVariableContext bddVariableContext;
    private final JsonUtils jsonUtils;
    private final Duration receiveTimeout;

    private final TokenCredential tokenCredential;

    private final LoadingCache<String, QueueClient> storageQueueClients = CacheBuilder.newBuilder()
            .build(new CacheLoader<>()
            {
                @Override
                public QueueClient load(String storageQueueEndpoint)
                {
                    return new QueueClientBuilder()
                            .credential(tokenCredential)
                            .endpoint(storageQueueEndpoint)
                            .buildClient();
                }
            });

    public StorageQueueSteps(PropertyMappedCollection<String> storageQueueEndpoints, Duration receiveTimeout,
            TokenCredential tokenCredential, IBddVariableContext bddVariableContext, JsonUtils jsonUtils)
    {
        this.storageQueueEndpoints = storageQueueEndpoints;
        this.receiveTimeout = receiveTimeout;
        this.bddVariableContext = bddVariableContext;
        this.jsonUtils = jsonUtils;
        this.tokenCredential = tokenCredential;
    }

    /**
     * Peek messages from the front of the queue up to the maximum number of messages.
     *
     * @param maxMessagesNumber The maximum number of messages to peek, if there are less messages exist in the queue
     *                          than requested all the messages will be peeked. The allowed range is 1 to 32 messages.
     * @param storageQueueKey   The key to Storage Queue endpoint.
     * @param scopes            The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variables
     *                          scopes<br>
     *                          <i>Available scopes:</i>
     *                          <ul>
     *                          <li><b>STEP</b> - the variable will be available only within the step,
     *                          <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                          <li><b>STORY</b> - the variable will be available within the whole story,
     *                          <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                          </ul>scopes
     * @param variableName      The variable name to store the list of found message bodies. The messages are
     *                          accessible via zero-based index, e.g. <code>${my-keys[0]}</code> will return the
     *                          first found message body.
     */
    @When("I peek up to `$maxMessagesNumber` messages from queue `$storageQueueKey` and save result to $scopes "
            + "variable `$variableName`")
    public void peekMessages(int maxMessagesNumber, String storageQueueKey, Set<VariableScope> scopes,
            String variableName)
    {
        List<String> messages = getQueueClient(storageQueueKey)
                .peekMessages(maxMessagesNumber, receiveTimeout, Context.NONE)
                .stream()
                .map(PeekedMessageItem::getBody)
                .map(BinaryData::toString)
                .collect(Collectors.toList());
        bddVariableContext.putVariable(scopes, variableName, messages);
    }

    /**
     * Sends a message that has a time-to-live of 7 days and is instantly visible.
     *
     * @param message         The message to send.
     * @param storageQueueKey The key to Storage Queue endpoint.
     * @param scopes          The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variables scopes<br>
     *                        <i>Available scopes:</i>
     *                        <ul>
     *                        <li><b>STEP</b> - the variable will be available only within the step,
     *                        <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                        <li><b>STORY</b> - the variable will be available within the whole story,
     *                        <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                        </ul>scopes
     * @param variableName    The name of the variable to save the send message result in JSON format.
     */
    @When("I send message `$message` to queue `$storageQueueKey` and save result as JSON to $scopes variable "
            + "`$variableName`")
    public void sendMessage(String message, String storageQueueKey, Set<VariableScope> scopes, String variableName)
    {
        SendMessageResult sendMessageResult = getQueueClient(storageQueueKey).sendMessage(message);
        bddVariableContext.putVariable(scopes, variableName, jsonUtils.toJson(sendMessageResult));
    }

    /**
     * Deletes all messages from the queue.
     *
     * @param storageQueueKey The key to Storage Queue endpoint.
     */
    @When("I clear queue `$storageQueueKey`")
    public void clearMessages(String storageQueueKey)
    {
        getQueueClient(storageQueueKey).clearMessages();
    }

    private QueueClient getQueueClient(String storageQueueKey)
    {
        String endpoint = storageQueueEndpoints.get(storageQueueKey,
                "Storage queue with key '%s' is not configured in properties", storageQueueKey);
        return storageQueueClients.getUnchecked(endpoint);
    }
}
