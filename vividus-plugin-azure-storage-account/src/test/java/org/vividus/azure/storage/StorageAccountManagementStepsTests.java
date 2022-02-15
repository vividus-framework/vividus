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
import com.azure.resourcemanager.storage.StorageManager;
import com.azure.resourcemanager.storage.fluent.BlobServicesClient;
import com.azure.resourcemanager.storage.fluent.StorageAccountsClient;
import com.azure.resourcemanager.storage.fluent.StorageManagementClient;
import com.azure.resourcemanager.storage.fluent.models.BlobServicePropertiesInner;
import com.azure.resourcemanager.storage.fluent.models.StorageAccountInner;
import com.azure.resourcemanager.storage.models.DeleteRetentionPolicy;

import org.apache.commons.lang3.function.FailableBiConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.util.InnersJacksonAdapter;
import org.vividus.context.VariableContext;
import org.vividus.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class StorageAccountManagementStepsTests
{
    private static final String RESOURCE_GROUP_NAME = "resourceGroupName";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.STORY);
    private static final String VAR_NAME = "varName";

    @Mock private AzureProfile azureProfile;
    @Mock private TokenCredential tokenCredential;
    @Mock private VariableContext variableContext;

    @Test
    @SuppressWarnings("unchecked")
    void shouldListStorageAccounts() throws IOException
    {
        runWithStorageManagementClient((storageManagementClient, steps) ->
        {
            var storageAccountsClient = mock(StorageAccountsClient.class);
            when(storageManagementClient.getStorageAccounts()).thenReturn(storageAccountsClient);
            PagedIterable<StorageAccountInner> storageAccounts = mock(PagedIterable.class);
            when(storageAccountsClient.listByResourceGroup(RESOURCE_GROUP_NAME)).thenReturn(storageAccounts);
            var storageAccount = new StorageAccountInner();
            storageAccount.withAllowBlobPublicAccess(Boolean.TRUE);
            storageAccount.withTags(Map.of());
            when(storageAccounts.stream()).thenReturn(Stream.of(storageAccount));
            steps.listStorageAccounts(RESOURCE_GROUP_NAME, SCOPES, VAR_NAME);
            verify(variableContext).putVariable(SCOPES, VAR_NAME,
                    "[{\"tags\":{},\"properties\":{\"allowBlobPublicAccess\":true}}]");
        });
    }

    @Test
    void shouldRetrieveBlobServiceProperties() throws IOException
    {
        runWithStorageManagementClient((storageManagementClient, steps) ->
        {
            var blobServicesClient = mock(BlobServicesClient.class);
            when(storageManagementClient.getBlobServices()).thenReturn(blobServicesClient);
            var storageAccountName = "storageaccountname";
            var properties = new BlobServicePropertiesInner();
            properties.withContainerDeleteRetentionPolicy(new DeleteRetentionPolicy().withEnabled(Boolean.TRUE));
            when(blobServicesClient.getServiceProperties(RESOURCE_GROUP_NAME, storageAccountName)).thenReturn(
                    properties);
            steps.retrieveBlobServiceProperties(storageAccountName, RESOURCE_GROUP_NAME, SCOPES, VAR_NAME);
            verify(variableContext).putVariable(SCOPES, VAR_NAME,
                    "{\"properties\":{\"containerDeleteRetentionPolicy\":{\"enabled\":true}}}");
        });
    }

    private void runWithStorageManagementClient(
            FailableBiConsumer<StorageManagementClient, StorageAccountManagementSteps, IOException> test)
            throws IOException
    {
        try (MockedStatic<StorageManager> storageManagerStaticMock = mockStatic(StorageManager.class))
        {
            var storageManager = mock(StorageManager.class);
            storageManagerStaticMock.when(() -> StorageManager.authenticate(tokenCredential, azureProfile)).thenReturn(
                    storageManager);
            var storageManagementClient = mock(StorageManagementClient.class);
            when(storageManager.serviceClient()).thenReturn(storageManagementClient);
            var steps = new StorageAccountManagementSteps(azureProfile, tokenCredential, new InnersJacksonAdapter(),
                    variableContext);
            test.accept(storageManagementClient, steps);
        }
    }
}
