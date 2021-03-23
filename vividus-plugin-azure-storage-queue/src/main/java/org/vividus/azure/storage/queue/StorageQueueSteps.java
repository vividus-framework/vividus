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

import java.util.Set;

import org.jbehave.core.annotations.When;
import org.vividus.azure.storage.queue.model.Queue;
import org.vividus.bdd.context.BddVariableContext;
import org.vividus.bdd.variable.VariableScope;
import org.vividus.util.property.PropertyMappedCollection;

public class StorageQueueSteps
{
    private final StorageQueueService storageQueueService;
    private final BddVariableContext bddVariableContext;
    private final PropertyMappedCollection<Queue> queues;

    public StorageQueueSteps(StorageQueueService storageQueueService, BddVariableContext bddVariableContext,
            PropertyMappedCollection<Queue> queues)
    {
        this.storageQueueService = storageQueueService;
        this.bddVariableContext = bddVariableContext;
        this.queues = queues;
    }

    /**
     * Peeks desired quantity of messages from the queue.
     * @param messagesCount The count of messages to pick
     * @param queueName     The name of the queue
     * @param scopes        The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variables scopes<br>
     *                      <i>Available scopes:</i>
     *                      <ul>
     *                      <li><b>STEP</b> - the variable will be available only within the step,
     *                      <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                      <li><b>STORY</b> - the variable will be available within the whole story,
     *                      <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                      </ul>scopes
     * @param variableName  The variable name to store result. If the variable name is my-var, the following
     *                      variables will be created:
     *                      <ul>
     *                      <li>${my-var[0].messageId} - the message id</li>
     *                      <li>${my-var[0].insertionTime} - the message insertion time</li>
     *                      <li>${my-var[0].expirationTime} - the message expiration time</li>
     *                      <li>${my-var[0].messageText} - the message text</li>
     *                      </ul>
     */
    @When("I peek up to `$count` messages from queue `$queueName` and save result to $scopes variable `$variableName`")
    public void peekMessages(int messagesCount, String queueName, Set<VariableScope> scopes, String variableName)
    {
        bddVariableContext.putVariable(scopes, variableName, storageQueueService.peekMessages(getQueue(queueName),
                messagesCount));
    }

    private Queue getQueue(String queueName)
    {
        return queues.get(queueName, "No connection details provided for the queue: %s", queueName);
    }

    /**
     * @param message       The message to send
     * @param queueName     The name of the queue
     * @param scopes        The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of variables scopes<br>
     *                      <i>Available scopes:</i>
     *                      <ul>
     *                      <li><b>STEP</b> - the variable will be available only within the step,
     *                      <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                      <li><b>STORY</b> - the variable will be available within the whole story,
     *                      <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                      </ul>scopes
     * @param variableName  The variable name to store the result. If the variable name is <b>my-var</b>, the following
     *                      variables will be created:
     *                      <ul>
     *                      <li>${my-var.messageId} - the message id</li>
     *                      <li>${my-var.insertionTime} - the message insertion time</li>
     *                      <li>${my-var.expirationTime} - the message expiration time</li>
     *                      </ul>
     */
    @When("I send message `$message` to queue `$queueName` and save result to $scopes variable `$variableName`")
    public void sendMessage(String message, String queueName, Set<VariableScope> scopes, String variableName)
    {
        bddVariableContext.putVariable(scopes, variableName, storageQueueService.sendMessage(getQueue(queueName),
                message));
    }
}
