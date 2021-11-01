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

import java.util.function.Consumer;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.eventhubs.EventHubsManager;
import com.azure.resourcemanager.eventhubs.models.EventHub;
import com.azure.resourcemanager.eventhubs.models.EventHub.Update;

import org.jbehave.core.annotations.When;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EventHubManagementSteps
{
    private static final Logger LOGGER = LoggerFactory.getLogger(EventHubManagementSteps.class);

    private final EventHubsManager eventHubsManager;

    public EventHubManagementSteps(AzureProfile azureProfile, TokenCredential tokenCredential)
    {
        this.eventHubsManager = EventHubsManager.authenticate(tokenCredential, azureProfile);
    }

    /**
     * Toggles data capturing option (enables or disables it) for the specified Azure Event Hub.
     *
     * @param toggle            The data capturing toggle: either <code>enable</code> or <code>disable</code>.
     * @param eventHubName      The event hub name.
     * @param namespaceName     The name of the namespace the event hub belongs to.
     * @param resourceGroupName The resource group name.
     */
    @When("I $toggle data capturing for event hub `$eventHubName` in namespace `$namespaceName` from resource group "
            + "`$resourceGroupName`")
    public void enableDataCapturing(DataCapturingToggle toggle, String eventHubName, String namespaceName,
            String resourceGroupName)
    {
        EventHub.Update updatableEventHub = eventHubsManager.eventHubs()
                .getByName(resourceGroupName, namespaceName, eventHubName)
                .update();
        toggle.changeSetting(updatableEventHub);
        updatableEventHub.apply();
        LOGGER.atInfo()
                .addArgument(() -> toggle.toString().toLowerCase())
                .addArgument(eventHubName)
                .addArgument(namespaceName)
                .addArgument(resourceGroupName)
                .log("The data capturing is {}d for event hub '{}' in namespace '{}' from resource group '{}'");
    }

    public enum DataCapturingToggle
    {
        ENABLE(EventHub.Update::withDataCaptureEnabled),
        DISABLE(EventHub.Update::withDataCaptureDisabled);

        private final Consumer<Update> settingsUpdater;

        DataCapturingToggle(Consumer<EventHub.Update> settingsUpdater)
        {
            this.settingsUpdater = settingsUpdater;
        }

        public void changeSetting(EventHub.Update updatableEventHub)
        {
            settingsUpdater.accept(updatableEventHub);
        }
    }
}
