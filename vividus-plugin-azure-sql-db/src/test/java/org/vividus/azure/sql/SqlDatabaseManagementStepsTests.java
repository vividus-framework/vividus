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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Set;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.sql.SqlServerManager;
import com.azure.resourcemanager.sql.fluent.models.DatabaseInner;
import com.azure.resourcemanager.sql.models.CreateMode;
import com.azure.resourcemanager.sql.models.SqlDatabase;
import com.azure.resourcemanager.sql.models.SqlDatabaseOperations;
import com.azure.resourcemanager.sql.models.SqlServers;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.util.InnersJacksonAdapter;
import org.vividus.context.VariableContext;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class SqlDatabaseManagementStepsTests
{
    private static final String RESOURCE_GROUP_NAME = "resourceGroupName";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.STORY);
    private static final String VAR_NAME = "varName";

    @Mock private AzureProfile azureProfile;
    @Mock private TokenCredential tokenCredential;
    @Mock private VariableContext variableContext;

    @Test
    void shouldRetrieveSqlServerDatabaseProperties() throws IOException
    {
        try (MockedStatic<SqlServerManager> sqlServerManagerStaticMock = mockStatic(SqlServerManager.class))
        {
            var sqlServerManager = mock(SqlServerManager.class);
            sqlServerManagerStaticMock.when(() -> SqlServerManager.authenticate(tokenCredential, azureProfile))
                    .thenReturn(sqlServerManager);
            var sqlServers = mock(SqlServers.class);
            when(sqlServerManager.sqlServers()).thenReturn(sqlServers);
            var databases = mock(SqlDatabaseOperations.class);
            when(sqlServers.databases()).thenReturn(databases);
            var sqlServerName = "sqlServerName";
            var databaseName = "dbname";
            SqlDatabase sqlDatabase = mock(SqlDatabase.class);
            when(databases.getBySqlServer(RESOURCE_GROUP_NAME, sqlServerName, databaseName)).thenReturn(sqlDatabase);
            DatabaseInner database = new DatabaseInner();
            database.withCreateMode(CreateMode.POINT_IN_TIME_RESTORE);
            when(sqlDatabase.innerModel()).thenReturn(database);
            var steps = new SqlDatabaseManagementSteps(azureProfile, tokenCredential, new InnersJacksonAdapter(),
                    variableContext);
            steps.retrieveSqlServerDatabaseProperties(databaseName, sqlServerName, RESOURCE_GROUP_NAME, SCOPES,
                    VAR_NAME);
            verify(variableContext).putVariable(SCOPES, VAR_NAME,
                    "{\"properties\":{\"createMode\":\"PointInTimeRestore\"}}");
        }
    }
}
