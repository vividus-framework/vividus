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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Set;

import com.azure.core.credential.TokenCredential;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.resourcemanager.keyvault.fluent.KeyVaultManagementClient;
import com.azure.resourcemanager.keyvault.fluent.VaultsClient;
import com.azure.resourcemanager.keyvault.fluent.models.VaultInner;
import com.azure.resourcemanager.keyvault.models.VaultProperties;

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
class KeyVaultManagementStepsTests
{
    private static final String RESOURCE_GROUP_NAME = "resourceGroupName";
    private static final Set<VariableScope> SCOPES = Set.of(VariableScope.STORY);
    private static final String VAR_NAME = "varName";

    @Mock private AzureProfile azureProfile;
    @Mock private TokenCredential tokenCredential;
    @Mock private VariableContext variableContext;

    @Test
    void shouldRetrieveKeyVaultProperties() throws IOException
    {
        runWithKeyVaultClient((keyVaultManagementClient, steps) ->
        {
            var vaultsClient = mock(VaultsClient.class);
            when(keyVaultManagementClient.getVaults()).thenReturn(vaultsClient);
            var keyVaultAccountName = "keyvaultname";
            VaultProperties vaultProperties = new VaultProperties();
            vaultProperties.withEnableSoftDelete(Boolean.TRUE);
            var vault = new VaultInner();
            vault.withProperties(vaultProperties);
            when(vaultsClient.getByResourceGroup(RESOURCE_GROUP_NAME, keyVaultAccountName)).thenReturn(vault);
            steps.retrieveKeyVaultProperties(keyVaultAccountName, RESOURCE_GROUP_NAME, SCOPES, VAR_NAME);
            verify(variableContext).putVariable(SCOPES, VAR_NAME,
                    "{\"properties\":{\"enableSoftDelete\":true}}");
        });
    }

    private void runWithKeyVaultClient(
            FailableBiConsumer<KeyVaultManagementClient, KeyVaultManagementSteps, IOException> test) throws IOException
    {
        try (MockedStatic<KeyVaultManager> keyVaultManagerStaticMock = mockStatic(KeyVaultManager.class))
        {
            var keyVaultManager = mock(KeyVaultManager.class);
            keyVaultManagerStaticMock.when(() -> KeyVaultManager.authenticate(tokenCredential, azureProfile))
                    .thenReturn(keyVaultManager);
            var keyVaultManagementClient = mock(KeyVaultManagementClient.class);
            when(keyVaultManager.serviceClient()).thenReturn(keyVaultManagementClient);
            var steps = new KeyVaultManagementSteps(azureProfile, tokenCredential, new InnersJacksonAdapter(),
                    variableContext);
            test.accept(keyVaultManagementClient, steps);
        }
    }
}
