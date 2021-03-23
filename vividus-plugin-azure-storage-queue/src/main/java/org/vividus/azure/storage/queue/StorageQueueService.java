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

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import com.azure.core.util.Context;
import com.azure.storage.queue.QueueClient;
import com.azure.storage.queue.QueueClientBuilder;
import com.azure.storage.queue.models.PeekedMessageItem;
import com.azure.storage.queue.models.SendMessageResult;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import org.vividus.azure.storage.queue.model.Queue;

public class StorageQueueService
{
    private static final LoadingCache<Queue, QueueClient> CLIENTS = CacheBuilder.newBuilder()
            .build(
                new CacheLoader<Queue, QueueClient>()
                {
                    @Override
                    public QueueClient load(Queue queue)
                    {
                        return new QueueClientBuilder().endpoint(queue.getEndpoint())
                                                       .sasToken(queue.getSasToken())
                                                       .queueName(queue.getName())
                                                       .buildClient();
                    }
                });

    private Duration receiveTimeout;

    public List<PeekedMessageItem> peekMessages(Queue queue, int size)
    {
        QueueClient unchecked = CLIENTS.getUnchecked(queue);
        return StreamSupport.stream(unchecked
                                           .peekMessages(size, receiveTimeout, Context.NONE)
                                           .spliterator(), false)
                            .collect(Collectors.toList());
    }

    public SendMessageResult sendMessage(Queue queue, String message)
    {
        return CLIENTS.getUnchecked(queue).sendMessage(message);
    }

    public void setReceiveTimeout(Duration receiveTimeout)
    {
        this.receiveTimeout = receiveTimeout;
    }
}
