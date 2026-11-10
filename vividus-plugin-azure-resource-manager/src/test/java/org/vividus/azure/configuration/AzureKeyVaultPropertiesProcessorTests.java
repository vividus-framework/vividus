/*
 * Copyright 2019-2026 the original author or authors.
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.vividus.azure.client.AzureResourceManagementClient;

@SuppressWarnings({ "try", "PMD.CloseResource" })
@ExtendWith(MockitoExtension.class)
class AzureKeyVaultPropertiesProcessorTests
{
    private static final String KEY_VAULT_NAME_PROPERTY = "secrets-manager.azure-key-vault.name";
    private static final String API_VERSION_PROPERTY = "secrets-manager.azure-key-vault.api-version";
    private static final String KEY_VAULT_ENVIRONMENT_PROPERTY = "azure.environment";
    private static final String KEY_VAULT_PROCESSOR_ENABLED_PROPERTY = "secrets-manager.azure-key-vault.enabled";
    private static final String KEY_VAULT_NAME = "testkeyvault";
    private static final String PROPERTY_NAME = "property-name";
    private static final String SECRET_NAME = "variables-userPassword";
    private static final String ENVIRONMENT = "AZURE";
    private static final String TRUE = "true";
    private static final String PROPERTY_VALUE = "AZURE_KEY_VAULT(variables-userPassword)";

    @Test
    void shouldNotProcessPropertiesWhenProcessorDisabled()
    {
        var properties = new Properties();
        properties.put(KEY_VAULT_NAME_PROPERTY, KEY_VAULT_NAME);
        properties.put(KEY_VAULT_ENVIRONMENT_PROPERTY, ENVIRONMENT);
        properties.put(PROPERTY_NAME, PROPERTY_VALUE);
        var processor = new AzureKeyVaultPropertiesProcessor();
        assertEquals(properties, processor.processProperties(properties));
    }

    @Test
    void shouldFailToProcessPropertiesWithoutKeyVaultName()
    {
        var properties = new Properties();
        properties.put(KEY_VAULT_ENVIRONMENT_PROPERTY, ENVIRONMENT);
        properties.put(KEY_VAULT_NAME_PROPERTY, "");
        properties.put(KEY_VAULT_PROCESSOR_ENABLED_PROPERTY, TRUE);
        properties.put(PROPERTY_NAME, PROPERTY_VALUE);
        var processor = new AzureKeyVaultPropertiesProcessor();
        var exception = assertThrows(IllegalArgumentException.class, () -> processor.processProperties(properties));
        assertEquals(
                String.format("Secrets can't be read from Azure Key Vault. "
                        + "Please, provide Azure Key Vault name as value into property `%s`", KEY_VAULT_NAME_PROPERTY),
                exception.getMessage());
    }

    @Test
    void shouldProcessPropertiesWithKeyVaultNameWithoutClient()
    {
        var firstExpectedResult = "admin_12345";
        var secondSecretName = "variables-hello";
        var secondExpectedResult = "hello world";
        var secondPropertyName = "property-name-2";
        var url = "https://testkeyvault.vault.azure.net/secrets/%s?api-version=7.6";
        var response = "{\"value\":\"%s\"}";

        try (var ignored = mockConstruction(AzureResourceManagementClient.class, (mock, context) -> {
                    doAnswer(invocation -> {
                        Consumer<String> consumer = invocation.getArgument(3);
                        consumer.accept(String.format(response, firstExpectedResult));
                        return null;
                    }).when(mock).executeHttpRequest(eq(HttpMethod.GET), eq(String.format(url, SECRET_NAME)),
                        eq(Optional.empty()), any(Consumer.class), any(Consumer.class));
                    doAnswer(invocation -> {
                        Consumer<String> consumer = invocation.getArgument(3);
                        consumer.accept(String.format(response, secondExpectedResult));
                        return null;
                    }).when(mock).executeHttpRequest(eq(HttpMethod.GET), eq(String.format(url, secondSecretName)),
                            eq(Optional.empty()), any(Consumer.class), any(Consumer.class));
                }
        ))
        {
            var properties = new Properties();
            properties.put(KEY_VAULT_NAME_PROPERTY, KEY_VAULT_NAME);
            properties.put(KEY_VAULT_ENVIRONMENT_PROPERTY, ENVIRONMENT);
            properties.put(API_VERSION_PROPERTY, "7.6");
            properties.put(KEY_VAULT_PROCESSOR_ENABLED_PROPERTY, TRUE);
            properties.put(PROPERTY_NAME, PROPERTY_VALUE);
            properties.put(secondPropertyName, "AZURE_KEY_VAULT(variables-hello)");

            var updatedProperties = new AzureKeyVaultPropertiesProcessor().processProperties(properties);
            assertEquals(firstExpectedResult, updatedProperties.getProperty(PROPERTY_NAME));
            assertEquals(secondExpectedResult, updatedProperties.getProperty(secondPropertyName));
        }
    }

    @Test
    void shouldFailToProcessPropertiesOnProcessValue()
    {
        var properties = new Properties();
        properties.put(KEY_VAULT_NAME_PROPERTY, KEY_VAULT_NAME);
        properties.put(KEY_VAULT_ENVIRONMENT_PROPERTY, ENVIRONMENT);
        properties.put(API_VERSION_PROPERTY, "-1");
        properties.put(KEY_VAULT_PROCESSOR_ENABLED_PROPERTY, TRUE);
        properties.put(PROPERTY_NAME, PROPERTY_VALUE);

        try (var ignored = mockConstruction(AzureResourceManagementClient.class, (mockClient, context) -> {
                    doAnswer(invocation ->
                    {
                        Consumer<String> onError = invocation.getArgument(4);
                        onError.accept("simulated error");
                        return null;
                    }).when(mockClient).executeHttpRequest(any(), any(), any(), any(), any());
                }
        ))
        {
            var processor = new AzureKeyVaultPropertiesProcessor();
            var exception = assertThrows(IllegalArgumentException.class, () -> processor.processProperties(properties));
            assertEquals(String.format("Unable to extract value from secret with name `%s` for Azure Key Vault `%s`",
                            SECRET_NAME, KEY_VAULT_NAME), exception.getMessage());
        }
    }
}
