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

package org.vividus.azure.eventgrid;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.eventgrid.model.Topic;
import org.vividus.azure.eventgrid.service.EventGridService;
import org.vividus.util.property.PropertyMappedCollection;

@ExtendWith(MockitoExtension.class)
class EventGridStepsTests
{
    private static final String PAYLOAD = "{}";
    private static final String TOPIC_NAME = "user-create";

    @Mock private EventGridService service;
    @Mock private PropertyMappedCollection<Topic> topics;
    @Mock private Topic topic;

    @InjectMocks private EventGridSteps eventGridSteps;

    @Test
    void shouldPublishAnEvent()
    {
        when(topics.get(TOPIC_NAME, "No connection details provided for the topic: %s", TOPIC_NAME)).thenReturn(topic);
        eventGridSteps.sendEvent(TOPIC_NAME, PAYLOAD);
        verify(service).sendEvent(topic, PAYLOAD);
    }
}
