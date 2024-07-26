/*
 * Copyright 2019-2024 the original author or authors.
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

import java.util.Optional;
import java.util.Properties;

import com.azure.core.http.HttpMethod;
import com.azure.core.management.AzureEnvironment;
import com.azure.core.management.profile.AzureProfile;
import com.azure.identity.DefaultAzureCredentialBuilder;

import org.apache.commons.lang3.Validate;
import org.vividus.azure.client.AzureHttpClient;
import org.vividus.configuration.AbstractPropertiesProcessor;
import org.vividus.softassert.SoftAssert;
import org.vividus.util.json.JsonPathUtils;

public class AzureKeyVaultPropertiesProcessor extends AbstractPropertiesProcessor
{
    private static final String KEY_VAULT_PROPERTY_NAME = "azure.key-vault.url";

    private String keyVaultUrl;
    private AzureHttpClient client;

    public AzureKeyVaultPropertiesProcessor()
    {
        super("AZURE_KEY_VAULT");
    }

    @Override
    public Properties processProperties(Properties properties)
    {
        this.keyVaultUrl = properties.getProperty(KEY_VAULT_PROPERTY_NAME);
        return super.processProperties(properties);
    }

    @Override
    protected String processValue(String propertyName, String partOfPropertyValueToProcess)
    {
        return getKeyVaultSecretValue(partOfPropertyValueToProcess);
    }

    private String getKeyVaultSecretValue(String secretName)
    {
        if (client == null)
        {
            Validate.notEmpty(keyVaultUrl, "Provide value into property `%s`", KEY_VAULT_PROPERTY_NAME);
            AzureEnvironment environment = AzureEnvironment.knownEnvironments().stream()
                    .filter(e -> keyVaultUrl.contains(e.getKeyVaultDnsSuffix()))
                    .findFirst().get();
            this.client = new AzureHttpClient(new AzureProfile(environment),
                    new DefaultAzureCredentialBuilder().build(), new SoftAssert());
        }
        String[] value = new String[1];
        client.executeHttpRequest(HttpMethod.GET,
                String.format("%ssecrets/%s?api-version=7.5", keyVaultUrl, secretName),
                Optional.empty(), x -> value[0] = JsonPathUtils.getData(x, "$.value"));
        return value[0];
    }
}
