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

package org.vividus.azure.eventgrid;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.eventgrid.EventGridManager;
import com.azure.resourcemanager.eventgrid.fluent.EventGridManagementClient;
import com.azure.resourcemanager.eventgrid.fluent.SystemTopicsClient;
import com.azure.resourcemanager.eventgrid.fluent.models.SystemTopicInner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.util.InnersJacksonAdapter;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class EventGridManagementStepsTests
{
    @Mock private AzureProfile azureProfile;
    @Mock private TokenCredential tokenCredential;
    @Mock private IBddVariableContext bddVariableContext;

    @Test
    @SuppressWarnings("unchecked")
    void shouldListSystemTopics() throws IOException
    {
        try (MockedStatic<EventGridManager> eventGridManagerStaticMock = mockStatic(EventGridManager.class))
        {
            var eventGridManager = mock(EventGridManager.class);
            eventGridManagerStaticMock.when(() -> EventGridManager.authenticate(tokenCredential, azureProfile))
                    .thenReturn(eventGridManager);
            var eventGridManagementClient = mock(EventGridManagementClient.class);
            when(eventGridManager.serviceClient()).thenReturn(eventGridManagementClient);
            var systemTopicsClient = mock(SystemTopicsClient.class);
            when(eventGridManagementClient.getSystemTopics()).thenReturn(systemTopicsClient);
            PagedIterable<SystemTopicInner> systemTopics = mock(PagedIterable.class);
            var resourceGroupName = "resourceGroupName";
            when(systemTopicsClient.listByResourceGroup(resourceGroupName)).thenReturn(systemTopics);
            var systemTopic = new SystemTopicInner();
            systemTopic.withSource("storageaccount");
            systemTopic.withTags(Map.of());
            when(systemTopics.stream()).thenReturn(Stream.of(systemTopic));
            var steps = new EventGridManagementSteps(azureProfile, tokenCredential, new InnersJacksonAdapter(),
                    bddVariableContext);
            var scopes = Set.of(VariableScope.STORY);
            var varName = "varName";
            steps.listSystemTopics(resourceGroupName, scopes, varName);
            verify(bddVariableContext).putVariable(scopes, varName,
                    "[{\"tags\":{},\"properties\":{\"source\":\"storageaccount\"}}]");
        }
    }
}
