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

package org.vividus.azure.eventgrid.service;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.models.CloudEvent;
import com.azure.core.util.BinaryData;
import com.azure.messaging.eventgrid.EventGridEvent;
import com.azure.messaging.eventgrid.EventGridPublisherClient;
import com.azure.messaging.eventgrid.EventGridPublisherClientBuilder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.eventgrid.model.EventSchema;
import org.vividus.azure.eventgrid.model.Topic;

@ExtendWith(MockitoExtension.class)
class EventGridServiceTests
{
    private static final String PAYLOAD = "{}";
    private static final String KEY = "key";
    private static final String ENDPOINT = "https://azure.com";
    private static final EventGridService SERVICE = new EventGridService();

    private Topic topic;

    @BeforeEach
    void beforeEach()
    {
        topic = new Topic();
        topic.setEndpoint(ENDPOINT);
        topic.setKey(KEY);
    }

    @Test
    void shouldSendGridEvent()
    {
        @SuppressWarnings("unchecked")
        EventGridPublisherClient<EventGridEvent> client = mock(EventGridPublisherClient.class);
        try (MockedConstruction<EventGridPublisherClientBuilder> builderConstructor =
                mockConstruction(EventGridPublisherClientBuilder.class, (mock, context) -> {
                    when(mock.endpoint(ENDPOINT)).thenReturn(mock);
                    ArgumentMatcher<AzureKeyCredential> matcher = c -> KEY.equals(c.getKey());
                    when(mock.credential(argThat(matcher))).thenReturn(mock);
                    when(mock.buildEventGridEventPublisherClient()).thenReturn(client);
                });
            MockedStatic<EventGridEvent> event = mockStatic(EventGridEvent.class))
        {
            List<EventGridEvent> eventGridEvent = List.of(mock(EventGridEvent.class));
            event.when(() -> EventGridEvent.fromString(PAYLOAD)).thenReturn(eventGridEvent);
            topic.setEventSchema(EventSchema.GRID);
            SERVICE.sendEvent(topic, PAYLOAD);
            verify(client).sendEvents(eventGridEvent);
        }
    }

    @Test
    void shouldSendCloudEvent()
    {
        @SuppressWarnings("unchecked")
        EventGridPublisherClient<CloudEvent> client = mock(EventGridPublisherClient.class);
        try (MockedConstruction<EventGridPublisherClientBuilder> builderConstructor =
                mockConstruction(EventGridPublisherClientBuilder.class, (mock, context) -> {
                    when(mock.endpoint(ENDPOINT)).thenReturn(mock);
                    ArgumentMatcher<AzureKeyCredential> matcher = c -> KEY.equals(c.getKey());
                    when(mock.credential(argThat(matcher))).thenReturn(mock);
                    when(mock.buildCloudEventPublisherClient()).thenReturn(client);
                });
            MockedStatic<CloudEvent> mockedEvent = mockStatic(CloudEvent.class))
        {
            List<CloudEvent> event = List.of(mock(CloudEvent.class));
            mockedEvent.when(() -> CloudEvent.fromString(PAYLOAD)).thenReturn(event);
            topic.setEventSchema(EventSchema.CLOUD);
            SERVICE.sendEvent(topic, PAYLOAD);
            verify(client).sendEvents(event);
        }
    }

    @Test
    void shouldSendCustomEvent()
    {
        @SuppressWarnings("unchecked")
        EventGridPublisherClient<BinaryData> client = mock(EventGridPublisherClient.class);
        try (MockedConstruction<EventGridPublisherClientBuilder> builderConstructor =
                mockConstruction(EventGridPublisherClientBuilder.class, (mock, context) -> {
                    when(mock.endpoint(ENDPOINT)).thenReturn(mock);
                    ArgumentMatcher<AzureKeyCredential> matcher = c -> KEY.equals(c.getKey());
                    when(mock.credential(argThat(matcher))).thenReturn(mock);
                    when(mock.buildCustomEventPublisherClient()).thenReturn(client);
                });
            MockedStatic<BinaryData> mockedEvent = mockStatic(BinaryData.class))
        {
            BinaryData event = mock(BinaryData.class);
            mockedEvent.when(() -> BinaryData.fromString(PAYLOAD)).thenReturn(event);
            topic.setEventSchema(EventSchema.CUSTOM);
            SERVICE.sendEvent(topic, PAYLOAD);
            verify(client).sendEvents(List.of(event));
        }
    }
}
