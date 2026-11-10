/*
 * Copyright 2019-2025 the original author or authors.
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

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import com.azure.core.http.HttpMethod;

import org.jbehave.core.annotations.When;
import org.vividus.azure.client.AzureResourceManagementClient;
import org.vividus.context.VariableContext;
import org.vividus.reporter.event.IAttachmentPublisher;
import org.vividus.softassert.ISoftAssert;
import org.vividus.variable.VariableScope;

public class ResourceManagementSteps
{
    private final AzureResourceManagementClient client;
    private final VariableContext variableContext;
    private final ISoftAssert softAssert;
    private final IAttachmentPublisher attachmentPublisher;

    public ResourceManagementSteps(AzureResourceManagementClient client, VariableContext variableContext,
                                   ISoftAssert softAssert, IAttachmentPublisher attachmentPublisher)
    {
        this.client = client;
        this.variableContext = variableContext;
        this.softAssert = softAssert;
        this.attachmentPublisher = attachmentPublisher;
    }

    /**
     * Gets the info about the specified Azure resource using the declared Azure resource URL and saves it to a
     * variable. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/azure/">Azure REST API reference</a>.
     *
     * @param azureResourceUrl It's used to specify Azure resource uniquely. For example:
     *                         <ul>
     *                         <li><code>https://management.azure.com/subscriptions/
     *                         00000000-0000-0000-0000-000000000000/resourceGroups/sample-resource-group/
     *                         providers/Microsoft.KeyVault/vaults/sample-vault?api-version=2021-10-01</code>
     *                         <br><i>or</i>
     *                         <br>
     *                         <li><code>https://api.loganalytics.io/v1/workspaces/00000000-0000-0000-0000-000000000000/
     *                         query?query=Syslog</code>
     *                         </ul>
     * @param scopes           The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                         scopes.<br>
     *                         <i>Available scopes:</i>
     *                         <ul>
     *                         <li><b>STEP</b> - the variable will be available only within the step,
     *                         <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                         <li><b>STORY</b> - the variable will be available within the whole story,
     *                         <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                         </ul>
     * @param variableName     The variable name to store the Azure resource info.
     */
    @When("I get Azure resource with URL `$azureResourceUrl` and save it to $scopes variable `$variableName`")
    public void saveAzureResourceAsVariableWithResourceUrl(String azureResourceUrl, Set<VariableScope> scopes,
            String variableName)
    {
        client.executeHttpRequest(HttpMethod.GET, azureResourceUrl, Optional.empty(),
                putVariableWithAttachment(scopes, variableName),
                softAssert::recordFailedAssertion);
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
        client.executeHttpRequest(HttpMethod.GET, azureResourceIdentifier, apiVersion, Optional.empty(),
                putVariableWithAttachment(scopes, variableName),
                softAssert::recordFailedAssertion);
    }

    /**
     * Executes the specified Azure operation using the declared Azure API version and saves the result to a variable.
     * For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/azure/">Azure REST API reference</a>.
     *
     * @param azureOperationIdentifier This is a VIVIDUS-only term. It's used to specify Azure operation uniquely. From
     *                                 the technical perspective it's a part of Azure operation REST API URL path. For
     *                                 example, if the full Azure operation URL is
     *                                 <br>
     *                                 <code>https://management.azure.com/subscriptions/
     *                                 00000000-0000-0000-0000-000000000000/providers/
     *                                 Microsoft.KeyVault/checkNameAvailability?api-version=2021-10-01</code>,
     *                                 <br>
     *                                 then the operation identifier will be
     *                                 <br>
     *                                 <code>providers/Microsoft.KeyVault/checkNameAvailability</code>.
     * @param apiVersion               Azure resource provider API version. Note API versions may vary depending on
     *                                 the resource type.
     * @param azureOperationBody       The Azure operation definition in JSON format.
     * @param scopes                   The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the
     *                                 variable
     *                                 scopes.<br>
     *                                 <i>Available scopes:</i>
     *                                 <ul>
     *                                 <li><b>STEP</b> - the variable will be available only within the step,
     *                                 <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                                 <li><b>STORY</b> - the variable will be available within the whole story,
     *                                 <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                                 </ul>
     * @param variableName             The variable name to store the result of Azure operation execution.
     */
    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    @When("I execute Azure operation with identifier `$azureOperationIdentifier` using API version `$apiVersion` and "
            + "body `$azureOperationBody` and save result to $scopes variable `$variableName`")
    public void executeOperationAtAzureResource(String azureOperationIdentifier, String apiVersion,
            String azureOperationBody, Set<VariableScope> scopes, String variableName)
    {
        client.executeHttpRequest(HttpMethod.POST, azureOperationIdentifier, apiVersion,
                Optional.of(azureOperationBody), responseBody -> putVariableWithAttachment(scopes, variableName),
                softAssert::recordFailedAssertion);
    }

    /**
     * Creates (if resource doesn't exist) or updates the specified Azure resource using the declared Azure API
     * version. For more information, see the
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
     * @param azureResourceBody       The Azure resource configuration in JSON format.
     * @param apiVersion              Azure resource provider API version. Note API versions may vary depending on
     *                                the resource type.
     */
    @When("I configure Azure resource with identifier `$azureResourceIdentifier` and body `$azureResourceBody` using "
            + "API version `$apiVersion`")
    public void configureAzureResource(String azureResourceIdentifier, String azureResourceBody, String apiVersion)
    {
        client.executeHttpRequest(HttpMethod.PUT, azureResourceIdentifier, apiVersion, Optional.of(azureResourceBody),
                responseBody -> { }, softAssert::recordFailedAssertion);
    }

    /**
     * Deletes the specified Azure resource using the declared Azure API version. For more information, see the
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
     */
    @When("I delete Azure resource with identifier `$azureResourceIdentifier` using API version `$apiVersion`")
    public void deleteAzureResource(String azureResourceIdentifier, String apiVersion)
    {
        client.executeHttpRequest(HttpMethod.DELETE, azureResourceIdentifier, apiVersion, Optional.empty(),
                responseBody -> { }, softAssert::recordFailedAssertion);
    }

    private Consumer<String> putVariableWithAttachment(Set<VariableScope> scopes, String variableName)
    {
        return responseBody ->
        {
            attachmentPublisher.publishAttachment(
                    "/vividus-plugin/azure/resourcemanager/attachments/resource-as-json.ftl",
                    Map.of("body", responseBody, "bodyContentType", "application/json"), "Azure Resource JSON");
            variableContext.putVariable(scopes, variableName, responseBody);
        };
    }
}
