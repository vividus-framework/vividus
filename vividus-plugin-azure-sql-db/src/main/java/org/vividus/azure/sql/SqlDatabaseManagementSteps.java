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

import java.io.IOException;
import java.util.Set;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.fluent.models.DatabaseInner;

import org.jbehave.core.annotations.When;
import org.vividus.azure.util.InnersJacksonAdapter;
import org.vividus.context.VariableContext;
import org.vividus.variable.VariableScope;

public class SqlDatabaseManagementSteps
{
    private final SqlServerManager sqlServerManager;
    private final VariableContext variableContext;
    private final InnersJacksonAdapter innersJacksonAdapter;

    public SqlDatabaseManagementSteps(AzureProfile azureProfile, TokenCredential tokenCredential,
            InnersJacksonAdapter innersJacksonAdapter, VariableContext variableContext)
    {
        this.sqlServerManager = SqlServerManager.authenticate(tokenCredential, azureProfile);
        this.variableContext = variableContext;
        this.innersJacksonAdapter = innersJacksonAdapter;
    }

    /**
     * Retrieves the properties of the specified SQL Database belonging to the identified SQL Server and saves them as
     * JSON to a variable. For more information, see the
     * <a href="https://docs.microsoft.com/en-us/rest/api/sql/databases/get">Azure Docs</a>.
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
     * @throws IOException If an input or output exception occurred
     */
    @SuppressWarnings("PMD.UseObjectForClearerAPI")
    @When("I retrieve properties of database with name `$databaseName` from SQL Server `$sqlServerName` from resource"
            + " group `$resourceGroupName` and save them as JSON to $scopes variable `$variableName`")
    public void retrieveSqlServerDatabaseProperties(String databaseName, String sqlServerName, String resourceGroupName,
            Set<VariableScope> scopes, String variableName) throws IOException
    {
        DatabaseInner sqlDatabase = sqlServerManager.sqlServers()
                .databases()
                .getBySqlServer(resourceGroupName, sqlServerName, databaseName)
                .innerModel();
        variableContext.putVariable(scopes, variableName, innersJacksonAdapter.serializeToJson(sqlDatabase));
    }
}
