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

package org.vividus.azure.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockConstruction;

import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

import com.azure.core.http.HttpMethod;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.client.AzureResourceManagementClient;

@ExtendWith(MockitoExtension.class)
class AzureKeyVaultPropertiesProcessorTests
{
    private static final String URL = "https://testkeyvault.vault.azure.net/secrets/%s?api-version=7.6";
    private static final String RESPONSE = "{\"value\":\"%s\"}";
    private static final String KEY_VAULT_NAME_PROPERTY = "secrets-manager.azure-key-vault.name";
    private static final String API_VERSION_PROPERTY = "secrets-manager.azure-key-vault.api-version";
    private static final String KEY_VAULT_ENVIRONMENT_PROPERTY = "azure.environment";
    private static final String KEY_VAULT_NAME = "testkeyvault";
    private static final String PROPERTY_NAME = "property-name";
    private static final String SECRET_NAME = "variables-userPassword";
    private static final String ENVIRONMENT = "AZURE";

    @Test
    void shouldFailToProcessPropertiesWithoutKeyVaultName()
    {
        var properties = new Properties();
        properties.put(KEY_VAULT_ENVIRONMENT_PROPERTY, KEY_VAULT_NAME);
        properties.setProperty(KEY_VAULT_NAME_PROPERTY, "");
        var processor = new AzureKeyVaultPropertiesProcessor();
        processor.processProperties(properties);
        var exception = assertThrows(IllegalArgumentException.class,
                () -> processor.processValue(PROPERTY_NAME, SECRET_NAME));
        assertEquals(
                String.format("Secrets can't be read from Azure Key Vault. "
                        + "Please, provide Azure Key Vault name as value into property `%s`", KEY_VAULT_NAME_PROPERTY),
                exception.getMessage());
    }

    @Test
    void shouldProcessPropertiesWithKeyVaultNameWithoutClient()
    {
        Properties properties = new Properties();
        properties.put(KEY_VAULT_NAME_PROPERTY, KEY_VAULT_NAME);
        properties.put(KEY_VAULT_ENVIRONMENT_PROPERTY, ENVIRONMENT);
        properties.put(API_VERSION_PROPERTY, "7.6");

        String firstExpectedResult = "admin_12345";
        String secondSecretName = "variables-hello";
        String secondExpectedResult = "hello world";

        try (MockedConstruction<AzureResourceManagementClient> client = Mockito.mockConstruction(
                AzureResourceManagementClient.class, (mock, context) -> {
                    doAnswer(invocation -> {
                        Consumer<String> consumer = invocation.getArgument(3);
                        consumer.accept(String.format(RESPONSE, firstExpectedResult));
                        return null;
                    }).when(mock).executeHttpRequest(eq(HttpMethod.GET), eq(String.format(URL, SECRET_NAME)),
                        eq(Optional.empty()), any(Consumer.class), any(Consumer.class));
                    doAnswer(invocation -> {
                        Consumer<String> consumer = invocation.getArgument(3);
                        consumer.accept(String.format(RESPONSE, secondExpectedResult));
                        return null;
                    }).when(mock).executeHttpRequest(eq(HttpMethod.GET), eq(String.format(URL, secondSecretName)),
                            eq(Optional.empty()), any(Consumer.class), any(Consumer.class));
                }
        ))
        {
            var processor = new AzureKeyVaultPropertiesProcessor();
            processor.processProperties(properties);
            assertEquals(firstExpectedResult, processor.processValue(PROPERTY_NAME, SECRET_NAME));
            assertEquals(secondExpectedResult, processor.processValue(PROPERTY_NAME, secondSecretName));
        }
    }

    @Test
    void shouldFailToProcessPropertiesOnProcessValue()
    {
        Properties properties = new Properties();
        properties.put(KEY_VAULT_NAME_PROPERTY, KEY_VAULT_NAME);
        properties.put(KEY_VAULT_ENVIRONMENT_PROPERTY, ENVIRONMENT);
        properties.put(API_VERSION_PROPERTY, "-1");

        try (MockedConstruction<AzureResourceManagementClient> mocked =
                     mockConstruction(AzureResourceManagementClient.class, (mockClient, context) ->
                     {
                         doAnswer(invocation ->
                         {
                             Consumer<String> onError = invocation.getArgument(4);
                             onError.accept("simulated error");
                             return null;
                         }).when(mockClient).executeHttpRequest(any(), any(), any(), any(), any());
                     }))
        {
            var processor = new AzureKeyVaultPropertiesProcessor();
            processor.processProperties(properties);
            var exception = assertThrows(IllegalArgumentException.class,
                    () -> processor.processValue(PROPERTY_NAME, SECRET_NAME));
            assertEquals(String.format("Unable to extract value from secret with name `%s` for Azure Key Vault `%s`",
                            SECRET_NAME, KEY_VAULT_NAME), exception.getMessage());
        }
    }
}
