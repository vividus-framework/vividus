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

package org.vividus.azure.keyvault;

import java.util.Set;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;

import org.jbehave.core.annotations.When;
import org.vividus.azure.resourcemanager.AbstractAzureResourceManagementSteps;
import org.vividus.context.VariableContext;
import org.vividus.softassert.SoftAssert;
import org.vividus.variable.VariableScope;

public class KeyVaultManagementSteps extends AbstractAzureResourceManagementSteps
{
    private final AzureProfile azureProfile;

    public KeyVaultManagementSteps(AzureProfile azureProfile, TokenCredential tokenCredential, SoftAssert softAssert,
            VariableContext variableContext)
    {
        super(HttpPipelineProvider.buildHttpPipeline(tokenCredential, azureProfile),
                azureProfile.getEnvironment().getResourceManagerEndpoint(), "2021-10-01", softAssert,
                variableContext);
        this.azureProfile = azureProfile;
    }

    /**
     * Retrieves the properties of the specified Azure key vault and saves them as JSON to a variable. For more
     * information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/keyvault/keyvault/vaults/get">Azure Docs</a>.
     *
     * @param keyVaultName      The name of the key vault within the specified resource group.
     * @param resourceGroupName The name of the resource group within the user's subscription to retrieve the key
     *                          vault from. The name is case-insensitive.
     * @param scopes            The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                          scopes.<br>
     *                          <i>Available scopes:</i>
     *                          <ul>
     *                          <li><b>STEP</b> - the variable will be available only within the step,
     *                          <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                          <li><b>STORY</b> - the variable will be available within the whole story,
     *                          <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                          </ul>
     * @param variableName      The variable name to store the key vault properties as JSON.
     */
    @When("I retrieve properties of key vault with name `$keyVaultName` from resource group `$resourceGroupName` and "
            + "save them as JSON to $scopes variable `$variableName`")
    public void retrieveKeyVaultProperties(String keyVaultName, String resourceGroupName, Set<VariableScope> scopes,
            String variableName)
    {
        String urlPath = String.format(
                "subscriptions/%s/resourceGroups/%s/providers/Microsoft.KeyVault/vaults/%s",
                azureProfile.getSubscriptionId(), resourceGroupName, keyVaultName);
        saveHttpResponseAsVariable(urlPath, scopes, variableName);
    }
}
