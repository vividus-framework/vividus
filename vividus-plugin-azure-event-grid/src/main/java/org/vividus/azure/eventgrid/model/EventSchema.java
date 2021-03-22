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

package org.vividus.azure.eventgrid.model;

import java.util.List;
import java.util.function.Supplier;

import com.azure.core.models.CloudEvent;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;

public enum EventSchema
{
    GRID
    {
        @Override
        public void sendEvent(EventGridPublisherClientBuilder builder, String payload)
        {
            buildClientAndSendEvent(builder::buildEventGridEventPublisherClient,
                    () -> EventGridEvent.fromString(payload));
        }
    },
    CLOUD
    {
        @Override
        public void sendEvent(EventGridPublisherClientBuilder builder, String payload)
        {
            buildClientAndSendEvent(builder::buildCloudEventPublisherClient,
                    () -> CloudEvent.fromString(payload));
        }
    },
    CUSTOM
    {
        @Override
        public void sendEvent(EventGridPublisherClientBuilder builder, String payload)
        {
            buildClientAndSendEvent(builder::buildCustomEventPublisherClient,
                    () -> List.of(BinaryData.fromString(payload)));
        }
    };

    public abstract void sendEvent(EventGridPublisherClientBuilder builder, String payload);

    private static <T> void buildClientAndSendEvent(Supplier<EventGridPublisherClient<T>> clientBuilder,
            Supplier<List<T>> eventFactory)
    {
        clientBuilder.get().sendEvents(eventFactory.get());
    }
}
