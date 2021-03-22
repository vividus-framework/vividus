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

import org.jbehave.core.annotations.When;
import org.vividus.azure.eventgrid.model.Topic;
import org.vividus.azure.eventgrid.service.EventGridService;
import org.vividus.util.property.PropertyMappedCollection;

public class EventGridSteps
{
    private final PropertyMappedCollection<Topic> topics;
    private final EventGridService eventGridService;

    public EventGridSteps(PropertyMappedCollection<Topic> topics, EventGridService eventGridService)
    {
        this.topics = topics;
        this.eventGridService = eventGridService;
    }

    /**
     * Sends an event to a topic.
     * <br>
     * <br>Topic <b>connection details</b> should be defined via properties with a following format:
     * <br><code>azure.event-grid.{topic-name}.{property-name}</code>
     * <br>Example:
     * <br><code>
     * <br>azure.event-grid.create-user.key=BnUN7rPhDCoi9CU3H34Yu/0w123d9wj4P9BHqEhGPKE=
     * <br>azure.event-grid.create-user.endpoint=https://topic-name.eventgrid.azure.net/events
     * <br>azure.event-grid.create-user.event-schema=GRID
     * </code>
     * <br>
     * <br> Where:
     * <ul>
     *     <li><b>key</b> - topic access key</li>
     *     <li><b>endpoint</b> - topic endpoint</li>
     *     <li><b>event-schema</b> - the schema used to define events format. Possible values CLOUD, GRID, CUSTOM</li>
     * </ul>
     * @param topicName Identifying the topic connection details
     * @param payload   JSON to send to the topic
     */
    @When("I send event to `$topicName` topic with payload:$payload")
    public void sendEvent(String topicName, String payload)
    {
        Topic topic = topics.get(topicName, "No connection details provided for the topic: %s", topicName);
        eventGridService.sendEvent(topic, payload);
    }
}
