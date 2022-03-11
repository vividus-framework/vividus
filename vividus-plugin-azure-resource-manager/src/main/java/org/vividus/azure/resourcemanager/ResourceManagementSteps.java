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

package org.vividus.azure.resourcemanager;

import java.util.Set;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;

import org.apache.commons.lang3.StringUtils;
import org.jbehave.core.annotations.When;
import org.vividus.context.VariableContext;
import org.vividus.softassert.SoftAssert;
import org.vividus.variable.VariableScope;

public class ResourceManagementSteps extends AbstractAzureResourceManagementSteps
{
    private final AzureProfile azureProfile;

    public ResourceManagementSteps(AzureProfile azureProfile, TokenCredential tokenCredential, SoftAssert softAssert,
            VariableContext variableContext)
    {
        super(HttpPipelineProvider.buildHttpPipeline(tokenCredential, azureProfile),
                azureProfile.getEnvironment().getResourceManagerEndpoint(), softAssert, variableContext);
        this.azureProfile = azureProfile;
    }

    /**
     * Gets the info about the specified Azure resource using the declared Azure API version and saves it to a
     * variable. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/azure/">Azure REST API reference</a>.
     *
     * @param azureResourceIdentifier This is a VIVIDUS-only term. It's used to specify Azure resource uniquely. From
     *                                the technical perspective it's a part of Azure resource REST API URL path. For
     *                                example, if the full Azure resource URL is
     *                                <br>
     *                                <code>https://management.azure.com/subscriptions/
     *                                00000000-0000-0000-0000-000000000000/resourceGroups/sample-resource-group/
     *                                providers/Microsoft.KeyVault/vaults/sample-vault?api-version=2021-10-01</code>,
     *                                <br>
     *                                then resource identifier will be
     *                                <br>
     *                                <code>resourceGroups/sample-resource-group/providers/Microsoft.KeyVault/
     *                                vaults/sample-vault</code>.
     * @param apiVersion              Azure resource provider API version. Note API versions may vary depending on
     *                                the resource type.
     * @param scopes                  The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                                scopes.<br>
     *                                <i>Available scopes:</i>
     *                                <ul>
     *                                <li><b>STEP</b> - the variable will be available only within the step,
     *                                <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                                <li><b>STORY</b> - the variable will be available within the whole story,
     *                                <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                                </ul>
     * @param variableName            The variable name to store the Azure resource info.
     */
    @When("I get Azure resource with identifier `$azureResourceIdentifier` using API version `$apiVersion` and save "
            + "it to $scopes variable `$variableName`")
    public void saveAzureResourceAsVariable(String azureResourceIdentifier, String apiVersion,
            Set<VariableScope> scopes, String variableName)
    {
        String urlPath = "subscriptions/" + azureProfile.getSubscriptionId() + StringUtils.prependIfMissing(
                azureResourceIdentifier, "/");
        saveHttpResponseAsVariable(urlPath, apiVersion, scopes, variableName);
    }
}
