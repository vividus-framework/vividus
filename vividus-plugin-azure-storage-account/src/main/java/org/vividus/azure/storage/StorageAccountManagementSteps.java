/*
 * Copyright 2019-2022 the original author or authors.
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

package org.vividus.azure.storage;

import java.util.Set;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;

import org.jbehave.core.annotations.When;
import org.vividus.azure.resourcemanager.AbstractAzureResourceManagementSteps;
import org.vividus.context.VariableContext;
import org.vividus.softassert.SoftAssert;
import org.vividus.variable.VariableScope;

public class StorageAccountManagementSteps extends AbstractAzureResourceManagementSteps
{
    private final AzureProfile azureProfile;

    public StorageAccountManagementSteps(AzureProfile azureProfile, TokenCredential tokenCredential,
            SoftAssert softAssert, VariableContext variableContext)
    {
        super(HttpPipelineProvider.buildHttpPipeline(tokenCredential, azureProfile),
                azureProfile.getEnvironment().getResourceManagerEndpoint(), "2021-08-01", softAssert, variableContext);
        this.azureProfile = azureProfile;
    }

    /**
     * Collects the info about all the storage accounts under the specified resource group and saves it as JSON to a
     * variable. Note that storage keys are not returned. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storagerp/storage-accounts/list">Azure Docs</a>.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription to list the storage
     *                          accounts from. The name is case-insensitive.
     * @param scopes            The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                          scopes.<br>
     *                          <i>Available scopes:</i>
     *                          <ul>
     *                          <li><b>STEP</b> - the variable will be available only within the step,
     *                          <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                          <li><b>STORY</b> - the variable will be available within the whole story,
     *                          <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                          </ul>
     * @param variableName      The variable name to store the info about storage accounts as JSON.
     */
    @When("I collect storage accounts in resource group `$resourceGroupName` and save them as JSON to $scopes variable "
            + "`$variableName`")
    public void listStorageAccounts(String resourceGroupName, Set<VariableScope> scopes, String variableName)
    {
        String urlPath = String.format("subscriptions/%s/resourceGroups/%s/providers/Microsoft.Storage/storageAccounts",
                azureProfile.getSubscriptionId(), resourceGroupName);
        saveHttpResponseAsVariable(urlPath, scopes, variableName);
    }

    /**
     * Retrieves the properties of a storage account’s Blob service, including properties for Storage Analytics and CORS
     * (Cross-Origin Resource Sharing) rules, and saves them as JSON to a variable. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/storagerp/blob-services/get-service-properties">Azure
     * Docs</a>.
     *
     * @param storageAccountName The name of the storage account within the specified resource group.
     * @param resourceGroupName  The name of the resource group within the user's subscription to retrieve the storage
     *                           account from. The name is case-insensitive.
     * @param scopes             The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                           scopes.<br>
     *                           <i>Available scopes:</i>
     *                           <ul>
     *                           <li><b>STEP</b> - the variable will be available only within the step,
     *                           <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                           <li><b>STORY</b> - the variable will be available within the whole story,
     *                           <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                           </ul>
     * @param variableName       The variable name to store the blob service properties as JSON.
     */
    @When("I retrieve blob service properties of storage account with name `$storageAccountName` from resource group "
            + "`$resourceGroupName` and save them as JSON to $scopes variable `$variableName`")
    public void retrieveBlobServiceProperties(String storageAccountName, String resourceGroupName,
            Set<VariableScope> scopes, String variableName)
    {
        String urlPath = String.format(
                "subscriptions/%s/resourceGroups/%s/providers/Microsoft"
                        + ".Storage/storageAccounts/%s/blobServices/default",
                azureProfile.getSubscriptionId(), resourceGroupName, storageAccountName);
        saveHttpResponseAsVariable(urlPath, scopes, variableName);
    }
}
