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
import com.azure.resourcemanager.storage.fluent.StorageAccountsClient;
import com.azure.resourcemanager.storage.fluent.StorageManagementClient;
import com.azure.resourcemanager.storage.fluent.models.StorageAccountInner;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.bdd.context.IBddVariableContext;
import org.vividus.bdd.variable.VariableScope;

@ExtendWith(MockitoExtension.class)
class StorageAccountManagementStepsTests
{
    @Mock private AzureProfile azureProfile;
    @Mock private TokenCredential tokenCredential;
    @Mock private IBddVariableContext bddVariableContext;

    @Test
    @SuppressWarnings("unchecked")
    void shouldListStorageAccounts() throws IOException
    {
        try (MockedStatic<StorageManager> storageManagerStaticMock = mockStatic(StorageManager.class))
        {
            var storageManager = mock(StorageManager.class);
            storageManagerStaticMock.when(() -> StorageManager.authenticate(tokenCredential, azureProfile)).thenReturn(
                    storageManager);
            var storageManagementClient = mock(StorageManagementClient.class);
            when(storageManager.serviceClient()).thenReturn(storageManagementClient);
            var storageAccountsClient = mock(StorageAccountsClient.class);
            when(storageManagementClient.getStorageAccounts()).thenReturn(storageAccountsClient);
            PagedIterable<StorageAccountInner> storageAccounts = mock(PagedIterable.class);
            var resourceGroupName = "resourceGroupName";
            when(storageAccountsClient.listByResourceGroup(resourceGroupName)).thenReturn(storageAccounts);
            var storageAccount = new StorageAccountInner();
            storageAccount.withAllowBlobPublicAccess(Boolean.TRUE);
            storageAccount.withTags(Map.of());
            when(storageAccounts.stream()).thenReturn(Stream.of(storageAccount));
            var steps = new StorageAccountManagementSteps(azureProfile, tokenCredential, bddVariableContext);
            var scopes = Set.of(VariableScope.STORY);
            var varName = "varName";
            steps.listStorageAccounts(resourceGroupName, scopes, varName);
            verify(bddVariableContext).putVariable(scopes, varName,
                    "[{\"tags\":{},\"properties\":{\"allowBlobPublicAccess\":true}}]");
        }
    }
}
