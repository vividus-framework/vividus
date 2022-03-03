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

package org.vividus.azure.sql;

import java.util.Set;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;

import org.jbehave.core.annotations.When;
import org.vividus.context.VariableContext;
import org.vividus.softassert.SoftAssert;
import org.vividus.variable.VariableScope;

import io.netty.handler.codec.http.HttpResponseStatus;

public class SqlDatabaseManagementSteps
{
    private final AzureProfile azureProfile;
    private final SoftAssert softAssert;
    private final VariableContext variableContext;
    private final HttpPipeline httpPipeline;

    public SqlDatabaseManagementSteps(AzureProfile azureProfile, TokenCredential tokenCredential, SoftAssert softAssert,
            VariableContext variableContext)
    {
        this.azureProfile = azureProfile;
        this.softAssert = softAssert;
        this.variableContext = variableContext;
        this.httpPipeline = HttpPipelineProvider.buildHttpPipeline(tokenCredential, azureProfile);
    }

    /**
     * Collects the info about all the SQL Servers under the specified resource group and saves it as JSON to a
     * variable. For more information, see the <a href=
     * "https://docs.microsoft.com/en-us/rest/api/sql/2021-08-01-preview/servers/list-by-resource-group">Azure Docs</a>.
     *
     * @param resourceGroupName The name of the resource group within the user's subscription to retrieve SQL Servers
     *                          from. The name is case-insensitive.
     * @param scopes            The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                          scopes.<br>
     *                          <i>Available scopes:</i>
     *                          <ul>
     *                          <li><b>STEP</b> - the variable will be available only within the step,
     *                          <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                          <li><b>STORY</b> - the variable will be available within the whole story,
     *                          <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                          </ul>
     * @param variableName      The variable name to store the info about SQL Servers as JSON.
     */
    @When("I collect SQL Servers from resource group `$resourceGroupName` and save them as JSON to $scopes variable "
            + "`$variableName`")
    public void listSqlServers(String resourceGroupName, Set<VariableScope> scopes, String variableName)
    {
        String urlPath = String.format("subscriptions/%s/resourceGroups/%s/providers/Microsoft.Sql/servers",
                azureProfile.getSubscriptionId(), resourceGroupName);
        saveHttpResponseAsVariable(urlPath, scopes, variableName);
    }

    /**
     * Collects the info about all the databases belonging to the identified SQL Server under the specified resource
     * group and saves it as JSON to a variable. For more information, see the <a
     * href="https://docs.microsoft.com/en-us/rest/api/sql/2021-08-01-preview/databases/list-by-server">Azure Docs</a>.
     *
     * @param sqlServerName     The name of the SQL Server to list databases from.
     * @param resourceGroupName The name of the resource group within the user's subscription to retrieve the SQL
     *                          Server from. The name is case-insensitive.
     * @param scopes            The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                          scopes.<br>
     *                          <i>Available scopes:</i>
     *                          <ul>
     *                          <li><b>STEP</b> - the variable will be available only within the step,
     *                          <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                          <li><b>STORY</b> - the variable will be available within the whole story,
     *                          <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                          </ul>
     * @param variableName      The variable name to store the info about databases as JSON.
     */
    @When("I collect databases from SQL Server `$sqlServerName` from resource group `$resourceGroupName` and save "
            + "them as JSON to $scopes variable `$variableName`")
    public void listSqlDatabases(String sqlServerName, String resourceGroupName, Set<VariableScope> scopes,
            String variableName)
    {
        String urlPath = String.format(
                "subscriptions/%s/resourceGroups/%s/providers/Microsoft.Sql/servers/%s/databases",
                azureProfile.getSubscriptionId(), resourceGroupName, sqlServerName);
        saveHttpResponseAsVariable(urlPath, scopes, variableName);
    }

    /**
     * Retrieves the properties of the specified SQL Database belonging to the identified SQL Server and saves them as
     * JSON to a variable. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/sql/2021-08-01-preview/databases/get">Azure Docs</a>.
     *
     * @param databaseName      The name of the SQL Database.
     * @param sqlServerName     The name of the SQL Server.
     * @param resourceGroupName The name of the resource group within the user's subscription to retrieve the SQL
     *                          Server from. The name is case-insensitive.
     * @param scopes            The set (comma separated list of scopes e.g.: STORY, NEXT_BATCHES) of the variable
     *                          scopes.<br>
     *                          <i>Available scopes:</i>
     *                          <ul>
     *                          <li><b>STEP</b> - the variable will be available only within the step,
     *                          <li><b>SCENARIO</b> - the variable will be available only within the scenario,
     *                          <li><b>STORY</b> - the variable will be available within the whole story,
     *                          <li><b>NEXT_BATCHES</b> - the variable will be available starting from next batch
     *                          </ul>
     * @param variableName      The variable name to store the SQL Database properties as JSON.
     */
    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    @When("I retrieve properties of database with name `$databaseName` from SQL Server `$sqlServerName` from resource"
            + " group `$resourceGroupName` and save them as JSON to $scopes variable `$variableName`")
    public void retrieveSqlServerDatabaseProperties(String databaseName, String sqlServerName, String resourceGroupName,
            Set<VariableScope> scopes, String variableName)
    {
        String urlPath = String.format(
                "subscriptions/%s/resourceGroups/%s/providers/Microsoft.Sql/servers/%s/databases/%s",
                azureProfile.getSubscriptionId(), resourceGroupName, sqlServerName, databaseName);
        saveHttpResponseAsVariable(urlPath, scopes, variableName);
    }

    private void saveHttpResponseAsVariable(String urlPath, Set<VariableScope> scopes, String variableName)
    {
        // Workaround for https://github.com/Azure/azure-sdk-for-java/issues/27268
        String url = azureProfile.getEnvironment().getResourceManagerEndpoint() + urlPath
                + "?api-version=2021-08-01-preview";
        HttpRequest httpRequest = new HttpRequest(HttpMethod.GET, url);
        try (HttpResponse httpResponse = httpPipeline.send(httpRequest).block())
        {
            String responseBody = httpResponse.getBodyAsString().block();
            if (httpResponse.getStatusCode() == HttpResponseStatus.OK.code())
            {
                variableContext.putVariable(scopes, variableName, responseBody);
            }
            else
            {
                softAssert.recordFailedAssertion("Azure REST API HTTP request execution is failed: " + responseBody);
            }
        }
    }
}
