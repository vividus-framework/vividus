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

package org.vividus.azure.eventhub;

import static com.github.valfirst.slf4jtest.LoggingEvent.info;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.models.EventHub;
import com.azure.resourcemanager.eventhubs.models.EventHub.Update;
import com.azure.resourcemanager.eventhubs.models.EventHubs;
import com.github.valfirst.slf4jtest.TestLogger;
import com.github.valfirst.slf4jtest.TestLoggerFactory;
import com.github.valfirst.slf4jtest.TestLoggerFactoryExtension;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.eventhub.EventHubManagementSteps.DataCapturingToggle;

@ExtendWith({ MockitoExtension.class, TestLoggerFactoryExtension.class })
class EventHubManagementStepsTests
{
    @Mock private AzureProfile azureProfile;
    @Mock private TokenCredential tokenCredential;

    private final TestLogger logger = TestLoggerFactory.getTestLogger(EventHubManagementSteps.class);

    static Stream<Arguments> dataCapturingToggles()
    {
        return Stream.of(
                arguments(DataCapturingToggle.ENABLE, (Consumer<Update>) Update::withDataCaptureEnabled),
                arguments(DataCapturingToggle.DISABLE, (Consumer<Update>) Update::withDataCaptureDisabled)
        );
    }

    @ParameterizedTest
    @MethodSource("dataCapturingToggles")
    void shouldChangeDataCapturingState(DataCapturingToggle toggle, Consumer<Update> actionValidation)
    {
        var resourceGroupName = "resourceGroupName";
        var namespaceName = "namespaceName";
        var eventHubName = "eventHubName";
        try (MockedStatic<EventHubsManager> eventHubsManagerStaticMock = mockStatic(EventHubsManager.class))
        {
            var eventGridManager = mock(EventHubsManager.class);
            eventHubsManagerStaticMock.when(() -> EventHubsManager.authenticate(tokenCredential, azureProfile))
                    .thenReturn(eventGridManager);
            var eventHubs = mock(EventHubs.class);
            when(eventGridManager.eventHubs()).thenReturn(eventHubs);
            var eventHub = mock(EventHub.class);
            var updatableEventHub = mock(Update.class);
            when(eventHub.update()).thenReturn(updatableEventHub);
            when(eventHubs.getByName(resourceGroupName, namespaceName, eventHubName)).thenReturn(eventHub);
            var steps = new EventHubManagementSteps(azureProfile, tokenCredential);
            steps.enableDataCapturing(toggle, eventHubName, namespaceName, resourceGroupName);
            var ordered = inOrder(updatableEventHub);
            actionValidation.accept(ordered.verify(updatableEventHub));
            ordered.verify(updatableEventHub).apply();
            ordered.verifyNoMoreInteractions();
            assertThat(logger.getLoggingEvents(), is(List.of(
                    info("The data capturing is {}d for event hub '{}' in namespace '{}' from resource group '{}'",
                            toggle.toString().toLowerCase(), eventHubName, namespaceName, resourceGroupName)
            )));
        }
    }
}
